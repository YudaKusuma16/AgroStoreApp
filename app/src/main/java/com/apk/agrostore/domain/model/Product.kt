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
    @SerializedName("image_url")
    val imageUrl: String = "", // Default empty string, matching API field name
    @SerializedName("seller_id")
    val sellerId: String
)