package com.example.dayoffset.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dayoffset.calculator.DayOffsetCalculator
import com.example.dayoffset.formatter.DayOffsetFormatter
import com.example.dayoffset.model.DayOffsetHistory
import com.example.dayoffset.model.DayOffsetResult
import com.example.dayoffset.model.DayOffsetUiState
import com.example.dayoffset.model.OffsetDirection
import com.example.dayoffset.model.OffsetUnit
import com.example.dayoffset.model.QuickOffsetPreset
import com.example.dayoffset.util.SystemActionHelper
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: DayOffsetUiState,
    formatter: DayOffsetFormatter,
    calculator: DayOffsetCalculator,
    onStartDateSelected: (LocalDate) -> Unit,
    onTodayClicked: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onOffsetInputChanged: (String) -> Unit,
    onUnitSelected: (OffsetUnit) -> Unit,
    onDirectionSelected: (OffsetDirection) -> Unit,
    onReverseDirection: () -> Unit,
    onQuickOffsetSelected: (QuickOffsetPreset) -> Unit,
    onCalculateClicked: () -> Unit,
    onResetClicked: () -> Unit,
    onSaveToHistory: () -> Boolean,
    onRestoreHistory: (DayOffsetHistory) -> Unit,
    onToggleCalculationExpanded: () -> Unit,
    onShowDatePicker: (Boolean) -> Unit,
    onCopyDate: () -> Unit,
    onCopyResult: () -> Unit,
    onShareResult: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val hapticsEnabled = uiState.settings.hapticFeedback
    val animationsEnabled = uiState.settings.animations
    val dateFormat = uiState.settings.dateFormat

    val formattedStartDate = remember(uiState.startDate, dateFormat) {
        formatter.formatDate(uiState.startDate, dateFormat)
    }
    val formattedStartWeekday = remember(uiState.startDate) {
        formatter.formatWeekday(uiState.startDate)
    }

    if (uiState.isDatePickerVisible) {
        val initialMillis = remember(uiState.startDate) {
            uiState.startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis
        )
        DatePickerDialog(
            onDismissRequest = { onShowDatePicker(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val pickedDate = Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onStartDateSelected(pickedDate)
                        } else {
                            onShowDatePicker(false)
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowDatePicker(false) }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = true
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DayOffset",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Move a date forward or backward.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateToHistory) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History"
                    )
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
                IconButton(onClick = onNavigateToAbout) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About"
                    )
                }
            }
        }

        // START DATE SECTION
        SectionHeader(title = "START DATE")

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Start date, $formattedStartDate."
                }
                .clickable {
                    SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                    onShowDatePicker(true)
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formattedStartDate,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.settings.showWeekday) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formattedStartWeekday,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onShowDatePicker(true)
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Change Date")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Date Navigation Row: ‹ WEEK | ‹ DAY | TODAY | DAY › | WEEK ›
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onPreviousWeek()
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text("‹ WEEK", style = MaterialTheme.typography.labelLarge)
                    }

                    TextButton(
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onPreviousDay()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("DAY", style = MaterialTheme.typography.labelLarge)
                    }

                    AssistChip(
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onTodayClicked()
                        },
                        label = {
                            Text(
                                text = "TODAY",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )

                    TextButton(
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onNextDay()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("DAY", style = MaterialTheme.typography.labelLarge)
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    TextButton(
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onNextWeek()
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text("WEEK ›", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // OFFSET INPUT & DIRECTION SECTION
        SectionHeader(title = "OFFSET")

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = uiState.offsetInput,
                    onValueChange = onOffsetInputChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Offset, ${uiState.offsetInput.ifBlank { "empty" }}."
                        },
                    label = { Text("Offset value") },
                    placeholder = { Text("0") },
                    prefix = {
                        Text(
                            text = if (uiState.offsetInput.trim().startsWith("+") ||
                                uiState.offsetInput.trim().startsWith("-") ||
                                uiState.offsetInput.trim().startsWith("−")
                            ) {
                                ""
                            } else {
                                "${uiState.direction.symbol} "
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    suffix = {
                        Text(
                            text = uiState.unit.labelForCount(uiState.offset),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    isError = uiState.errorMessage != null,
                    supportingText = if (uiState.errorMessage != null) {
                        {
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (!uiState.settings.liveCalculation) {
                                SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                                onCalculateClicked()
                            }
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Direction Selector [ ADD ] [ SUBTRACT ] + Reverse button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.weight(1f)
                    ) {
                        OffsetDirection.entries.forEachIndexed { index, dir ->
                            val selected = uiState.direction == dir
                            SegmentedButton(
                                selected = selected,
                                onClick = {
                                    SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                                    onDirectionSelected(dir)
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = OffsetDirection.entries.size
                                ),
                                icon = {
                                    Icon(
                                        imageVector = if (dir == OffsetDirection.ADD) {
                                            Icons.Default.Add
                                        } else {
                                            Icons.Default.Remove
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.semantics {
                                    contentDescription = if (selected) {
                                        "${dir.displayTitle} selected."
                                    } else {
                                        dir.displayTitle
                                    }
                                }
                            ) {
                                Text(
                                    text = dir.displayTitle.uppercase(),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onReverseDirection()
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier.semantics {
                            contentDescription = "Reverse offset direction"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reverse", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // UNIT SELECTOR SECTION
        SectionHeader(title = "UNIT")

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            OffsetUnit.entries.forEachIndexed { index, unitOption ->
                val selected = uiState.unit == unitOption
                SegmentedButton(
                    selected = selected,
                    onClick = {
                        SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                        onUnitSelected(unitOption)
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = OffsetUnit.entries.size
                    ),
                    modifier = Modifier.semantics {
                        contentDescription = if (selected) {
                            "${unitOption.displayTitle} selected."
                        } else {
                            unitOption.displayTitle
                        }
                    }
                ) {
                    Text(
                        text = unitOption.displayTitle,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // QUICK OFFSETS SECTION
        SectionHeader(title = "QUICK OFFSETS")

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(QuickOffsetPreset.POSITIVE_PRESETS) { preset ->
                    val isSelected = uiState.errorMessage == null &&
                        uiState.offset == preset.offset &&
                        uiState.unit == preset.unit &&
                        uiState.direction == preset.direction
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onQuickOffsetSelected(preset)
                        },
                        label = {
                            Text(
                                text = preset.label,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    )
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(QuickOffsetPreset.NEGATIVE_PRESETS) { preset ->
                    val isSelected = uiState.errorMessage == null &&
                        uiState.offset == preset.offset &&
                        uiState.unit == preset.unit &&
                        uiState.direction == preset.direction
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onQuickOffsetSelected(preset)
                        },
                        label = {
                            Text(
                                text = preset.label,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
        }

        // CALCULATE DATE BUTTON (Prominent when Live Calculation is Off, secondary when On)
        if (!uiState.settings.liveCalculation) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                    onCalculateClicked()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "CALCULATE DATE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            FilledTonalButton(
                onClick = {
                    focusManager.clearFocus()
                    SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                    onCalculateClicked()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "CALCULATE DATE",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // RESULT CARD SECTION
        SectionHeader(title = "RESULT")

        val currentResult = uiState.result
        if (currentResult != null) {
            ResultCard(
                result = currentResult,
                formatter = formatter,
                calculator = calculator,
                uiState = uiState,
                animationsEnabled = animationsEnabled
            )

            // EXPANDABLE BEFORE / AFTER CALCULATION SECTION
            ExpandableCalculationCard(
                result = currentResult,
                formatter = formatter,
                dateFormat = dateFormat,
                expanded = uiState.isCalculationExpanded,
                animationsEnabled = animationsEnabled,
                onToggleExpanded = onToggleCalculationExpanded
            )

            // RESULT DETAILS CARD
            ResultDetailsCard(
                result = currentResult,
                formatter = formatter,
                calculator = calculator,
                uiState = uiState
            )

            // ACTION BUTTONS: [ COPY DATE ] [ COPY RESULT ] and [ SAVE ] [ SHARE ] [ RESET ]
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onCopyDate()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COPY DATE")
                    }

                    OutlinedButton(
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onCopyResult()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COPY RESULT")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onSaveToHistory()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE")
                    }

                    FilledTonalButton(
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onShareResult()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SHARE")
                    }

                    OutlinedButton(
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onResetClicked()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RESET")
                    }
                }
            }
        } else {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Tap CALCULATE DATE to see the resulting date.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (uiState.errorMessage != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center
                    )
                    OutlinedButton(
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onResetClicked()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RESET")
                    }
                }
            }
        }

        // RECENT HISTORY ON HOME SCREEN (Compact)
        if (uiState.history.isNotEmpty()) {
            RecentHistorySection(
                history = uiState.history.take(3),
                formatter = formatter,
                calculator = calculator,
                onRestoreHistory = { entry ->
                    SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                    onRestoreHistory(entry)
                },
                onNavigateToHistory = onNavigateToHistory
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun ResultCard(
    result: DayOffsetResult,
    formatter: DayOffsetFormatter,
    calculator: DayOffsetCalculator,
    uiState: DayOffsetUiState,
    animationsEnabled: Boolean
) {
    val dateFormat = uiState.settings.dateFormat
    val formattedResultDate = formatter.formatDate(result.resultDate, dateFormat)
    val formattedWeekday = formatter.formatWeekday(result.resultDate)
    val summaryText = formatter.formatOffsetSummary(
        startDate = result.startDate,
        offset = result.offset,
        unit = result.unit,
        direction = result.direction,
        formatOption = dateFormat
    )
    val isClamped = calculator.isEndOfMonthClamped(result)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Result date, $formattedResultDate, $formattedWeekday."
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "RESULT",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                letterSpacing = 1.6.sp
            )

            if (animationsEnabled) {
                AnimatedContent(
                    targetState = formattedResultDate,
                    transitionSpec = {
                        fadeIn(tween(240)) togetherWith fadeOut(tween(180))
                    },
                    label = "result_date_transition"
                ) { targetDateText ->
                    Text(
                        text = targetDateText,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = formattedResultDate,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            if (uiState.settings.showWeekday) {
                Text(
                    text = formattedWeekday,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
            ) {
                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    textAlign = TextAlign.Center
                )
            }

            if (result.offset == 0L) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "No date change",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            } else if (isClamped) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "End-of-month adjusted (${result.resultDate.dayOfMonth} ${formatter.formatMonthName(result.resultDate)})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableCalculationCard(
    result: DayOffsetResult,
    formatter: DayOffsetFormatter,
    dateFormat: com.example.dayoffset.model.DateFormatOption,
    expanded: Boolean,
    animationsEnabled: Boolean,
    onToggleExpanded: () -> Unit
) {
    val startStr = formatter.formatDate(result.startDate, dateFormat)
    val offsetStr = formatter.formatOffsetClause(
        offset = result.offset,
        unit = result.unit,
        direction = result.direction,
        compactSign = true
    )
    val resultStr = formatter.formatDate(result.resultDate, dateFormat)

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CALCULATION",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (expanded) "Hide" else "Show step-by-step",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse calculation" else "Expand calculation"
                    )
                }
            }

            val enterTransition = if (animationsEnabled) {
                expandVertically(tween(250)) + fadeIn(tween(250))
            } else {
                expandVertically(tween(0))
            }
            val exitTransition = if (animationsEnabled) {
                shrinkVertically(tween(220)) + fadeOut(tween(200))
            } else {
                shrinkVertically(tween(0))
            }

            AnimatedVisibility(
                visible = expanded,
                enter = enterTransition,
                exit = exitTransition
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    CalculationStepRow(label = "START", value = startStr)
                    CalculationStepRow(label = "OFFSET", value = offsetStr, highlight = true)

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )

                    CalculationStepRow(label = "RESULT", value = resultStr, bold = true)
                }
            }
        }
    }
}

@Composable
private fun CalculationStepRow(
    label: String,
    value: String,
    highlight: Boolean = false,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (bold || highlight) FontWeight.Bold else FontWeight.Medium,
            color = when {
                highlight -> MaterialTheme.colorScheme.primary
                bold -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun ResultDetailsCard(
    result: DayOffsetResult,
    formatter: DayOffsetFormatter,
    calculator: DayOffsetCalculator,
    uiState: DayOffsetUiState
) {
    val settings = uiState.settings
    val formattedResultDate = formatter.formatDate(result.resultDate, settings.dateFormat)
    val weekdayName = formatter.formatWeekday(result.resultDate)
    val monthName = formatter.formatMonthName(result.resultDate)
    val totalDaysDiff = calculator.totalDaysBetween(result.startDate, result.resultDate)

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "RESULT DETAILS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            DetailRow(label = "Result date", value = formattedResultDate)

            if (settings.showWeekday) {
                DetailRow(label = "Weekday", value = weekdayName)
            }

            if (settings.showDayOfYear) {
                DetailRow(label = "Day of year", value = result.dayOfYear.toString())
            }

            if (settings.showWeekNumber) {
                DetailRow(label = "Week of year", value = result.weekOfYear.toString())
            }

            if (settings.showIsoWeek) {
                DetailRow(label = "ISO Week", value = result.isoWeek.toString())
            }

            DetailRow(label = "Month", value = monthName)
            DetailRow(label = "Year", value = result.resultDate.year.toString())

            if (settings.showLeapYear) {
                DetailRow(label = "Leap year", value = if (result.isLeapYear) "Yes" else "No")
            }

            DetailRow(
                label = "Total calendar days shifted",
                value = if (totalDaysDiff >= 0) "+$totalDaysDiff days" else "−${kotlin.math.abs(totalDaysDiff)} days"
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RecentHistorySection(
    history: List<DayOffsetHistory>,
    formatter: DayOffsetFormatter,
    calculator: DayOffsetCalculator,
    onRestoreHistory: (DayOffsetHistory) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onNavigateToHistory,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("View All")
                }
            }

            history.forEachIndexed { index, entry ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
                val parsedStart = runCatching { LocalDate.parse(entry.startDate) }.getOrNull()
                val calcResult = parsedStart?.let {
                    calculator.calculateSafe(it, entry.offset, entry.unit, entry.direction).getOrNull()
                }
                if (parsedStart != null && calcResult != null) {
                    val startCompact = formatter.formatCompactDate(parsedStart, includeYear = false)
                    val clause = formatter.formatOffsetClause(entry.offset, entry.unit, entry.direction)
                    val resultCompact = formatter.formatCompactDate(calcResult.resultDate, includeYear = true)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRestoreHistory(entry) }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$startCompact $clause",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "→ $resultCompact",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
