package com.apk.agrostore.data.repository

import com.apk.agrostore.data.remote.ApiService
import com.apk.agrostore.data.remote.RetrofitClient
import com.apk.agrostore.data.remote.ErrorResponse
import com.apk.agrostore.data.remote.LoginRequest
import com.apk.agrostore.data.remote.RegisterRequest
import com.apk.agrostore.data.remote.CreateOrderRequest
import com.apk.agrostore.data.remote.OrderItemRequest
import com.apk.agrostore.data.remote.ProductDetailResponse
import com.apk.agrostore.data.remote.ProductResponse
import com.apk.agrostore.data.remote.OrderResponse
import com.apk.agrostore.data.remote.OrderItemResponse
import com.apk.agrostore.domain.model.CartItem
import com.apk.agrostore.domain.model.Order
import com.apk.agrostore.domain.model.Product
import com.apk.agrostore.domain.model.User
import com.apk.agrostore.domain.repository.AgroRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real implementation of AgroRepository using API calls
 */
@Singleton
class AgroRepositoryImpl @Inject constructor() : AgroRepository {

    private val apiService = RetrofitClient.apiService
    private val gson = Gson()

    // In-memory storage for cart and current user
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    private var _currentUser: MutableStateFlow<User?> = MutableStateFlow(null)

