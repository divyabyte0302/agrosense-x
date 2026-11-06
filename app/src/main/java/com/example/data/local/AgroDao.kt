package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AgroDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserByIdOnce(userId: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users ORDER BY joinedTimestamp DESC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users ORDER BY joinedTimestamp DESC")
    suspend fun getAllUsersList(): List<User>

    @Query("SELECT * FROM users WHERE role = :role ORDER BY joinedTimestamp DESC")
    fun getUsersByRole(role: String): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("UPDATE users SET status = :status WHERE id = :userId")
    suspend fun updateUserStatus(userId: String, status: String)

    @Query("UPDATE users SET role = :role WHERE id = :userId")
    suspend fun updateUserRole(userId: String, role: String)

    @Query("UPDATE users SET displayName = :displayName, phoneNumber = :phoneNumber, farmLocation = :farmLocation WHERE id = :userId")
    suspend fun updateUserProfile(userId: String, displayName: String, phoneNumber: String, farmLocation: String)

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE id = :userId")
    suspend fun updateUserPassword(userId: String, newPasswordHash: String)

    @Query("UPDATE users SET notificationEnabled = :enabled, preferredLanguage = :language WHERE id = :userId")
    suspend fun updateUserSettings(userId: String, enabled: Boolean, language: String)

    @Query("UPDATE users SET lastLoginTimestamp = :timestamp WHERE id = :userId")
    suspend fun updateLastLogin(userId: String, timestamp: Long)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("DELETE FROM users")
    suspend fun clearUsers()

    // --- Plants ---
    @Query("SELECT * FROM plants ORDER BY dateAdded DESC")
    fun getAllPlants(): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE id = :plantId LIMIT 1")
    suspend fun getPlantById(plantId: Long): Plant?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant): Long

    @Update
    suspend fun updatePlant(plant: Plant)

    @Delete
    suspend fun deletePlant(plant: Plant)

    @Query("DELETE FROM plants WHERE id = :plantId")
    suspend fun deletePlantById(plantId: Long)

    @Query("DELETE FROM plants")
    suspend fun clearPlants()

    // --- Sensor Data ---
    @Query("SELECT * FROM sensor_data ORDER BY timestamp DESC LIMIT 1")
    fun getLatestSensorData(): Flow<SensorData?>

    @Query("SELECT * FROM sensor_data ORDER BY timestamp DESC")
    fun getAllSensorData(): Flow<List<SensorData>>

    @Query("SELECT * FROM sensor_data ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getSensorDataPaged(limit: Int, offset: Int): List<SensorData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensorData(sensorData: SensorData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensorDataBatch(sensorDataList: List<SensorData>)

    @Update
    suspend fun updateSensorData(sensorData: SensorData)

    @Query("DELETE FROM sensor_data WHERE id = :sensorId")
    suspend fun deleteSensorDataById(sensorId: Long)

    @Query("DELETE FROM sensor_data WHERE timestamp < :cutoffTimestamp")
    suspend fun pruneOldSensorData(cutoffTimestamp: Long): Int

    @Query("DELETE FROM sensor_data")
    suspend fun clearSensorData()

    // --- Predictions ---
    @Query("SELECT * FROM predictions WHERE plantId = :plantId ORDER BY timestamp DESC")
    fun getPredictionsForPlant(plantId: Long): Flow<List<Prediction>>

    @Query("SELECT * FROM predictions ORDER BY timestamp DESC")
    fun getAllPredictions(): Flow<List<Prediction>>

    @Query("SELECT * FROM predictions WHERE id = :predictionId LIMIT 1")
    fun getPredictionById(predictionId: Long): Flow<Prediction?>

    @Query("SELECT * FROM predictions WHERE plantId = :plantId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestPredictionForPlant(plantId: Long): Flow<Prediction?>

    @Query("SELECT * FROM predictions ORDER BY timestamp DESC LIMIT 1")
    fun getLatestPrediction(): Flow<Prediction?>

    @Query("SELECT * FROM predictions WHERE plantId = :plantId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getPredictionsForPlantPaged(plantId: Long, limit: Int, offset: Int): List<Prediction>

    @Query("SELECT COUNT(*) FROM predictions WHERE plantId = :plantId")
    suspend fun getPredictionsCountForPlant(plantId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: Prediction): Long

    @Update
    suspend fun updatePrediction(prediction: Prediction)

    @Query("UPDATE predictions SET isConfirmed = :confirmed WHERE id = :predictionId")
    suspend fun updatePredictionConfirmation(predictionId: Long, confirmed: Boolean)

    @Query("DELETE FROM predictions WHERE id = :predictionId")
    suspend fun deletePrediction(predictionId: Long)

    @Query("DELETE FROM predictions")
    suspend fun clearPredictions()

    // --- Health History ---
    @Query("SELECT * FROM health_history WHERE plantId = :plantId ORDER BY timestamp ASC")
    fun getHealthHistoryForPlant(plantId: Long): Flow<List<HealthHistory>>

    @Query("SELECT * FROM health_history WHERE plantId = :plantId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestHealthForPlant(plantId: Long): Flow<HealthHistory?>

    @Query("SELECT * FROM health_history ORDER BY timestamp ASC")
    fun getAllHealthHistory(): Flow<List<HealthHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthHistory(history: HealthHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthHistories(histories: List<HealthHistory>)

    @Update
    suspend fun updateHealthHistory(history: HealthHistory)

    @Query("DELETE FROM health_history WHERE id = :historyId")
    suspend fun deleteHealthHistoryById(historyId: Long)

    @Query("DELETE FROM health_history")
    suspend fun clearHealthHistory()

    // --- Alerts ---
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<PlantAlert>>

    @Query("SELECT COUNT(*) FROM alerts WHERE isRead = 0")
    fun getUnreadAlertsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PlantAlert)

    @Update
    suspend fun updateAlert(alert: PlantAlert)

    @Query("UPDATE alerts SET isRead = 1 WHERE id = :alertId")
    suspend fun markAlertAsRead(alertId: Long)

    @Query("UPDATE alerts SET isRead = 1")
    suspend fun markAllAlertsAsRead()

    @Query("DELETE FROM alerts WHERE id = :alertId")
    suspend fun deleteAlertById(alertId: Long)

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

    @Update
    suspend fun updateFeedback(feedback: ScanFeedback)

    @Query("DELETE FROM feedback WHERE id = :feedbackId")
    suspend fun deleteFeedbackById(feedbackId: Long)

    @Query("DELETE FROM feedback")
    suspend fun clearFeedback()

    // --- Live Weather Telemetry ---
    @Query("SELECT * FROM weather_telemetry WHERE id = 1 LIMIT 1")
    fun getLiveWeatherTelemetry(): Flow<LiveWeatherTelemetry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveWeather(weather: LiveWeatherTelemetry)

    @Query("DELETE FROM weather_telemetry")
    suspend fun clearLiveWeather()

    // --- Planting Calendar Entries ---
    @Query("SELECT * FROM planting_calendar WHERE isArchived = 0 ORDER BY plantingTimestamp DESC")
    fun getActivePlantingEntries(): Flow<List<PlantingCalendarEntry>>

    @Query("SELECT * FROM planting_calendar ORDER BY plantingTimestamp DESC")
    fun getAllPlantingEntries(): Flow<List<PlantingCalendarEntry>>

    @Query("SELECT * FROM planting_calendar WHERE id = :id LIMIT 1")
    suspend fun getPlantingEntryById(id: Long): PlantingCalendarEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlantingEntry(entry: PlantingCalendarEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlantingEntries(entries: List<PlantingCalendarEntry>)

    @Update
    suspend fun updatePlantingEntry(entry: PlantingCalendarEntry)

    @Query("UPDATE planting_calendar SET currentStageKey = :stageKey, stageProgressPercent = :progress, accumulatedGdd = :gdd, lastUpdated = :now WHERE id = :id")
    suspend fun updatePlantingStage(id: Long, stageKey: String, progress: Int, gdd: Float, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM planting_calendar WHERE id = :id")
    suspend fun deletePlantingEntryById(id: Long)

    @Query("DELETE FROM planting_calendar")
    suspend fun clearPlantingCalendar()

    // --- Crop Care Reminders ---
    @Query("SELECT * FROM care_reminders ORDER BY isCompleted ASC, dueTimestamp ASC")
    fun getAllCareReminders(): Flow<List<CropCareReminder>>

    @Query("SELECT * FROM care_reminders WHERE isCompleted = 0 ORDER BY dueTimestamp ASC")
    fun getActiveCareReminders(): Flow<List<CropCareReminder>>

    @Query("SELECT * FROM care_reminders WHERE calendarEntryId = :calendarEntryId ORDER BY dueTimestamp ASC")
    fun getRemindersForCrop(calendarEntryId: Long): Flow<List<CropCareReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareReminder(reminder: CropCareReminder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareReminders(reminders: List<CropCareReminder>)

    @Update
    suspend fun updateCareReminder(reminder: CropCareReminder)

    @Query("UPDATE care_reminders SET isCompleted = :completed, completedTimestamp = :completedTime WHERE id = :reminderId")
    suspend fun setCareReminderCompletion(reminderId: Long, completed: Boolean, completedTime: Long? = if (completed) System.currentTimeMillis() else null)

    @Query("DELETE FROM care_reminders WHERE id = :reminderId")
    suspend fun deleteCareReminderById(reminderId: Long)

    @Query("DELETE FROM care_reminders WHERE calendarEntryId = :calendarEntryId")
    suspend fun deleteRemindersForCrop(calendarEntryId: Long)

    @Query("DELETE FROM care_reminders")
    suspend fun clearCareReminders()
}

