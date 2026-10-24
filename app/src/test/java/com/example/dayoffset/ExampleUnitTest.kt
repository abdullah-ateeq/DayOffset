package com.example.dayoffset

import com.example.dayoffset.calculator.DateRangeExceededException
import com.example.dayoffset.calculator.DayOffsetCalculator
import com.example.dayoffset.data.InMemoryPreferencesDataSource
import com.example.dayoffset.domain.OffsetValidationResult
import com.example.dayoffset.domain.OffsetValidator
import com.example.dayoffset.formatter.DayOffsetFormatter
import com.example.dayoffset.history.HistoryManager
import com.example.dayoffset.model.AppSettings
import com.example.dayoffset.model.DateFormatOption
import com.example.dayoffset.model.OffsetDirection
import com.example.dayoffset.model.OffsetUnit
import com.example.dayoffset.model.ThemeMode
import com.example.dayoffset.navigation.DateNavigator
import com.example.dayoffset.repository.DayOffsetRepository
import com.example.dayoffset.ui.DayOffsetViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

class ExampleUnitTest {

    private val calculator = DayOffsetCalculator()
    private val dateNavigator = DateNavigator()
    private val formatter = DayOffsetFormatter { Locale.ENGLISH }
    private val historyManager = HistoryManager()

    @Test
    fun criticalTest_basicDays() {
        val start = LocalDate.of(2026, 10, 24)
        val result = calculator.calculate(start, 10L, OffsetUnit.DAYS, OffsetDirection.ADD, Locale.ENGLISH)
        assertEquals(LocalDate.of(2026, 11, 3), result.resultDate)
        assertEquals(DayOfWeek.TUESDAY, result.weekday)
        assertEquals(307, result.dayOfYear)
        assertEquals(45, result.isoWeek)
        assertFalse(result.isLeapYear)
    }

    @Test
    fun criticalTest_subtractDays() {
        val start = LocalDate.of(2026, 10, 24)
        val result = calculator.calculate(start, 10L, OffsetUnit.DAYS, OffsetDirection.SUBTRACT)
        assertEquals(LocalDate.of(2026, 10, 14), result.resultDate)
    }

    @Test
    fun criticalTest_week() {
        val start = LocalDate.of(2026, 10, 24)
        val result = calculator.calculate(start, 1L, OffsetUnit.WEEKS, OffsetDirection.ADD)
        assertEquals(LocalDate.of(2026, 10, 31), result.resultDate)
    }

    @Test
    fun criticalTest_month() {
        val start = LocalDate.of(2026, 10, 24)
        val result = calculator.calculate(start, 3L, OffsetUnit.MONTHS, OffsetDirection.ADD)
        assertEquals(LocalDate.of(2027, 1, 24), result.resultDate)
    }

    @Test
    fun criticalTest_monthEnd_nonLeapYear() {
        val start = LocalDate.of(2026, 1, 31)
        val result = calculator.calculate(start, 1L, OffsetUnit.MONTHS, OffsetDirection.ADD)
        assertEquals(LocalDate.of(2026, 2, 28), result.resultDate)
        assertTrue(calculator.isEndOfMonthClamped(result))
    }

    @Test
    fun criticalTest_leapMonthEnd() {
        val start = LocalDate.of(2024, 1, 31)
        val result = calculator.calculate(start, 1L, OffsetUnit.MONTHS, OffsetDirection.ADD)
        assertEquals(LocalDate.of(2024, 2, 29), result.resultDate)
        assertTrue(result.isLeapYear)
        assertTrue(calculator.isEndOfMonthClamped(result))
    }

    @Test
    fun criticalTest_subtractMonth() {
        val startNonLeap = LocalDate.of(2026, 3, 31)
        val resultNonLeap = calculator.calculate(startNonLeap, 1L, OffsetUnit.MONTHS, OffsetDirection.SUBTRACT)
        assertEquals(LocalDate.of(2026, 2, 28), resultNonLeap.resultDate)

        val startLeap = LocalDate.of(2024, 3, 31)
        val resultLeap = calculator.calculate(startLeap, 1L, OffsetUnit.MONTHS, OffsetDirection.SUBTRACT)
        assertEquals(LocalDate.of(2024, 2, 29), resultLeap.resultDate)
    }

