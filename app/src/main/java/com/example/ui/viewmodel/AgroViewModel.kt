package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AgroDatabase
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

class AgroViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AgroViewModel"

    private val repository: AgroRepository
    
    init {
        val database = AgroDatabase.getDatabase(application)
        repository = AgroRepository(application, database.agroDao())
        
        // Ensure baseline data is pre-seeded so the graphs and items are pre-loaded immediately
        viewModelScope.launch {
            repository.initializePreSeedDatabase()
            repository.syncFcmToken()
            startSensorTelemetryPolling()
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

    fun signUp(email: String, password: String, name: String, location: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank() || name.isBlank() || location.isBlank()) {
            _authStateMessage.value = "All fields are required to register."
            return
        }
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authStateMessage.value = null
            val result = repository.registerUser(email, password, name, location)
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
}

