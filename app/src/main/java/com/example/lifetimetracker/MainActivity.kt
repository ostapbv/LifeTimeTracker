package com.example.lifetimetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lifetimetracker.presentation.screen.dashboard.DashboardScreen
import com.example.lifetimetracker.presentation.screen.logactivity.AddActivityScreen
import com.example.lifetimetracker.ui.theme.LifeTimeTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LifeTimeTrackerTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard") {
                        DashboardScreen(
                            onAddActivityClick = {
                                navController.navigate("add_activity")
                            }
                        )
                    }
                    composable("add_activity") {
                        AddActivityScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}