package com.example.mealx.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mealx.ui.screens.viewmodels.FunderType
import com.example.mealx.ui.screens.viewmodels.ProfileViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun BottomNavigationBar(navController: NavController, profileViewModel: ProfileViewModel) {
    val currentFunder by profileViewModel.currentFunder.collectAsStateWithLifecycle()

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val items = listOf(
            Screens.Home,
            Screens.Scan,
            Screens.Wallet,
            Screens.Profile
        )

        items.forEach { item ->
            val isSelected = currentRoute == item.route
            
            NavigationBarItem(
                icon = { 
                    Icon(
                        painter = painterResource(id = item.icon), 
                        contentDescription = item.title 
                    ) 
                },
                label = {
                    val labelText = if (item is Screens.Profile) {
                        val selection = if (currentFunder == FunderType.ME) "Me" else "Org"
                        "${item.title} ($selection)"
                    } else {
                        item.title
                    }
                    Text(labelText)
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}