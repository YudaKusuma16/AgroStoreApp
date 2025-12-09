package com.apk.agrostore.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apk.agrostore.domain.model.User
import com.apk.agrostore.domain.repository.AgroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication operations.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AgroRepository
) : ViewModel() {

    // Login state
    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    // Register state
    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()

    // Input fields
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    /**
     * Update email field
     */
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    /**
     * Update password field
     */
    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    /**
     * Update name field
     */
    fun onNameChange(newName: String) {
        _name.value = newName
    }

    /**
     * Login user with email and password
     */
    fun login(email: String, password: String) {
        _loginState.value = AuthState.Loading

        viewModelScope.launch {
            repository.login(email, password).collect { result ->
                if (result.isSuccess) {
                    _loginState.value = AuthState.Success(result.getOrNull()!!)
                } else {
                    _loginState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Login gagal")
                }
            }
        }
    }

    /**
     * Register new user
     */
    fun register(name: String, email: String, password: String) {
        _registerState.value = AuthState.Loading

        viewModelScope.launch {
            repository.register(name, email, password).collect { result ->
                if (result.isSuccess) {
                    _registerState.value = AuthState.Success()
                } else {
                    _registerState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Registrasi gagal")
                }
            }
        }
    }

    /**
     * Reset login state
     */
    fun resetLoginState() {
        _loginState.value = AuthState.Idle
    }

    /**
     * Reset register state
     */
    fun resetRegisterState() {
        _registerState.value = AuthState.Idle
    }
}

/**
 * Authentication state sealed class
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}