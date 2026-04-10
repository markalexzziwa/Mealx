package com.example.mealx

import com.example.mealx.navigation.BottomNavigationBar
import com.example.mealx.navigation.Screens
import com.example.mealx.ui.screens.HomeScreen
import com.example.mealx.ui.screens.ScanScreen
import com.example.mealx.ui.screens.WalletScreen
import com.example.mealx.ui.screens.ProfileScreen
import com.example.mealx.ui.screens.viewmodels.ProfileViewModel
import com.example.mealx.ui.screens.viewmodels.ScanViewModel
import com.example.mealx.ui.theme.MealXTheme
import androidx.activity.viewModels
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode by profileViewModel.isDarkMode.collectAsStateWithLifecycle()
            MealXTheme(darkTheme = isDarkMode) {
                MealXApp(profileViewModel = profileViewModel)
            }
        }
    }
}

@Composable
fun MealXApp(profileViewModel: ProfileViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, profileViewModel = profileViewModel)
        }
    ) { innerPadding ->
        NavigationHost(
            navController = navController,
            padding = innerPadding,
            profileViewModel = profileViewModel
        )
    }
}

@Composable
fun NavigationHost(
    navController: NavHostController,
    padding: PaddingValues,
    profileViewModel: ProfileViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screens.Home.route,
        modifier = Modifier.padding(padding)
    ) {
        composable(Screens.Home.route) {
            HomeScreen(profileViewModel = profileViewModel)
        }
        composable(Screens.Scan.route) {
            ScanScreen() // ViewModel is created via viewModel() default parameter
        }
        composable(Screens.Wallet.route) { WalletScreen() }
        composable(Screens.Profile.route) { ProfileScreen(profileViewModel) }
    }
}