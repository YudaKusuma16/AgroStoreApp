package com.apk.agrostore.presentation.healthbot

import androidx.room.Entity
import androidx.room.PrimaryKey

data class HealthBotPrediction(
    val label: String,
    val probability: Float,
    val description: String = ""
)

data class HealthBotResponse(
    val predictions: List<HealthBotPrediction>,
    val recommendations: List<String> = emptyList(),
    val emergencyLevel: EmergencyLevel = EmergencyLevel.LOW
)

enum class EmergencyLevel {
    HIGH,    // Perlu segera ke dokter
    MEDIUM,  // Perlu perhatian khusus
    LOW      // Tidak emergency
}

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isUser: Boolean,  // true = user, false = bot
    val message: String,
    val predictions: String? = null,  // JSON string untuk predictions
    val sessionId: String
)

data class ChatSession(
    val id: String,
    val messages: List<ChatMessage>,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActive: Long = System.currentTimeMillis()
)

data class HealthBotUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = "",
    val error: String? = null,
    val currentPrediction: HealthBotResponse? = null
)