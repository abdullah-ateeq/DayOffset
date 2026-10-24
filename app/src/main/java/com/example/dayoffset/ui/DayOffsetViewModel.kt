package com.example.dayoffset.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dayoffset.calculator.DateRangeExceededException
import com.example.dayoffset.calculator.DayOffsetCalculator
import com.example.dayoffset.domain.OffsetValidationResult
import com.example.dayoffset.domain.OffsetValidator
import com.example.dayoffset.formatter.DayOffsetFormatter
import com.example.dayoffset.model.AppSettings
import com.example.dayoffset.model.DateFormatOption
import com.example.dayoffset.model.DayOffsetHistory
import com.example.dayoffset.model.DayOffsetResult
import com.example.dayoffset.model.DayOffsetUiState
import com.example.dayoffset.model.OffsetDirection
import com.example.dayoffset.model.OffsetUnit
import com.example.dayoffset.model.QuickOffsetPreset
import com.example.dayoffset.model.ThemeMode
import com.example.dayoffset.navigation.DateNavigator
import com.example.dayoffset.repository.DayOffsetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.util.Locale

class DayOffsetViewModel(
    private val repository: DayOffsetRepository,
    private val calculator: DayOffsetCalculator = DayOffsetCalculator(),
    private val dateNavigator: DateNavigator = DateNavigator(),
    private val formatter: DayOffsetFormatter = DayOffsetFormatter()
) : ViewModel() {

    private val _uiState: MutableStateFlow<DayOffsetUiState>
    val uiState: StateFlow<DayOffsetUiState>

    init {
        val savedSettings = repository.getSettings()
        val initialStartDate = repository.getInitialStartDate(dateNavigator.today())
        val initialUnit = savedSettings.defaultUnit
        val initialDirection = savedSettings.defaultDirection
        val initialOffset = 10L
        val initialResult = runCatching {
            calculator.calculate(
                startDate = initialStartDate,
                offset = initialOffset,
                unit = initialUnit,
                direction = initialDirection
            )
        }.getOrNull()

        _uiState = MutableStateFlow(
            DayOffsetUiState(
                startDate = initialStartDate,
                offset = initialOffset,
                offsetInput = initialOffset.toString(),
                unit = initialUnit,
                direction = initialDirection,
                resultDate = initialResult?.resultDate,
                result = initialResult,
                isCalculating = false,
                errorMessage = null,
                history = repository.getHistory(),
                settings = savedSettings
            )
        )
        uiState = _uiState.asStateFlow()
    }

    fun onStartDateSelected(date: LocalDate) {
        repository.saveLastSelectedDate(date)
        _uiState.update { current ->
            val updated = current.copy(
                startDate = date,
                isDatePickerVisible = false
            )
            if (updated.settings.liveCalculation) {
                recalculateInternal(updated)
            } else {
                updated
            }
        }
    }

    fun onTodayClicked() {
        val today = dateNavigator.today()
        repository.saveLastSelectedDate(today)
        _uiState.update { current ->
            val updated = current.copy(startDate = today)
            recalculateInternal(updated)
        }
    }

    fun onPreviousDay() = navigateStartDate { dateNavigator.previousDay(it) }

    fun onNextDay() = navigateStartDate { dateNavigator.nextDay(it) }

    fun onPreviousWeek() = navigateStartDate { dateNavigator.previousWeek(it) }

    fun onNextWeek() = navigateStartDate { dateNavigator.nextWeek(it) }

    fun onPreviousMonth() = navigateStartDate { dateNavigator.previousMonth(it) }

    fun onNextMonth() = navigateStartDate { dateNavigator.nextMonth(it) }

    private fun navigateStartDate(transform: (LocalDate) -> LocalDate) {
        _uiState.update { current ->
            val newStartDate = transform(current.startDate)
            repository.saveLastSelectedDate(newStartDate)
            val updated = current.copy(startDate = newStartDate)
            if (updated.settings.liveCalculation) {
                recalculateInternal(updated)
            } else {
                updated
            }
        }
    }

    fun onOffsetInputChanged(rawInput: String) {
        _uiState.update { current ->
            when (val validation = OffsetValidator.validate(rawInput, current.direction)) {
                is OffsetValidationResult.Valid -> {
                    val updated = current.copy(
                        offsetInput = rawInput,
                        offset = validation.magnitude,
                        direction = validation.resolvedDirection,
                        errorMessage = null
                    )
                    if (updated.settings.liveCalculation) {
                        recalculateInternal(updated)
                    } else {
                        updated
                    }
                }
                is OffsetValidationResult.Invalid -> {
                    current.copy(
                        offsetInput = rawInput,
                        errorMessage = validation.errorMessage,
                        result = null,
                        resultDate = null
                    )
                }
            }
        }
    }

    fun onUnitSelected(unit: OffsetUnit) {
        _uiState.update { current ->
            val updated = current.copy(unit = unit)
            if (updated.settings.liveCalculation) {
                recalculateInternal(updated)
            } else {
                updated
            }
        }
    }

    fun onDirectionSelected(direction: OffsetDirection) {
        _uiState.update { current ->
            val normalizedInput = normalizeSignedInput(current.offsetInput)
            val updated = current.copy(
                direction = direction,
                offsetInput = normalizedInput
            )
            if (updated.settings.liveCalculation) {
                recalculateInternal(updated)
            } else {
                updated
            }
        }
    }

    fun onReverseDirection() {
        _uiState.update { current ->
            val newDirection = current.direction.reversed()
            val normalizedInput = normalizeSignedInput(current.offsetInput)
            val updated = current.copy(
                direction = newDirection,
                offsetInput = normalizedInput
            )
            if (updated.settings.liveCalculation || updated.result != null) {
                recalculateInternal(updated)
            } else {
                updated
            }
        }
    }

    fun onQuickOffsetSelected(preset: QuickOffsetPreset) {
        applyQuickOffset(
            offset = preset.offset,
            unit = preset.unit,
            direction = preset.direction
        )
    }

    fun applyQuickOffset(
        offset: Long,
        unit: OffsetUnit,
        direction: OffsetDirection
    ) {
        val magnitude = kotlin.math.abs(offset)
        _uiState.update { current ->
            val updated = current.copy(
                offset = magnitude,
                offsetInput = magnitude.toString(),
                unit = unit,
                direction = direction,
                errorMessage = null
            )
            recalculateInternal(updated)
        }
    }

    fun onCalculateClicked() {
        _uiState.update { current ->
            recalculateInternal(current)
        }
    }

    fun onResetClicked() {
        val today = dateNavigator.today()
        repository.saveLastSelectedDate(today)
        _uiState.update { current ->
            val resetState = current.copy(
                startDate = today,
                offset = 0L,
                offsetInput = "0",
                unit = OffsetUnit.DAYS,
                direction = OffsetDirection.ADD,
                errorMessage = null,
                result = null,
                resultDate = null
            )
            if (resetState.settings.liveCalculation) {
                recalculateInternal(resetState)
            } else {
                resetState
            }
        }
    }

    fun onToggleCalculationExpanded() {
        _uiState.update { it.copy(isCalculationExpanded = !it.isCalculationExpanded) }
    }

    fun onShowDatePicker(show: Boolean) {
        _uiState.update { it.copy(isDatePickerVisible = show) }
    }

    fun onSaveToHistory(): Boolean {
        val current = _uiState.value
        val validResult = current.result ?: return false
        if (current.errorMessage != null) return false

        val updatedHistory = repository.saveCalculationToHistory(
            startDate = validResult.startDate,
            offset = validResult.offset,
            unit = validResult.unit,
            direction = validResult.direction
        )
        _uiState.update {
            it.copy(
                history = updatedHistory,
                snackbarMessage = "Calculation saved to history"
            )
        }
        return true
    }

    fun onRestoreHistory(entry: DayOffsetHistory) {
        val restoredStartDate = runCatching {
            repository.parseHistoryStartDate(entry)
        }.getOrElse { dateNavigator.today() }

        repository.saveLastSelectedDate(restoredStartDate)

        _uiState.update { current ->
            val restoredState = current.copy(
                startDate = restoredStartDate,
                offset = entry.offset,
                offsetInput = entry.offset.toString(),
                unit = entry.unit,
                direction = entry.direction,
                errorMessage = null,
                snackbarMessage = "Calculation restored"
            )
            recalculateInternal(restoredState)
        }
    }

    fun onDeleteHistoryEntry(id: String) {
        val updatedHistory = repository.deleteHistoryEntry(id)
        _uiState.update {
            it.copy(
                history = updatedHistory,
                snackbarMessage = "Saved calculation deleted"
            )
        }
    }

    fun onClearHistory() {
        val cleared = repository.clearHistory()
        _uiState.update {
            it.copy(
                history = cleared,
                snackbarMessage = "All saved calculations cleared"
            )
        }
    }

    fun getCopyDateText(locale: Locale = Locale.getDefault()): String? {
        val current = _uiState.value
        val result = current.result ?: return null
        return formatter.formatCopyDate(result, current.settings.dateFormat, locale)
    }

    fun getCopyResultText(locale: Locale = Locale.getDefault()): String? {
        val current = _uiState.value
        val result = current.result ?: return null
        return formatter.formatCopyResult(result, current.settings.dateFormat, locale)
    }

    fun onMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onSnackMessage(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    // Settings Management
    fun updateThemeMode(themeMode: ThemeMode) = updateSettings { it.copy(themeMode = themeMode) }

    fun updateDynamicColor(enabled: Boolean) = updateSettings { it.copy(dynamicColor = enabled) }

    fun updateLiveCalculation(enabled: Boolean) {
        updateSettings { it.copy(liveCalculation = enabled) }
        if (enabled) {
            _uiState.update { recalculateInternal(it) }
        }
    }

    fun updateDefaultUnit(unit: OffsetUnit) = updateSettings { it.copy(defaultUnit = unit) }

    fun updateDefaultDirection(direction: OffsetDirection) = updateSettings { it.copy(defaultDirection = direction) }

    fun updateDateFormat(dateFormat: DateFormatOption) = updateSettings { it.copy(dateFormat = dateFormat) }

    fun updateShowWeekday(show: Boolean) = updateSettings { it.copy(showWeekday = show) }

    fun updateShowWeekNumber(show: Boolean) = updateSettings { it.copy(showWeekNumber = show) }

    fun updateShowIsoWeek(show: Boolean) = updateSettings { it.copy(showIsoWeek = show) }

    fun updateShowDayOfYear(show: Boolean) = updateSettings { it.copy(showDayOfYear = show) }

    fun updateShowLeapYear(show: Boolean) = updateSettings { it.copy(showLeapYear = show) }

    fun updateHapticFeedback(enabled: Boolean) = updateSettings { it.copy(hapticFeedback = enabled) }

    fun updateAnimations(enabled: Boolean) = updateSettings { it.copy(animations = enabled) }

    fun updateConfirmDelete(enabled: Boolean) = updateSettings { it.copy(confirmDelete = enabled) }

    fun updateRestoreLastDate(enabled: Boolean) {
        updateSettings { it.copy(restoreLastDate = enabled) }
        if (enabled) {
            repository.saveLastSelectedDate(_uiState.value.startDate)
        }
    }

    fun onResetSettings() {
        val defaults = repository.resetSettings()
        _uiState.update { current ->
            val updated = current.copy(
                settings = defaults,
                snackbarMessage = "Settings restored to defaults"
            )
            if (defaults.liveCalculation) {
                recalculateInternal(updated)
            } else {
                updated
            }
        }
    }

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        _uiState.update { current ->
            val newSettings = transform(current.settings)
            repository.updateSettings(newSettings)
            current.copy(settings = newSettings)
        }
    }

    private fun recalculateInternal(state: DayOffsetUiState): DayOffsetUiState {
        return when (val validation = OffsetValidator.validate(state.offsetInput, state.direction)) {
            is OffsetValidationResult.Valid -> {
                try {
                    val calcResult: DayOffsetResult = calculator.calculate(
                        startDate = state.startDate,
                        offset = validation.magnitude,
                        unit = state.unit,
                        direction = validation.resolvedDirection
                    )
                    state.copy(
                        offset = validation.magnitude,
                        direction = validation.resolvedDirection,
                        resultDate = calcResult.resultDate,
                        result = calcResult,
                        isCalculating = false,
                        errorMessage = null
                    )
                } catch (e: DateRangeExceededException) {
                    state.copy(
                        resultDate = null,
                        result = null,
                        isCalculating = false,
                        errorMessage = e.message
                    )
                }
            }
            is OffsetValidationResult.Invalid -> {
                state.copy(
                    resultDate = null,
                    result = null,
                    isCalculating = false,
                    errorMessage = validation.errorMessage
                )
            }
        }
    }

    private fun normalizeSignedInput(rawInput: String): String {
        val trimmed = rawInput.trim().replace('−', '-')
        if (trimmed.startsWith("+") || trimmed.startsWith("-")) {
            val withoutSign = trimmed.substring(1)
            if (withoutSign.isNotEmpty() && withoutSign.all { it.isDigit() }) {
                return withoutSign
            }
        }
        return rawInput
    }

    companion object {
        fun provideFactory(repository: DayOffsetRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DayOffsetViewModel(repository = repository) as T
                }
            }
        }
    }
}
