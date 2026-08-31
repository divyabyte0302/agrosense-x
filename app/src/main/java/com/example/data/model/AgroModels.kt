package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represent user authentication and profile status locally.
 */
@Entity(tableName = "users")
@JsonClass(generateAdapter = true)
data class User(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val farmLocation: String,
    val joinedTimestamp: Long = System.currentTimeMillis()
)

/**
 * Plant profiles registered by the farmer.
 */
@Entity(tableName = "plants")
@JsonClass(generateAdapter = true)
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val variety: String,
    val datePlanted: String,
    val location: String,
    val notes: String,
    val dateAdded: Long = System.currentTimeMillis()
) {
    val tag: String
        get() = if (notes.contains("Tag:")) notes.substringAfter("Tag:").substringBefore(";").trim() else "PLT-${100 + id}"

    val plantedDate: String
        get() = datePlanted

    val healthScore: Int
        get() = when ((id % 4).toInt()) {
            0 -> 88
            1 -> 72
            2 -> 45
            else -> 91
        }
}

/**
 * Real-time climate and soil sensor telemetry.
 */
@Entity(tableName = "sensor_data")
@JsonClass(generateAdapter = true)
data class SensorData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val temperature: Float,
    val humidity: Float,
    val soilMoisture: Float,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Crop health diagnoses results from standard camera leaf scanning or API analysis.
 */
@Entity(tableName = "predictions")
@JsonClass(generateAdapter = true)
data class Prediction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val imageUrl: String,
    val healthScore: Int,
    val stressProbability: Int,
    val predictedIssue: String,
    val recommendation: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val issueName: String get() = predictedIssue
    val confidence: Float get() = (100 - stressProbability).toFloat()
}

/**
 * Continuous crop physiological stress tracking timeline.
 */
@Entity(tableName = "health_history")
@JsonClass(generateAdapter = true)
data class HealthHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val dateString: String, // e.g. "2026-06-12"
    val healthScore: Int,
    val stressProbability: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Automated warning notifications issued on critical thresholds.
 */
@Entity(tableName = "alerts")
@JsonClass(generateAdapter = true)
data class PlantAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val message: String,
    val severity: String, // "INFO", "WARNING", "CRITICAL"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

/**
 * Standard API response format from FastAPI crop scanning or internal Gemini simulation.
 */
@JsonClass(generateAdapter = true)
data class PredictionResponse(
    @Json(name = "health_score") val healthScore: Int,
    @Json(name = "stress_probability") val stressProbability: Int,
    @Json(name = "predicted_issue") val predictedIssue: String,
    @Json(name = "recommendation") val recommendation: String
)

/**
 * Farmer feedback rating the accuracy of AI's predictions and recommendations for scans.
 * Stored in Room locally and synced to the 'Feedback' collection in Firebase Firestore.
 */
@Entity(tableName = "feedback")
@JsonClass(generateAdapter = true)
data class ScanFeedback(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firestoreId: String = "",
    val plantId: Long = 0,
    val plantName: String = "",
    val predictionId: Long = 0,
    val predictedIssue: String = "",
    val recommendation: String = "",
    val predictionAccuracyRating: Int = 5, // 1 to 5 stars
    val recommendationAccuracyRating: Int = 5, // 1 to 5 stars
    val comments: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Supported display languages for farmers.
 */
enum class AppLanguage(val code: String, val displayNameEn: String, val displayNameNative: String) {
    ENGLISH("en", "English", "English"),
    TAMIL("ta", "Tamil", "தமிழ்")
}


