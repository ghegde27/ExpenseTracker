package com.example.fitnesstracker.presentation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitnesstracker.navigation.Routes
import com.example.fitnesstracker.permissions.PermissionManager

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun TrainerTrackerApp() {
    MaterialTheme {
        val context = LocalContext.current
        val prefs = remember(context) { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
        var showPermissionOnboarding by remember {
            mutableStateOf(
                !prefs.getBoolean("permissions_onboarding_completed", false) ||
                        !PermissionManager.allPermissionsGranted(context)
            )
        }

        if (showPermissionOnboarding) {
            PermissionOnboardingScreen(
                onContinue = {
                    prefs.edit().putBoolean("permissions_onboarding_completed", true).apply()
                    showPermissionOnboarding = false
                }
            )
        } else {
            val nav = rememberNavController()
            val navBackStackEntry by nav.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val navItems = listOf(
                NavItem(Routes.HOME, "Home", Icons.Default.Home),
                NavItem(Routes.CLIENTS, "Clients", Icons.Default.People),
                NavItem(Routes.INCOME, "Income", Icons.Default.AttachMoney),
                NavItem(Routes.EXPENSES, "Expenses", Icons.AutoMirrored.Filled.ReceiptLong),
                NavItem(Routes.REPORTS, "Reports", Icons.Default.Analytics),
                NavItem(Routes.SETTINGS, "Settings", Icons.Default.Settings)
            )

            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1E3C72)
                    ) {
                        navItems.forEach { item ->
                            val isSelected = currentRoute == item.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        nav.navigate(item.route) {
                                            popUpTo(Routes.HOME) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF1E3C72),
                                    selectedTextColor = Color(0xFF1E3C72),
                                    indicatorColor = Color(0xFFE3F2FD),
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                )
                            )
                        }
                    }
                }
            ) { paddingValues ->
                NavHost(
                    navController = nav,
                    startDestination = Routes.HOME,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable(Routes.HOME) {
                        DashboardScreen(
                            onNavigateToIncome = { nav.navigate(Routes.INCOME) },
                            onNavigateToExpense = { nav.navigate(Routes.EXPENSES) },
                            onNavigateToClients = { nav.navigate(Routes.CLIENTS) }
                        )
                    }
                    composable(Routes.CLIENTS) { ClientsScreen() }
                    composable(Routes.INCOME) { IncomeScreen() }
                    composable(Routes.EXPENSES) { ExpenseScreen() }
                    composable(Routes.REPORTS) { ReportsScreen() }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            onReviewPermissions = {
                                showPermissionOnboarding = true
                            }
                        )
                    }
                    composable(Routes.PERMISSIONS) {
                        PermissionOnboardingScreen(
                            onContinue = {
                                nav.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
