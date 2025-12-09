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
 * ViewModel for updating user profile.
 */
@HiltViewModel
class UpdateProfileViewModel @Inject constructor(
    private val repository: AgroRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(UpdateProfileUiState())
    val uiState: StateFlow<UpdateProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    /**
     * Load current user data.
     */
    private fun loadCurrentUser() {
        viewModelScope.launch {
            repository.getCurrentUser().collect { user ->
                user?.let {
                    _uiState.value = _uiState.value.copy(
                        name = it.name,
                        email = it.email,
                        role = it.role,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Update user name.
     */
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    /**
     * Update user email.
     */
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    /**
     * Save profile changes.
     */
    fun saveProfile() {
        val currentState = _uiState.value

        // Validate input
        if (currentState.name.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Nama tidak boleh kosong"
            )
            return
        }

        if (currentState.email.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Email tidak boleh kosong"
            )
            return
        }

        if (!isValidEmail(currentState.email)) {
            _uiState.value = _uiState.value.copy(
                error = "Format email tidak valid"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                repository.getCurrentUser().first()?.let { currentUser ->
                    val updatedUser = currentUser.copy(
                        name = currentState.name,
                        email = currentState.email
                    )

                    repository.updateUser(updatedUser).collect { result ->
                        if (result.isSuccess) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isUpdateSuccessful = true,
                                error = null
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.exceptionOrNull()?.message ?: "Gagal memperbarui profil"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Terjadi kesalahan saat memperbarui profil"
                )
            }
        }
    }

    /**
     * Check if email format is valid.
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Clear success state.
     */
    fun clearUpdateSuccess() {
        _uiState.value = _uiState.value.copy(isUpdateSuccessful = false)
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for update profile screen.
 */
data class UpdateProfileUiState(
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val isLoading: Boolean = true,
    val isUpdateSuccessful: Boolean = false,
    val error: String? = null
)