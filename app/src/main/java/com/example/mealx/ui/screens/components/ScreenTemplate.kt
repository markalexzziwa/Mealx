package com.example.mealx.ui.screens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun ScreenTemplate(
    title: String,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Column {
        topBar()
        content(PaddingValues(0.dp))
    }
}