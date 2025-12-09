package com.apk.agrostore.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton object untuk Retrofit client
 */
object RetrofitClient {

    // Ganti dengan URL API Anda
    private const val BASE_URL = "http://10.0.2.2/agrostore/api/" // Untuk Android Emulator
    // Jika menggunakan device fisik, gunakan IP komputer:
    // private const val BASE_URL = "http://192.168.1.6/agrostore/api/"

    // Configure Gson to handle camelCase to snake_case conversion
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}