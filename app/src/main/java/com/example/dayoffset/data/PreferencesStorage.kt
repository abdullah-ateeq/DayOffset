package com.example.dayoffset.data

import android.content.Context
import android.content.SharedPreferences
import com.example.dayoffset.history.HistoryManager
import com.example.dayoffset.model.AppSettings
import com.example.dayoffset.model.DateFormatOption
import com.example.dayoffset.model.DayOffsetHistory
import com.example.dayoffset.model.OffsetDirection
import com.example.dayoffset.model.OffsetUnit
import com.example.dayoffset.model.ThemeMode
import java.time.LocalDate

interface OffsetPreferencesDataSource {
    fun loadSettings(): AppSettings
    fun saveSettings(settings: AppSettings)
    fun resetSettingsToDefaults(): AppSettings
    fun loadLastSelectedDate(): LocalDate?
    fun saveLastSelectedDate(date: LocalDate)
    fun loadHistory(): List<DayOffsetHistory>
    fun saveHistory(history: List<DayOffsetHistory>)
}

class SharedPreferencesDataSource(
    private val sharedPreferences: SharedPreferences,
    private val historyManager: HistoryManager = HistoryManager()
) : OffsetPreferencesDataSource {

    companion object {
        const val PREFS_NAME = "day_offset_preferences"

        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_LIVE_CALCULATION = "live_calculation"
        private const val KEY_DEFAULT_UNIT = "default_unit"
        private const val KEY_DEFAULT_DIRECTION = "default_direction"
        private const val KEY_DATE_FORMAT = "date_format"
        private const val KEY_SHOW_WEEKDAY = "show_weekday"
        private const val KEY_SHOW_WEEK_NUMBER = "show_week_number"
        private const val KEY_SHOW_ISO_WEEK = "show_iso_week"
        private const val KEY_SHOW_DAY_OF_YEAR = "show_day_of_year"
        private const val KEY_SHOW_LEAP_YEAR = "show_leap_year"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_ANIMATIONS = "animations"
        private const val KEY_CONFIRM_DELETE = "confirm_delete"
        private const val KEY_RESTORE_LAST_DATE = "restore_last_date"
        private const val KEY_LAST_SELECTED_DATE = "last_selected_date"
        private const val KEY_HISTORY = "saved_history"

        fun create(context: Context): SharedPreferencesDataSource {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return SharedPreferencesDataSource(prefs)
        }
    }

    override fun loadSettings(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            themeMode = runCatching {
                ThemeMode.valueOf(sharedPreferences.getString(KEY_THEME_MODE, defaults.themeMode.name)!!)
            }.getOrDefault(defaults.themeMode),
            dynamicColor = sharedPreferences.getBoolean(KEY_DYNAMIC_COLOR, defaults.dynamicColor),
            liveCalculation = sharedPreferences.getBoolean(KEY_LIVE_CALCULATION, defaults.liveCalculation),
            defaultUnit = runCatching {
                OffsetUnit.valueOf(sharedPreferences.getString(KEY_DEFAULT_UNIT, defaults.defaultUnit.name)!!)
            }.getOrDefault(defaults.defaultUnit),
            defaultDirection = runCatching {
                OffsetDirection.valueOf(sharedPreferences.getString(KEY_DEFAULT_DIRECTION, defaults.defaultDirection.name)!!)
            }.getOrDefault(defaults.defaultDirection),
            dateFormat = runCatching {
                DateFormatOption.valueOf(sharedPreferences.getString(KEY_DATE_FORMAT, defaults.dateFormat.name)!!)
            }.getOrDefault(defaults.dateFormat),
            showWeekday = sharedPreferences.getBoolean(KEY_SHOW_WEEKDAY, defaults.showWeekday),
            showWeekNumber = sharedPreferences.getBoolean(KEY_SHOW_WEEK_NUMBER, defaults.showWeekNumber),
            showIsoWeek = sharedPreferences.getBoolean(KEY_SHOW_ISO_WEEK, defaults.showIsoWeek),
            showDayOfYear = sharedPreferences.getBoolean(KEY_SHOW_DAY_OF_YEAR, defaults.showDayOfYear),
            showLeapYear = sharedPreferences.getBoolean(KEY_SHOW_LEAP_YEAR, defaults.showLeapYear),
            hapticFeedback = sharedPreferences.getBoolean(KEY_HAPTIC_FEEDBACK, defaults.hapticFeedback),
            animations = sharedPreferences.getBoolean(KEY_ANIMATIONS, defaults.animations),
            confirmDelete = sharedPreferences.getBoolean(KEY_CONFIRM_DELETE, defaults.confirmDelete),
            restoreLastDate = sharedPreferences.getBoolean(KEY_RESTORE_LAST_DATE, defaults.restoreLastDate)
        )
    }

    override fun saveSettings(settings: AppSettings) {
        sharedPreferences.edit()
            .putString(KEY_THEME_MODE, settings.themeMode.name)
            .putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
            .putBoolean(KEY_LIVE_CALCULATION, settings.liveCalculation)
            .putString(KEY_DEFAULT_UNIT, settings.defaultUnit.name)
            .putString(KEY_DEFAULT_DIRECTION, settings.defaultDirection.name)
            .putString(KEY_DATE_FORMAT, settings.dateFormat.name)
            .putBoolean(KEY_SHOW_WEEKDAY, settings.showWeekday)
            .putBoolean(KEY_SHOW_WEEK_NUMBER, settings.showWeekNumber)
            .putBoolean(KEY_SHOW_ISO_WEEK, settings.showIsoWeek)
            .putBoolean(KEY_SHOW_DAY_OF_YEAR, settings.showDayOfYear)
            .putBoolean(KEY_SHOW_LEAP_YEAR, settings.showLeapYear)
            .putBoolean(KEY_HAPTIC_FEEDBACK, settings.hapticFeedback)
            .putBoolean(KEY_ANIMATIONS, settings.animations)
            .putBoolean(KEY_CONFIRM_DELETE, settings.confirmDelete)
            .putBoolean(KEY_RESTORE_LAST_DATE, settings.restoreLastDate)
            .apply()
    }

    override fun resetSettingsToDefaults(): AppSettings {
        val defaults = AppSettings()
        saveSettings(defaults)
        return defaults
    }

    override fun loadLastSelectedDate(): LocalDate? {
        val isoString = sharedPreferences.getString(KEY_LAST_SELECTED_DATE, null) ?: return null
        return runCatching { LocalDate.parse(isoString) }.getOrNull()
    }

    override fun saveLastSelectedDate(date: LocalDate) {
        sharedPreferences.edit()
            .putString(KEY_LAST_SELECTED_DATE, date.toString())
            .apply()
    }

    override fun loadHistory(): List<DayOffsetHistory> {
        val raw = sharedPreferences.getString(KEY_HISTORY, null)
        return historyManager.deserialize(raw)
    }

    override fun saveHistory(history: List<DayOffsetHistory>) {
        sharedPreferences.edit()
            .putString(KEY_HISTORY, historyManager.serialize(history))
            .apply()
    }
}

class InMemoryPreferencesDataSource(
    initialSettings: AppSettings = AppSettings(),
    initialLastDate: LocalDate? = null,
    initialHistory: List<DayOffsetHistory> = emptyList()
) : OffsetPreferencesDataSource {
    private var settings: AppSettings = initialSettings
    private var lastSelectedDate: LocalDate? = initialLastDate
    private var history: List<DayOffsetHistory> = initialHistory

    override fun loadSettings(): AppSettings = settings

    override fun saveSettings(settings: AppSettings) {
        this.settings = settings
    }

    override fun resetSettingsToDefaults(): AppSettings {
        settings = AppSettings()
        return settings
    }

    override fun loadLastSelectedDate(): LocalDate? = lastSelectedDate

    override fun saveLastSelectedDate(date: LocalDate) {
        lastSelectedDate = date
    }

    override fun loadHistory(): List<DayOffsetHistory> = history

    override fun saveHistory(history: List<DayOffsetHistory>) {
        this.history = history
    }
}
