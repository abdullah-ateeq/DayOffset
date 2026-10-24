package com.example.dayoffset.model

enum class ThemeMode(val displayTitle: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class DateFormatOption(
    val pattern: String,
    val exampleLabel: String
) {
    DAY_MONTH_YEAR_LONG(pattern = "d MMMM yyyy", exampleLabel = "24 October 2026"),
    MONTH_DAY_YEAR_SHORT(pattern = "MMM d, yyyy", exampleLabel = "Oct 24, 2026"),
    SLASH_DMY(pattern = "dd/MM/yyyy", exampleLabel = "24/10/2026"),
    ISO_YMD(pattern = "yyyy-MM-dd", exampleLabel = "2026-10-24")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val liveCalculation: Boolean = true,
    val defaultUnit: OffsetUnit = OffsetUnit.DAYS,
    val defaultDirection: OffsetDirection = OffsetDirection.ADD,
    val dateFormat: DateFormatOption = DateFormatOption.DAY_MONTH_YEAR_LONG,
    val showWeekday: Boolean = true,
    val showWeekNumber: Boolean = true,
    val showIsoWeek: Boolean = false,
    val showDayOfYear: Boolean = true,
    val showLeapYear: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animations: Boolean = true,
    val confirmDelete: Boolean = true,
    val restoreLastDate: Boolean = true
)

data class QuickOffsetPreset(
    val label: String,
    val offset: Long,
    val unit: OffsetUnit,
    val direction: OffsetDirection
) {
    companion object {
        val POSITIVE_PRESETS = listOf(
            QuickOffsetPreset("+1 Day", 1L, OffsetUnit.DAYS, OffsetDirection.ADD),
            QuickOffsetPreset("+7 Days", 7L, OffsetUnit.DAYS, OffsetDirection.ADD),
            QuickOffsetPreset("+14 Days", 14L, OffsetUnit.DAYS, OffsetDirection.ADD),
            QuickOffsetPreset("+30 Days", 30L, OffsetUnit.DAYS, OffsetDirection.ADD),
            QuickOffsetPreset("+1 Month", 1L, OffsetUnit.MONTHS, OffsetDirection.ADD),
            QuickOffsetPreset("+3 Months", 3L, OffsetUnit.MONTHS, OffsetDirection.ADD),
            QuickOffsetPreset("+6 Months", 6L, OffsetUnit.MONTHS, OffsetDirection.ADD),
            QuickOffsetPreset("+1 Year", 1L, OffsetUnit.YEARS, OffsetDirection.ADD)
        )

        val NEGATIVE_PRESETS = listOf(
            QuickOffsetPreset("−1 Day", 1L, OffsetUnit.DAYS, OffsetDirection.SUBTRACT),
            QuickOffsetPreset("−7 Days", 7L, OffsetUnit.DAYS, OffsetDirection.SUBTRACT),
            QuickOffsetPreset("−14 Days", 14L, OffsetUnit.DAYS, OffsetDirection.SUBTRACT),
            QuickOffsetPreset("−1 Month", 1L, OffsetUnit.MONTHS, OffsetDirection.SUBTRACT),
            QuickOffsetPreset("−3 Months", 3L, OffsetUnit.MONTHS, OffsetDirection.SUBTRACT),
            QuickOffsetPreset("−6 Months", 6L, OffsetUnit.MONTHS, OffsetDirection.SUBTRACT),
            QuickOffsetPreset("−1 Year", 1L, OffsetUnit.YEARS, OffsetDirection.SUBTRACT)
        )
    }
}
