package com.example.dayoffset.model

enum class OffsetUnit(
    val singularLabel: String,
    val pluralLabel: String,
    val displayTitle: String
) {
    DAYS(singularLabel = "day", pluralLabel = "days", displayTitle = "Days"),
    WEEKS(singularLabel = "week", pluralLabel = "weeks", displayTitle = "Weeks"),
    MONTHS(singularLabel = "month", pluralLabel = "months", displayTitle = "Months"),
    YEARS(singularLabel = "year", pluralLabel = "years", displayTitle = "Years");

    fun labelForCount(count: Long): String {
        return if (kotlin.math.abs(count) == 1L) singularLabel else pluralLabel
    }
}
