package com.apk.agrostore.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apk.agrostore.domain.repository.AgroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the checkout process.
 */
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val repository: AgroRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        loadCartSummary()
    }

    /**
     * Load cart items and calculate total.
     */
    private fun loadCartSummary() {
        viewModelScope.launch {
            repository.getCartItems().collect { items ->
                val total = items.sumOf { it.getTotalPrice() }
                _uiState.value = _uiState.value.copy(
                    cartItems = items,
                    totalAmount = total
                )
            }
        }
    }

    /**
     * Update shipping address.
     */
    fun updateAddress(address: String) {
        _uiState.value = _uiState.value.copy(shippingAddress = address)
    }

    /**
     * Update payment method.
     */
    fun updatePaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }

    /**
     * Validate stock availability before payment.
     */
    private suspend fun validateStockAvailability(): Boolean {
        val cartItems = _uiState.value.cartItems
        val outOfStockItems = mutableListOf<String>()

        for (cartItem in cartItems) {
            try {
                repository.getProductById(cartItem.product.id).collect { product ->
                    if (product == null) {
                        outOfStockItems.add("${cartItem.product.name} - Produk tidak ditemukan")
                    } else if (product.stock < cartItem.quantity) {
                        outOfStockItems.add("${cartItem.product.name} - Stok tersedia: ${product.stock} (dibutuhkan: ${cartItem.quantity})")
                    }
                }
            } catch (e: Exception) {
                outOfStockItems.add("${cartItem.product.name} - Error checking stock")
                return false
            }
        }

        if (outOfStockItems.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Stok tidak mencukupup:\n\n${outOfStockItems.joinToString("\n")}"
            )
            return false
        }

        return true
    }

    /**
     * Process payment with 2 second delay simulation.
     */
    fun processPayment() {
        val address = _uiState.value.shippingAddress
        val paymentMethod = _uiState.value.paymentMethod

        if (address.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Alamat pengiriman tidak boleh kosong"
            )
            return
        }

        if (_uiState.value.cartItems.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Keranjang kosong"
            )
            return
        }

        viewModelScope.launch {
            try {
                // First validate stock availability
                if (!validateStockAvailability()) {
                    return@launch
                }

                // Set loading state
                _uiState.value = _uiState.value.copy(
                    isProcessing = true,
                    error = null
                )

                // Simulate payment processing with 2 second delay
                delay(2000)

                // Call repository checkout
                val result = repository.checkout(address, paymentMethod)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        isPaymentSuccessful = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = result.exceptionOrNull()?.message ?: "Pembayaran gagal"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Terjadi kesalahan saat pembayaran"
                )
            }
        }
    }

    /**
     * Reset payment success state.
     */
    fun resetPaymentState() {
        _uiState.value = _uiState.value.copy(
            isPaymentSuccessful = false,
            isProcessing = false
        )
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for the checkout process.
 */
data class CheckoutUiState(
    val cartItems: List<com.apk.agrostore.domain.model.CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val shippingAddress: String = "",
    val paymentMethod: String = "Transfer Bank", // Default payment method
    val isProcessing: Boolean = false,
    val isPaymentSuccessful: Boolean = false,
    val error: String? = null
) {
    val availablePaymentMethods = listOf(
        "Transfer Bank",
        "E-Wallet",
        "COD (Bayar di Tempat)"
    )
}