package com.example.dayoffset.ui.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dayoffset.calculator.DayOffsetCalculator
import com.example.dayoffset.formatter.DayOffsetFormatter
import com.example.dayoffset.model.AppSettings
import com.example.dayoffset.model.DayOffsetHistory
import com.example.dayoffset.util.SystemActionHelper
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    history: List<DayOffsetHistory>,
    settings: AppSettings,
    formatter: DayOffsetFormatter,
    calculator: DayOffsetCalculator,
    onRestoreEntry: (DayOffsetHistory) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val hapticsEnabled = settings.hapticFeedback
    var pendingDeleteEntry by remember { mutableStateOf<DayOffsetHistory?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    val entryToDelete = pendingDeleteEntry
    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteEntry = null },
            title = { Text("Delete Calculation") },
            text = { Text("Delete this saved calculation?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                        onDeleteEntry(entryToDelete.id)
                        pendingDeleteEntry = null
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteEntry = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear History") },
            text = { Text("Clear all saved calculations?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                        onClearAllHistory()
                        showClearAllDialog = false
                    }
                ) {
                    Text(
                        text = "Clear",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${history.size} / 10 saved calculations",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                if (history.isNotEmpty()) {
                    IconButton(onClick = { showClearAllDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear all history"
                        )
                    }
                }
            }
        )

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "No saved calculations yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap SAVE on the Home screen to store up to 10 calculations for quick recall.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = history,
                    key = { it.id }
                ) { item ->
                    @Suppress("DEPRECATION")
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart ||
                                dismissValue == SwipeToDismissBoxValue.StartToEnd
                            ) {
                                if (settings.confirmDelete) {
                                    pendingDeleteEntry = item
                                    false
                                } else {
                                    SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                                    onDeleteEntry(item.id)
                                    true
                                }
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    ) {
                        HistoryEntryCard(
                            entry = item,
                            settings = settings,
                            formatter = formatter,
                            calculator = calculator,
                            onClick = {
                                SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                                onRestoreEntry(item)
                            },
                            onDeleteClick = {
                                if (settings.confirmDelete) {
                                    pendingDeleteEntry = item
                                } else {
                                    SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                                    onDeleteEntry(item.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEntryCard(
    entry: DayOffsetHistory,
    settings: AppSettings,
    formatter: DayOffsetFormatter,
    calculator: DayOffsetCalculator,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val parsedStartDate = remember(entry.startDate) {
        runCatching { LocalDate.parse(entry.startDate) }.getOrNull()
    }
    val recalculatedResult = remember(parsedStartDate, entry.offset, entry.unit, entry.direction) {
        parsedStartDate?.let {
            calculator.calculateSafe(it, entry.offset, entry.unit, entry.direction).getOrNull()
        }
    }

    val startText = parsedStartDate?.let { formatter.formatDate(it, settings.dateFormat) } ?: entry.startDate
    val offsetText = formatter.formatOffsetClause(entry.offset, entry.unit, entry.direction)
    val resultText = recalculatedResult?.let { formatter.formatDate(it.resultDate, settings.dateFormat) } ?: "—"
    val weekdayText = recalculatedResult?.let { formatter.formatWeekday(it.resultDate) }
    val savedTimestampText = remember(entry.savedAt) {
        formatter.formatSavedTimestamp(entry.savedAt)
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "DayOffset",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = startText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = offsetText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (settings.showWeekday && weekdayText != null) {
                        "$resultText • $weekdayText"
                    } else {
                        resultText
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = savedTimestampText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete saved calculation",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
