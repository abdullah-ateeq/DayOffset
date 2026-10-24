package com.example.dayoffset.navigation

import java.time.Clock
import java.time.DateTimeException
import java.time.LocalDate

class DateNavigator(
    private val clock: Clock = Clock.systemDefaultZone()
) {
    fun today(): LocalDate = LocalDate.now(clock)

    fun previousDay(date: LocalDate): LocalDate = safeNavigate(date) { it.minusDays(1L) }

    fun nextDay(date: LocalDate): LocalDate = safeNavigate(date) { it.plusDays(1L) }

    fun previousWeek(date: LocalDate): LocalDate = safeNavigate(date) { it.minusWeeks(1L) }

    fun nextWeek(date: LocalDate): LocalDate = safeNavigate(date) { it.plusWeeks(1L) }

    fun previousMonth(date: LocalDate): LocalDate = safeNavigate(date) { it.minusMonths(1L) }

    fun nextMonth(date: LocalDate): LocalDate = safeNavigate(date) { it.plusMonths(1L) }

    private inline fun safeNavigate(
        date: LocalDate,
        transform: (LocalDate) -> LocalDate
    ): LocalDate {
        return try {
            transform(date)
        } catch (_: DateTimeException) {
            date
        }
    }
}
