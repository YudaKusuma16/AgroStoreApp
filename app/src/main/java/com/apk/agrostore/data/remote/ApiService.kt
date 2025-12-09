package com.apk.agrostore.data.remote

import com.apk.agrostore.domain.model.CartItem
import com.apk.agrostore.domain.model.Order
import com.apk.agrostore.domain.model.Product
import com.apk.agrostore.domain.model.User
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface untuk API endpoints AgroStore
 */
interface ApiService {

    // Auth endpoints
    @POST("auth/login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/register.php")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    // Product endpoints
    @GET("products/index.php")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("seller_id") sellerId: String? = null
    ): Response<List<ProductResponse>>

    @GET("products/show.php")
    suspend fun getProductById(
        @Query("id") id: String
    ): Response<ProductDetailResponse>

    @POST("products/index.php")
    suspend fun addProduct(
        @Body product: Product
    ): Response<ApiResponse>

    @PUT("products/show.php")
    suspend fun updateProduct(
        @Query("id") id: String,
        @Body product: Product
    ): Response<ApiResponse>

    @DELETE("products/show.php")
    suspend fun deleteProduct(
        @Query("id") id: String,
        @Query("seller_id") sellerId: String
    ): Response<ApiResponse>

    // Order endpoints
    @GET("orders/index.php")
    suspend fun getOrders(
        @Query("user_id") userId: String
    ): Response<List<OrderResponse>>

    @POST("orders/index.php")
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): Response<CreateOrderResponse>
}

// Request/Response data classes
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val user: User
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String
)

data class RegisterResponse(
    val message: String,
    val user_id: String
)

data class ProductDetailResponse(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val description: String,
    val stock: Int,
    val image_url: String? = null, // Make it nullable
    val seller_id: String,
    val seller: User,
    val created_at: String
)

data class ProductResponse(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val description: String,
    val stock: Int,
    val image_url: String? = null, // Make it nullable
    val seller_id: String,
    val created_at: String
)

data class ApiResponse(
    val message: String,
    val product_id: String? = null
)

data class CreateOrderRequest(
    val user_id: String,
    val items: List<OrderItemRequest>,
    val total: Double,
    val shipping_address: String,
    val payment_method: String
)

data class OrderItemRequest(
    val product_id: String,
    val quantity: Int,
    val price: Double
)

data class CreateOrderResponse(
    val message: String,
    val order_id: String
)

data class OrderResponse(
    val id: String,
    val total: Double,
    val status: String,
    val shipping_address: String?,
    val payment_method: String?,
    val created_at: String,
    val items: List<OrderItemResponse>
)

data class OrderItemResponse(
    val product_id: String,
    val product_name: String,
    val product_image: String?,
    val quantity: Int,
    val price: Double
)

data class ErrorResponse(
    val error: String
)