    override fun getProducts(): Flow<List<Product>> = flow {
        try {
            val response = apiService.getProducts()
            if (response.isSuccessful) {
                response.body()?.let { products ->
                    val productList = products.map {
                        Product(
                            id = it.id,
                            name = it.name,
                            category = it.category,
                            price = it.price,
                            description = it.description,
                            stock = it.stock,
                            imageUrl = it.image_url ?: "", // Handle null value
                            sellerId = it.seller_id
                        )
                    }
                    Log.d("AgroRepository", "Fetched ${productList.size} products from API")
                    productList.forEach { product ->
                        Log.d("AgroRepository", "Product: ${product.id} - ${product.name} (seller: ${product.sellerId})")
                    }
                    emit(productList)
                }
            } else {
                Log.e("AgroRepository", "Failed to fetch products. HTTP Code: ${response.code()}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("AgroRepository", "Error fetching products", e)
            // Return empty list on error
            emit(emptyList())
        }
    }

    override suspend fun getProductById(id: String): Flow<Product?> = flow {
        try {
            val response = apiService.getProductById(id)
            if (response.isSuccessful) {
                response.body()?.let { productResponse ->
                    val product = Product(
                        id = productResponse.id,
                        name = productResponse.name,
                        category = productResponse.category,
                        price = productResponse.price,
                        description = productResponse.description,
                        stock = productResponse.stock,
                        imageUrl = productResponse.image_url ?: "", // Handle null value
                        sellerId = productResponse.seller_id
                    )
                    emit(product)
                }
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    override suspend fun searchProducts(query: String): Flow<List<Product>> = flow {
        try {
            val response = apiService.getProducts(search = query)
            if (response.isSuccessful) {
                response.body()?.let { products ->
                    emit(products.map {
                        Product(
                            id = it.id,
                            name = it.name,
                            category = it.category,
                            price = it.price,
                            description = it.description,
                            stock = it.stock,
                            imageUrl = it.image_url ?: "", // Handle null value
                            sellerId = it.seller_id
                        )
                    })
                }
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun addToCart(product: Product, quantity: Int) {
        val currentCart = _cartItems.value.toMutableList()
        val existingItem = currentCart.find { it.product.id == product.id }

        if (existingItem != null) {
            // Update quantity if item already in cart
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + quantity)
            currentCart[currentCart.indexOf(existingItem)] = updatedItem
        } else {
            // Add new item to cart
            currentCart.add(CartItem(product, quantity))
        }

        _cartItems.value = currentCart
    }

    override fun getCartItems(): Flow<List<CartItem>> = _cartItems

    override suspend fun removeFromCart(productId: String) {
        val currentCart = _cartItems.value.toMutableList()
        currentCart.removeAll { it.product.id == productId }
        _cartItems.value = currentCart
    }

    override suspend fun updateCartItemQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }

        val currentCart = _cartItems.value.toMutableList()
        val itemIndex = currentCart.indexOfFirst { it.product.id == productId }

        if (itemIndex != -1) {
            currentCart[itemIndex] = currentCart[itemIndex].copy(quantity = quantity)
            _cartItems.value = currentCart
        }
    }

    override suspend fun clearCart() {
        _cartItems.value = emptyList()
    }

    override suspend fun checkout(
        address: String,
        paymentMethod: String
    ): Result<Boolean> {
        return try {
            val currentCart = _cartItems.value
            if (currentCart.isEmpty()) {
                return Result.failure(Exception("Cart is empty"))
            }

            val currentUser = _currentUser.value
                ?: return Result.failure(Exception("No user logged in"))

            val totalAmount = currentCart.sumOf { it.getTotalPrice() }
            val orderItems = currentCart.map { item ->
                OrderItemRequest(
                    product_id = item.product.id,
                    quantity = item.quantity,
                    price = item.product.price
                )
            }

            val request = CreateOrderRequest(
                user_id = currentUser.id,
                items = orderItems,
                total = totalAmount,
                shipping_address = address,
                payment_method = paymentMethod
            )

            val response = apiService.createOrder(request)

            if (response.isSuccessful) {
                // Clear cart after successful checkout
                clearCart()
                Result.success(true)
            } else {
                val errorBody = response.errorBody()?.string()
                try {
                    // Try to parse error with details
                    val errorJson = org.json.JSONObject(errorBody ?: "{}")
                    val errorMessage = errorJson.optString("error", "Checkout failed")
                    val detailsArray = errorJson.optJSONArray("details")

                    if (detailsArray != null && detailsArray.length() > 0) {
                        val details = mutableListOf<String>()
                        for (i in 0 until detailsArray.length()) {
                            details.add(detailsArray.optString(i, ""))
                        }
                        Result.failure(Exception("$errorMessage:\n${details.joinToString("\n")}"))
                    } else {
                        Result.failure(Exception(errorMessage))
                    }
                } catch (e: Exception) {
                    // Fallback to simple error
                    Result.failure(Exception(errorBody ?: "Checkout failed"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Flow<Result<User>> = flow {
        try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)

            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    _currentUser.value = loginResponse.user
                    emit(Result.success(loginResponse.user))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val error = gson.fromJson(errorBody, ErrorResponse::class.java)
                emit(Result.failure(Exception(error?.error ?: "Login failed")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun register(name: String, email: String, password: String): Flow<Result<Boolean>> = flow {
        try {
            val request = RegisterRequest(name, email, password, "pembeli") // Default role
            val response = apiService.register(request)

            if (response.isSuccessful) {
                emit(Result.success(true))
            } else {
                val errorBody = response.errorBody()?.string()
                val error = gson.fromJson(errorBody, ErrorResponse::class.java)
                emit(Result.failure(Exception(error?.error ?: "Registration failed")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun addProduct(product: Product): Flow<Result<Boolean>> = flow {
        try {
            val response = apiService.addProduct(product)

            if (response.isSuccessful) {
                emit(Result.success(true))
            } else {
                val errorBody = response.errorBody()?.string()
                val error = gson.fromJson(errorBody, ErrorResponse::class.java)
                emit(Result.failure(Exception(error?.error ?: "Failed to add product")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun updateProduct(product: Product): Flow<Result<Boolean>> = flow {
        try {
            val response = apiService.updateProduct(product.id, product)
            Log.d("AgroRepository", "Updating product ${product.id}")

            if (response.isSuccessful) {
                Log.d("AgroRepository", "Product updated successfully")
                emit(Result.success(true))
            } else {
                val errorBody = response.errorBody()?.string()
                val error = gson.fromJson(errorBody, ErrorResponse::class.java)
                Log.e("AgroRepository", "Failed to update product: ${error?.error}")
                emit(Result.failure(Exception(error?.error ?: "Failed to update product")))
            }
        } catch (e: Exception) {
            Log.e("AgroRepository", "Error updating product", e)
            emit(Result.failure(e))
        }
    }

    override suspend fun deleteProduct(productId: String): Flow<Result<Boolean>> = flow {
        try {
            val currentUser = _currentUser.value
                ?: throw Exception("No user logged in")

            val response = apiService.deleteProduct(productId, currentUser.id)

            if (response.isSuccessful) {
                emit(Result.success(true))
            } else {
                val errorBody = response.errorBody()?.string()
                val error = gson.fromJson(errorBody, ErrorResponse::class.java)
                emit(Result.failure(Exception(error?.error ?: "Failed to delete product")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getCurrentUser(): Flow<User?> = _currentUser

    override suspend fun logout() {
        _currentUser.value = null
        clearCart()
    }

    override fun getOrders(): Flow<List<Order>> = flow {
        try {
            val currentUser = _currentUser.value
                ?: throw Exception("No user logged in")

            val response = apiService.getOrders(currentUser.id)

            if (response.isSuccessful) {
                response.body()?.let { orders ->
                    val formattedOrders = orders.map { order ->
                        // Convert API order items to CartItem format
                        val cartItems = order.items.map { item ->
                            CartItem(
                                product = Product(
                                    id = item.product_id,
                                    name = item.product_name,
                                    category = "",
                                    price = item.price,
                                    description = "",
                                    stock = 0,
                                    imageUrl = item.product_image ?: "",
                                    sellerId = ""
                                ),
                                quantity = item.quantity
                            )
                        }

                        Order(
                            id = order.id,
                            items = cartItems,
                            total = order.total,
                            status = order.status,
                            date = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                                .format(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                    .parse(order.created_at) ?: Date()),
                            shippingAddress = order.shipping_address ?: "",
                            paymentMethod = order.payment_method ?: ""
                        )
                    }
                    emit(formattedOrders)
                }
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun updateUser(user: User): Flow<Result<Boolean>> = flow {
        // Note: Update user API endpoint not implemented yet
        // For now, just update local state
        _currentUser.value = user
        emit(Result.success(true))
    }
}