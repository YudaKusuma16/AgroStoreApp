package com.apk.agrostore.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a product in the AgroStore application.
 */
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val description: String,
    val stock: Int,
    @SerializedName("imageUrl")
    val imageUrl: String = "", // Default empty string
    @SerializedName("sellerId")
    val sellerId: String
)