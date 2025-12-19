package com.apk.agrostore.presentation.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apk.agrostore.domain.repository.AgroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

/**
 * ViewModel for the transaction screen.
 */
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: AgroRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    /**
     * Load transactions for the current seller.
     */
    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                repository.getCurrentUser().first { user ->
                    user != null && user.role == "penjual"
                }?.let { seller ->
                    Log.d("TransactionViewModel", "Loading transactions for seller: ${seller.id}")

                    repository.getSellerOrders().collect { orders ->
                        Log.d("TransactionViewModel", "Received ${orders.size} orders")

                        val transactionItems = orders.map { order ->
                            TransactionUiState.TransactionItem(
                                id = order.id,
                                buyerName = order.buyerName ?: "Unknown",
                                status = order.status,
                                date = order.date,
                                total = order.total,
                                items = order.items.map { cartItem ->
                                    TransactionUiState.TransactionItem.OrderItem(
                                        productId = cartItem.product.id,
                                        productName = cartItem.product.name,
                                        productImage = cartItem.product.imageUrl,
                                        quantity = cartItem.quantity,
                                        price = cartItem.product.price
                                    )
                                }
                            )
                        }.sortedByDescending { it.id }

                        _uiState.value = _uiState.value.copy(
                            transactions = transactionItems,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                } ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Anda harus login sebagai penjual untuk melihat transaksi"
                    )
                }
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error loading transactions", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat transaksi: ${e.message}"
                )
            }
        }
    }

    /**
     * Clear any error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Refresh transactions.
     */
    fun refresh() {
        loadTransactions()
    }
}

/**
 * UI state for the transaction screen.
 */
data class TransactionUiState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionItem> = emptyList(),
    val errorMessage: String? = null
) {
    data class TransactionItem(
        val id: String,
        val buyerName: String,
        val status: String,
        val date: String,
        val total: Double,
        val items: List<OrderItem>
    ) {
        data class OrderItem(
            val productId: String,
            val productName: String,
            val productImage: String?,
            val quantity: Int,
            val price: Double
        )
    }
}