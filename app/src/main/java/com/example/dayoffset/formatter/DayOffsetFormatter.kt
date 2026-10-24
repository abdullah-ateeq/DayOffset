package com.example.dayoffset.formatter

import com.example.dayoffset.model.DateFormatOption
import com.example.dayoffset.model.DayOffsetResult
import com.example.dayoffset.model.OffsetDirection
import com.example.dayoffset.model.OffsetUnit
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class DayOffsetFormatter(
    private val defaultLocaleProvider: () -> Locale = { Locale.getDefault() }
) {
    fun formatDate(
        date: LocalDate,
        formatOption: DateFormatOption = DateFormatOption.DAY_MONTH_YEAR_LONG,
        locale: Locale = defaultLocaleProvider()
    ): String {
        val formatter = DateTimeFormatter.ofPattern(formatOption.pattern, locale)
        return date.format(formatter)
    }

    fun formatCompactDate(
        date: LocalDate,
        includeYear: Boolean = false,
        locale: Locale = defaultLocaleProvider()
    ): String {
        val pattern = if (includeYear) "d MMM yyyy" else "d MMM"
        return date.format(DateTimeFormatter.ofPattern(pattern, locale))
    }

    fun formatWeekday(
        date: LocalDate,
        locale: Locale = defaultLocaleProvider()
    ): String {
        return date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
    }

    fun formatMonthName(
        date: LocalDate,
        locale: Locale = defaultLocaleProvider()
    ): String {
        return date.month.getDisplayName(TextStyle.FULL, locale)
    }

    fun formatOffsetClause(
        offset: Long,
        unit: OffsetUnit,
        direction: OffsetDirection,
        compactSign: Boolean = false
    ): String {
        val sign = if (offset == 0L) "+" else direction.symbol
        val separator = if (compactSign) "" else " "
        return "$sign$separator$offset ${unit.labelForCount(offset)}"
    }

    fun formatOffsetSummary(
        startDate: LocalDate,
        offset: Long,
        unit: OffsetUnit,
        direction: OffsetDirection,
        formatOption: DateFormatOption = DateFormatOption.DAY_MONTH_YEAR_LONG,
        locale: Locale = defaultLocaleProvider()
    ): String {
        val formattedStart = formatDate(startDate, formatOption, locale)
        val clause = formatOffsetClause(offset, unit, direction, compactSign = false)
        return "$formattedStart $clause"
    }

    fun formatCopyDate(
        result: DayOffsetResult,
        formatOption: DateFormatOption = DateFormatOption.DAY_MONTH_YEAR_LONG,
        locale: Locale = defaultLocaleProvider()
    ): String {
        return formatDate(result.resultDate, formatOption, locale)
    }

    fun formatCopyResult(
        result: DayOffsetResult,
        formatOption: DateFormatOption = DateFormatOption.DAY_MONTH_YEAR_LONG,
        locale: Locale = defaultLocaleProvider()
    ): String {
        val startStr = formatDate(result.startDate, formatOption, locale)
        val offsetStr = formatOffsetClause(result.offset, result.unit, result.direction, compactSign = true)
        val resultStr = formatDate(result.resultDate, formatOption, locale)
        val weekdayStr = formatWeekday(result.resultDate, locale)
        return buildString {
            appendLine("DayOffset")
            appendLine()
            appendLine("Start date: $startStr")
            appendLine("Offset: $offsetStr")
            appendLine("Result: $resultStr")
            append("Weekday: $weekdayStr")
        }
    }

    fun formatSavedTimestamp(
        savedAtMillis: Long,
        nowDate: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = defaultLocaleProvider()
    ): String {
        val zonedDateTime = Instant.ofEpochMilli(savedAtMillis).atZone(zoneId)
        val savedDate = zonedDateTime.toLocalDate()
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", locale)
        val formattedTime = zonedDateTime.format(timeFormatter)

        val dayLabel = when (savedDate) {
            nowDate -> "Saved today"
            nowDate.minusDays(1L) -> "Saved yesterday"
            else -> "Saved ${formatCompactDate(savedDate, includeYear = true, locale = locale)}"
        }
        return "$dayLabel • $formattedTime"
    }
}
