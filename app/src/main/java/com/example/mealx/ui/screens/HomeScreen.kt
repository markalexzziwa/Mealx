package com.example.mealx.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mealx.ui.components.EmptyStateComponent
import com.example.mealx.ui.components.ErrorComponent
import com.example.mealx.ui.components.FoodItemCard
import com.example.mealx.ui.components.LoadingComponent
import com.example.mealx.ui.screens.components.ScreenTemplate
import com.example.mealx.ui.screens.viewmodels.HomeViewModel
import com.example.mealx.ui.screens.viewmodels.ProfileViewModel
import com.example.mealx.ui.theme.MealXTheme

sealed class HomeScreenState {
    data object Loading : HomeScreenState()
    data class Success(val items: List<FoodItem>) : HomeScreenState()
    data class Error(val message: String) : HomeScreenState()
}

data class FoodItem(
    val id: String,
    val title: String,
    val description: String,
    val price: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel? = null,
    profileViewModel: ProfileViewModel? = null,
    onMenuClick: () -> Unit = {},      // Menu click handler
    onProfileClick: () -> Unit = {}    // Profile click handler
) {
    var screenState by remember { mutableStateOf<HomeScreenState>(HomeScreenState.Loading) }

    // Get the user's first name from ProfileViewModel
    val userProfile = profileViewModel?.userProfile?.collectAsStateWithLifecycle()?.value
    val firstName = userProfile?.firstName ?: "Timothy"

    LaunchedEffect(Unit) {
        screenState = HomeScreenState.Success(
            listOf(
                FoodItem("1", "Chicken Luwombo", "Classic Italian pasta", "$12.99"),
                FoodItem("2", "Caesar Salad", "Fresh salad with dressing", "$8.99"),
                FoodItem("3", "Chicken Burger", "Juicy chicken burger", "$10.99"),
                FoodItem("4", "French Fries", "Crispy golden fries", "$4.99"),
            )
        )
    }

    ScreenTemplate(
        title = "Home",
        topBar = {
            TopAppBar(
                title = {
                    // Empty title since we're placing content manually
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    // Left side: Menu icon and "MealX" text
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MealX",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                actions = {
                    // Right side: "Hi, [FirstName]" text and profile icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Hi, $firstName",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onProfileClick) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        }
    ) {
        when (val state = screenState) {
            is HomeScreenState.Loading -> {
                LoadingComponent()
            }
            is HomeScreenState.Error -> {
                ErrorComponent(
                    message = state.message,
                    onRetry = {
                        screenState = HomeScreenState.Loading
                        // Trigger retry logic
                    }
                )
            }
            is HomeScreenState.Success -> {
                if (state.items.isEmpty()) {
                    EmptyStateComponent(
                        title = "No Food Items",
                        description = "Check back later for new menu items!"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(state.items) { item ->
                            FoodItemCard(
                                title = item.title,
                                description = item.description,
                                price = item.price,
                                onItemClick = {
                                    // Navigate to detail screen
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    MealXTheme {
        HomeScreen()
    }
}