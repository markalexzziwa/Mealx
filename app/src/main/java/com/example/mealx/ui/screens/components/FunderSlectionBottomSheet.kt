package com.example.mealx.ui.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mealx.ui.screens.viewmodels.FunderType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunderSelectionBottomSheet(
    currentFunder: FunderType,
    onFunderSelected: (FunderType) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Select Active Funder",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            Divider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onFunderSelected(FunderType.ME)
                        onDismiss()
                    }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Me (Individual)",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                RadioButton(
                    selected = currentFunder == FunderType.ME,
                    onClick = { }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onFunderSelected(FunderType.ORGANISATION)
                        onDismiss()
                    }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Organisation",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                RadioButton(
                    selected = currentFunder == FunderType.ORGANISATION,
                    onClick = { }
                )
            }
        }
    }
}