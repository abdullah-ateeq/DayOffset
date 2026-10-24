package com.example.dayoffset.model

enum class OffsetDirection(
    val displayTitle: String,
    val symbol: String
) {
    ADD(displayTitle = "Add", symbol = "+"),
    SUBTRACT(displayTitle = "Subtract", symbol = "−");

    fun reversed(): OffsetDirection = when (this) {
        ADD -> SUBTRACT
        SUBTRACT -> ADD
    }
}
