package com.apk.agrostore.presentation.seller

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apk.agrostore.domain.model.Product
import com.apk.agrostore.domain.repository.AgroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for editing an existing product.
 */
@HiltViewModel
class EditProductViewModel @Inject constructor(
    private val repository: AgroRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get productId from navigation arguments
    private val productId: String = savedStateHandle.get<String>("productId") ?: ""

    // UI State
    private val _uiState = MutableStateFlow(EditProductUiState())
    val uiState: StateFlow<EditProductUiState> = _uiState.asStateFlow()

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

    private val _originalProduct = MutableStateFlow<Product?>(null)
    val originalProduct: StateFlow<Product?> = _originalProduct.asStateFlow()

    // Track selected image URI for upload
    private val _selectedImagePath = MutableStateFlow<String?>(null)
    val selectedImagePath: StateFlow<String?> = _selectedImagePath.asStateFlow()

    // Track upload state
    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

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
                error = "Product ID tidak valid"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                repository.getProductById(productId).collect { product ->
                    if (product != null) {
                        _originalProduct.value = product
                        _name.value = product.name
                        _category.value = product.category
                        _price.value = String.format("%.0f", product.price)
                        _stock.value = product.stock.toString()
                        _description.value = product.description
                        _imageUrl.value = product.imageUrl
                        _uiState.value = _uiState.value.copy(isLoading = false, error = null)
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
                    error = e.message ?: "Gagal memuat produk"
                )
            }
        }
    }

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
     * Select image and upload to server.
     */
    fun selectAndUploadImage(imagePath: String) {
        _selectedImagePath.value = imagePath

        viewModelScope.launch {
            _isUploadingImage.value = true

            try {
                repository.uploadImage(imagePath).collect { result ->
                    if (result.isSuccess) {
                        _imageUrl.value = result.getOrNull() ?: ""
                        Log.d("EditProductViewModel", "Image uploaded: ${_imageUrl.value}")
                    } else {
                        Log.e("EditProductViewModel", "Failed to upload image", result.exceptionOrNull())
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to upload image: ${result.exceptionOrNull()?.message}"
                        )
                    }
                    _isUploadingImage.value = false
                }
            } catch (e: Exception) {
                Log.e("EditProductViewModel", "Error uploading image", e)
                _uiState.value = _uiState.value.copy(
                    error = "Error uploading image: ${e.message}"
                )
                _isUploadingImage.value = false
            }
        }
    }

    /**
     * Clear selected image.
     */
    fun clearSelectedImage() {
        _selectedImagePath.value = null
        _imageUrl.value = ""
    }

    /**
     * Validate and update product.
     */
    fun updateProduct() {
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

            try {
                // Create updated product with same ID and sellerId
                val updatedProduct = Product(
                    id = productId,
                    name = name.value,
                    category = category.value,
                    price = price.value.toDoubleOrNull() ?: 0.0,
                    stock = stock.value.toIntOrNull() ?: 0,
                    description = description.value,
                    imageUrl = imageUrl.value,
                    sellerId = _originalProduct.value?.sellerId ?: ""
                )

                Log.d("EditProductViewModel", "Updating product: $productId")
                Log.d("EditProductViewModel", "Product data: ${updatedProduct}")

                // Use proper updateProduct method
                repository.updateProduct(updatedProduct).collect { result ->
                    if (result.isSuccess) {
                        Log.d("EditProductViewModel", "Product updated successfully")
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isUpdated = true,
                            error = null
                        )
                    } else {
                        Log.e("EditProductViewModel", "Failed to update product", result.exceptionOrNull())
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = result.exceptionOrNull()?.message ?: "Gagal memperbarui produk"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("EditProductViewModel", "Error updating product", e)
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
        _uiState.value = _uiState.value.copy(isUpdated = false)
    }

    /**
     * Clear error.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for editing a product.
 */
data class EditProductUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUpdated: Boolean = false,
    val error: String? = null
)