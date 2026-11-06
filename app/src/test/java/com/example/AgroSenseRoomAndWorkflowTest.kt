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

    @Test
    fun testBatchInsertPaginationAndPruningPerformance() = runBlocking {
        // 1. Batch Insert 100 Sensor telemetry readings
        val now = System.currentTimeMillis()
        val oneHour = 3600_000L
        val batch = (1..100).map { i ->
            SensorData(
                id = i.toLong(),
                temperature = 20f + (i % 15),
                humidity = 50f + (i % 30),
                soilMoisture = 30f + (i % 40),
                timestamp = now - (i * oneHour)
            )
        }
        val startTime = System.currentTimeMillis()
        dao.insertSensorDataBatch(batch)
        val insertDuration = System.currentTimeMillis() - startTime
        assertTrue("Batch insert of 100 records should be efficient (< 2000ms)", insertDuration < 2000)

        // 2. Pagination verification
        val page1 = dao.getSensorDataPaged(limit = 10, offset = 0)
        assertEquals(10, page1.size)
        // Verify order is timestamp DESC
        assertTrue(page1[0].timestamp >= page1[1].timestamp)

        val page2 = dao.getSensorDataPaged(limit = 10, offset = 10)
        assertEquals(10, page2.size)
        assertTrue(page1[9].timestamp >= page2[0].timestamp)

        // 3. Paginated predictions and confirmation verification
        dao.insertPrediction(
            Prediction(
                id = 201L,
                plantId = 10L,
                imageUrl = "local://leaf_201.jpg",
                healthScore = 85,
                stressProbability = 15,
                predictedIssue = "Optimal Photosynthesis",
                recommendation = "Continue standard photoperiod",
                timestamp = now,
                modelVersion = "MobileNetV4-Agro-v2.1",
                isConfirmed = false
            )
        )
        dao.insertPrediction(
            Prediction(
                id = 202L,
                plantId = 10L,
                imageUrl = "local://leaf_202.jpg",
                healthScore = 65,
                stressProbability = 35,
                predictedIssue = "Water Deficit Stress",
                recommendation = "Increase irrigation cycle",
                timestamp = now + 1000,
                modelVersion = "MobileNetV4-Agro-v2.1",
                isConfirmed = false
            )
        )

        val predCount = dao.getPredictionsCountForPlant(10L)
        assertEquals(2, predCount)

        val pagedPredictions = dao.getPredictionsForPlantPaged(10L, limit = 1, offset = 0)
        assertEquals(1, pagedPredictions.size)
        assertEquals(202L, pagedPredictions[0].id)
        assertFalse(pagedPredictions[0].isConfirmed)

        // Update confirmation
        dao.updatePredictionConfirmation(202L, true)
        val pagedAfterConfirm = dao.getPredictionsForPlantPaged(10L, limit = 1, offset = 0)
        assertTrue(pagedAfterConfirm[0].isConfirmed)

        // 4. Data retention pruning (prune older than 50 hours)
        val cutoff = now - (50 * oneHour)
        val prunedCount = dao.pruneOldSensorData(cutoff)
        assertTrue("Should prune records older than cutoff (> 40 records)", prunedCount > 40)
    }

    @Test
    fun testUserRbacAndAccountManagement() = runBlocking {
        // 1. Insert Farmer
        val farmer = User(
            id = "farmer_01",
            email = "farmer.ram@agrosense.org",
            displayName = "Ram Kumar",
            phoneNumber = "+91 98401 11223",
            farmLocation = "Coimbatore Zone 2",
            role = "FARMER",
            passwordHash = "hash_farmer_123",
            status = "ACTIVE"
        )
        dao.insertUser(farmer)

        // 2. Insert Researcher
        val researcher = User(
            id = "researcher_01",
            email = "dr.swami@tnau.ac.in",
            displayName = "Dr. Swaminathan",
            phoneNumber = "+91 94432 33445",
            farmLocation = "TNAU Phenomics Lab",
            role = "RESEARCHER",
            passwordHash = "hash_researcher_123",
            status = "ACTIVE"
        )
        dao.insertUser(researcher)

        // 3. Insert Admin
        val admin = User(
            id = "admin_01",
            email = "admin@agrosense.org",
            displayName = "System Root",
            phoneNumber = "+91 97890 55667",
            farmLocation = "Agronomy HQ",
            role = "ADMIN",
            passwordHash = "hash_admin_123",
            status = "ACTIVE"
        )
        dao.insertUser(admin)

        // Verify counts and roles
        val allUsers = dao.getAllUsersList()
        assertEquals(3, allUsers.size)

        val retrievedFarmer = dao.getUserByEmail("farmer.ram@agrosense.org")
        assertNotNull(retrievedFarmer)
        assertTrue(retrievedFarmer!!.isFarmer)
        assertFalse(retrievedFarmer.isAdmin)
        assertFalse(retrievedFarmer.isResearcher)

        val retrievedResearcher = dao.getUserByEmail("dr.swami@tnau.ac.in")
        assertNotNull(retrievedResearcher)
        assertTrue(retrievedResearcher!!.isResearcher)
        assertFalse(retrievedResearcher.isAdmin)

        val retrievedAdmin = dao.getUserByEmail("admin@agrosense.org")
        assertNotNull(retrievedAdmin)
        assertTrue(retrievedAdmin!!.isAdmin)
        assertTrue(retrievedAdmin.isResearcher)
        assertTrue(retrievedAdmin.isFarmer)

        // 4. Update Profile
        dao.updateUserProfile("farmer_01", "Ram Kumar Updated", "+91 98401 99999", "Coimbatore Polyhouse Block 7")
        val updatedFarmer = dao.getUserByIdOnce("farmer_01")
        assertEquals("Ram Kumar Updated", updatedFarmer?.displayName)
        assertEquals("+91 98401 99999", updatedFarmer?.phoneNumber)
        assertEquals("Coimbatore Polyhouse Block 7", updatedFarmer?.farmLocation)

        // 5. Suspend User Account
        dao.updateUserStatus("farmer_01", "SUSPENDED")
        val suspendedFarmer = dao.getUserByIdOnce("farmer_01")
        assertEquals("SUSPENDED", suspendedFarmer?.status)
        assertFalse(suspendedFarmer!!.isActive)

        // 6. Change Password Hash
        dao.updateUserPassword("farmer_01", "new_salted_hash_999")
        val passUpdated = dao.getUserByIdOnce("farmer_01")
        assertEquals("new_salted_hash_999", passUpdated?.passwordHash)

        // 7. Delete User Account
        dao.deleteUserById("farmer_01")
        val deletedFarmer = dao.getUserByIdOnce("farmer_01")
        assertNull(deletedFarmer)
        assertEquals(2, dao.getAllUsersList().size)
    }
}
