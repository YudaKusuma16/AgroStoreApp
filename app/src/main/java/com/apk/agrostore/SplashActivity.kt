package com.apk.agrostore

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private val SPLASH_DELAY: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Using Handler to delay the launch of MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            // Start MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close this activity
        }, SPLASH_DELAY)
    }
}