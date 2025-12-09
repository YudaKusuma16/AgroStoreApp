package com.apk.agrostore.presentation.healthbot

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.common.ops.QuantizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.support.label.TensorLabel
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class HealthBotModel(context: Context) {
    private var interpreter: Interpreter? = null
    private var textPreprocessor: TextPreprocessor = TextPreprocessor(context)
    private val labelList = listOf("1-FR", "2-GI", "3-PI", "4-DM", "5-EDTRB", "6-RE")

    // Label descriptions
    private val labelDescriptions = mapOf(
        "1-FR" to "Febrile Illness (Demam dan Penyakit Infeksi)",
        "2-GI" to "Gastrointestinal (Masalah Pencernaan)",
        "3-PI" to "Parasitic Infections (Infeksi Parasit)",
        "4-DM" to "Diabetes Mellitus (Kencing Manis)",
        "5-EDTRB" to "Endocrine, Dermatological, Trauma, and Burn Disorders",
        "6-RE" to "Reproductive and Endocrine Disorders"
    )

    // Emergency levels untuk setiap kategori
    private val emergencyLevels = mapOf(
        "4-DM" to EmergencyLevel.HIGH,  // Diabetes - high priority
        "1-FR" to EmergencyLevel.MEDIUM,  // Fever - medium priority
        "5-EDTRB" to EmergencyLevel.MEDIUM,  // Emergency conditions
        "2-GI" to EmergencyLevel.LOW,  // GI issues
        "3-PI" to EmergencyLevel.LOW,  // Parasitic
        "6-RE" to EmergencyLevel.LOW   // Reproductive
    )

    init {
        loadModel(context)
    }

    private fun loadModel(context: Context) {
        try {
            val modelBuffer = loadModelFile(context, "healthbot_classifier.tflite")
            val options = Interpreter.Options()
            interpreter = Interpreter(modelBuffer, options)
        } catch (e: Exception) {
            throw RuntimeException("Error loading TFLite model", e)
        }
    }

    private fun loadModelFile(context: Context, modelName: String): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelName)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun close() {
        interpreter?.close()
    }

    suspend fun predict(text: String): HealthBotResponse = withContext(Dispatchers.Default) {
        try {
            // Preprocess text
            val features = textPreprocessor.preprocessText(text)
            println("Features size: ${features.size}")  // Debug log

            // Prepare input buffer
            val inputBuffer = ByteBuffer.allocateDirect(features.size * 4)
            inputBuffer.order(ByteOrder.nativeOrder())
            for (feature in features) {
                inputBuffer.putFloat(feature)
            }
            inputBuffer.rewind()

            // Prepare output buffer
            val outputShape = interpreter?.getOutputTensor(0)?.shape()
            val outputBuffer = Array(outputShape?.get(0) ?: 1) { FloatArray(1) }

            // Run inference
            interpreter?.run(inputBuffer, outputBuffer)

            // Process predictions
            val predictions = mutableListOf<HealthBotPrediction>()
            var maxEmergencyLevel = EmergencyLevel.LOW

            for (i in labelList.indices) {
                val probability = if (outputBuffer.isNotEmpty()) {
                    outputBuffer[i][0]
                } else {
                    0.0f
                }

                if (probability > 0.5f) {  // Threshold
                    val label = labelList[i]
                    val description = labelDescriptions[label] ?: ""
                    val emergency = emergencyLevels[label] ?: EmergencyLevel.LOW

                    predictions.add(
                        HealthBotPrediction(
                            label = label,
                            probability = probability,
                            description = description
                        )
                    )

                    // Update max emergency level
                    if (emergency.ordinal > maxEmergencyLevel.ordinal) {
                        maxEmergencyLevel = emergency
                    }
                }
            }

            // Generate recommendations based on predictions
            val recommendations = generateRecommendations(predictions)

            HealthBotResponse(
                predictions = predictions,
                recommendations = recommendations,
                emergencyLevel = maxEmergencyLevel
            )

        } catch (e: Exception) {
            HealthBotResponse(
                predictions = emptyList(),
                recommendations = listOf("Maaf, terjadi kesalahan. Silakan coba lagi."),
                emergencyLevel = EmergencyLevel.LOW
            )
        }
    }

    
    private fun generateRecommendations(predictions: List<HealthBotPrediction>): List<String> {
        val recommendations = mutableListOf<String>()

        if (predictions.isEmpty()) {
            recommendations.add("Saya tidak dapat mengidentifikasi gejala yang Anda sebutkan. " +
                    "Silakan jelaskan lebih detail atau konsultasi dengan dokter.")
            return recommendations
        }

        // Generate based on emergency level
        val emergencyLevel = predictions.map {
            emergencyLevels[it.label] ?: EmergencyLevel.LOW
        }.maxByOrNull { it.ordinal } ?: EmergencyLevel.LOW

        when (emergencyLevel) {
            EmergencyLevel.HIGH -> {
                recommendations.add("⚠️ PERHATIAN: Gejala Anda memerlukan penanganan segera!")
                recommendations.add("Segera konsultasi ke fasilitas kesehatan terdekat.")
                recommendations.add("Jika gejala berat, hubungi nomor darurat 119.")
            }
            EmergencyLevel.MEDIUM -> {
                recommendations.add("Gejala Anda perlu mendapat perhatian medis.")
                recommendations.add("Disarankan untuk memeriksakan diri ke dokter dalam 24-48 jam.")
                recommendations.add("Monitor gejala dan segera ke dokter jika memburuk.")
            }
            EmergencyLevel.LOW -> {
                recommendations.add("Gejala Anda cenderung ringan.")
                recommendations.add("Istirahat yang cukup dan minum air yang banyak.")
                recommendations.add("Jika gejala berlanjut >3 hari, konsultasi ke dokter.")
            }
        }

        // Specific recommendations based on conditions
        predictions.forEach { pred ->
            when (pred.label) {
                "4-DM" -> {
                    recommendations.add("Kontrol gula darah secara rutin.")
                    recommendations.add("Perhatikan pola makan dan obat diabetes.")
                }
                "1-FR" -> {
                    recommendations.add("Minum paracetamol jika demam >38°C.")
                    recommendations.add("Kompres dengan air hangat.")
                }
                "2-GI" -> {
                    recommendations.add("Hindari makanan pedas dan berminyak.")
                    recommendations.add("Minum ORS jika diare.")
                }
            }
        }

        return recommendations
    }
}