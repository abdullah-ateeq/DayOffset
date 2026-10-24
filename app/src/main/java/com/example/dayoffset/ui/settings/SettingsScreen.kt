package com.example.dayoffset.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dayoffset.model.AppSettings
import com.example.dayoffset.model.DateFormatOption
import com.example.dayoffset.model.OffsetDirection
import com.example.dayoffset.model.OffsetUnit
import com.example.dayoffset.model.ThemeMode
import com.example.dayoffset.util.SystemActionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateThemeMode: (ThemeMode) -> Unit,
    onUpdateDynamicColor: (Boolean) -> Unit,
    onUpdateLiveCalculation: (Boolean) -> Unit,
    onUpdateDefaultUnit: (OffsetUnit) -> Unit,
    onUpdateDefaultDirection: (OffsetDirection) -> Unit,
    onUpdateDateFormat: (DateFormatOption) -> Unit,
    onUpdateShowWeekday: (Boolean) -> Unit,
    onUpdateShowWeekNumber: (Boolean) -> Unit,
    onUpdateShowIsoWeek: (Boolean) -> Unit,
    onUpdateShowDayOfYear: (Boolean) -> Unit,
    onUpdateShowLeapYear: (Boolean) -> Unit,
    onUpdateHapticFeedback: (Boolean) -> Unit,
    onUpdateAnimations: (Boolean) -> Unit,
    onUpdateConfirmDelete: (Boolean) -> Unit,
    onUpdateRestoreLastDate: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onResetSettings: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val hapticsEnabled = settings.hapticFeedback
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History") },
            text = { Text("Clear all saved calculations?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                        onClearHistory()
                        showClearHistoryDialog = false
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
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // APPEARANCE
            SettingsSectionCard(title = "APPEARANCE") {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = settings.themeMode == mode,
                            onClick = {
                                SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                                onUpdateThemeMode(mode)
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = ThemeMode.entries.size
                            )
                        ) {
                            Text(mode.displayTitle)
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                SettingsSwitchRow(
                    title = "Dynamic Color",
                    subtitle = "Use Material You wallpaper colors on Android 12+",
                    checked = settings.dynamicColor,
                    onCheckedChange = {
                        SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                        onUpdateDynamicColor(it)
                    }
                )
            }

            // CALCULATION
            SettingsSectionCard(title = "CALCULATION") {
                SettingsSwitchRow(
                    title = "Live Calculation",
                    subtitle = "Immediately update result when date, offset, unit, or direction changes",
                    checked = settings.liveCalculation,
                    onCheckedChange = {
                        SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                        onUpdateLiveCalculation(it)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Text(
                    text = "Default Unit",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                val coreDefaultUnits = listOf(OffsetUnit.DAYS, OffsetUnit.WEEKS, OffsetUnit.MONTHS)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    coreDefaultUnits.forEachIndexed { index, unit ->
                        SegmentedButton(
                            selected = settings.defaultUnit == unit,
                            onClick = {
                                SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                                onUpdateDefaultUnit(unit)
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = coreDefaultUnits.size
                            )
                        ) {
                            Text(unit.displayTitle)
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Text(
                    text = "Default Direction",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    OffsetDirection.entries.forEachIndexed { index, direction ->
                        SegmentedButton(
                            selected = settings.defaultDirection == direction,
                            onClick = {
                                SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                                onUpdateDefaultDirection(direction)
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = OffsetDirection.entries.size
                            )
                        ) {
                            Text(direction.displayTitle)
                        }
                    }
                }
            }

            // DISPLAY
            SettingsSectionCard(title = "DISPLAY") {
                Text(
                    text = "Date Format",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                DateFormatOption.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                                onUpdateDateFormat(option)
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.dateFormat == option,
                            onClick = {
                                SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                                onUpdateDateFormat(option)
                            }
                        )
                        Text(
                            text = option.exampleLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                SettingsSwitchRow(
                    title = "Show Weekday",
                    subtitle = "Display day of the week (e.g., Tuesday)",
                    checked = settings.showWeekday,
                    onCheckedChange = {
                        SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                        onUpdateShowWeekday(it)
                    }
                )

                SettingsSwitchRow(
                    title = "Show Week Number",
                    subtitle = "Display locale-aware week of year",
                    checked = settings.showWeekNumber,
                    onCheckedChange = {
                        SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                        onUpdateShowWeekNumber(it)
                    }
                )

                SettingsSwitchRow(
                    title = "Show ISO Week",
                    subtitle = "Display ISO-8601 week of year",
                    checked = settings.showIsoWeek,
                    onCheckedChange = {
                        SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                        onUpdateShowIsoWeek(it)
                    }
                )

                SettingsSwitchRow(
                    title = "Show Day of Year",
                    subtitle = "Display day number within the year (1–366)",
                    checked = settings.showDayOfYear,
                    onCheckedChange = {
                        SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                        onUpdateShowDayOfYear(it)
                    }
                )

                SettingsSwitchRow(
                    title = "Show Leap Year",
                    subtitle = "Display whether the resulting year is a leap year",
                    checked = settings.showLeapYear,
                    onCheckedChange = {
                        SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                        onUpdateShowLeapYear(it)
                    }
                )
            }

            // BEHAVIOR
            SettingsSectionCard(title = "BEHAVIOR") {
                SettingsSwitchRow(
                    title = "Haptic Feedback",
                    subtitle = "Light tactile feedback on controls and actions",
                    checked = settings.hapticFeedback,
                    onCheckedChange = {
                        SystemActionHelper.performLightHaptic(view, it)
                        onUpdateHapticFeedback(it)
                    }
                )

                SettingsSwitchRow(
                    title = "Animations",
                    subtitle = "Subtle transitions when updating dates and expanding details",
                    checked = settings.animations,
                    onCheckedChange = {
                        SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                        onUpdateAnimations(it)
                    }
                )

                SettingsSwitchRow(
                    title = "Confirm Delete",
                    subtitle = "Ask for confirmation before deleting saved calculations",
                    checked = settings.confirmDelete,
                    onCheckedChange = {
                        SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                        onUpdateConfirmDelete(it)
                    }
                )

                SettingsSwitchRow(
                    title = "Restore Last Date",
                    subtitle = "Remember the last selected starting date on app restart",
                    checked = settings.restoreLastDate,
                    onCheckedChange = {
                        SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                        onUpdateRestoreLastDate(it)
                    }
                )
            }

            // DATA & RESET ACTIONS
            SettingsSectionCard(title = "DATA & DEFAULTS") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showClearHistoryDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear History")
                    }

                    OutlinedButton(
                        onClick = {
                            SystemActionHelper.performLightHaptic(view, hapticsEnabled)
                            onResetSettings()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset Settings")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