    @Test
    fun criticalTest_multiMonthOffset_doesNotClampRepeatedly() {
        val start = LocalDate.of(2026, 1, 31)
        val result = calculator.calculate(start, 2L, OffsetUnit.MONTHS, OffsetDirection.ADD)
        assertEquals(LocalDate.of(2026, 3, 31), result.resultDate)
    }

    @Test
    fun criticalTest_leapDay() {
        val leapDay = LocalDate.of(2024, 2, 29)
        val plus12Months = calculator.calculate(leapDay, 12L, OffsetUnit.MONTHS, OffsetDirection.ADD)
        assertEquals(LocalDate.of(2025, 2, 28), plus12Months.resultDate)

        val minus12Months = calculator.calculate(leapDay, 12L, OffsetUnit.MONTHS, OffsetDirection.SUBTRACT)
        assertEquals(LocalDate.of(2023, 2, 28), minus12Months.resultDate)
    }

    @Test
    fun criticalTest_yearCrossing_andReverseYear() {
        val dec31 = LocalDate.of(2026, 12, 31)
        val forward = calculator.calculate(dec31, 1L, OffsetUnit.DAYS, OffsetDirection.ADD)
        assertEquals(LocalDate.of(2027, 1, 1), forward.resultDate)

        val jan1 = LocalDate.of(2027, 1, 1)
        val backward = calculator.calculate(jan1, 1L, OffsetUnit.DAYS, OffsetDirection.SUBTRACT)
        assertEquals(LocalDate.of(2026, 12, 31), backward.resultDate)
    }

    @Test
    fun criticalTest_zeroOffset() {
        val start = LocalDate.of(2026, 10, 24)
        val result = calculator.calculate(start, 0L, OffsetUnit.DAYS, OffsetDirection.ADD)
        assertEquals(start, result.resultDate)
    }

    @Test
    fun criticalTest_negativeDirection() {
        val start = LocalDate.of(2026, 10, 24)
        val result = calculator.calculate(start, 7L, OffsetUnit.DAYS, OffsetDirection.SUBTRACT)
        assertEquals(LocalDate.of(2026, 10, 17), result.resultDate)
    }

    @Test
    fun criticalTest_largeDayOffset_andLeapYearDayOffset() {
        val jan2026 = LocalDate.of(2026, 1, 1)
        val result365 = calculator.calculate(jan2026, 365L, OffsetUnit.DAYS, OffsetDirection.ADD)
        assertEquals(LocalDate.of(2027, 1, 1), result365.resultDate)

        val jan2024 = LocalDate.of(2024, 1, 1)
        val result366 = calculator.calculate(jan2024, 366L, OffsetUnit.DAYS, OffsetDirection.ADD)
        assertEquals(LocalDate.of(2025, 1, 1), result366.resultDate)
    }

    @Test
    fun criticalTest_monthOffsetIsNot30Days() {
        val jan1 = LocalDate.of(2026, 1, 1)
        val result = calculator.calculate(jan1, 1L, OffsetUnit.MONTHS, OffsetDirection.ADD)
        assertEquals(LocalDate.of(2026, 2, 1), result.resultDate)
    }

    @Test
    fun criticalTest_allMonthLengthsProduceValidDates() {
        val monthEndDates = listOf(
            LocalDate.of(2026, 1, 31),
            LocalDate.of(2026, 2, 28),
            LocalDate.of(2024, 2, 29),
            LocalDate.of(2026, 4, 30),
            LocalDate.of(2026, 5, 31),
            LocalDate.of(2026, 6, 30),
            LocalDate.of(2026, 7, 31),
            LocalDate.of(2026, 8, 31),
            LocalDate.of(2026, 9, 30),
            LocalDate.of(2026, 10, 31),
            LocalDate.of(2026, 11, 30),
            LocalDate.of(2026, 12, 31)
        )
        for (start in monthEndDates) {
            for (months in 1L..12L) {
                val added = calculator.calculate(start, months, OffsetUnit.MONTHS, OffsetDirection.ADD)
                val subtracted = calculator.calculate(start, months, OffsetUnit.MONTHS, OffsetDirection.SUBTRACT)
                assertEquals(added.resultDate.dayOfWeek, added.weekday)
                assertEquals(subtracted.resultDate.dayOfWeek, subtracted.weekday)
            }
        }
    }

