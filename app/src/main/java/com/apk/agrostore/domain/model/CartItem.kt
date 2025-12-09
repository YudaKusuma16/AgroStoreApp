package com.apk.agrostore.domain.model

/**
 * Represents a cart item with product and quantity.
 */
data class CartItem(
    val product: Product,
    val quantity: Int
) {
    /**
     * Calculate total price for this cart item.
     */
    fun getTotalPrice(): Double = product.price * quantity
}