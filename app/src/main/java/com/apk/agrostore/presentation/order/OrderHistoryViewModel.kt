package com.apk.agrostore.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apk.agrostore.domain.repository.AgroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing order history.
 */
@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val repository: AgroRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    /**
     * Load user orders from repository.
     */
    private fun loadOrders() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                repository.getOrders().collect { orders ->
                    _uiState.value = _uiState.value.copy(
                        orders = orders,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Gagal memuat riwayat pesanan"
                )
            }
        }
    }

    /**
     * Refresh orders.
     */
    fun refreshOrders() {
        loadOrders()
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for order history screen.
 */
data class OrderHistoryUiState(
    val orders: List<com.apk.agrostore.domain.model.Order> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)