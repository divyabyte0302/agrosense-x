package com.example.data.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.data.fcm.AgroNotificationHelper
import com.example.data.local.AgroDatabase
import com.example.data.model.PlantAlert
import com.example.data.model.SensorData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager Worker that monitors Firestore telemetry data and triggers
 * local notifications when environmental thresholds for temperature or moisture are exceeded.
 */
class TelemetryMonitorWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "TelemetryMonitorWorker started: monitoring Firestore environmental telemetry...")
        val sharedPrefs = applicationContext.getSharedPreferences("agrosense_prefs", Context.MODE_PRIVATE)

        val moistureLowThreshold = sharedPrefs.getFloat("soil_moisture_threshold", 30.0f)
        val moistureHighThreshold = sharedPrefs.getFloat("soil_moisture_high_threshold", 80.0f)
        val tempHighThreshold = sharedPrefs.getFloat("temperature_high_threshold", 32.0f)
        val tempLowThreshold = sharedPrefs.getFloat("temperature_low_threshold", 12.0f)

        try {
            val firestore = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
            val database = AgroDatabase.getDatabase(applicationContext)
            val dao = database.agroDao()

            var fetchedFromFirestore = false

            // 1. Fetch latest environmental telemetry from Firestore 'SensorData' collection
            if (firestore != null) {
                try {
                    val snapshot = firestore.collection("SensorData")
                        .document("latest")
                        .get()
                        .await()

                    if (snapshot.exists()) {
                        val temp = snapshot.getDouble("temperature")?.toFloat() ?: 24.5f
                        val humid = snapshot.getDouble("humidity")?.toFloat() ?: 62.0f
                        val moisture = snapshot.getDouble("soilMoisture")?.toFloat() ?: 45.0f

                        val sensorData = SensorData(
                            temperature = temp,
                            humidity = humid,
                            soilMoisture = moisture,
                            timestamp = System.currentTimeMillis()
                        )

                        dao.insertSensorData(sensorData)
                        processThresholds(sensorData, moistureLowThreshold, moistureHighThreshold, tempHighThreshold, tempLowThreshold, dao, firestore)
                        fetchedFromFirestore = true
                        Log.i(TAG, "TelemetryMonitorWorker successfully processed Firestore snapshot: Temp=$temp°C, Moisture=$moisture%")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore telemetry fetch in background worker deferred: ${e.message}")
                }
            }

            // 2. If offline or no Firestore document, inspect latest local telemetry in Room
            if (!fetchedFromFirestore) {
                val latestLocal = dao.getAllSensorDataList(limit = 1).firstOrNull()
                if (latestLocal != null) {
                    processThresholds(latestLocal, moistureLowThreshold, moistureHighThreshold, tempHighThreshold, tempLowThreshold, dao, firestore)
                    Log.d(TAG, "TelemetryMonitorWorker evaluated latest local sensor reading: Temp=${latestLocal.temperature}°C, Moisture=${latestLocal.soilMoisture}%")
                }
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in TelemetryMonitorWorker: ${e.message}", e)
            return Result.retry()
        }
    }

    private suspend fun processThresholds(
        sd: SensorData,
        moistureLowThreshold: Float,
        moistureHighThreshold: Float,
        tempHighThreshold: Float,
        tempLowThreshold: Float,
        dao: com.example.data.local.AgroDao,
        firestore: FirebaseFirestore?
    ) {
        val targetZone = "Polyhouse Canopy Zone"

        // 1. Moisture Below Threshold
        if (sd.soilMoisture < moistureLowThreshold) {
            val alert = PlantAlert(
                message = "CRITICAL: Soil moisture deficit detected (${String.format("%.1f", sd.soilMoisture)}% < ${String.format("%.0f", moistureLowThreshold)}% threshold) in $targetZone.",
                severity = "CRITICAL"
            )
            dao.insertAlert(alert)
            AgroNotificationHelper.sendSoilMoistureAlert(applicationContext, targetZone, sd.soilMoisture, moistureLowThreshold)
            try { firestore?.collection("Alerts")?.add(alert) } catch (e: Exception) {}
        }

        // 2. Moisture Saturation Limit
        if (sd.soilMoisture > moistureHighThreshold) {
            val alert = PlantAlert(
                message = "WARNING: Saturated soil hazard (${String.format("%.1f", sd.soilMoisture)}% > ${String.format("%.0f", moistureHighThreshold)}% limit) in $targetZone.",
                severity = "WARNING"
            )
            dao.insertAlert(alert)
            AgroNotificationHelper.sendSoilMoistureHighAlert(applicationContext, targetZone, sd.soilMoisture, moistureHighThreshold)
            try { firestore?.collection("Alerts")?.add(alert) } catch (e: Exception) {}
        }

        // 3. High Temperature Hazard
        if (sd.temperature > tempHighThreshold) {
            val alert = PlantAlert(
                message = "CRITICAL: Thermal heat hazard (${String.format("%.1f", sd.temperature)}°C > ${String.format("%.0f", tempHighThreshold)}°C limit) in $targetZone.",
                severity = "CRITICAL"
            )
            dao.insertAlert(alert)
            AgroNotificationHelper.sendTemperatureAlert(applicationContext, targetZone, sd.temperature, isHigh = true, threshold = tempHighThreshold)
            try { firestore?.collection("Alerts")?.add(alert) } catch (e: Exception) {}
        }

        // 4. Low Temperature Chill Warning
        if (sd.temperature < tempLowThreshold) {
            val alert = PlantAlert(
                message = "WARNING: Cold chill temperature risk (${String.format("%.1f", sd.temperature)}°C < ${String.format("%.0f", tempLowThreshold)}°C limit) in $targetZone.",
                severity = "WARNING"
            )
            dao.insertAlert(alert)
            AgroNotificationHelper.sendTemperatureAlert(applicationContext, targetZone, sd.temperature, isHigh = false, threshold = tempLowThreshold)
            try { firestore?.collection("Alerts")?.add(alert) } catch (e: Exception) {}
        }
    }

    companion object {
        private const val TAG = "TelemetryWorker"
        const val UNIQUE_WORK_NAME = "AgroSenseFirestoreTelemetryMonitorWork"

        /**
         * Enqueue a periodic background WorkManager task to monitor Firestore telemetry every 15 minutes.
         */
        fun enqueuePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<TelemetryMonitorWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
            Log.d(TAG, "Enqueued periodic WorkManager telemetry monitor task (15 min interval).")
        }

        /**
         * Enqueue an immediate one-time background telemetry check.
         */
        fun enqueueImmediateWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<TelemetryMonitorWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(oneTimeRequest)
            Log.d(TAG, "Enqueued one-time immediate telemetry monitor check.")
        }
    }
}
