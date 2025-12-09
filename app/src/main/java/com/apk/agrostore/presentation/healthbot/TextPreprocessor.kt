package com.apk.agrostore.presentation.healthbot

import android.content.Context
import org.json.JSONObject
import kotlin.math.sqrt

class TextPreprocessor(private val context: Context) {
    private var vocabulary: Map<String, Int> = emptyMap()
    private var totalFeatures: Int = 8300
    private var idfScores: FloatArray? = null

    init {
        loadVocabulary()
    }

    private fun loadVocabulary() {
        try {
            // Coba load vocabulary yang lengkap jika ada
            val vocabJson = context.assets.open("android_vocabulary.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(vocabJson)

            val wordToIndex = mutableMapOf<String, Int>()
            jsonObject.getJSONObject("word_to_index").keys().forEach { key ->
                wordToIndex[key] = jsonObject.getJSONObject("word_to_index").getInt(key)
            }

            vocabulary = wordToIndex
            totalFeatures = jsonObject.getInt("total_features")

            // Load IDF scores jika ada
            context.assets.open("idf_scores.json").bufferedReader().use {
                val idfArray = JSONObject(it.readText()).getJSONArray("idf_scores")
                idfScores = FloatArray(idfArray.length())
                for (i in 0 until idfArray.length()) {
                    idfScores!![i] = idfArray.getDouble(i).toFloat()
                }
            }

        } catch (e: Exception) {
            // Fallback ke hardcoded vocabulary jika file tidak ada
            loadFallbackVocabulary()
        }
    }

    private fun loadFallbackVocabulary() {
        vocabulary = mapOf(
            // Common symptoms
            "demam" to 0, "panas" to 1, "sakit" to 2, "kepala" to 3, "migren" to 4,
            "perut" to 5, "muntah" to 6, "mual" to 7, "diare" to 8, "mules" to 9,
            "sembelit" to 10, "bab" to 11, "buangairbesar" to 12,
            // Diabetes related
            "kencing" to 13, "manis" to 14, "diabetes" to 15, "guladarah" to 16,
            "insulin" to 17, "gula" to 18, "kencingmanis" to 19,
            // Skin related
            "gatal" to 20, "kulit" to 21, "ruam" to 22, "bintik" to 23, "bisul" to 24,
            "jerawat" to 25, "panu" to 26, "kurap" to 27,
            // Respiratory
            "batuk" to 28, "pilek" to 29, "sesak" to 30, "napas" to 31, "dada" to 32,
            "asma" to 33, "flu" to 34, "hidung" to 35, "tenggorokan" to 36,
            // General pain
            "nyeri" to 37, "linu" to 38, "pegal" to 39, "kram" to 40, "rematik" to 41,
            // Digestive
            "maag" to 42, "asam" to 43, "lambung" to 44, "mulas" to 45, "gastritis" to 46,
            // Emergency
            "pingsan" to 47, "lemas" to 48, "menggigil" to 49, "berkeringat" to 50,
            "pusing" to 51, "mata" to 52, "telinga" to 53,
            // Reproductive
            "haid" to 54, "menstruasi" to 55, "keputihan" to 56, "hamil" to 57,
            // Parasites
            "cacingan" to 58, "kutu" to 59, "tungau" to 60,
            // Additional common words
            "badan" to 61, "letih" to 62, "lelah" to 63, "capek" to 64,
            "bersin" to 65, "hidung" to 66, "tenggorok" to 67, "radang" to 68,
            "infeksi" to 69, "virus" to 70, "bakteri" to 71, "alergi" to 72,
            "obat" to 73, "minum" to 74, "makan" to 75, "nafsu" to 76,
            "makan" to 77, "minum" to 78, "air" to 79, "putih" to 80,
            "merah" to 81, "bengkak" to 82, "pedih" to 83, "panas" to 84,
            "dingin" to 85, "keringat" to 86, "kering" to 87, "basah" to 88
        )
        totalFeatures = 8300
    }

    fun preprocessText(text: String): FloatArray {
        // Text preprocessing
        val processedText = text.lowercase()
            .replace(Regex("[^a-z\\s]"), "")  // Keep only letters and spaces
            .trim()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }

        // Initialize features array dengan total features dari model
        val features = FloatArray(totalFeatures) { 0.0f }

        // Count word occurrences
        val wordCounts = mutableMapOf<String, Int>()
        processedText.forEach { word ->
            wordCounts[word] = wordCounts.getOrDefault(word, 0) + 1
        }

        // Calculate TF-IDF
        val totalWords = processedText.size
        wordCounts.forEach { (word, count) ->
            val index = vocabulary[word]
            if (index != null && index < totalFeatures) {
                // Term Frequency (TF)
                val tf = count.toFloat() / totalWords

                // Use IDF score if available, otherwise use 1.0
                val idf = if (idfScores != null && index < idfScores!!.size) {
                    idfScores!![index]
                } else {
                    1.0f
                }

                // TF-IDF score
                features[index] = tf * idf
            }
        }

        // Add bigram features
        val bigrams = processedText.zipWithNext { a, b -> "${a}_${b}" }
        bigrams.forEach { bigram ->
            val index = vocabulary[bigram]
            if (index != null && index < totalFeatures) {
                features[index] = 1.0f
            }
        }

        // L2 Normalization
        val norm = sqrt(features.map { it * it }.sum())
        return if (norm > 0) {
            features.map { it / norm }.toFloatArray()
        } else {
            features
        }
    }

    fun getVocabularySize(): Int = vocabulary.size
    fun getTotalFeatures(): Int = totalFeatures
}