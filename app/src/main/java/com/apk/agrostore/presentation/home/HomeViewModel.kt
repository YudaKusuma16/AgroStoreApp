package com.apk.agrostore.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apk.agrostore.domain.model.Product
import com.apk.agrostore.domain.repository.AgroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the home screen that manages product listing.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AgroRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    /**
     * Load all products from repository.
     */
    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getProducts().collect { products ->
                _uiState.value = _uiState.value.copy(
                    products = products,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    /**
     * Search products by query.
     */
    fun searchProducts(query: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)

        repository.searchProducts(query).collect { products ->
            _uiState.value = _uiState.value.copy(
                products = products,
                isLoading = false,
                searchQuery = query
            )
        }
    }

    /**
     * Clear search and show all products.
     */
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "")
        loadProducts()
    }
}

/**
 * UI state for the home screen.
 */
data class HomeUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null
)