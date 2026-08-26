package com.example.data.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object AgroNotificationHelper {
    private const val TAG = "AgroNotificationHelper"
    const val CHANNEL_ID = "agrosense_plant_health_alerts"
    const val CHANNEL_NAME = "AgroSense Plant Health Alerts"
    const val CHANNEL_DESC = "Push notifications for critical crop health drops, moisture deficits, and stress alerts."

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        try {
            createNotificationChannel(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                pendingIntentFlags
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager = NotificationManagerCompat.from(context)
            try {
                notificationManager.notify(notificationId, builder.build())
            } catch (se: SecurityException) {
                Log.w(TAG, "Notification permission not granted yet: ${se.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to display notification: ${e.message}", e)
        }
    }

    /**
     * Trigger notification when plant's health score drops below 70.
     * Clearly states the issue and the affected plant.
     */
    fun sendHealthScoreAlert(context: Context, plantName: String, healthScore: Int, issue: String) {
        val title = "⚠️ Low Health Alert: $plantName"
        val message = "Health score dropped to $healthScore% (Issue: $issue). Recommended agronomist intervention required."
        showNotification(context, title, message, notificationId = 1001 + (plantName.hashCode() % 500))
    }

    /**
     * Trigger notification when soil moisture falls below a predefined threshold.
     * Clearly states the issue and the affected plant/location.
     */
    fun sendSoilMoistureAlert(context: Context, plantName: String, moisture: Float, threshold: Float) {
        val title = "💧 Soil Moisture Alert: $plantName"
        val message = "Soil moisture level dropped to ${"%.1f".format(moisture)}% (below ${"%.0f".format(threshold)}% threshold). Immediate irrigation required."
        showNotification(context, title, message, notificationId = 2001 + (plantName.hashCode() % 500))
    }

    /**
     * Trigger notification when stress probability exceeds 60%.
     * Clearly states the issue and the affected plant.
     */
    fun sendStressProbabilityAlert(context: Context, plantName: String, stressProbability: Int, issue: String) {
        val title = "🚨 High Plant Stress: $plantName"
        val message = "Physiological stress index reached $stressProbability% (Risk: $issue). Pre-symptomatic stress mitigation advised."
        showNotification(context, title, message, notificationId = 3001 + (plantName.hashCode() % 500))
    }

    suspend fun getFcmToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch FCM token: ${e.message}")
            null
        }
    }
}
