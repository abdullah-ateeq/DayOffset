package com.example.dayoffset.calculator

import com.example.dayoffset.model.DayOffsetResult
import com.example.dayoffset.model.OffsetDirection
import com.example.dayoffset.model.OffsetUnit
import java.time.DateTimeException
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale

class DateRangeExceededException(
    override val message: String = ERROR_OUT_OF_RANGE,
    cause: Throwable? = null
) : IllegalArgumentException(message, cause) {
    companion object {
        const val ERROR_OUT_OF_RANGE = "The resulting date is outside the supported date range."
    }
}

class DayOffsetCalculator {

    @JvmOverloads
    fun calculate(
        startDate: LocalDate,
        offset: Long,
        unit: OffsetUnit,
        direction: OffsetDirection,
        locale: Locale = Locale.getDefault()
    ): DayOffsetResult {
        try {
            val effectiveDirection: OffsetDirection
            val magnitude: Long

            if (offset < 0L) {
                magnitude = Math.absExact(offset)
                effectiveDirection = OffsetDirection.SUBTRACT
            } else {
                magnitude = offset
                effectiveDirection = direction
            }

            val resultDate: LocalDate = when (effectiveDirection) {
                OffsetDirection.ADD -> when (unit) {
                    OffsetUnit.DAYS -> startDate.plusDays(magnitude)
                    OffsetUnit.WEEKS -> startDate.plusWeeks(magnitude)
                    OffsetUnit.MONTHS -> startDate.plusMonths(magnitude)
                    OffsetUnit.YEARS -> startDate.plusYears(magnitude)
                }
                OffsetDirection.SUBTRACT -> when (unit) {
                    OffsetUnit.DAYS -> startDate.minusDays(magnitude)
                    OffsetUnit.WEEKS -> startDate.minusWeeks(magnitude)
                    OffsetUnit.MONTHS -> startDate.minusMonths(magnitude)
                    OffsetUnit.YEARS -> startDate.minusYears(magnitude)
                }
            }

            val weekFields = WeekFields.of(locale)
            val weekOfYear = resultDate.get(weekFields.weekOfWeekBasedYear())
            val isoWeek = resultDate.get(WeekFields.ISO.weekOfWeekBasedYear())

            return DayOffsetResult(
                startDate = startDate,
                resultDate = resultDate,
                offset = magnitude,
                unit = unit,
                direction = effectiveDirection,
                weekday = resultDate.dayOfWeek,
                dayOfYear = resultDate.dayOfYear,
                weekOfYear = weekOfYear,
                isoWeek = isoWeek,
                isLeapYear = resultDate.isLeapYear
            )
        } catch (e: DateTimeException) {
            throw DateRangeExceededException(cause = e)
        } catch (e: ArithmeticException) {
            throw DateRangeExceededException(cause = e)
        }
    }

    @JvmOverloads
    fun calculateSafe(
        startDate: LocalDate,
        offset: Long,
        unit: OffsetUnit,
        direction: OffsetDirection,
        locale: Locale = Locale.getDefault()
    ): Result<DayOffsetResult> {
        return try {
            Result.success(calculate(startDate, offset, unit, direction, locale))
        } catch (e: DateRangeExceededException) {
            Result.failure(e)
        }
    }

    fun totalDaysBetween(startDate: LocalDate, resultDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(startDate, resultDate)
    }

    fun calendarPeriodBetween(startDate: LocalDate, resultDate: LocalDate): Period {
        return Period.between(startDate, resultDate)
    }

    fun targetYearMonth(resultDate: LocalDate): YearMonth {
        return YearMonth.from(resultDate)
    }

    fun isEndOfMonthClamped(result: DayOffsetResult): Boolean {
        if (result.offset == 0L) return false
        if (result.unit != OffsetUnit.MONTHS && result.unit != OffsetUnit.YEARS) return false
        val targetYm = YearMonth.from(result.resultDate)
        return result.startDate.dayOfMonth > result.resultDate.dayOfMonth &&
            result.resultDate.dayOfMonth == targetYm.lengthOfMonth()
    }
}
