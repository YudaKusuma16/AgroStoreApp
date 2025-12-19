package com.apk.agrostore.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log
import java.util.concurrent.TimeUnit

/**
 * Singleton object untuk Retrofit client
 */
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2/agrostore/api/" // Android Emulator
    // private const val BASE_URL = "http://192.168.1.6/agrostore/api/" // Physical Device

    // Configure Gson to handle camelCase to snake_case conversion
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    // Configure OkHttp client with timeouts and logging
    private val okHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                // Log original request
                Log.d("RetrofitClient", "Request URL: ${request.url}")
                Log.d("RetrofitClient", "Request Method: ${request.method}")
                Log.d("RetrofitClient", "Request Headers: ${request.headers}")

                val response = chain.proceed(request)

                // Log response
                Log.d("RetrofitClient", "Response Code: ${response.code}")
                Log.d("RetrofitClient", "Response Message: ${response.message}")
                Log.d("RetrofitClient", "Response Headers: ${response.headers}")

                response
            }
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}