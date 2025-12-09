package com.apk.agrostore.presentation.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apk.agrostore.domain.model.Product
import com.apk.agrostore.domain.repository.AgroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

/**
 * ViewModel for adding a new product.
 */
@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val repository: AgroRepository
) : ViewModel() {

    // Track if seller is logged in
    private val _isSellerLoggedIn = MutableStateFlow(false)
    val isSellerLoggedIn: StateFlow<Boolean> = _isSellerLoggedIn.asStateFlow()

    // Seller ID will be fetched when saving product
    private var currentSellerId: String = ""

    init {
        // Check if seller is logged in
        viewModelScope.launch {
            repository.getCurrentUser().collect { user ->
                if (user != null && user.role == "penjual") {
                    currentSellerId = user.id
                    _isSellerLoggedIn.value = true
                    Log.d("AddProductViewModel", "Seller logged in: $currentSellerId")
                } else {
                    currentSellerId = ""
                    _isSellerLoggedIn.value = false
                    Log.d("AddProductViewModel", "No seller logged in")
                }
            }
        }
    }

    // UI State
    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()

    // Form fields
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price.asStateFlow()

    private val _stock = MutableStateFlow("")
    val stock: StateFlow<String> = _stock.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _imageUrl = MutableStateFlow("")
    val imageUrl: StateFlow<String> = _imageUrl.asStateFlow()

    /**
     * Update product name.
     */
    fun onNameChange(newName: String) {
        _name.value = newName
    }

    /**
     * Update product category.
     */
    fun onCategoryChange(newCategory: String) {
        _category.value = newCategory
    }

    /**
     * Update product price.
     */
    fun onPriceChange(newPrice: String) {
        // Only allow digits and decimal point
        if (newPrice.isEmpty() || newPrice.matches(Regex("^\\d*\\.?\\d*$"))) {
            _price.value = newPrice
        }
    }

    /**
     * Update product stock.
     */
    fun onStockChange(newStock: String) {
        // Only allow digits
        if (newStock.isEmpty() || newStock.matches(Regex("^\\d*$"))) {
            _stock.value = newStock
        }
    }

    /**
     * Update product description.
     */
    fun onDescriptionChange(newDescription: String) {
        _description.value = newDescription
    }

    /**
     * Update image URL.
     */
    fun onImageUrlChange(newImageUrl: String) {
        _imageUrl.value = newImageUrl
    }

    /**
     * Validate and save product.
     */
    fun saveProduct() {
        // Check if seller is logged in
        if (currentSellerId.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Anda harus login sebagai penjual untuk menambah produk"
            )
            return
        }

        // Validate inputs
        if (name.value.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Nama produk tidak boleh kosong"
            )
            return
        }

        if (category.value.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Kategori tidak boleh kosong"
            )
            return
        }

        if (price.value.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Harga tidak boleh kosong"
            )
            return
        }

        if (stock.value.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Stok tidak boleh kosong"
            )
            return
        }

        if (description.value.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Deskripsi tidak boleh kosong"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            Log.d("AddProductViewModel", "Saving product for seller: $currentSellerId")

            try {
                val product = Product(
                    id = "", // ID will be generated by server
                    name = name.value,
                    category = category.value,
                    price = price.value.toDoubleOrNull() ?: 0.0,
                    stock = stock.value.toIntOrNull() ?: 0,
                    description = description.value,
                    imageUrl = imageUrl.value,
                    sellerId = currentSellerId
                )

                repository.addProduct(product).collect { result ->
                    if (result.isSuccess) {
                        Log.d("AddProductViewModel", "Product saved successfully")
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isSaved = true,
                            error = null
                        )
                    } else {
                        Log.e("AddProductViewModel", "Failed to save product", result.exceptionOrNull())
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = result.exceptionOrNull()?.message ?: "Gagal menyimpan produk"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AddProductViewModel", "Error saving product", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    /**
     * Reset save state.
     */
    fun resetSaveState() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }

    /**
     * Clear error.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for adding a product.
 */
data class AddProductUiState(
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)