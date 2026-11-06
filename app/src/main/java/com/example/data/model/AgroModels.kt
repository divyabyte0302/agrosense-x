package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class UserRole(val displayName: String, val level: Int) {
    ADMIN("Administrator", 3),
    RESEARCHER("Agricultural Researcher", 2),
    FARMER("Farm Operator / Farmer", 1);

    companion object {
        fun fromString(value: String?): UserRole {
            return when (value?.uppercase()?.trim()) {
                "ADMIN" -> ADMIN
                "RESEARCHER" -> RESEARCHER
                else -> FARMER
            }
        }
    }
}

enum class AccountStatus(val label: String) {
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    DEACTIVATED("Deactivated");

    companion object {
        fun fromString(value: String?): AccountStatus {
            return when (value?.uppercase()?.trim()) {
                "SUSPENDED" -> SUSPENDED
                "DEACTIVATED" -> DEACTIVATED
                else -> ACTIVE
            }
        }
    }
}

/**
 * Represent user authentication, role, security credentials and profile status locally.
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["role"]),
        Index(value = ["status"])
    ]
)
@JsonClass(generateAdapter = true)
data class User(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val phoneNumber: String = "",
    val farmLocation: String = "",
    val role: String = "FARMER", // ADMIN, RESEARCHER, FARMER
    val passwordHash: String = "", // SHA-256 salted hash
    val status: String = "ACTIVE", // ACTIVE, SUSPENDED, DEACTIVATED
    val profileImageUrl: String = "",
    val notificationEnabled: Boolean = true,
    val preferredLanguage: String = "EN",
    val joinedTimestamp: Long = System.currentTimeMillis(),
    val lastLoginTimestamp: Long = System.currentTimeMillis()
) {
    val userRole: UserRole
        get() = UserRole.fromString(role)

    val accountStatus: AccountStatus
        get() = AccountStatus.fromString(status)

    val isAdmin: Boolean
        get() = userRole == UserRole.ADMIN

    val isResearcher: Boolean
        get() = userRole == UserRole.RESEARCHER || userRole == UserRole.ADMIN

    val isFarmer: Boolean
        get() = userRole == UserRole.FARMER || userRole == UserRole.ADMIN

    val isActive: Boolean
        get() = accountStatus == AccountStatus.ACTIVE
}

/**
 * Plant profiles registered by the farmer.
 */
@Entity(
    tableName = "plants",
    indices = [
        Index(value = ["dateAdded"]),
        Index(value = ["name"])
    ]
)
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
@Entity(
    tableName = "sensor_data",
    indices = [
        Index(value = ["timestamp"])
    ]
)
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
@Entity(
    tableName = "predictions",
    indices = [
        Index(value = ["plantId"]),
        Index(value = ["timestamp"]),
        Index(value = ["plantId", "timestamp"])
    ]
)
@JsonClass(generateAdapter = true)
data class Prediction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val imageUrl: String,
    val healthScore: Int,
    val stressProbability: Int,
    val predictedIssue: String,
    val recommendation: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelVersion: String = "MobileNetV4-Agro-v2.1",
    val isConfirmed: Boolean = false
) {
    val issueName: String get() = predictedIssue
    val confidence: Float get() = (100 - stressProbability).toFloat()
}

/**
 * Continuous crop physiological stress tracking timeline.
 */
@Entity(
    tableName = "health_history",
    indices = [
        Index(value = ["plantId"]),
        Index(value = ["timestamp"]),
        Index(value = ["plantId", "timestamp"])
    ]
)
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
@Entity(
    tableName = "alerts",
    indices = [
        Index(value = ["isRead"]),
        Index(value = ["timestamp"]),
        Index(value = ["isRead", "timestamp"])
    ]
)
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
    @field:Json(name = "health_score") val healthScore: Int,
    @field:Json(name = "stress_probability") val stressProbability: Int,
    @field:Json(name = "predicted_issue") val predictedIssue: String,
    @field:Json(name = "recommendation") val recommendation: String
)

/**
 * Farmer feedback rating the accuracy of AI's predictions and recommendations for scans.
 * Stored in Room locally and synced to the 'Feedback' collection in Firebase Firestore.
 */
@Entity(
    tableName = "feedback",
    indices = [
        Index(value = ["plantId"]),
        Index(value = ["predictionId"]),
        Index(value = ["timestamp"])
    ]
)
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


