package com.example.dayoffset.model

import java.time.LocalDate

data class DayOffsetUiState(
    val startDate: LocalDate = LocalDate.now(),
    val offset: Long = 0L,
    val unit: OffsetUnit = OffsetUnit.DAYS,
    val direction: OffsetDirection = OffsetDirection.ADD,
    val resultDate: LocalDate? = null,
    val result: DayOffsetResult? = null,
    val isCalculating: Boolean = false,
    val errorMessage: String? = null,
    val history: List<DayOffsetHistory> = emptyList(),
    val offsetInput: String = "0",
    val settings: AppSettings = AppSettings(),
    val isCalculationExpanded: Boolean = false,
    val isDatePickerVisible: Boolean = false,
    val snackbarMessage: String? = null
)
