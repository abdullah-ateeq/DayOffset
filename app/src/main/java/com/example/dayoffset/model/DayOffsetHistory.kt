package com.example.dayoffset.model

data class DayOffsetHistory(
    val id: String,
    val startDate: String,
    val offset: Long,
    val unit: OffsetUnit,
    val direction: OffsetDirection,
    val savedAt: Long
)
