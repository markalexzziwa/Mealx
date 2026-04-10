package com.example.mealx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mealx.ui.screens.components.ScreenTemplate
import com.example.mealx.ui.screens.viewmodels.PaymentMethod
import com.example.mealx.ui.screens.viewmodels.WalletEvent
import com.example.mealx.ui.screens.viewmodels.WalletViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun WalletScreen() {
    val viewModel: WalletViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    ScreenTemplate(title = "Wallet") { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                RemainingMealsCard(state.remainingMeals, state.balance)
            }

            item {
                SubscriptionInfoCard(state)
            }

            item {
                MonthlyCalendarCard(
                    state = state,
                    onMonthChange = { newMonth ->
                        viewModel.handleEvent(WalletEvent.ChangeMonth(newMonth))
                    },
                    onDateToggle = { date ->
                        viewModel.handleEvent(WalletEvent.ToggleDayPaid(date))
                    }
                )
            }

            item {
                PaymentMethodSection(state, viewModel)
            }

            item {
                PhoneNumberSection(state, viewModel)
            }

            item {
                PaymentButton(loading, viewModel)
            }

            if (error != null) {
                item {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun RemainingMealsCard(remainingMeals: Int, balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Remaining Meals",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$remainingMeals",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Balance to Pay",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "UGX ${"%,.0f".format(balance)}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SubscriptionInfoCard(state: com.example.mealx.ui.screens.viewmodels.WalletState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Subscription Info",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Active until:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = state.subscriptionEndDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Meal Price:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "UGX ${"%,.0f".format(state.mealPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MonthlyCalendarCard(
    state: com.example.mealx.ui.screens.viewmodels.WalletState,
    onMonthChange: (YearMonth) -> Unit,
    onDateToggle: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Calendar Header with Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(state.currentMonth.minusMonths(1)) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                }
                
                Text(
                    text = state.currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = { onMonthChange(state.currentMonth.plusMonths(1)) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Color(0xFF4CAF50), label = "Paid")
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(color = Color(0xFFF44336), label = "Unpaid")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Days of Week Header
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid Calculation
            val firstDayOfMonth = state.currentMonth.atDay(1)
            val lastDayOfMonth = state.currentMonth.atEndOfMonth()
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 (Mon) to 7 (Sun)
            
            val days = mutableListOf<LocalDate?>()
            for (i in 1 until firstDayOfWeek) {
                days.add(null)
            }
            var currentDay = firstDayOfMonth
            while (!currentDay.isAfter(lastDayOfMonth)) {
                days.add(currentDay)
                currentDay = currentDay.plusDays(1)
            }

            // Render Rows
            days.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (date != null) {
                                val isPaid = state.mealCalendar[date] ?: false
                                val isToday = date == LocalDate.now()
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isPaid) Color(0xFF4CAF50)
                                            else Color(0xFFF44336)
                                        )
                                        .clickable { onDateToggle(date) }
                                        .then(
                                            if (isToday) Modifier.border(
                                                2.dp, 
                                                MaterialTheme.colorScheme.tertiary, 
                                                RoundedCornerShape(8.dp)
                                            )
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    // Handle last partial week
                    if (week.size < 7) {
                        repeat(7 - week.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PaymentMethodSection(state: com.example.mealx.ui.screens.viewmodels.WalletState, viewModel: WalletViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            PaymentMethod.values().forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.handleEvent(WalletEvent.UpdatePaymentMethod(method)) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.selectedPaymentMethod == method,
                        onClick = {
                            viewModel.handleEvent(WalletEvent.UpdatePaymentMethod(method))
                        }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = when (method) {
                            PaymentMethod.MTN -> "MTN Mobile Money"
                            PaymentMethod.AIRTEL -> "Airtel Money"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun PhoneNumberSection(state: com.example.mealx.ui.screens.viewmodels.WalletState, viewModel: WalletViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Payment Phone Number",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = { newNumber ->
                    viewModel.handleEvent(WalletEvent.UpdatePhoneNumber(newNumber))
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phone Number") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = "Phone", tint = MaterialTheme.colorScheme.secondary)
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Phone
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Text(
                text = "Using ${state.selectedPaymentMethod} for payment",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun PaymentButton(loading: Boolean, viewModel: WalletViewModel) {
    Button(
        onClick = { viewModel.handleEvent(WalletEvent.MakePayment) },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        enabled = !loading
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Text(
            text = if (loading) "Processing..." else "Pay for Balance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}