    @Test
    fun criticalTest_leapYearRules() {
        assertTrue(calculator.calculate(LocalDate.of(2024, 6, 1), 0L, OffsetUnit.DAYS, OffsetDirection.ADD).isLeapYear)
        assertFalse(calculator.calculate(LocalDate.of(2025, 6, 1), 0L, OffsetUnit.DAYS, OffsetDirection.ADD).isLeapYear)
        assertTrue(calculator.calculate(LocalDate.of(2000, 6, 1), 0L, OffsetUnit.DAYS, OffsetDirection.ADD).isLeapYear)
        assertFalse(calculator.calculate(LocalDate.of(1900, 6, 1), 0L, OffsetUnit.DAYS, OffsetDirection.ADD).isLeapYear)
    }

    @Test
    fun criticalTest_largeOffsetSafety() {
        val start = LocalDate.of(2026, 10, 24)
        val safeResult = calculator.calculateSafe(start, Long.MAX_VALUE, OffsetUnit.DAYS, OffsetDirection.ADD)
        assertTrue(safeResult.isFailure)
        assertEquals(
            DateRangeExceededException.ERROR_OUT_OF_RANGE,
            safeResult.exceptionOrNull()?.message
        )
    }

    @Test
    fun criticalTest_offsetValidation_invalid_decimal_empty_and_overflow() {
        val emptyResult = OffsetValidator.validate("", OffsetDirection.ADD)
        assertEquals(
            OffsetValidationResult.Invalid("Please enter an offset."),
            emptyResult
        )

        val invalidResult = OffsetValidator.validate("abc", OffsetDirection.ADD)
        assertEquals(
            OffsetValidationResult.Invalid("Please enter a valid whole number."),
            invalidResult
        )

        val decimalResult = OffsetValidator.validate("1.5", OffsetDirection.ADD)
        assertEquals(
            OffsetValidationResult.Invalid("Please enter a whole number."),
            decimalResult
        )

        val hugeOverflow = OffsetValidator.validate("999999999999999999999999999", OffsetDirection.ADD)
        assertEquals(
            OffsetValidationResult.Invalid("The resulting date is outside the supported date range."),
            hugeOverflow
        )

        val signedMinus = OffsetValidator.validate("-10", OffsetDirection.ADD)
        assertEquals(
            OffsetValidationResult.Valid(10L, OffsetDirection.SUBTRACT, true),
            signedMinus
        )
    }

    @Test
    fun criticalTest_dateNavigator() {
        val jan31NonLeap = LocalDate.of(2026, 1, 31)
        assertEquals(LocalDate.of(2026, 2, 28), dateNavigator.nextMonth(jan31NonLeap))

        val jan31Leap = LocalDate.of(2024, 1, 31)
        assertEquals(LocalDate.of(2024, 2, 29), dateNavigator.nextMonth(jan31Leap))

        val oct24 = LocalDate.of(2026, 10, 24)
        assertEquals(LocalDate.of(2026, 10, 23), dateNavigator.previousDay(oct24))
        assertEquals(LocalDate.of(2026, 10, 25), dateNavigator.nextDay(oct24))
        assertEquals(LocalDate.of(2026, 10, 17), dateNavigator.previousWeek(oct24))
        assertEquals(LocalDate.of(2026, 10, 31), dateNavigator.nextWeek(oct24))
    }

    @Test
    fun criticalTest_historyLimit_andDeduplication() {
        var history = emptyList<com.example.dayoffset.model.DayOffsetHistory>()
        val baseDate = LocalDate.of(2026, 10, 24)

        for (i in 1..10) {
            history = historyManager.addEntry(
                currentHistory = history,
                startDate = baseDate,
                offset = i.toLong(),
                unit = OffsetUnit.DAYS,
                direction = OffsetDirection.ADD,
                savedAt = i * 1000L,
                id = "id_$i"
            )
        }
        assertEquals(10, history.size)
        assertEquals("id_10", history.first().id)
        assertEquals("id_1", history.last().id)

        // Save 11th -> oldest ("id_1") removed
        history = historyManager.addEntry(
            currentHistory = history,
            startDate = baseDate,
            offset = 11L,
            unit = OffsetUnit.DAYS,
            direction = OffsetDirection.ADD,
            savedAt = 11000L,
            id = "id_11"
        )
        assertEquals(10, history.size)
        assertEquals("id_11", history.first().id)
        assertEquals("id_2", history.last().id)
        assertFalse(history.any { it.id == "id_1" })

        // Consecutive duplicate should not add a duplicate entry
        val afterDuplicate = historyManager.addEntry(
            currentHistory = history,
            startDate = baseDate,
            offset = 11L,
            unit = OffsetUnit.DAYS,
            direction = OffsetDirection.ADD,
            savedAt = 12000L,
            id = "id_duplicate"
        )
        assertEquals(10, afterDuplicate.size)
        assertEquals("id_11", afterDuplicate.first().id)
    }

