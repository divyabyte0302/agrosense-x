package com.example.data.fcm

import android.content.Context
import android.util.Log
import com.example.data.local.AgroDatabase
import com.example.data.model.PlantAlert
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AgroFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "AgroFCMService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token received: $token")
        saveTokenLocallyAndToCloud(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val plantName = data["plantName"] ?: data["plant_name"] ?: "Protected Greenhouse Crop"
        val issue = data["issue"] ?: data["predictedIssue"] ?: "Environmental Distress"
        val healthScore = data["healthScore"]?.toIntOrNull()
        val soilMoisture = data["soilMoisture"]?.toFloatOrNull()
        val stressProbability = data["stressProbability"]?.toIntOrNull()
        val threshold = data["threshold"]?.toFloatOrNull() ?: 30.0f

        when {
            healthScore != null && healthScore < 70 -> {
                AgroNotificationHelper.sendHealthScoreAlert(
                    applicationContext,
                    plantName,
                    healthScore,
                    issue
                )
                recordAlertLocally("CRITICAL: $plantName health dropped to $healthScore% ($issue)", "CRITICAL")
            }
            soilMoisture != null && soilMoisture < threshold -> {
                AgroNotificationHelper.sendSoilMoistureAlert(
                    applicationContext,
                    plantName,
                    soilMoisture,
                    threshold
                )
                recordAlertLocally("WARNING: $plantName soil moisture deficit (${soilMoisture}%)", "WARNING")
            }
            stressProbability != null && stressProbability > 60 -> {
                AgroNotificationHelper.sendStressProbabilityAlert(
                    applicationContext,
                    plantName,
                    stressProbability,
                    issue
                )
                recordAlertLocally("ALERT: $plantName stress probability reached $stressProbability% ($issue)", "CRITICAL")
            }
            else -> {
                val title = notification?.title ?: data["title"] ?: "AgroSense-X Alert"
                val body = notification?.body ?: data["body"] ?: "Crop alert triggered for $plantName: $issue"
                AgroNotificationHelper.showNotification(applicationContext, title, body)
                recordAlertLocally(body, "INFO")
            }
        }
    }

    private fun saveTokenLocallyAndToCloud(token: String) {
        try {
            val prefs = applicationContext.getSharedPreferences("agrosense_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("fcm_device_token", token).apply()

            val uid = prefs.getString("active_uid", null)
            if (uid != null) {
                try {
                    FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(uid)
                        .update("fcmToken", token)
                } catch (e: Exception) {
                    Log.w(TAG, "Could not update user token in Firestore: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving FCM token: ${e.message}")
        }
    }

    private fun recordAlertLocally(message: String, severity: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AgroDatabase.getDatabase(applicationContext)
                db.agroDao().insertAlert(
                    PlantAlert(
                        message = message,
                        severity = severity,
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to record alert in local Room database: ${e.message}")
            }
        }
    }
}
