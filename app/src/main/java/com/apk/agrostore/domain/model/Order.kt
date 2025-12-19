package com.apk.agrostore.domain.model

/**
 * Represents an order in the AgroStore application.
 */
data class Order(
    val id: String,
    val items: List<CartItem>,
    val total: Double,
    val status: String, // e.g., "Menunggu Pembayaran", "Dibayar", "Dikemas", "Dikirim", "Selesai"
    val date: String,
    val shippingAddress: String = "",
    val paymentMethod: String = "",
    val buyerName: String = "",
    val buyerId: String = ""
) {
    /**
     * Get formatted total price in Indonesian Rupiah format.
     */
    fun getFormattedTotal(): String = String.format("Rp %,.0f", total)
}