package com.apk.agrostore.domain.repository

import com.apk.agrostore.domain.model.CartItem
import com.apk.agrostore.domain.model.Order
import com.apk.agrostore.domain.model.Product
import com.apk.agrostore.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for AgroStore data operations.
 */
interface AgroRepository {
    /**
     * Get all products as a flow.
     */
    fun getProducts(): Flow<List<Product>>

    /**
     * Get product by ID.
     */
    suspend fun getProductById(id: String): Flow<Product?>

    /**
     * Search products by query.
     */
    suspend fun searchProducts(query: String): Flow<List<Product>>

    /**
     * Add product to cart.
     */
    suspend fun addToCart(product: Product, quantity: Int)

    /**
     * Get all cart items.
     */
    fun getCartItems(): Flow<List<CartItem>>

    /**
     * Remove item from cart.
     */
    suspend fun removeFromCart(productId: String)

    /**
     * Update cart item quantity.
     */
    suspend fun updateCartItemQuantity(productId: String, quantity: Int)

    /**
     * Clear all cart items.
     */
    suspend fun clearCart()

    /**
     * Checkout process (simulation).
     * Returns success if payment is processed successfully.
     */
    suspend fun checkout(
        address: String,
        paymentMethod: String
    ): Result<Boolean>

    /**
     * Authenticate user with email and password.
     */
    suspend fun login(email: String, password: String): Flow<Result<User>>

    /**
     * Register a new user.
     */
    suspend fun register(name: String, email: String, password: String): Flow<Result<Boolean>>

    /**
     * Add a new product.
     */
    suspend fun addProduct(product: Product): Flow<Result<Boolean>>

    /**
     * Update an existing product.
     */
    suspend fun updateProduct(product: Product): Flow<Result<Boolean>>

    /**
     * Delete a product by ID.
     */
    suspend fun deleteProduct(productId: String): Flow<Result<Boolean>>

    /**
     * Get current logged-in user.
     */
    fun getCurrentUser(): Flow<User?>

    /**
     * Logout user and clear session.
     */
    suspend fun logout()

    /**
     * Get all orders for the current user.
     */
    fun getOrders(): Flow<List<Order>>

    /**
     * Get all orders for the seller (products sold by current seller).
     */
    fun getSellerOrders(): Flow<List<Order>>

    /**
     * Update user profile information.
     */
    suspend fun updateUser(user: User): Flow<Result<Boolean>>

    /**
     * Upload image file to server and return URL.
     */
    suspend fun uploadImage(imagePath: String): Flow<Result<String>>
}