package com.example.dayoffset.model

import java.time.DayOfWeek
import java.time.LocalDate

data class DayOffsetResult(
    val startDate: LocalDate,
    val resultDate: LocalDate,
    val offset: Long,
    val unit: OffsetUnit,
    val direction: OffsetDirection,
    val weekday: DayOfWeek,
    val dayOfYear: Int,
    val weekOfYear: Int,
    val isoWeek: Int,
    val isLeapYear: Boolean
)
