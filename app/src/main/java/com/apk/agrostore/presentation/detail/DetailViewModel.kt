package com.apk.agrostore.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apk.agrostore.domain.model.Product
import com.apk.agrostore.domain.repository.AgroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the product detail screen.
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: AgroRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get productId from navigation arguments
    private val productId: String = savedStateHandle.get<String>("productId") ?: ""

    // UI State
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    /**
     * Load product details by ID.
     */
    private fun loadProduct() {
        if (productId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Product ID not found"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                repository.getProductById(productId).collect { product ->
                    if (product != null) {
                        _uiState.value = _uiState.value.copy(
                            product = product,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Produk tidak ditemukan"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    /**
     * Add product to cart.
     */
    fun addToCart(quantity: Int = 1) {
        val currentProduct = _uiState.value.product
        if (currentProduct != null) {
            viewModelScope.launch {
                try {
                    repository.addToCart(currentProduct, quantity)
                    _uiState.value = _uiState.value.copy(
                        isAddedToCart = true,
                        cartMessage = "${currentProduct.name} ditambahkan ke keranjang"
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Gagal menambahkan ke keranjang"
                    )
                }
            }
        }
    }

    /**
     * Reset cart success state.
     */
    fun resetCartState() {
        _uiState.value = _uiState.value.copy(
            isAddedToCart = false,
            cartMessage = null
        )
    }

    /**
     * Clear error.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for the detail screen.
 */
data class DetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isAddedToCart: Boolean = false,
    val cartMessage: String? = null
)