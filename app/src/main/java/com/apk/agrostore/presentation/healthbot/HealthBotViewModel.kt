package com.apk.agrostore.presentation.healthbot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HealthBotViewModel @Inject constructor(
    private val healthBotModel: HealthBotModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthBotUiState())
    val uiState: StateFlow<HealthBotUiState> = _uiState.asStateFlow()

    private var predictionJob: Job? = null
    private val currentSessionId = UUID.randomUUID().toString()

    fun updateInputText(newText: String) {
        _uiState.value = _uiState.value.copy(inputText = newText)
    }

    fun sendMessage() {
        val currentState = _uiState.value
        val userMessage = currentState.inputText.trim()

        if (userMessage.isEmpty()) return

        // Add user message
        val userChatMessage = ChatMessage(
            isUser = true,
            message = userMessage,
            sessionId = currentSessionId
        )

        _uiState.value = currentState.copy(
            messages = currentState.messages + userChatMessage,
            inputText = "",
            isLoading = true,
            error = null
        )

        // Simulate bot thinking and processing
        predictionJob?.cancel()
        predictionJob = viewModelScope.launch {
            try {
                // Add small delay for better UX
                delay(500)

                // Temporarily use simulated prediction to test logic
                val prediction = simulatePrediction(userMessage)
                println("DEBUG: Using simulated prediction only")

                // Create bot response
                val botMessage = createBotResponse(userMessage, prediction)

                val botChatMessage = ChatMessage(
                    isUser = false,
                    message = botMessage,
                    predictions = prediction.predictions.joinToString("\n") {
                        "${it.label}: ${kotlin.math.round(it.probability * 100)}%"
                    },
                    sessionId = currentSessionId
                )

                _uiState.value = currentState.copy(
                    messages = currentState.messages + userChatMessage + botChatMessage,
                    isLoading = false,
                    inputText = "",
                    currentPrediction = prediction,
                    error = null
                )

            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    messages = currentState.messages + userChatMessage,
                    isLoading = false,
                    error = "Maaf, terjadi kesalahan. Silakan coba lagi."
                )
            }
        }
    }

    private fun simulatePrediction(text: String): HealthBotResponse {
        // Simulate ML prediction (replace with real implementation)
        val possiblePredictions = listOf(
            HealthBotPrediction("1-FR", 0.85f, "Febrile Illness"),
            HealthBotPrediction("2-GI", 0.75f, "Gastrointestinal"),
            HealthBotPrediction("3-PI", 0.65f, "Parasitic Infections"),
            HealthBotPrediction("4-DM", 0.95f, "Diabetes Mellitus"),
            HealthBotPrediction("5-EDTRB", 0.70f, "Emergency Disorders"),
            HealthBotPrediction("6-RE", 0.60f, "Reproductive Disorders")
        )

        val selectedPredictions = mutableListOf<HealthBotPrediction>()
        val lowerText = text.lowercase()

        // Debug - log input text
        println("DEBUG: Input text: '$lowerText'")

        // Check for multiple symptoms and add predictions
        if (lowerText.contains("demam") || lowerText.contains("panas") || lowerText.contains("suhu") || lowerText.contains("dingin")) {
            selectedPredictions.add(HealthBotPrediction("1-FR", 0.90f, "Demam dan Penyakit Infeksi"))
            println("DEBUG: Added fever prediction")
        }
        if (lowerText.contains("perut") || lowerText.contains("muntah") || lowerText.contains("mules") ||
            lowerText.contains("diare") || lowerText.contains("muntaber") || lowerText.contains("mulas") ||
            lowerText.contains("sakit perut") || lowerText.contains("mencret")) {
            selectedPredictions.add(HealthBotPrediction("2-GI", 0.85f, "Masalah Pencernaan"))
            println("DEBUG: Added stomach prediction")
        }
        if (lowerText.contains("gula") || lowerText.contains("kencing") || lowerText.contains("diabetes") ||
            lowerText.contains("haus") || lowerText.contains("buang air kecil") || lowerText.contains("kencing manis")) {
            selectedPredictions.add(HealthBotPrediction("4-DM", 0.95f, "Diabetes Mellitus"))
            println("DEBUG: Added diabetes prediction")
        }
        if (lowerText.contains("gatal") || lowerText.contains("kulit") || lowerText.contains("ruam") ||
            lowerText.contains("bintik") || lowerText.contains("jerawat") || lowerText.contains("biduran")) {
            selectedPredictions.add(HealthBotPrediction("5-EDTRB", 0.75f, "Masalah Kulit"))
            println("DEBUG: Added skin prediction")
        }
        if (lowerText.contains("batuk") || lowerText.contains("pilek") || lowerText.contains("sesak") ||
            lowerText.contains("napas") || lowerText.contains("dada") || lowerText.contains("flu") ||
            lowerText.contains("masuk angin")) {
            selectedPredictions.add(HealthBotPrediction("1-FR", 0.70f, "Masalah Pernapasan"))
            println("DEBUG: Added respiratory prediction")
        }
        if (lowerText.contains("kepala") || lowerText.contains("pusing") || lowerText.contains("migrain") ||
            lowerText.contains("sakit kepala")) {
            selectedPredictions.add(HealthBotPrediction("1-FR", 0.60f, "Sakit Kepala"))
            println("DEBUG: Added headache prediction")
        }
        if (lowerText.contains("badan") || lowerText.contains("lelah") || lowerText.contains("capek") ||
            lowerText.contains("lemas") || lowerText.contains("lesu")) {
            selectedPredictions.add(HealthBotPrediction("1-FR", 0.50f, "Kelelahan Umum"))
            println("DEBUG: Added fatigue prediction")
        }

        // Debug - log number of predictions
        println("DEBUG: Total predictions: ${selectedPredictions.size}")

        // If no specific symptoms detected, analyze length of message
        if (selectedPredictions.isEmpty()) {
            if (lowerText.length < 5) {
                // Very short input - likely just testing
                selectedPredictions.add(HealthBotPrediction("1-FR", 0.10f, "Perlu Informasi Lebih Detail"))
                println("DEBUG: Added 'too short' prediction")
            } else {
                // Try to give a general wellness check
                selectedPredictions.add(HealthBotPrediction("1-FR", 0.60f, "Pemeriksaan Umum"))
                println("DEBUG: Added general check prediction")
            }
        }

        val emergencyLevel = when {
            selectedPredictions.any { it.label == "4-DM" } -> EmergencyLevel.HIGH
            selectedPredictions.any { it.label == "1-FR" || it.label == "5-EDTRB" } -> EmergencyLevel.MEDIUM
            else -> EmergencyLevel.LOW
        }

        val recommendations = generateRecommendations(selectedPredictions)

        return HealthBotResponse(
            predictions = selectedPredictions,
            recommendations = recommendations,
            emergencyLevel = emergencyLevel
        )
    }

    private fun createBotResponse(userMessage: String, prediction: HealthBotResponse): String {
        // Debug log
        println("DEBUG: createBotResponse called with ${prediction.predictions.size} predictions")

        // First message handling - check if it's a very short greeting
        if (userMessage.trim().length < 5 && prediction.predictions.isEmpty()) {
            return "Halo! Saya HealthBot, asisten kesehatan Anda. Saya siap membantu mengidentifikasi gejala kesehatan Anda.\n\n" +
                    "Silakan ceritakan gejala yang Anda alami dengan detail, misalnya:\n" +
                    "• \"Saya demam sejak kemarin, suhu badan 38.5°C\"\n" +
                    "• \"Perut saya mules dan diare\"\n" +
                    "• \"Saya sering haus dan sering buang air kecil\""
        }

        var response = "Berdasarkan gejala yang Anda sebutkan:\n\n"

        prediction.predictions.forEach { pred ->
            val confidence = kotlin.math.round(pred.probability * 100)
            response += "• ${pred.description} ($confidence% kemungkinan)\n"
        }

        if (prediction.recommendations.isNotEmpty()) {
            response += "\nRekomendasi:\n"
            prediction.recommendations.forEach { rec ->
                response += "• $rec\n"
            }
        }

              // Add general advice if prediction confidence is low
        if (prediction.predictions.all { it.probability < 0.5f }) {
            response += "\n\nMohon maaf, saya tidak dapat mengidentifikasi gejala dengan pasti. " +
                    "Untuk hasil yang lebih akurat, silakan:\n" +
                    "• Jelaskan gejala lebih detail (misal: durasi, intensitas)\n" +
                    "• Sertakan lokasi gejala di tubuh\n" +
                    "• Sebutkan gejala lain yang menyertai\n\n" +
                    "Jika gejala berlanjut atau memburuk, segera konsultasi ke dokter."
        }

        response += "\nDisclaimer: Ini bukan diagnosis medis. Untuk diagnosis akurat, silakan konsultasi dengan dokter."

        // Debug - log the final response
        println("DEBUG: Final response: $response")

        return response
    }

    private fun generateRecommendations(predictions: List<HealthBotPrediction>): List<String> {
        val recommendations = mutableListOf<String>()

        if (predictions.isEmpty()) {
            return listOf("Silakan jelaskan gejala Anda lebih detail.")
        }

        predictions.forEach { pred ->
            when (pred.label) {
                "4-DM" -> {
                    recommendations.add("⚠️ SEGERA ke dokter! Gejala diabetes perlu penanganan medis.")
                    recommendations.add("Monitor gula darah secara rutin.")
                    recommendations.add("Perhatikan pola makan dan obat diabetes.")
                }
                "1-FR" -> {
                    recommendations.add("Minum paracetamol jika demam >38°C.")
                    recommendations.add("Kompres dengan air hangat.")
                    recommendations.add("Banyak minum air putih.")
                    recommendations.add("Jika demam >3 hari, ke dokter.")
                }
                "2-GI" -> {
                    recommendations.add("Hindari makanan pedas dan berminyak.")
                    recommendations.add("Minum air putih yang banyak.")
                    recommendations.add("Jika diare, minum ORS.")
                    recommendations.add("Istirahat perut (makan bubur atau nasi hangat).")
                }
            }
        }

        return recommendations.distinct()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearChat() {
        _uiState.value = HealthBotUiState()
    }

    override fun onCleared() {
        super.onCleared()
        predictionJob?.cancel()
    }
}