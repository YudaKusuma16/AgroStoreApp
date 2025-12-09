package com.apk.agrostore.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apk.agrostore.domain.model.User
import com.apk.agrostore.domain.repository.AgroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing user profile and logout functionality.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: AgroRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    /**
     * Load current user data from repository.
     */
    private fun loadUserData() {
        viewModelScope.launch {
            repository.getCurrentUser().collect { user ->
                _uiState.value = _uiState.value.copy(
                    currentUser = user,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Logout user and clear session.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Call repository to logout
                repository.logout()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLogoutSuccessful = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Gagal melakukan logout"
                )
            }
        }
    }

    /**
     * Clear success state after navigation.
     */
    fun clearLogoutSuccess() {
        _uiState.value = _uiState.value.copy(isLogoutSuccessful = false)
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for profile screen.
 */
data class ProfileUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = true,
    val isLogoutSuccessful: Boolean = false,
    val error: String? = null
)