package com.example.dayoffset.domain

import com.example.dayoffset.calculator.DateRangeExceededException
import com.example.dayoffset.model.OffsetDirection

sealed class OffsetValidationResult {
    data class Valid(
        val magnitude: Long,
        val resolvedDirection: OffsetDirection,
        val hadExplicitSign: Boolean
    ) : OffsetValidationResult()

    data class Invalid(val errorMessage: String) : OffsetValidationResult()
}

object OffsetValidator {
    const val ERROR_EMPTY = "Please enter an offset."
    const val ERROR_DECIMAL = "Please enter a whole number."
    const val ERROR_INVALID_NUMBER = "Please enter a valid whole number."
    const val ERROR_OUT_OF_RANGE = DateRangeExceededException.ERROR_OUT_OF_RANGE

    private val DECIMAL_REGEX = Regex("""^[+-]?\d+[.,]\d+$""")
    private val HUGE_INTEGER_REGEX = Regex("""^[+-]?\d+$""")

    /**
     * Authoritative signed-input & whole-number validation rule:
     * - Empty / blank input -> "Please enter an offset."
     * - Decimal input (e.g., "1.5" or "2,5") -> "Please enter a whole number."
     * - Non-numeric input (e.g., "abc", "+", "-") -> "Please enter a valid whole number."
     * - Signed input:
     *   - "+10" sets resolvedDirection = ADD and magnitude = 10.
     *   - "-10" sets resolvedDirection = SUBTRACT and magnitude = 10.
     *   - Unsigned "10" preserves currentDirection and sets magnitude = 10.
     *   This ensures the sign and the direction control never contradict each other.
     * - Integer exceeding Long range -> "The resulting date is outside the supported date range."
     */
    fun validate(
        rawInput: String,
        currentDirection: OffsetDirection
    ): OffsetValidationResult {
        val trimmed = rawInput.trim().replace('−', '-')
        if (trimmed.isEmpty()) {
            return OffsetValidationResult.Invalid(ERROR_EMPTY)
        }

        if (DECIMAL_REGEX.matches(trimmed) || (trimmed.contains('.') || trimmed.contains(',')) &&
            trimmed.any { it.isDigit() } &&
            trimmed.all { it.isDigit() || it == '.' || it == ',' || it == '+' || it == '-' }
        ) {
            return OffsetValidationResult.Invalid(ERROR_DECIMAL)
        }

        if (!HUGE_INTEGER_REGEX.matches(trimmed)) {
            return OffsetValidationResult.Invalid(ERROR_INVALID_NUMBER)
        }

        val hasPlus = trimmed.startsWith("+")
        val hasMinus = trimmed.startsWith("-")
        val hadExplicitSign = hasPlus || hasMinus

        val parsed = trimmed.toLongOrNull()
            ?: return OffsetValidationResult.Invalid(ERROR_OUT_OF_RANGE)

        if (parsed == Long.MIN_VALUE) {
            return OffsetValidationResult.Invalid(ERROR_OUT_OF_RANGE)
        }

        val magnitude = kotlin.math.abs(parsed)
        val resolvedDirection = when {
            hasMinus && magnitude > 0L -> OffsetDirection.SUBTRACT
            hasPlus && magnitude > 0L -> OffsetDirection.ADD
            else -> currentDirection
        }

        return OffsetValidationResult.Valid(
            magnitude = magnitude,
            resolvedDirection = resolvedDirection,
            hadExplicitSign = hadExplicitSign
        )
    }
}
