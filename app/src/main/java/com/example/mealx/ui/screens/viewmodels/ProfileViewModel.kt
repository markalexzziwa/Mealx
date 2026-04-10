package com.example.mealx.ui.screens.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Defines the possible Funder types.
enum class FunderType {
    ME,
    ORGANISATION
}

data class UserProfile(
    val firstName: String = "Timothy",
    val email: String = "timothy@example.com",
    val phone: String = "+1 (555) 123-4567",
    val profileImageUri: String? = null,
    val profileImageBitmap: Bitmap? = null
)

class ProfileViewModel : ViewModel() {
    private val _currentFunder = MutableStateFlow(FunderType.ME)
    val currentFunder = _currentFunder.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile = _userProfile.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing = _isEditing.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode = _isDarkMode.asStateFlow()

    fun setFunder(funder: FunderType) {
        viewModelScope.launch {
            _currentFunder.value = funder
        }
    }

    fun setEditing(editing: Boolean) {
        viewModelScope.launch {
            _isEditing.value = editing
        }
    }

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            _isDarkMode.value = isDark
        }
    }

    fun updateProfileFirstName(newFirstName: String) {
        viewModelScope.launch {
            _userProfile.value = _userProfile.value.copy(firstName = newFirstName)
        }
    }

    fun updateProfilePhone(newPhone: String) {
        viewModelScope.launch {
            _userProfile.value = _userProfile.value.copy(phone = newPhone)
        }
    }

    fun updateProfileImage(bitmap: Bitmap?) {
        viewModelScope.launch {
            _userProfile.value = _userProfile.value.copy(profileImageBitmap = bitmap)
        }
    }

    fun resetProfile() {
        viewModelScope.launch {
            _userProfile.value = UserProfile()
        }
    }
}