package com.example.mealx.ui.screens.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class WalletState(
    val remainingMeals: Int = 0,
    val balance: Double = 0.0,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.MTN,
    val phoneNumber: String = "0747280475",
    val currentMonth: YearMonth = YearMonth.now(),
    val mealCalendar: Map<LocalDate, Boolean> = emptyMap(),
    val mealPrice: Double = 5000.0,
    val subscriptionEndDate: LocalDate = LocalDate.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val missedMeals: Int = 0 // Track missed meals that need to be extended
)

enum class PaymentMethod {
    MTN, AIRTEL
}

sealed class WalletEvent {
    data class UpdatePaymentMethod(val method: PaymentMethod) : WalletEvent()
    data class UpdatePhoneNumber(val number: String) : WalletEvent()
    data class ChangeMonth(val month: YearMonth) : WalletEvent()
    data class ToggleDayPaid(val date: LocalDate) : WalletEvent()
    data class UpdateSelectedDate(val date: LocalDate) : WalletEvent()
    data class MarkMealMissed(val date: LocalDate) : WalletEvent() // New event for missed meals
    object MakePayment : WalletEvent()
}

class WalletViewModel : ViewModel() {
    private val _state = MutableStateFlow(WalletState())
    val state: StateFlow<WalletState> = _state.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Track missed meals that need to be extended
    private val missedMealsToExtend = mutableSetOf<LocalDate>()

    init {
        generateCalendarData()
    }

    fun handleEvent(event: WalletEvent) {
        when (event) {
            is WalletEvent.UpdatePaymentMethod -> {
                _state.value = _state.value.copy(selectedPaymentMethod = event.method)
            }
            is WalletEvent.UpdatePhoneNumber -> {
                _state.value = _state.value.copy(phoneNumber = event.number)
            }
            is WalletEvent.ChangeMonth -> {
                _state.value = _state.value.copy(currentMonth = event.month)
                generateCalendarData()
            }
            is WalletEvent.ToggleDayPaid -> {
                val updatedCalendar = _state.value.mealCalendar.toMutableMap()
                val isCurrentlyPaid = updatedCalendar[event.date] ?: false
                updatedCalendar[event.date] = !isCurrentlyPaid
                _state.value = _state.value.copy(mealCalendar = updatedCalendar)
                updateRemainingMeals()
            }
            is WalletEvent.UpdateSelectedDate -> {
                _state.value = _state.value.copy(selectedDate = event.date)
            }
            is WalletEvent.MarkMealMissed -> {
                markMealMissed(event.date)
            }
            is WalletEvent.MakePayment -> {
                makePayment()
            }
        }
    }

    private fun markMealMissed(date: LocalDate) {
        val today = LocalDate.now()
        if (date.isBefore(today) || date.isEqual(today)) {
            // Only mark past or today's meals as missed
            if (_state.value.mealCalendar[date] == true) {
                // This meal was paid but missed, so it should be extended
                missedMealsToExtend.add(date)
                _state.value = _state.value.copy(
                    missedMeals = missedMealsToExtend.size
                )
                updateRemainingMeals()
            }
        }
    }

    private fun generateCalendarData() {
        val month = _state.value.currentMonth
        val calendarData = mutableMapOf<LocalDate, Boolean>()

        val firstDay = month.atDay(1)
        val lastDay = month.atEndOfMonth()
        val today = LocalDate.now()

        var currentDay = firstDay
        while (!currentDay.isAfter(lastDay)) {
            // Logic to simulate some paid days
            val isPaid = when {
                currentDay.isBefore(today) -> currentDay.dayOfMonth % 4 != 0
                currentDay.isEqual(today) -> true
                currentDay.isAfter(today) && currentDay.isBefore(today.plusDays(6)) -> true
                else -> false
            }
            calendarData[currentDay] = isPaid
            currentDay = currentDay.plusDays(1)
        }

        _state.value = _state.value.copy(mealCalendar = calendarData)
        updateRemainingMeals()
    }

    private fun updateRemainingMeals() {
        val today = LocalDate.now()
        val calendar = _state.value.mealCalendar

        // Remaining meals are future dates (including today) that are already paid
        val remainingPaid = calendar.filter {
            (it.key.isAfter(today) || it.key.isEqual(today)) && it.value
        }.size

        // Balance is for future dates (including today) that are not yet paid
        val unpaidFuture = calendar.filter {
            (it.key.isAfter(today) || it.key.isEqual(today)) && !it.value
        }.size

        // Find the last paid date in the entire calendar
        val lastPaidDate = calendar.filter { it.value }.keys.maxOrNull() ?: today

        // Calculate extended subscription end date considering missed meals
        val extendedEndDate = if (missedMealsToExtend.isNotEmpty()) {
            lastPaidDate.plusDays(missedMealsToExtend.size.toLong())
        } else {
            lastPaidDate
        }

        val estimatedAmount = unpaidFuture * _state.value.mealPrice

        _state.value = _state.value.copy(
            remainingMeals = remainingPaid,
            balance = estimatedAmount,
            subscriptionEndDate = extendedEndDate,
            missedMeals = missedMealsToExtend.size
        )
    }

    private fun makePayment() {
        _loading.value = true
        viewModelScope.launch {
            try {
                kotlinx.coroutines.delay(2000)

                val today = LocalDate.now()
                val updatedCalendar = _state.value.mealCalendar.mapValues { (date, isPaid) ->
                    // Pay for all future unpaid dates
                    if (!isPaid && (date.isAfter(today) || date.isEqual(today))) true else isPaid
                }

                _state.value = _state.value.copy(mealCalendar = updatedCalendar)
                updateRemainingMeals()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Payment failed: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}