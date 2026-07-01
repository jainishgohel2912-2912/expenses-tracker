package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge full bleed rendering
        enableEdgeToEdge()
        
        setContent {
            val viewModel: FinanceViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isUserLoggedIn) {
                        // Secure Authentication screen
                        AuthScreen(
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .navigationBarsPadding()
                        )
                    } else {
                        // Logged in Dashboard + Navigation Scaffold
                        MainAppContent(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: FinanceViewModel) {
    var selectedTab by remember { mutableStateOf("DASHBOARD") }
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val navBarBg = if (isDarkMode) Color(0xFF1E293B) else Color.White
    val selectedIconColor = Color(0xFF3B82F6)
    val unselectedIconColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // High fidelity Material 3 Bottom Navigation bar
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .testTag("app_bottom_nav_bar"),
                containerColor = navBarBg,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                // Dashboard Tab
                NavigationBarItem(
                    selected = selectedTab == "DASHBOARD",
                    onClick = { selectedTab = "DASHBOARD" },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedIconColor,
                        unselectedIconColor = unselectedIconColor,
                        indicatorColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.testTag("nav_dashboard_tab")
                )

                // Transactions Tab
                NavigationBarItem(
                    selected = selectedTab == "TRANSACTIONS",
                    onClick = { selectedTab = "TRANSACTIONS" },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Logs") },
                    label = { Text("Logs") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedIconColor,
                        unselectedIconColor = unselectedIconColor,
                        indicatorColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.testTag("nav_transactions_tab")
                )

                // Budgets & Savings Tab
                NavigationBarItem(
                    selected = selectedTab == "BUDGETS",
                    onClick = { selectedTab = "BUDGETS" },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Targets") },
                    label = { Text("Targets") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedIconColor,
                        unselectedIconColor = unselectedIconColor,
                        indicatorColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.testTag("nav_budgets_tab")
                )

                // AI Coach Tab
                NavigationBarItem(
                    selected = selectedTab == "AIC_COACH",
                    onClick = { selectedTab = "AIC_COACH" },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "AI Coach") },
                    label = { Text("AI Coach") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedIconColor,
                        unselectedIconColor = unselectedIconColor,
                        indicatorColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.testTag("nav_ai_coach_tab")
                )

                // Settings & Profile Tab
                NavigationBarItem(
                    selected = selectedTab == "PROFILE",
                    onClick = { selectedTab = "PROFILE" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedIconColor,
                        unselectedIconColor = unselectedIconColor,
                        indicatorColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.testTag("nav_profile_tab")
                )
            }
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "DASHBOARD" -> DashboardScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                "TRANSACTIONS" -> TransactionsScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                "BUDGETS" -> BudgetsScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                "AIC_COACH" -> AiCoachScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                "PROFILE" -> ProfileScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
