package com.example.mealx.navigation

import com.example.mealx.R

sealed class Screens(val route: String, val title: String, val icon: Int) {
    object Home : Screens("home", "Home", R.drawable.ic_home)
    object Scan : Screens("scan", "Scan", R.drawable.ic_scan)
    object Wallet : Screens("wallet", "Wallet", R.drawable.ic_wallet)
    object Profile : Screens("profile", "Profile", R.drawable.ic_profile)
}