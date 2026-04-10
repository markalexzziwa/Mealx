package com.example.mealx.ui.screens.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Template for a simple ViewModel
abstract class BaseViewModel<State, Event> : ViewModel() {
    protected val _state = MutableStateFlow<State?>(null)
    val state: StateFlow<State?> = _state

    protected val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    protected val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    abstract fun handleEvent(event: Event)

    protected fun <T> executeWithState(
        block: suspend () -> T,
        onSuccess: (T) -> Unit = {},
        onError: (Throwable) -> Unit = { _error.value = it.message }
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                val result = block()
                onSuccess(result)
            } catch (e: Exception) {
                onError(e)
            } finally {
                _loading.value = false
            }
        }
    }
}