package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AgroDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("DELETE FROM users")
    suspend fun clearUsers()

    // --- Plants ---
    @Query("SELECT * FROM plants ORDER BY dateAdded DESC")
    fun getAllPlants(): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE id = :plantId LIMIT 1")
    suspend fun getPlantById(plantId: Long): Plant?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant): Long

    @Delete
    suspend fun deletePlant(plant: Plant)

    // --- Sensor Data ---
    @Query("SELECT * FROM sensor_data ORDER BY timestamp DESC LIMIT 1")
    fun getLatestSensorData(): Flow<SensorData?>

    @Query("SELECT * FROM sensor_data ORDER BY timestamp DESC")
    fun getAllSensorData(): Flow<List<SensorData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensorData(sensorData: SensorData)

    @Query("DELETE FROM sensor_data")
    suspend fun clearSensorData()

    // --- Predictions ---
    @Query("SELECT * FROM predictions WHERE plantId = :plantId ORDER BY timestamp DESC")
    fun getPredictionsForPlant(plantId: Long): Flow<List<Prediction>>

    @Query("SELECT * FROM predictions ORDER BY timestamp DESC")
    fun getAllPredictions(): Flow<List<Prediction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: Prediction): Long

    // --- Health History ---
    @Query("SELECT * FROM health_history WHERE plantId = :plantId ORDER BY timestamp ASC")
    fun getHealthHistoryForPlant(plantId: Long): Flow<List<HealthHistory>>

    @Query("SELECT * FROM health_history ORDER BY timestamp ASC")
    fun getAllHealthHistory(): Flow<List<HealthHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthHistory(history: HealthHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthHistories(histories: List<HealthHistory>)

    @Query("DELETE FROM health_history")
    suspend fun clearHealthHistory()

    // --- Alerts ---
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<PlantAlert>>

    @Query("SELECT COUNT(*) FROM alerts WHERE isRead = 0")
    fun getUnreadAlertsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PlantAlert)

    @Query("UPDATE alerts SET isRead = 1 WHERE id = :alertId")
    suspend fun markAlertAsRead(alertId: Long)

    @Query("UPDATE alerts SET isRead = 1")
    suspend fun markAllAlertsAsRead()

    @Query("DELETE FROM alerts")
    suspend fun clearAlerts()

    // --- Feedback ---
    @Query("SELECT * FROM feedback ORDER BY timestamp DESC")
    fun getAllFeedback(): Flow<List<ScanFeedback>>

    @Query("SELECT * FROM feedback WHERE plantId = :plantId ORDER BY timestamp DESC")
    fun getFeedbackForPlant(plantId: Long): Flow<List<ScanFeedback>>

    @Query("SELECT * FROM feedback WHERE predictionId = :predictionId LIMIT 1")
    fun getFeedbackForPrediction(predictionId: Long): Flow<ScanFeedback?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: ScanFeedback): Long

    @Query("DELETE FROM feedback")
    suspend fun clearFeedback()
}

