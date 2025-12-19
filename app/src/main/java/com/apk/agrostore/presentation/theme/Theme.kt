package com.apk.agrostore.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AgroGreen,
    secondary = AgroGreenAccent,
    tertiary = AgroGreenLight,
    background = Color(0xFF1C1C1C),
    surface = Color(0xFF2C2C2C),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AgroGreen,
    secondary = AgroGreenAccent,
    tertiary = AgroGreenLight,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun AgroStoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled to use our custom green theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Always use our custom color scheme, not dynamic colors
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}