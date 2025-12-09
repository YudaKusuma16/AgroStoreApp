package com.apk.agrostore.domain.model

/**
 * Represents a user in the AgroStore application.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: String // "pembeli" or "penjual"
)