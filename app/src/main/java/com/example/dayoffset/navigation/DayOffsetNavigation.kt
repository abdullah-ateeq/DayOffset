package com.example.dayoffset.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dayoffset.calculator.DayOffsetCalculator
import com.example.dayoffset.formatter.DayOffsetFormatter
import com.example.dayoffset.ui.DayOffsetViewModel
import com.example.dayoffset.ui.about.AboutScreen
import com.example.dayoffset.ui.history.HistoryScreen
import com.example.dayoffset.ui.home.HomeScreen
import com.example.dayoffset.ui.settings.SettingsScreen
import com.example.dayoffset.ui.splash.SplashScreen
import com.example.dayoffset.util.SystemActionHelper

enum class DayOffsetScreen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    SPLASH(route = "splash", title = "Splash"),
    HOME(route = "home", title = "Home", icon = Icons.Default.CalendarMonth),
    HISTORY(route = "history", title = "History", icon = Icons.Default.History),
    SETTINGS(route = "settings", title = "Settings", icon = Icons.Default.Settings),
    ABOUT(route = "about", title = "About", icon = Icons.Default.Info)
}

@Composable
fun DayOffsetApp(
    viewModel: DayOffsetViewModel,
    navController: NavHostController = rememberNavController(),
    startDestination: String = DayOffsetScreen.SPLASH.route
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val formatter = remember { DayOffsetFormatter() }
    val calculator = remember { DayOffsetCalculator() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    val topLevelDestinations = remember {
        listOf(
            DayOffsetScreen.HOME,
            DayOffsetScreen.HISTORY,
            DayOffsetScreen.SETTINGS,
            DayOffsetScreen.ABOUT
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (currentRoute != null && currentRoute != DayOffsetScreen.SPLASH.route) {
                NavigationBar {
                    topLevelDestinations.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                screen.icon?.let {
                                    Icon(imageVector = it, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(DayOffsetScreen.SPLASH.route) {
                SplashScreen(
                    animationsEnabled = uiState.settings.animations,
                    onSplashFinished = {
                        navController.navigate(DayOffsetScreen.HOME.route) {
                            popUpTo(DayOffsetScreen.SPLASH.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(DayOffsetScreen.HOME.route) {
                HomeScreen(
                    uiState = uiState,
                    formatter = formatter,
                    calculator = calculator,
                    onStartDateSelected = viewModel::onStartDateSelected,
                    onTodayClicked = viewModel::onTodayClicked,
                    onPreviousDay = viewModel::onPreviousDay,
                    onNextDay = viewModel::onNextDay,
                    onPreviousWeek = viewModel::onPreviousWeek,
                    onNextWeek = viewModel::onNextWeek,
                    onOffsetInputChanged = viewModel::onOffsetInputChanged,
                    onUnitSelected = viewModel::onUnitSelected,
                    onDirectionSelected = viewModel::onDirectionSelected,
                    onReverseDirection = viewModel::onReverseDirection,
                    onQuickOffsetSelected = viewModel::onQuickOffsetSelected,
                    onCalculateClicked = viewModel::onCalculateClicked,
                    onResetClicked = viewModel::onResetClicked,
                    onSaveToHistory = viewModel::onSaveToHistory,
                    onRestoreHistory = viewModel::onRestoreHistory,
                    onToggleCalculationExpanded = viewModel::onToggleCalculationExpanded,
                    onShowDatePicker = viewModel::onShowDatePicker,
                    onCopyDate = {
                        val text = viewModel.getCopyDateText()
                        if (text != null) {
                            SystemActionHelper.copyToClipboard(context, "Result Date", text)
                            viewModel.onSnackMessage("Result date copied: $text")
                        }
                    },
                    onCopyResult = {
                        val text = viewModel.getCopyResultText()
                        if (text != null) {
                            SystemActionHelper.copyToClipboard(context, "DayOffset Calculation", text)
                            viewModel.onSnackMessage("Full calculation copied to clipboard")
                        }
                    },
                    onShareResult = {
                        val text = viewModel.getCopyResultText()
                        if (text != null) {
                            SystemActionHelper.shareText(context, text)
                        }
                    },
                    onNavigateToHistory = {
                        navController.navigate(DayOffsetScreen.HISTORY.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(DayOffsetScreen.SETTINGS.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToAbout = {
                        navController.navigate(DayOffsetScreen.ABOUT.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(DayOffsetScreen.HISTORY.route) {
                HistoryScreen(
                    history = uiState.history,
                    settings = uiState.settings,
                    formatter = formatter,
                    calculator = calculator,
                    onRestoreEntry = { entry ->
                        viewModel.onRestoreHistory(entry)
                        navController.navigate(DayOffsetScreen.HOME.route) {
                            popUpTo(DayOffsetScreen.HOME.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onDeleteEntry = viewModel::onDeleteHistoryEntry,
                    onClearAllHistory = viewModel::onClearHistory,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(DayOffsetScreen.SETTINGS.route) {
                SettingsScreen(
                    settings = uiState.settings,
                    onUpdateThemeMode = viewModel::updateThemeMode,
                    onUpdateDynamicColor = viewModel::updateDynamicColor,
                    onUpdateLiveCalculation = viewModel::updateLiveCalculation,
                    onUpdateDefaultUnit = viewModel::updateDefaultUnit,
                    onUpdateDefaultDirection = viewModel::updateDefaultDirection,
                    onUpdateDateFormat = viewModel::updateDateFormat,
                    onUpdateShowWeekday = viewModel::updateShowWeekday,
                    onUpdateShowWeekNumber = viewModel::updateShowWeekNumber,
                    onUpdateShowIsoWeek = viewModel::updateShowIsoWeek,
                    onUpdateShowDayOfYear = viewModel::updateShowDayOfYear,
                    onUpdateShowLeapYear = viewModel::updateShowLeapYear,
                    onUpdateHapticFeedback = viewModel::updateHapticFeedback,
                    onUpdateAnimations = viewModel::updateAnimations,
                    onUpdateConfirmDelete = viewModel::updateConfirmDelete,
                    onUpdateRestoreLastDate = viewModel::updateRestoreLastDate,
                    onClearHistory = viewModel::onClearHistory,
                    onResetSettings = viewModel::onResetSettings,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(DayOffsetScreen.ABOUT.route) {
                AboutScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