    @Test
    fun criticalTest_viewModel_historyRestore_settingsPersistence_andResetSettings() {
        val dataSource = InMemoryPreferencesDataSource()
        val repository = DayOffsetRepository(dataSource)
        val viewModel = DayOffsetViewModel(
            repository = repository,
            calculator = calculator,
            dateNavigator = dateNavigator,
            formatter = formatter
        )

        val start = LocalDate.of(2026, 10, 24)
        viewModel.onStartDateSelected(start)
        viewModel.onOffsetInputChanged("10")
        viewModel.onUnitSelected(OffsetUnit.DAYS)
        viewModel.onDirectionSelected(OffsetDirection.ADD)
        viewModel.onSaveToHistory()

        assertEquals(1, viewModel.uiState.value.history.size)
        val savedEntry = viewModel.uiState.value.history.first()
        assertEquals("2026-10-24", savedEntry.startDate)

        // Change state, then restore from history and verify recalculation
        viewModel.onStartDateSelected(LocalDate.of(2025, 1, 1))
        viewModel.onOffsetInputChanged("5")
        viewModel.onRestoreHistory(savedEntry)

        assertEquals(LocalDate.of(2026, 10, 24), viewModel.uiState.value.startDate)
        assertEquals(10L, viewModel.uiState.value.offset)
        assertEquals(LocalDate.of(2026, 11, 3), viewModel.uiState.value.resultDate)

        // Modify settings
        viewModel.updateThemeMode(ThemeMode.DARK)
        viewModel.updateLiveCalculation(false)
        viewModel.updateDefaultUnit(OffsetUnit.MONTHS)
        viewModel.updateDefaultDirection(OffsetDirection.SUBTRACT)
        viewModel.updateDateFormat(DateFormatOption.ISO_YMD)
        viewModel.updateShowWeekday(false)
        viewModel.updateShowWeekNumber(false)
        viewModel.updateShowDayOfYear(false)

        // Simulate restart with new ViewModel over same repository
        val restartedViewModel = DayOffsetViewModel(
            repository = repository,
            calculator = calculator,
            dateNavigator = dateNavigator,
            formatter = formatter
        )
        val persistedSettings = restartedViewModel.uiState.value.settings
        assertEquals(ThemeMode.DARK, persistedSettings.themeMode)
        assertFalse(persistedSettings.liveCalculation)
        assertEquals(OffsetUnit.MONTHS, persistedSettings.defaultUnit)
        assertEquals(OffsetDirection.SUBTRACT, persistedSettings.defaultDirection)
        assertEquals(DateFormatOption.ISO_YMD, persistedSettings.dateFormat)
        assertFalse(persistedSettings.showWeekday)
        assertFalse(persistedSettings.showWeekNumber)
        assertFalse(persistedSettings.showDayOfYear)

        // Reset settings -> defaults restored, history remains intact
        restartedViewModel.onResetSettings()
        assertEquals(AppSettings(), restartedViewModel.uiState.value.settings)
        assertEquals(1, restartedViewModel.uiState.value.history.size)
    }

    @Test
    fun criticalTest_formatter_copyAndSummary() {
        val start = LocalDate.of(2026, 10, 24)
        val result = calculator.calculate(start, 10L, OffsetUnit.DAYS, OffsetDirection.ADD, Locale.ENGLISH)

        assertEquals(
            "3 November 2026",
            formatter.formatCopyDate(result, DateFormatOption.DAY_MONTH_YEAR_LONG, Locale.ENGLISH)
        )

        val expectedCopyResult = """
            DayOffset

            Start date: 24 October 2026
            Offset: +10 days
            Result: 3 November 2026
            Weekday: Tuesday
        """.trimIndent()
        assertEquals(
            expectedCopyResult,
            formatter.formatCopyResult(result, DateFormatOption.DAY_MONTH_YEAR_LONG, Locale.ENGLISH)
        )
    }
}
