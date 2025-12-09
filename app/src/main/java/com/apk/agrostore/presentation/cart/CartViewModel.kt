package com.apk.agrostore.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apk.agrostore.domain.model.CartItem
import com.apk.agrostore.domain.repository.AgroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the shopping cart.
 */
@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: AgroRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        loadCartItems()
    }

    /**
     * Load cart items from repository.
     */
    private fun loadCartItems() {
        viewModelScope.launch {
            repository.getCartItems().collect { items ->
                val total = items.sumOf { it.getTotalPrice() }
                _uiState.value = _uiState.value.copy(
                    cartItems = items,
                    totalPrice = total,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Update quantity of cart item with stock validation.
     */
    fun updateQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                if (quantity <= 0) {
                    _uiState.value = _uiState.value.copy(
                        error = "Jumlah harus lebih dari 0"
                    )
                    return@launch
                }

                // Get current cart item to check stock
                val currentCart = _uiState.value.cartItems.find { it.product.id == productId }
                if (currentCart != null) {
                    // Check stock availability
                    repository.getProductById(productId).collect { product ->
                        if (product != null && product.stock < quantity) {
                            _uiState.value = _uiState.value.copy(
                                error = "Stok tidak mencukupup. Tersedia ${product.stock} unit."
                            )
                        } else {
                            repository.updateCartItemQuantity(productId, quantity)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Gagal mengupdate jumlah"
                )
            }
        }
    }

    /**
     * Increase quantity of cart item with stock validation.
     */
    fun increaseQuantity(productId: String) {
        viewModelScope.launch {
            try {
                val currentCart = _uiState.value.cartItems.find { it.product.id == productId }
                if (currentCart != null) {
                    val newQuantity = currentCart.quantity + 1

                    // Check stock availability
                    repository.getProductById(productId).collect { product ->
                        if (product != null && product.stock < newQuantity) {
                            _uiState.value = _uiState.value.copy(
                                error = "Stok tidak mencukupup. Maksimal ${product.stock} unit."
                            )
                        } else {
                            repository.updateCartItemQuantity(productId, newQuantity)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Gagal menambah jumlah"
                )
            }
        }
    }

    /**
     * Remove item from cart.
     */
    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            try {
                repository.removeFromCart(productId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Gagal menghapus item"
                )
            }
        }
    }

    /**
     * Clear entire cart.
     */
    fun clearCart() {
        viewModelScope.launch {
            try {
                repository.clearCart()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Gagal mengosongkan keranjang"
                )
            }
        }
    }

    /**
     * Get total count of items in cart.
     */
    fun getItemCount(): Int {
        return _uiState.value.cartItems.sumOf { it.quantity }
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Refresh stock information for all cart items.
     */
    fun refreshStockInfo() {
        viewModelScope.launch {
            val cartItems = _uiState.value.cartItems.toMutableList()
            var allInStock = true
            val updatedCartItems = mutableListOf<com.apk.agrostore.domain.model.CartItem>()

            for (cartItem in cartItems) {
                try {
                    repository.getProductById(cartItem.product.id).collect { product ->
                        if (product != null) {
                            // Create new CartItem with updated product info
                            val updatedCartItem = cartItem.copy(
                                product = product
                            )
                            updatedCartItems.add(updatedCartItem)

                            // Check if current quantity exceeds stock
                            if (product.stock < cartItem.quantity) {
                                // Adjust quantity to available stock
                                val adjustedQuantity = maxOf(1, product.stock)
                                val adjustedCartItem = updatedCartItem.copy(
                                    quantity = adjustedQuantity
                                )
                                updatedCartItems[updatedCartItems.size - 1] = adjustedCartItem

                                if (allInStock) {
                                    _uiState.value = _uiState.value.copy(
                                        error = "Jumlah ${cartItem.product.name} disesuaikan menjadi $adjustedQuantity karena stok tersisa ${product.stock}"
                                    )
                                    allInStock = false
                                }
                            }
                        } else {
                            // Product no longer exists
                            updatedCartItems.add(cartItem)
                            if (allInStock) {
                                _uiState.value = _uiState.value.copy(
                                    error = "${cartItem.product.name} tidak tersedia lagi dan dihapus dari keranjang"
                                )
                                allInStock = false
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Keep original item if error
                    updatedCartItems.add(cartItem)
                }
            }

            // Update cart with adjusted items and recalculate total
            val total = updatedCartItems.sumOf { it.getTotalPrice() }
            _uiState.value = _uiState.value.copy(
                cartItems = updatedCartItems,
                totalPrice = total
            )
        }
    }
}

/**
 * UI state for the shopping cart.
 */
data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)