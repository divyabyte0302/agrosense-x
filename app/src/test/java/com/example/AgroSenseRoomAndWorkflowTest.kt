package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AgroDao
import com.example.data.local.AgroDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AgroSenseRoomAndWorkflowTest {

    private lateinit var db: AgroDatabase
    private lateinit var dao: AgroDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AgroDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.agroDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testUserAuthenticationAndProfilePersistence() = runBlocking {
        val user = User(
            id = "user_123",
            email = "farmer@agrosense.org",
            displayName = "Dr. Green Farm Lead",
            farmLocation = "Salinas Valley, CA"
        )
        dao.insertUser(user)

        val retrievedUser = dao.getUserByEmail("farmer@agrosense.org")
        assertNotNull(retrievedUser)
        assertEquals("Dr. Green Farm Lead", retrievedUser?.displayName)
        assertEquals("Salinas Valley, CA", retrievedUser?.farmLocation)

        val userFlow = dao.getUserById("user_123").first()
        assertEquals("user_123", userFlow?.id)
    }

    @Test
    fun testPlantRegistrationAndLifecycle() = runBlocking {
        val plant = Plant(
            id = 1L,
            name = "San Marzano Heirloom #1",
            variety = "Solanum lycopersicum",
            datePlanted = "2026-03-01",
            location = "Greenhouse Sector 4B",
            notes = "Drip fertigation line enabled"
        )
        val plantId = dao.insertPlant(plant)
        assertEquals(1L, plantId)

        val retrieved = dao.getPlantById(1L)
        assertNotNull(retrieved)
        assertEquals("San Marzano Heirloom #1", retrieved?.name)
        assertEquals("Greenhouse Sector 4B", retrieved?.location)

        val allPlants = dao.getAllPlants().first()
        assertEquals(1, allPlants.size)

        // Update plant
        val updatedPlant = plant.copy(notes = "Added organic bio-stimulant")
        dao.updatePlant(updatedPlant)
        val afterUpdate = dao.getPlantById(1L)
        assertEquals("Added organic bio-stimulant", afterUpdate?.notes)
    }

    @Test
    fun testSensorTelemetryIngestion() = runBlocking {
        val sensorData = SensorData(
            id = 1L,
            temperature = 27.5f,
            humidity = 64.0f,
            soilMoisture = 48.2f
        )
        dao.insertSensorData(sensorData)

        val latest = dao.getLatestSensorData().first()
        assertNotNull(latest)
        assertEquals(27.5f, latest?.temperature ?: 0f, 0.01f)
        assertEquals(64.0f, latest?.humidity ?: 0f, 0.01f)
        assertEquals(48.2f, latest?.soilMoisture ?: 0f, 0.01f)
    }

    @Test
    fun testPredictionAndHealthHistoryWorkflow() = runBlocking {
        val prediction = Prediction(
            id = 101L,
            plantId = 1L,
            imageUrl = "content://media/external/images/media/123",
            healthScore = 82,
            stressProbability = 24,
            predictedIssue = "Pre-Symptomatic Stomatal Transpiration Drop",
            recommendation = "Maintain soil moisture at 45-55% and increase air circulation."
        )
        dao.insertPrediction(prediction)

        val plantPredictions = dao.getPredictionsForPlant(1L).first()
        assertEquals(1, plantPredictions.size)
        assertEquals("Pre-Symptomatic Stomatal Transpiration Drop", plantPredictions[0].predictedIssue)

        // Health history trend
        val history1 = HealthHistory(
            id = 1L,
            plantId = 1L,
            dateString = "2026-06-10",
            healthScore = 92,
            stressProbability = 8
        )
        val history2 = HealthHistory(
            id = 2L,
            plantId = 1L,
            dateString = "2026-06-12",
            healthScore = 82,
            stressProbability = 24
        )
        dao.insertHealthHistories(listOf(history1, history2))

        val fullHistory = dao.getHealthHistoryForPlant(1L).first()
        assertEquals(2, fullHistory.size)
        assertEquals(92, fullHistory[0].healthScore)
        assertEquals(82, fullHistory[1].healthScore)
    }

    @Test
    fun testAlertsAndNotificationSystem() = runBlocking {
        val alert = PlantAlert(
            id = 1L,
            message = "High Vapor Pressure Deficit (VPD > 1.7 kPa) - High Transpiration Risk",
            severity = "WARNING",
            isRead = false
        )
        dao.insertAlert(alert)

        val unreadCount = dao.getUnreadAlertsCount().first()
        assertEquals(1, unreadCount)

        dao.markAlertAsRead(1L)
        val unreadCountAfter = dao.getUnreadAlertsCount().first()
        assertEquals(0, unreadCountAfter)
    }

    @Test
    fun testLiveWeatherTelemetryPersistence() = runBlocking {
        val weather = LiveWeatherTelemetry(
            id = 1,
            latitude = 36.6777,
            longitude = -121.6555,
            locationName = "Salinas Valley Agri-Center",
            temperatureCelsius = 26.2f,
            relativeHumidity = 58.0f,
            vaporPressureDeficitKPa = 1.42f,
            soilTemperatureCelsius = 21.0f,
            soilMoisturePercentage = 38.5f,
            weatherDescription = "Clear Sunny",
            weatherIcon = "☀️",
            isGpsLive = true
        )
        dao.insertLiveWeather(weather)

        val fetchedWeather = dao.getLiveWeatherTelemetry().first()
        assertNotNull(fetchedWeather)
        assertEquals("Salinas Valley Agri-Center", fetchedWeather?.locationName)
        assertEquals(26.2f, fetchedWeather?.temperatureCelsius ?: 0f, 0.01f)
        assertEquals(1.42f, fetchedWeather?.vaporPressureDeficitKPa ?: 0f, 0.01f)
    }

    @Test
    fun testPlantingCalendarAndCareReminders() = runBlocking {
        val entry = PlantingCalendarEntry(
            id = 1L,
            cropName = "Sweet Corn Pioneer Batch",
            variety = "Yellow Dent Hybrid",
            speciesKey = "MAIZE",
            plantingDate = "2026-04-01",
            plantingTimestamp = 1775001600000L,
            estimatedHarvestDate = "2026-07-01",
            estimatedHarvestTimestamp = 1782864000000L,
            currentStageKey = CropPhenologyStage.EARLY_VEGETATIVE.stageKey,
            stageProgressPercent = 35,
            plotLocation = "East Field Block A",
            plantingMethod = "Precision Seeding",
            totalExpectedDurationDays = 90,
            currentAgeDays = 20,
            targetYieldKg = 250f,
            accumulatedGdd = 210f
        )
        dao.insertPlantingEntry(entry)

        val activeEntries = dao.getActivePlantingEntries().first()
        assertEquals(1, activeEntries.size)
        assertEquals("Sweet Corn Pioneer Batch", activeEntries[0].cropName)

        val reminder = CropCareReminder(
            id = 501L,
            calendarEntryId = 1L,
            cropName = "Sweet Corn Pioneer Batch",
            title = "V4 Stage Side-Dress Nitrogen",
            description = "Apply urea solution at 40 kg N/ha prior to rapid elongation",
            taskTypeKey = CareTaskType.FERTILIZATION.typeKey,
            dueDate = "2026-04-22",
            intervalDays = 5,
            priority = "HIGH"
        )
        dao.insertCareReminder(reminder)

        val activeReminders = dao.getActiveCareReminders().first()
        assertEquals(1, activeReminders.size)
        assertFalse(activeReminders[0].isCompleted)

        // Complete reminder
        dao.setCareReminderCompletion(501L, true)
        val allReminders = dao.getAllCareReminders().first()
        assertTrue(allReminders[0].isCompleted)

        // Advance stage
        dao.updatePlantingStage(1L, CropPhenologyStage.RAPID_VEGETATIVE.stageKey, 10, 320f)
        val updatedEntry = dao.getPlantingEntryById(1L)
        assertEquals(CropPhenologyStage.RAPID_VEGETATIVE.stageKey, updatedEntry?.currentStageKey)
        assertEquals(320f, updatedEntry?.accumulatedGdd ?: 0f, 0.01f)
    }

    @Test
    fun testScanFeedbackLoopPersistence() = runBlocking {
        val feedback = ScanFeedback(
            id = 1L,
            predictionId = 101L,
            plantId = 1L,
            plantName = "San Marzano Heirloom #1",
            predictedIssue = "Pre-Symptomatic Stomatal Transpiration Drop",
            recommendation = "Maintain soil moisture at 45-55% and increase air circulation.",
            predictionAccuracyRating = 5,
            recommendationAccuracyRating = 5,
            comments = "Micro-irrigation adjustments prevented visible wilting within 48h.",
            userId = "user_123",
            userEmail = "farmer@agrosense.org"
        )
        dao.insertFeedback(feedback)

        val feedbacks = dao.getAllFeedback().first()
        assertEquals(1, feedbacks.size)
        assertEquals(5, feedbacks[0].predictionAccuracyRating)
        assertEquals(5, feedbacks[0].recommendationAccuracyRating)
        assertEquals("San Marzano Heirloom #1", feedbacks[0].plantName)
    }
}
