package com.apk.agrostore.presentation.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apk.agrostore.domain.model.Product
import com.apk.agrostore.domain.repository.AgroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

/**
 * ViewModel for the seller dashboard screen.
 */
@HiltViewModel
class SellerViewModel @Inject constructor(
    private val repository: AgroRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(SellerUiState())
    val uiState: StateFlow<SellerUiState> = _uiState.asStateFlow()

    private val _selectedProductId = MutableStateFlow<String?>(null)
    val selectedProductId: StateFlow<String?> = _selectedProductId.asStateFlow()

    init {
        // Get current user and load products
        viewModelScope.launch {
            repository.getCurrentUser().collect { user ->
                Log.d("SellerViewModel", "Current user: ${user?.id}, role: ${user?.role}")
                if (user != null && user.role == "penjual") {
                    // Use logged-in seller ID
                    Log.d("SellerViewModel", "Loading products for seller: ${user.id}")
                    loadSellerProducts(user.id)
                } else {
                    // No seller logged in, show error state
                    Log.d("SellerViewModel", "No seller logged in")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Anda harus login sebagai penjual untuk mengakses halaman ini"
                    )
                }
            }
        }
    }

    /**
     * Load products for the current seller.
     */
    private fun loadSellerProducts(sellerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            Log.d("SellerViewModel", "Loading products for seller ID: $sellerId")

            // Get all products and filter by seller ID
            repository.getProducts().collect { products ->
                Log.d("SellerViewModel", "Total products received: ${products.size}")
                val sellerProducts = products.filter { it.sellerId == sellerId }
                Log.d("SellerViewModel", "Filtered products for seller: ${sellerProducts.size}")

                // Log product IDs for debugging
                sellerProducts.forEach { product ->
                    Log.d("SellerViewModel", "Product: ${product.id} - ${product.name}")
                }

                _uiState.value = _uiState.value.copy(
                    products = sellerProducts,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    /**
     * Show delete confirmation dialog.
     */
    fun showDeleteConfirmation(productId: String) {
        _selectedProductId.value = productId
        _uiState.value = _uiState.value.copy(showDeleteDialog = true)
    }

    /**
     * Hide delete confirmation dialog.
     */
    fun hideDeleteConfirmation() {
        _selectedProductId.value = null
        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
    }

    /**
     * Confirm and delete a product.
     */
    fun confirmDeleteProduct() {
        val productId = _selectedProductId.value
        if (productId == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)

            try {
                repository.deleteProduct(productId).collect { result ->
                    if (result.isSuccess) {
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            deleteSuccess = true,
                            errorMessage = null,
                            showDeleteDialog = false
                        )
                        _selectedProductId.value = null
                        // deleteSuccess already set to true above, which will trigger refresh via LaunchedEffect
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Gagal menghapus produk"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    /**
     * Reset delete success state.
     */
    fun resetDeleteState() {
        _uiState.value = _uiState.value.copy(deleteSuccess = false)
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Refresh products.
     */
    fun refresh() {
        Log.d("SellerViewModel", "Refreshing products...")
        viewModelScope.launch {
            repository.getCurrentUser().collect { user ->
                Log.d("SellerViewModel", "Refresh - Current user: ${user?.id}, role: ${user?.role}")
                if (user != null && user.role == "penjual") {
                    loadSellerProducts(user.id)
                } else {
                    Log.d("SellerViewModel", "No seller logged in during refresh")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Anda harus login sebagai penjual"
                    )
                }
            }
        }
    }
}

/**
 * UI state for the seller dashboard.
 */
data class SellerUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val errorMessage: String? = null,
    val showDeleteDialog: Boolean = false
)