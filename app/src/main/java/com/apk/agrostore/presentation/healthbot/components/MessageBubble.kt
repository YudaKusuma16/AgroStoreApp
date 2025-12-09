package com.apk.agrostore.presentation.healthbot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apk.agrostore.presentation.healthbot.ChatMessage
import com.apk.agrostore.presentation.healthbot.EmergencyLevel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = if (message.isUser) 16.dp else 4.dp,
                            topEnd = if (message.isUser) 4.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .background(
                        if (message.isUser) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = message.message,
                    color = if (message.isUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Show predictions if available for bot messages
            val predictions = message.predictions
            if (!message.isUser && predictions != null) {
                PredictionChips(
                    predictions = predictions,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                )
            }

            // Timestamp
            Text(
                text = timeFormat.format(Date(message.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun PredictionChips(
    predictions: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        predictions.split("\n").forEach { prediction ->
            if (prediction.contains(":")) {
                val parts = prediction.split(":")
                if (parts.size == 2) {
                    PredictionChip(
                        label = parts[0],
                        confidence = parts[1]
                    )
                }
            }
        }
    }
}

@Composable
fun PredictionChip(
    label: String,
    confidence: String,
    modifier: Modifier = Modifier
) {
    val color = when {
        label.contains("4-DM") -> Color.Red.copy(alpha = 0.1f)
        label.contains("1-FR") || label.contains("5-EDTRB") -> Color(0xFFFFA726).copy(alpha = 0.1f) // Orange
        else -> Color(0xFF4CAF50).copy(alpha = 0.1f) // Green
    }

    val borderColor = when {
        label.contains("4-DM") -> Color.Red
        label.contains("1-FR") || label.contains("5-EDTRB") -> Color(0xFFFF9800) // Orange
        else -> Color(0xFF4CAF50) // Green
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$label $confidence",
            color = borderColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmergencyBanner(
    emergencyLevel: EmergencyLevel,
    modifier: Modifier = Modifier
) {
    when (emergencyLevel) {
        EmergencyLevel.LOW -> return
        else -> {}
    }

    val (backgroundColor, icon, text) = when (emergencyLevel) {
        EmergencyLevel.HIGH -> Triple(
            Color.Red.copy(alpha = 0.1f),
            "🚨",
            "EMERGENCY - Segera ke fasilitas kesehatan!"
        )
        EmergencyLevel.MEDIUM -> Triple(
            Color(0xFFFFA726).copy(alpha = 0.1f), // Orange
            "⚠️",
            "PERHATIAN - Perlu konsultasi medik"
        )
        EmergencyLevel.LOW -> Triple(
            Color.Transparent,
            "",
            ""
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            color = if (emergencyLevel == EmergencyLevel.HIGH) Color.Red else Color(0xFFFF9800).copy(alpha = 0.8f), // Orange
            fontWeight = FontWeight.Bold
        )
    }
}