package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.AgroBiotechEngine
import com.example.data.local.AgroDatabase
import com.example.data.local.NetworkMonitor
import com.example.data.model.*
import com.example.data.repository.AgroRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

enum class InferenceEngineMode(val title: String, val subtitle: String, val latencyMs: Int) {
    EDGE_MOBILENET_V4("Edge AI (MobileNetV4-Agro)", "On-device inference • 100% Offline • 42ms Latency", 42),
    CLOUD_GEMINI_VISION("Cloud Gemini 2.5 Vision", "Deep agricultural reasoning & multimodal synthesis", 850)
}

enum class RoomSyncState(val label: String, val badgeText: String, val description: String) {
    SYNCED("Synced", "Synced", "Local Room DB is in sync with cloud"),
    SYNCING("Syncing...", "Syncing...", "Synchronizing Room database with cloud..."),
    OFFLINE("Offline", "Offline (Room DB)", "Offline mode • All changes saved locally in Room SQLite DB")
}

data class RoomDatabaseMetrics(
    val plantCount: Int = 0,
    val sensorReadingCount: Int = 0,
    val alertCount: Int = 0,
    val isOffline: Boolean = false,
    val dbName: String = "agro_precision.db"
)

class AgroViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AgroViewModel"

    private val repository: AgroRepository
    private val networkMonitor = NetworkMonitor(application)

    // --- Real-time Network Connectivity & Offline Cache State ---
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _lastSyncedTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncedTimestamp: StateFlow<Long> = _lastSyncedTimestamp.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Combined live Room Database Sync Status StateFlow
    val syncStatus: StateFlow<RoomSyncState> = combine(isOnline, isSyncing) { online, syncing ->
        when {
            syncing -> RoomSyncState.SYNCING
            !online -> RoomSyncState.OFFLINE
            else -> RoomSyncState.SYNCED
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RoomSyncState.SYNCED)

    init {
        val database = AgroDatabase.getDatabase(application)
        repository = AgroRepository(application, database.agroDao())
        _lastSyncedTimestamp.value = repository.lastSyncedTimestamp
        
        // Ensure baseline data is pre-seeded so the graphs and items are pre-loaded immediately
        viewModelScope.launch {
            repository.initializePreSeedDatabase()
            repository.syncFcmToken()
            refreshLiveWeatherWithGps()
            startSensorTelemetryPolling()
        }

        // Automatic silent background synchronization upon reconnection
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                if (online) {
                    try {
                        repository.fetchCloudSensorTelemetry()
                        repository.updateLastSyncedTime()
                        _lastSyncedTimestamp.value = repository.lastSyncedTimestamp
                    } catch (e: Exception) {
                        Log.d(TAG, "Silent background sync: ${e.message}")
                    }
                }
            }
        }

        // Periodic automatic background synchronization loop
        viewModelScope.launch {
            while (true) {
                delay(30_000L)
                if (isOnline.value) {
                    try {
                        repository.fetchCloudSensorTelemetry()
                        repository.updateLastSyncedTime()
                        _lastSyncedTimestamp.value = repository.lastSyncedTimestamp
                    } catch (e: Exception) {
                        Log.d(TAG, "Background sync tick: ${e.message}")
                    }
                }
            }
        }
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                repository.fetchCloudSensorTelemetry()
                repository.updateLastSyncedTime()
                _lastSyncedTimestamp.value = repository.lastSyncedTimestamp
                delay(800) // Brief UI feedback indicator
            } catch (e: Exception) {
                Log.w(TAG, "Manual sync error: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    // --- Authentication State ---
    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    private val _authStateMessage = MutableStateFlow<String?>(null)
    val authStateMessage: StateFlow<String?> = _authStateMessage.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSessionUser().collect { user ->
                _userState.value = user
            }
        }
    }

    // --- RBAC & All Users State ---
    val allUsers: StateFlow<List<User>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _authStateMessage.value = "Email and password cannot be empty."
            return
        }
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authStateMessage.value = null
            val result = repository.loginUser(email, password)
            if (result.isSuccess) {
                _userState.value = result.getOrNull()
                _isAuthenticating.value = false
                onSuccess()
            } else {
                _authStateMessage.value = result.exceptionOrNull()?.message ?: "Login failed."
                _isAuthenticating.value = false
            }
        }
    }

    fun signUp(
        email: String,
        password: String,
        name: String,
        phoneNumber: String = "",
        location: String = "",
        role: String = "FARMER",
        onSuccess: () -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _authStateMessage.value = "Name, email, and password are required to register."
            return
        }
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authStateMessage.value = null
            val result = repository.registerUser(
                email = email,
                password = password,
                displayName = name,
                phoneNumber = phoneNumber,
                farmLocation = location,
                role = role
            )
            if (result.isSuccess) {
                _userState.value = result.getOrNull()
                _isAuthenticating.value = false
                onSuccess()
            } else {
                _authStateMessage.value = result.exceptionOrNull()?.message ?: "Signup failed."
                _isAuthenticating.value = false
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _authStateMessage.value = "Enter email address to send recovery mail."
            return
        }
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authStateMessage.value = null
            val result = repository.resetPassword(email)
            if (result.isSuccess) {
                _authStateMessage.value = "Password reset instructions sent to $email."
            } else {
                _authStateMessage.value = result.exceptionOrNull()?.message ?: "Reset request failed."
            }
            _isAuthenticating.value = false
        }
    }

    fun updateProfile(
        name: String,
        phoneNumber: String,
        location: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.updateCurrentUserProfile(name, phoneNumber, location)
            if (result.isSuccess) {
                onComplete(true, null)
            } else {
                onComplete(false, result.exceptionOrNull()?.message)
            }
        }
    }

    fun changePassword(
        oldPass: String,
        newPass: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.changeCurrentUserPassword(oldPass, newPass)
            if (result.isSuccess) {
                onComplete(true, null)
            } else {
                onComplete(false, result.exceptionOrNull()?.message)
            }
        }
    }

    fun updateSettings(
        notificationsEnabled: Boolean,
        preferredLanguage: String
    ) {
        viewModelScope.launch {
            repository.updateUserSettings(notificationsEnabled, preferredLanguage)
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteCurrentUserAccount()
            if (result.isSuccess) {
                _userState.value = null
                onSuccess()
            }
        }
    }

    // --- Admin User Operations ---
    fun adminSetUserStatus(userId: String, status: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.adminSetUserStatus(userId, status)
            onComplete(res.isSuccess)
        }
    }

    fun adminSetUserRole(userId: String, role: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.adminSetUserRole(userId, role)
            onComplete(res.isSuccess)
        }
    }

    fun adminDeleteUser(userId: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.adminDeleteUser(userId)
            onComplete(res.isSuccess)
        }
    }

    fun logout() {
        repository.logoutUser()
        _userState.value = null
    }

    // --- Plants state ---
    val plants: StateFlow<List<Plant>> = repository.getAllPlants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPlant = MutableStateFlow<Plant?>(null)
    val selectedPlant: StateFlow<Plant?> = _selectedPlant.asStateFlow()

    fun selectPlant(plant: Plant) {
        _selectedPlant.value = plant
        // Fetch specific details associated with this plant
        viewModelScope.launch {
            loadPlantHistory(plant.id)
        }
    }

    fun registerNewPlant(name: String, variety: String, datePlanted: String, location: String, notes: String) {
        viewModelScope.launch {
            repository.registerPlant(name, variety, datePlanted, location, notes)
        }
    }

    fun addPlant(name: String, tag: String = "", variety: String, location: String, datePlanted: String = "", plantedDate: String = "", notes: String = "") {
        val finalDate = when {
            plantedDate.isNotBlank() -> plantedDate
            datePlanted.isNotBlank() -> datePlanted
            else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
        val finalNotes = if (tag.isNotBlank()) "Tag: $tag; $notes" else notes
        registerNewPlant(name, variety, finalDate, location, finalNotes)
    }

    // --- Application Language & Localization State (English / Tamil) ---
    private val prefs = application.getSharedPreferences("agro_app_prefs", Context.MODE_PRIVATE)

    private val _appLanguage = MutableStateFlow(
        if (prefs.getString("app_language", "en") == "ta") AppLanguage.TAMIL else AppLanguage.ENGLISH
    )
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
        prefs.edit().putString("app_language", language.code).apply()
    }

    fun toggleAppLanguage() {
        val next = if (_appLanguage.value == AppLanguage.TAMIL) AppLanguage.ENGLISH else AppLanguage.TAMIL
        setAppLanguage(next)
    }

    fun updateSelectedPlantName(newName: String) {
        val current = _selectedPlant.value ?: return
        viewModelScope.launch {
            val updated = current.copy(name = newName)
            repository.updatePlant(updated)
            _selectedPlant.value = updated
        }
    }

    fun updatePlantNameById(plantId: Long, newName: String) {
        viewModelScope.launch {
            val existing = repository.getPlantById(plantId)
            if (existing != null) {
                val updated = existing.copy(name = newName)
                repository.updatePlant(updated)
                if (_selectedPlant.value?.id == plantId) {
                    _selectedPlant.value = updated
                }
            }
        }
    }

    fun createAndSelectNewPlant(name: String, variety: String = "Local High-Yield", location: String = "Greenhouse Sector Alpha") {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val id = repository.registerPlant(
                name = name,
                variety = variety,
                datePlanted = today,
                location = location,
                notes = "Auto-created from AI Leaf Scanner Identification."
            )
            val newPlant = repository.getPlantById(id)
            if (newPlant != null) {
                _selectedPlant.value = newPlant
            }
        }
    }

    fun deletePlant(plant: Plant) {
        viewModelScope.launch {
            repository.deletePlant(plant)
            if (_selectedPlant.value?.id == plant.id) {
                _selectedPlant.value = null
            }
        }
    }

    // --- Dynamic Backend URL & Threshold Config ---
    var fastApiUrl: String
        get() = repository.fastApiBaseUrl
        set(value) {
            repository.fastApiBaseUrl = value
        }

    var soilMoistureThreshold: Float
        get() = repository.soilMoistureThreshold
        set(value) {
            repository.soilMoistureThreshold = value
        }

    val fcmDeviceToken: String?
        get() = repository.fcmDeviceToken

    fun refreshFcmToken() {
        viewModelScope.launch {
            repository.syncFcmToken()
        }
    }

    // --- Real-Time Climatology / Environmental Sensors telemetry ---
    val latestSensorData: StateFlow<SensorData?> = repository.getLatestSensorTelemetry()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val sensorHistory: StateFlow<List<SensorData>> = repository.getAllSensorHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- REAL-TIME LIVE WEATHER FROM DEVICE LOCATION ACCESS ---
    val liveWeather: StateFlow<LiveWeatherTelemetry?> = repository.getLiveWeatherTelemetry()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isFetchingWeather = MutableStateFlow(false)
    val isFetchingWeather: StateFlow<Boolean> = _isFetchingWeather.asStateFlow()

    private val _weatherErrorMessage = MutableStateFlow<String?>(null)
    val weatherErrorMessage: StateFlow<String?> = _weatherErrorMessage.asStateFlow()

    private val _locationPermissionGranted = MutableStateFlow(repository.locationProvider.hasLocationPermission())
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted.asStateFlow()

    fun updateLocationPermissionStatus(granted: Boolean) {
        _locationPermissionGranted.value = granted
        if (granted) {
            refreshLiveWeatherWithGps()
        }
    }

    fun refreshLiveWeatherWithGps() {
        viewModelScope.launch {
            _isFetchingWeather.value = true
            _weatherErrorMessage.value = null
            try {
                val result = repository.refreshWeatherWithDeviceLocation()
                if (result.isSuccess) {
                    _locationPermissionGranted.value = repository.locationProvider.hasLocationPermission()
                    recomputeDigitalTwin()
                } else {
                    _weatherErrorMessage.value = result.exceptionOrNull()?.message ?: "Unable to fetch live meteorological data."
                }
            } catch (e: Exception) {
                _weatherErrorMessage.value = "Weather sync error: ${e.localizedMessage}"
            } finally {
                _isFetchingWeather.value = false
            }
        }
    }

    fun fetchWeatherForCoordinates(lat: Double, lon: Double, cityName: String = "") {
        viewModelScope.launch {
            _isFetchingWeather.value = true
            _weatherErrorMessage.value = null
            try {
                val result = repository.fetchRealTimeWeather(
                    latitude = lat,
                    longitude = lon,
                    cityName = cityName,
                    isGps = false
                )
                if (result.isSuccess) {
                    recomputeDigitalTwin()
                } else {
                    _weatherErrorMessage.value = result.exceptionOrNull()?.message ?: "Unable to fetch weather for coordinates."
                }
            } catch (e: Exception) {
                _weatherErrorMessage.value = "Weather error: ${e.localizedMessage}"
            } finally {
                _isFetchingWeather.value = false
            }
        }
    }

    private fun startSensorTelemetryPolling() {
        viewModelScope.launch {
            while (true) {
                // Try pulling from Firestore first
                repository.fetchCloudSensorTelemetry()
                
                // Then let's also update or generate minor environmental variances locally to demonstrate active real-time charts
                val base = latestSensorData.value ?: SensorData(temperature = 23.5f, humidity = 60f, soilMoisture = 38f)
                val newMoisture = (base.soilMoisture + (-1..1).random() * 0.4f).coerceIn(12f, 95f)
                val sd = SensorData(
                    temperature = (base.temperature + (-1..1).random() * 0.15f).coerceIn(15f, 42f),
                    humidity = (base.humidity + (-1..1).random() * 0.5f).coerceIn(30f, 98f),
                    soilMoisture = newMoisture
                )
                // Save locally to display live updates
                repository.dao.insertSensorData(sd)
                
                // Under certain levels, generate triggers & push notifications
                repository.checkThresholdsForAlerts(sd, _selectedPlant.value?.name ?: "Polyhouse Zone A")

                delay(12000L) // poll/update ambient environment readings every 12 seconds
            }
        }
    }

    // --- AI Leaf Scanner Engine ---
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _latestScanResult = MutableStateFlow<Prediction?>(null)
    val latestScanResult: StateFlow<Prediction?> = _latestScanResult.asStateFlow()

    fun runLeafScanAnalysis(imageFile: File, targetPlant: Plant) {
        viewModelScope.launch {
            _isScanning.value = true
            _latestScanResult.value = null
            val result = repository.analyzeLeafImage(imageFile, targetPlant.id)
            if (result.isSuccess) {
                _latestScanResult.value = result.getOrNull()
            } else {
                Log.e(TAG, "Scanner error: ${result.exceptionOrNull()?.message}")
            }
            _isScanning.value = false
        }
    }

    // --- Room Database Cached Scan & Prediction History (Available Offline) ---
    val allPredictions: StateFlow<List<Prediction>> = repository.getAllPredictions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestPrediction: StateFlow<Prediction?> = repository.getLatestPrediction()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun deleteScanHistory(predictionId: Long) {
        viewModelScope.launch {
            repository.deletePrediction(predictionId)
            if (_latestScanResult.value?.id == predictionId) {
                _latestScanResult.value = null
            }
        }
    }

    fun selectPreviousScan(prediction: Prediction) {
        _latestScanResult.value = prediction
    }

    // --- Leaf Movement analysis module (Turgor Pressure Nyctinasty Track) ---
    private val _movementImage1 = MutableStateFlow<String?>(null) // Local path
    val movementImage1: StateFlow<String?> = _movementImage1.asStateFlow()

    private val _movementImage2 = MutableStateFlow<String?>(null) // Local path
    val movementImage2: StateFlow<String?> = _movementImage2.asStateFlow()

    private val _analyzingMovement = MutableStateFlow(false)
    val analyzingMovement: StateFlow<Boolean> = _analyzingMovement.asStateFlow()

    private val _movementResult = MutableStateFlow<MovementAnalysis?>(null)
    val movementResult: StateFlow<MovementAnalysis?> = _movementResult.asStateFlow()

    data class MovementAnalysis(
        val baselineAngle: Float,
        val secondaryAngle: Float,
        val angularAdjustment: Float,
        val movementPercent: Float,
        val status: String, // "NORMAL", "MILD_STRESS", "SEVERE_STRESS"
        val explanation: String
    )

    fun registerMovementPhotos(file1: File, file2: File) {
        _movementImage1.value = file1.absolutePath
        _movementImage2.value = file2.absolutePath
        _movementResult.value = null
    }

    fun analyzeLeafMovementAngle() {
        val f1 = _movementImage1.value
        val f2 = _movementImage2.value
        if (f1 == null || f2 == null) return

        viewModelScope.launch {
            _analyzingMovement.value = true
            delay(2000) // Simulating image alignment and structural vector extraction

            // Compare leaf vein vectors:
            val baseAngle = (12..25).random().toFloat()
            // Simulating physical response: hydrated leaves track light/time with larger angle swings
            val currentMoisture = latestSensorData.value?.soilMoisture ?: 45f
            
            val swing = if (currentMoisture > 40f) {
                // Highly hydrated leaf moves a lot (nyctinastic/heliotropic heliotropism)
                (11..18).random().toFloat()
            } else if (currentMoisture > 30f) {
                // Stiffening / mild drooping reduces range of motion
                (4..9).random().toFloat()
            } else {
                // Plasmolyzed water-starved leaf is stiff/drooped with minimal dynamic movement
                (0..3).random().toFloat() + (0..9).random() * 0.1f
            }

            val resultAngle = baseAngle + swing
            val movementPercentage = (swing / baseAngle) * 100f

            val (status, explanation) = when {
                movementPercentage >= 45f -> Pair(
                    "NORMAL",
                    "The foliage shows healthy solar heliotropism and active turgor response (swing: ${"%.1f".format(swing)}°). Stomatal transpiration and hydration pressure are excellent."
                )
                movementPercentage >= 15f -> Pair(
                    "MILD_STRESS",
                    "Reduced leaves amplitude swing (${"%.1f".format(swing)}°). Mild cellular turgor droop. Check watering levels to prevent stomatal constriction."
                )
                else -> Pair(
                    "SEVERE_STRESS",
                    "Extremely low cellular movement amplitude detected (${"%.1f".format(swing)}°). Severe turgor loss and vascular wilting. Act quickly to hydrate roots."
                )
            }

            _movementResult.value = MovementAnalysis(
                baselineAngle = baseAngle,
                secondaryAngle = resultAngle,
                angularAdjustment = swing,
                movementPercent = movementPercentage,
                status = status,
                explanation = explanation
            )
            _analyzingMovement.value = false

            // Fire notice alerts on stress
            if (status == "SEVERE_STRESS") {
                repository.createManualAlert("URGENT: Wilting/turgor stress index indicates severe dehydration leaf deflection movement threshold.", "CRITICAL")
            } else if (status == "MILD_STRESS") {
                repository.createManualAlert("NOTICE: Impaired nyctinastic movement amplitude matches initial dehydration signs.", "WARNING")
            }
        }
    }

    fun clearMovementTrack() {
        _movementImage1.value = null
        _movementImage2.value = null
        _movementResult.value = null
    }

    // --- Historical Trend Series ---
    private val _plantHistory = MutableStateFlow<List<HealthHistory>>(emptyList())
    val plantHistory: StateFlow<List<HealthHistory>> = _plantHistory.asStateFlow()

    private suspend fun loadPlantHistory(plantId: Long) {
        _plantHistory.value = repository.dao.getHealthHistoryForPlant(plantId).firstOrNull() ?: emptyList()
    }

    // Expose all histories for aggregate analysis
    val allHistoryData: StateFlow<List<HealthHistory>> = repository.dao.getAllHealthHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val healthHistory: StateFlow<List<HealthHistory>> get() = allHistoryData

    // --- FEEDBACK & AI ACCURACY RATINGS ---
    val feedbackList: StateFlow<List<ScanFeedback>> = repository.getAllFeedback()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSubmittingFeedback = MutableStateFlow(false)
    val isSubmittingFeedback: StateFlow<Boolean> = _isSubmittingFeedback.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    fun submitScanFeedback(
        plant: Plant,
        prediction: Prediction,
        predictionAccuracyRating: Int,
        recommendationAccuracyRating: Int,
        comments: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingFeedback.value = true
            _feedbackMessage.value = null

            val currentUser = userState.value
            val feedback = ScanFeedback(
                plantId = plant.id,
                plantName = plant.name,
                predictionId = prediction.id,
                predictedIssue = prediction.predictedIssue,
                recommendation = prediction.recommendation,
                predictionAccuracyRating = predictionAccuracyRating.coerceIn(1, 5),
                recommendationAccuracyRating = recommendationAccuracyRating.coerceIn(1, 5),
                comments = comments.trim(),
                userId = currentUser?.id ?: "guest_farmer_0",
                userEmail = currentUser?.email ?: "farmer@agrosense.com",
                timestamp = System.currentTimeMillis()
            )

            val result = repository.submitFeedback(feedback)
            if (result.isSuccess) {
                _feedbackMessage.value = "Feedback successfully submitted & synced to Firestore!"
                _isSubmittingFeedback.value = false
                onSuccess()
            } else {
                _feedbackMessage.value = "Failed to record feedback: ${result.exceptionOrNull()?.message}"
                _isSubmittingFeedback.value = false
            }
        }
    }

    fun submitScanFeedback(
        plantId: Long,
        scanHistoryId: Long,
        accuracyRating: Int,
        recommendationRating: Int,
        comments: String,
        onSuccess: () -> Unit = {}
    ) {
        val targetPlant = _selectedPlant.value ?: Plant(
            id = plantId,
            name = "Target Crop #$plantId",
            variety = "General",
            datePlanted = "2026-01-01",
            location = "Polyhouse A",
            notes = "Auto-created profile"
        )
        val targetPrediction = _latestScanResult.value ?: Prediction(
            id = scanHistoryId,
            plantId = plantId,
            imageUrl = "",
            healthScore = 80,
            stressProbability = 20,
            predictedIssue = "AI Foliar Scan #$scanHistoryId",
            recommendation = "Standard irrigation & nutrient schedule."
        )
        submitScanFeedback(
            plant = targetPlant,
            prediction = targetPrediction,
            predictionAccuracyRating = accuracyRating,
            recommendationAccuracyRating = recommendationRating,
            comments = comments,
            onSuccess = onSuccess
        )
    }

    fun getFeedbackForScan(predictionId: Long): Flow<ScanFeedback?> {
        return repository.getFeedbackForPrediction(predictionId)
    }

    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }

    // --- PUSH NOTIFICATION TEST TRIGGERS ---
    fun triggerPushTest(type: String, plantName: String? = null) {
        val targetName = plantName ?: _selectedPlant.value?.name ?: "Monarda Hybrid Rose"
        viewModelScope.launch {
            repository.triggerTestPushNotification(type, targetName)
        }
    }

    // --- ALERTS AND ALARMS ---
    val activeAlerts: StateFlow<List<PlantAlert>> = repository.getAllAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadAlertsCount: StateFlow<Int> = repository.getUnreadAlertsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun readAlert(alertId: Long) {
        viewModelScope.launch {
            repository.markAlertAsRead(alertId)
        }
    }

    fun readAllAlerts() {
        viewModelScope.launch {
            repository.markAllAlertsAsRead()
        }
    }

    fun clearAlerts() {
        viewModelScope.launch {
            repository.clearAllAlerts()
        }
    }

    // ===============================================
    // --- ADMIN MODULE EXTENDED CONTROL METHODS ---
    // ===============================================

    private val _adminActionMessage = MutableStateFlow<String?>(null)
    val adminActionMessage: StateFlow<String?> = _adminActionMessage.asStateFlow()

    fun clearAdminActionMessage() {
        _adminActionMessage.value = null
    }

    fun adminUpdatePlant(plant: Plant) {
        viewModelScope.launch {
            repository.adminUpdatePlant(plant)
            _adminActionMessage.value = "Updated plant: ${plant.name}"
        }
    }

    fun adminDeletePlant(plantId: Long) {
        viewModelScope.launch {
            repository.adminDeletePlant(plantId)
            if (_selectedPlant.value?.id == plantId) {
                _selectedPlant.value = null
            }
            _adminActionMessage.value = "Crop #$plantId removed from database."
        }
    }

    fun adminAddPlant(name: String, variety: String, datePlanted: String, location: String, notes: String) {
        viewModelScope.launch {
            repository.registerPlant(name, variety, datePlanted, location, notes)
            _adminActionMessage.value = "Registered new crop: $name ($variety)"
        }
    }

    fun adminAddSensorReading(temperature: Float, humidity: Float, soilMoisture: Float) {
        viewModelScope.launch {
            repository.adminInsertSensorData(temperature, humidity, soilMoisture)
            _adminActionMessage.value = "Injected sensor record: ${temperature}°C, ${humidity}%, ${soilMoisture}%"
        }
    }

    fun adminUpdateSensorData(sensorData: SensorData) {
        viewModelScope.launch {
            repository.adminUpdateSensorData(sensorData)
            _adminActionMessage.value = "Updated sensor log #${sensorData.id}"
        }
    }

    fun adminDeleteSensorData(sensorId: Long) {
        viewModelScope.launch {
            repository.adminDeleteSensorData(sensorId)
            _adminActionMessage.value = "Deleted sensor log #$sensorId"
        }
    }

    fun adminClearSensorData() {
        viewModelScope.launch {
            repository.adminClearSensorData()
            _adminActionMessage.value = "Cleared all sensor telemetry logs."
        }
    }

    fun adminAddPrediction(
        plantId: Long,
        predictedIssue: String,
        healthScore: Int,
        stressProbability: Int,
        recommendation: String
    ) {
        viewModelScope.launch {
            repository.adminInsertPrediction(
                plantId = plantId,
                predictedIssue = predictedIssue,
                healthScore = healthScore,
                stressProbability = stressProbability,
                recommendation = recommendation
            )
            _adminActionMessage.value = "Added AI prediction for Crop #$plantId: $predictedIssue"
        }
    }

    fun adminUpdatePrediction(prediction: Prediction) {
        viewModelScope.launch {
            repository.adminUpdatePrediction(prediction)
            _adminActionMessage.value = "Updated AI prediction #${prediction.id}"
        }
    }

    fun adminDeletePrediction(predictionId: Long) {
        viewModelScope.launch {
            repository.adminDeletePrediction(predictionId)
            _adminActionMessage.value = "Deleted AI prediction #$predictionId"
        }
    }

    fun adminClearPredictions() {
        viewModelScope.launch {
            repository.adminClearPredictions()
            _adminActionMessage.value = "Cleared all AI predictions from database."
        }
    }

    fun adminAddHealthHistory(
        plantId: Long,
        dateString: String,
        healthScore: Int,
        stressProbability: Int
    ) {
        viewModelScope.launch {
            repository.adminInsertHealthHistory(
                plantId = plantId,
                dateString = dateString,
                healthScore = healthScore,
                stressProbability = stressProbability
            )
            _adminActionMessage.value = "Added health history entry for Crop #$plantId ($dateString)"
        }
    }

    fun adminUpdateHealthHistory(history: HealthHistory) {
        viewModelScope.launch {
            repository.adminUpdateHealthHistory(history)
            _adminActionMessage.value = "Updated health history entry #${history.id}"
        }
    }

    fun adminDeleteHealthHistory(historyId: Long) {
        viewModelScope.launch {
            repository.adminDeleteHealthHistory(historyId)
            _adminActionMessage.value = "Deleted health history entry #$historyId"
        }
    }

    fun adminClearHealthHistory() {
        viewModelScope.launch {
            repository.adminClearHealthHistory()
            _adminActionMessage.value = "Cleared all historical health tracking entries."
        }
    }

    fun adminCreateAlert(message: String, severity: String) {
        viewModelScope.launch {
            repository.adminCreateAlert(message, severity)
            _adminActionMessage.value = "Broadcasted $severity alert: $message"
        }
    }

    fun adminUpdateAlert(alert: PlantAlert) {
        viewModelScope.launch {
            repository.adminUpdateAlert(alert)
            _adminActionMessage.value = "Updated alert #${alert.id}"
        }
    }

    fun adminDeleteAlert(alertId: Long) {
        viewModelScope.launch {
            repository.adminDeleteAlert(alertId)
            _adminActionMessage.value = "Deleted alert #$alertId"
        }
    }

    fun adminClearAlerts() {
        viewModelScope.launch {
            repository.adminClearAlerts()
            _adminActionMessage.value = "Cleared all system alerts."
        }
    }

    fun adminUpdateFeedback(feedback: ScanFeedback) {
        viewModelScope.launch {
            repository.adminUpdateFeedback(feedback)
            _adminActionMessage.value = "Updated review #${feedback.id}"
        }
    }

    fun adminDeleteFeedback(feedbackId: Long) {
        viewModelScope.launch {
            repository.adminDeleteFeedback(feedbackId)
            _adminActionMessage.value = "Deleted review #${feedbackId}"
        }
    }

    fun adminClearFeedback() {
        viewModelScope.launch {
            repository.adminClearFeedback()
            _adminActionMessage.value = "Cleared all feedback entries."
        }
    }

    fun adminResetDatabaseToDefaults(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.adminResetDatabaseToDefaults()
            _adminActionMessage.value = "Reset database to initial seed dataset successfully."
            onComplete()
        }
    }

    fun adminWipeEntireDatabase(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.adminClearEntireDatabase()
            _selectedPlant.value = null
            _latestScanResult.value = null
            _adminActionMessage.value = "Wiped all database records. Database is now empty."
            onComplete()
        }
    }

    // =========================================================================
    // --- ADVANCED AGROSENSE-X SUITE (19 BIOTECH & MULTIMODAL CAPABILITIES) ---
    // =========================================================================

    private val _selectedModality = MutableStateFlow(ImagingModality.MULTISPECTRAL)
    val selectedModality: StateFlow<ImagingModality> = _selectedModality.asStateFlow()

    private val _selectedCropSpecies = MutableStateFlow(CropSpecies.TOMATO)
    val selectedCropSpecies: StateFlow<CropSpecies> = _selectedCropSpecies.asStateFlow()

    private val _selectedGrowthStage = MutableStateFlow(PlantGrowthStage.VEGETATIVE)
    val selectedGrowthStage: StateFlow<PlantGrowthStage> = _selectedGrowthStage.asStateFlow()

    private val _plantAgeDays = MutableStateFlow(38)
    val plantAgeDays: StateFlow<Int> = _plantAgeDays.asStateFlow()

    private val _inferenceEngineMode = MutableStateFlow(InferenceEngineMode.EDGE_MOBILENET_V4)
    val inferenceEngineMode: StateFlow<InferenceEngineMode> = _inferenceEngineMode.asStateFlow()

    private val _xaiShowAttentionMap = MutableStateFlow(false)
    val xaiShowAttentionMap: StateFlow<Boolean> = _xaiShowAttentionMap.asStateFlow()

    private val _smartphoneLightingLux = MutableStateFlow(480f)
    val smartphoneLightingLux: StateFlow<Float> = _smartphoneLightingLux.asStateFlow()

    private val _smartphoneGlareDetected = MutableStateFlow(false)
    val smartphoneGlareDetected: StateFlow<Boolean> = _smartphoneGlareDetected.asStateFlow()

    private val _smartphoneLeafRoiDetected = MutableStateFlow(true)
    val smartphoneLeafRoiDetected: StateFlow<Boolean> = _smartphoneLeafRoiDetected.asStateFlow()

    private val _selectedHeatmapZone = MutableStateFlow("Polyhouse Alpha")
    val selectedHeatmapZone: StateFlow<String> = _selectedHeatmapZone.asStateFlow()

    private val _continuousLearningCount = MutableStateFlow(142)
    val continuousLearningCount: StateFlow<Int> = _continuousLearningCount.asStateFlow()

    // 15. Digital Plant Twin State
    private val _digitalPlantTwin = MutableStateFlow(
        AgroBiotechEngine.runMultimodalFusion(
            species = CropSpecies.TOMATO,
            stage = PlantGrowthStage.VEGETATIVE,
            ageDays = 38,
            imagingModality = ImagingModality.MULTISPECTRAL,
            sensorTempC = 24.2f,
            sensorHumidity = 62f,
            sensorSoilMoisture = 39f,
            visualHealthScore = 82
        )
    )
    val digitalPlantTwin: StateFlow<DigitalPlantTwin> = _digitalPlantTwin.asStateFlow()

    // 11. Explainable AI Feature Attributions
    val xaiFeatureAttributions: StateFlow<List<XaiFeatureAttribution>> = _digitalPlantTwin.map { twin ->
        AgroBiotechEngine.computeXaiFeatureAttributions(twin)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 16. AI + Biochemical Confirmation Laboratory Test List
    private val _biochemicalTests = MutableStateFlow<List<BiochemicalConfirmationTest>>(
        listOf(
            BiochemicalConfirmationTest(
                id = "LAB-8041",
                plantId = 1L,
                plantTag = "T-102",
                cropName = "Tomato (Roma Hybrid)",
                aiSuspectedIssue = "Pre-Symptomatic Nitrogen Deficiency & Stomatal Transpiration Restriction",
                aiConfidence = 88,
                recommendedTestType = BiochemicalTestType.SPAD_CHLOROPHYLL,
                testStatus = LabTestStatus.CONFIRMED_MATCH,
                labResultSummary = "SPAD Meter Reading: 34.2 (Low). Leaf sap nitrate: 420 ppm (Deficit). Validated AI early warning.",
                resultMatchedAiDiagnosis = true,
                dateRequested = System.currentTimeMillis() - 86400_000 * 2,
                dateCompleted = System.currentTimeMillis() - 86400_000 * 1,
                laboratoryNotes = "Agronomist verified: Early foliar chlorosis averted via foliar urea application."
            ),
            BiochemicalConfirmationTest(
                id = "LAB-8042",
                plantId = 2L,
                plantTag = "CH-04",
                cropName = "Chilli (Bird's Eye)",
                aiSuspectedIssue = "Early Xanthomonas Leaf Blight (Bacterial Canker)",
                aiConfidence = 91,
                recommendedTestType = BiochemicalTestType.PCR_DIAGNOSTIC,
                testStatus = LabTestStatus.IN_ANALYSIS,
                labResultSummary = "Thermocycling running (35 cycles). Primers targeting hrp gene family.",
                resultMatchedAiDiagnosis = null,
                dateRequested = System.currentTimeMillis() - 3600_000 * 5,
                dateCompleted = null,
                laboratoryNotes = "Sample collected from polyhouse bed 2 leaf margin."
            ),
            BiochemicalConfirmationTest(
                id = "LAB-8043",
                plantId = 3L,
                plantTag = "RC-22",
                cropName = "Paddy Rice (Basmati)",
                aiSuspectedIssue = "Osmotic Root Salinity Stress & Pyricularia Blast Risk",
                aiConfidence = 84,
                recommendedTestType = BiochemicalTestType.SOIL_EC_PH_ANALYSIS,
                testStatus = LabTestStatus.CONFIRMED_MATCH,
                labResultSummary = "Saturated Paste EC: 4.8 mS/cm (Elevated). Soil pH: 7.9. Confirmed osmotic root restriction.",
                resultMatchedAiDiagnosis = true,
                dateRequested = System.currentTimeMillis() - 86400_000 * 4,
                dateCompleted = System.currentTimeMillis() - 86400_000 * 3,
                laboratoryNotes = "Fresh water leaching irrigation prescribed."
            )
        )
    )
    val biochemicalTests: StateFlow<List<BiochemicalConfirmationTest>> = _biochemicalTests.asStateFlow()

    // 9. Farm Stress Heatmap 5x5 Grid Nodes
    private val _farmHeatmapNodes = MutableStateFlow<List<FarmHeatmapNode>>(generateInitialHeatmapNodes())
    val farmHeatmapNodes: StateFlow<List<FarmHeatmapNode>> = _farmHeatmapNodes.asStateFlow()

    private fun generateInitialHeatmapNodes(): List<FarmHeatmapNode> {
        val list = mutableListOf<FarmHeatmapNode>()
        val speciesList = listOf(CropSpecies.TOMATO, CropSpecies.CHILLI, CropSpecies.POTATO, CropSpecies.MAIZE)
        for (r in 0..4) {
            for (c in 0..4) {
                val species = speciesList[(r + c) % speciesList.size]
                // Generate a localized gradient hotspot in middle-right (r=2,3 c=3,4)
                val isHotspot = (r in 1..2 && c in 2..3)
                val health = if (isHotspot) (35..55).random() else (78..96).random()
                val stress = when {
                    health < 50 -> "Water Deficit Stress (CWSI 0.65)"
                    health < 75 -> "Early Nitrogen Chlorosis"
                    else -> "Optimal Equilibrium"
                }
                list.add(
                    FarmHeatmapNode(
                        nodeId = "ZONE-$r-$c",
                        row = r,
                        col = c,
                        plantTag = "${species.commonName.take(2).uppercase()}-${100 + r * 5 + c}",
                        cropSpecies = species,
                        ashiHealthScore = health,
                        dominantStress = stress,
                        leafTempC = if (isHotspot) 27.8f else 23.2f,
                        soilMoisturePercent = if (isHotspot) 21.5f else 46.0f,
                        gpsLat = 13.0827 + (r * 0.00015),
                        gpsLng = 80.2707 + (c * 0.00015),
                        zone = "Polyhouse Alpha"
                    )
                )
            }
        }
        return list
    }

    fun setImagingModality(modality: ImagingModality) {
        _selectedModality.value = modality
        recomputeDigitalTwin()
    }

    fun setCropSpecies(species: CropSpecies) {
        _selectedCropSpecies.value = species
        recomputeDigitalTwin()
    }

    fun setGrowthStage(stage: PlantGrowthStage) {
        _selectedGrowthStage.value = stage
        recomputeDigitalTwin()
    }

    fun setPlantAgeDays(days: Int) {
        _plantAgeDays.value = days.coerceIn(1, 365)
        recomputeDigitalTwin()
    }

    fun setInferenceEngineMode(mode: InferenceEngineMode) {
        _inferenceEngineMode.value = mode
    }

    fun toggleXaiAttentionMap() {
        _xaiShowAttentionMap.value = !_xaiShowAttentionMap.value
    }

    fun setHeatmapZone(zone: String) {
        _selectedHeatmapZone.value = zone
    }

    fun triggerSmartphoneCameraCalibration() {
        _smartphoneLightingLux.value = (380..650).random().toFloat()
        _smartphoneGlareDetected.value = false
        _smartphoneLeafRoiDetected.value = true
    }

    private fun recomputeDigitalTwin() {
        val weather = liveWeather.value
        val sensor = latestSensorData.value
        val temp = weather?.temperatureCelsius ?: sensor?.temperature ?: 24.2f
        val humidity = weather?.relativeHumidity ?: sensor?.humidity ?: 62f
        val moisture = weather?.soilMoisturePercentage ?: sensor?.soilMoisture ?: 39f
        val visualScore = latestScanResult.value?.healthScore ?: 82

        _digitalPlantTwin.value = AgroBiotechEngine.runMultimodalFusion(
            species = _selectedCropSpecies.value,
            stage = _selectedGrowthStage.value,
            ageDays = _plantAgeDays.value,
            imagingModality = _selectedModality.value,
            sensorTempC = temp,
            sensorHumidity = humidity,
            sensorSoilMoisture = moisture,
            visualHealthScore = visualScore
        )
    }

    // AI + Biochemical Confirmation Test Actions
    fun requestBiochemicalTest(
        plantTag: String,
        cropName: String,
        aiSuspectedIssue: String,
        testType: BiochemicalTestType,
        notes: String
    ) {
        val newTest = BiochemicalConfirmationTest(
            id = "LAB-" + (1000..9999).random(),
            plantId = _selectedPlant.value?.id ?: 1L,
            plantTag = plantTag,
            cropName = cropName,
            aiSuspectedIssue = aiSuspectedIssue,
            aiConfidence = 89,
            recommendedTestType = testType,
            testStatus = LabTestStatus.PENDING_SAMPLE,
            laboratoryNotes = notes
        )
        _biochemicalTests.value = listOf(newTest) + _biochemicalTests.value
    }

    fun updateBiochemicalTestResult(
        testId: String,
        status: LabTestStatus,
        resultSummary: String,
        matched: Boolean,
        labNotes: String
    ) {
        _biochemicalTests.value = _biochemicalTests.value.map { test ->
            if (test.id == testId) {
                test.copy(
                    testStatus = status,
                    labResultSummary = resultSummary,
                    resultMatchedAiDiagnosis = matched,
                    dateCompleted = System.currentTimeMillis(),
                    laboratoryNotes = labNotes
                )
            } else test
        }
        // Continuous learning retrain trigger
        _continuousLearningCount.value += 1
    }

    fun submitContinuousLearningSample(
        plantTag: String,
        verifiedDiagnosis: String,
        notes: String
    ) {
        _continuousLearningCount.value += 1
    }

    // =========================================================================
    // --- CROP PLANTING CALENDAR & CLIMATE-ADAPTIVE CARE REMINDERS STATE ---
    // =========================================================================

    val activePlantingEntries: StateFlow<List<PlantingCalendarEntry>> = repository.getActivePlantingEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlantingEntries: StateFlow<List<PlantingCalendarEntry>> = repository.getAllPlantingEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCareReminders: StateFlow<List<CropCareReminder>> = repository.getActiveCareReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCareReminders: StateFlow<List<CropCareReminder>> = repository.getAllCareReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPlantingEntry = MutableStateFlow<PlantingCalendarEntry?>(null)
    val selectedPlantingEntry: StateFlow<PlantingCalendarEntry?> = _selectedPlantingEntry.asStateFlow()

    // Dynamically synthesized climate-adaptive recommendations combining live meteorological weather & active crops
    val climateRecommendations: StateFlow<List<ClimateCareRecommendation>> = combine(
        liveWeather,
        activePlantingEntries
    ) { weather, entries ->
        repository.computeClimateAdaptiveCareRecommendations(weather, entries)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectPlantingEntry(entry: PlantingCalendarEntry?) {
        _selectedPlantingEntry.value = entry
    }

    fun registerCropPlantingBatch(
        cropName: String,
        variety: String,
        speciesKey: String,
        plantingDate: String,
        estimatedHarvestDate: String,
        currentStageKey: String,
        plotLocation: String,
        plantingMethod: String,
        totalExpectedDurationDays: Int,
        targetYieldKg: Float,
        notes: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.registerPlantingBatch(
                cropName = cropName,
                variety = variety,
                speciesKey = speciesKey,
                plantingDate = plantingDate,
                estimatedHarvestDate = estimatedHarvestDate,
                currentStageKey = currentStageKey,
                plotLocation = plotLocation,
                plantingMethod = plantingMethod,
                totalExpectedDurationDays = totalExpectedDurationDays,
                targetYieldKg = targetYieldKg,
                notes = notes
            )
            onSuccess()
        }
    }

    fun advanceCropStage(calendarId: Long, newStage: CropPhenologyStage, progressPercent: Int = 0) {
        viewModelScope.launch {
            repository.advancePlantingStage(calendarId, newStage.stageKey, progressPercent)
        }
    }

    fun deleteCropPlantingBatch(calendarId: Long) {
        viewModelScope.launch {
            repository.deletePlantingEntry(calendarId)
            if (_selectedPlantingEntry.value?.id == calendarId) {
                _selectedPlantingEntry.value = null
            }
        }
    }

    fun toggleCareTaskCompleted(reminderId: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleCareReminderCompletion(reminderId, completed)
        }
    }

    fun addCustomCareReminder(
        calendarEntryId: Long,
        cropName: String,
        title: String,
        description: String,
        taskType: CareTaskType,
        dueDate: String,
        intervalDays: Int,
        priority: String,
        growthStage: String = "Active Growth"
    ) {
        viewModelScope.launch {
            val reminder = CropCareReminder(
                calendarEntryId = calendarEntryId,
                cropName = cropName,
                title = title,
                description = description,
                taskTypeKey = taskType.typeKey,
                dueDate = dueDate,
                dueTimestamp = try { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dueDate)?.time ?: System.currentTimeMillis() } catch (e: Exception) { System.currentTimeMillis() },
                intervalDays = intervalDays,
                priority = priority,
                growthStageApplicable = growthStage,
                climateTriggerReason = "Custom farmer care task scheduled with microclimate tracking.",
                suggestedAction = "Execute standard agricultural standard operating procedure.",
                isAutoAdaptive = false
            )
            repository.createCareReminder(reminder)
        }
    }

    fun deleteCareTask(reminderId: Long) {
        viewModelScope.launch {
            repository.deleteCareReminder(reminderId)
        }
    }

    fun applyClimateCareToActiveTasks() {
        viewModelScope.launch {
            // Trigger quick refresh of weather and re-compute reminders
            repository.refreshWeatherWithDeviceLocation()
            repository.createManualAlert(
                message = "🌦️ CLIMATE-SYNC: Care schedules re-calibrated against live temperature (${liveWeather.value?.temperatureCelsius ?: 24.5f}°C), VPD, and rainfall forecast.",
                severity = "INFO"
            )
        }
    }
}


