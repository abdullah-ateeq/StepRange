package com.example.steprange.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.steprange.ui.StepRangeUiState
import com.example.steprange.ui.StepRangeViewModel
import com.example.steprange.ui.about.AboutScreen
import com.example.steprange.ui.history.HistoryScreen
import com.example.steprange.ui.home.HomeScreen
import com.example.steprange.ui.settings.SettingsScreen
import com.example.steprange.ui.splash.SplashScreen

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Splash : Screen("splash", "Splash", Icons.Filled.FormatListNumbered, Icons.Outlined.FormatListNumbered)
    object Home : Screen("home", "Home", Icons.Filled.FormatListNumbered, Icons.Outlined.FormatListNumbered)
    object History : Screen("history", "History", Icons.Filled.History, Icons.Outlined.History)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    object About : Screen("about", "About", Icons.Filled.Info, Icons.Outlined.Info)
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.History,
    Screen.Settings,
    Screen.About
)

@Composable
fun StepRangeNavigation(
    viewModel: StepRangeViewModel,
    uiState: StepRangeUiState,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != null && currentRoute != Screen.Splash.route) {
                NavigationBar {
                    BottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    uiState = uiState,
                    onStartChanged = viewModel::onStartInputChanged,
                    onIncrementChanged = viewModel::onIncrementInputChanged,
                    onEndChanged = viewModel::onEndInputChanged,
                    onIncludeEndChanged = viewModel::onIncludeEndChanged,
                    onGenerate = viewModel::generateRange,
                    onCancel = viewModel::cancelCalculation,
                    onClear = viewModel::clearInputs,
                    onSaveToHistory = viewModel::saveCurrentToHistory,
                    onFindValueIndexChanged = viewModel::onFindValueIndexChanged,
                    onCalculateValueAtIndex = viewModel::calculateValueAtIndex,
                    onToggleFormulaExpanded = viewModel::toggleFormulaExpanded,
                    onClearSnackbar = viewModel::clearSnackbarMessage
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    history = uiState.history,
                    confirmDelete = uiState.settings.confirmDelete,
                    onRestore = { item ->
                        viewModel.restoreHistoryItem(item)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    },
                    onDelete = viewModel::deleteHistoryItem,
                    onClearAll = viewModel::clearHistory
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    settings = uiState.settings,
                    onThemeChanged = viewModel::updateTheme,
                    onDynamicColorChanged = viewModel::updateDynamicColor,
                    onHapticsChanged = viewModel::updateHapticFeedback,
                    onAnimationsChanged = viewModel::updateAnimations,
                    onConfirmDeleteChanged = viewModel::updateConfirmDelete,
                    onDecimalPlacesChanged = viewModel::updateDecimalPlaces,
                    onRoundingModeChanged = viewModel::updateRoundingMode,
                    onClearHistory = viewModel::clearHistory,
                    onResetSettings = viewModel::resetSettings
                )
            }

            composable(Screen.About.route) {
                AboutScreen()
            }
        }
    }
}
