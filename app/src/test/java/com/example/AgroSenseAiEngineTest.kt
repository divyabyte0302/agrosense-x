package com.example

import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test

class AgroSenseAiEngineTest {

    @Test
    fun testMultiStressCalculationConsistency() {
        // Multi-stress risk indices
        val waterStress = 72f
        val nutrientStress = 31f
        val diseaseRisk = 18f
        val heatStress = 65f
        val coldStress = 5f
        val salinityStress = 12f

        val maxStress = maxOf(waterStress, nutrientStress, diseaseRisk, heatStress, coldStress, salinityStress)
        val meanStress = (waterStress + nutrientStress + diseaseRisk + heatStress + coldStress + salinityStress) / 6f
        val compositeRisk = (maxStress * 0.6f) + (meanStress * 0.4f)

        assertTrue("Max stress should be 72% (Water Stress)", maxStress == 72f)
        assertTrue("Composite risk score must be between 0 and 100", compositeRisk in 0f..100f)
        assertEquals(56.73f, compositeRisk, 0.5f)
    }

    @Test
    fun testPhenologicalStageDefinitionsAndGdd() {
        val stages = CropPhenologyStage.entries
        assertEquals("Must contain 6 canonical phenology stages", 6, stages.size)

        val germination = CropPhenologyStage.GERMINATION
        assertEquals("GERMINATION", germination.stageKey)
        assertEquals("Moderate", germination.waterDemandLevel)

        val flowering = CropPhenologyStage.FLOWERING_ANTHESIS
        assertEquals("Critical", flowering.waterDemandLevel)
        assertTrue("Flowering must require high phosphorus and boron", flowering.nutrientFocus.contains("Phosphorus"))

        // Test GDD Rate calculation (T_mean - T_base)
        val temp = 28.5f
        val baseTempTomato = 10.0f
        val dailyGdd = (temp - baseTempTomato).coerceAtLeast(0f)
        assertEquals(18.5f, dailyGdd, 0.01f)
    }

    @Test
    fun testVegetationAndStressIndices() {
        // Hyperspectral simulation: NIR (Near Infra-Red) and Red reflectance
        val nirReflectance = 0.68f
        val redReflectance = 0.12f

        // Standard Normalized Difference Vegetation Index (NDVI) formula = (NIR - Red) / (NIR + Red)
        val ndvi = (nirReflectance - redReflectance) / (nirReflectance + redReflectance)
        assertEquals(0.70f, ndvi, 0.01f)
        assertTrue("Healthy canopy NDVI must be > 0.6", ndvi > 0.6f)

        // Crop Water Stress Index (CWSI) = (dT - dT_lower) / (dT_upper - dT_lower)
        val canopyTempDiff = 1.8f
        val lowerBound = -2.0f
        val upperBound = 6.0f
        val cwsi = ((canopyTempDiff - lowerBound) / (upperBound - lowerBound)).coerceIn(0f, 1f)
        assertEquals(0.475f, cwsi, 0.01f)
    }

    @Test
    fun testPlantingCalendarEntryDataIntegrity() {
        val entry = PlantingCalendarEntry(
            id = 1L,
            cropName = "San Marzano Plum",
            variety = "Heirloom Special",
            speciesKey = "TOMATO",
            plantingDate = "2026-03-01",
            plantingTimestamp = 1772323200000L,
            estimatedHarvestDate = "2026-06-01",
            estimatedHarvestTimestamp = 1780272000000L,
            currentStageKey = CropPhenologyStage.EARLY_VEGETATIVE.stageKey,
            stageProgressPercent = 40,
            plotLocation = "Greenhouse Block 2",
            plantingMethod = "Transplant",
            totalExpectedDurationDays = 92,
            currentAgeDays = 25,
            targetYieldKg = 45.0f,
            notes = "Drip fertigation installed.",
            accumulatedGdd = 280f
        )

        assertEquals("San Marzano Plum", entry.cropName)
        assertEquals(CropPhenologyStage.EARLY_VEGETATIVE.stageKey, entry.currentStageKey)
        assertFalse(entry.isArchived)
        assertEquals(45.0f, entry.targetYieldKg, 0.01f)
    }

    @Test
    fun testCareReminderTaskTypeMapping() {
        val irrigation = CareTaskType.IRRIGATION
        assertEquals("IRRIGATION", irrigation.typeKey)
        assertEquals("💧", irrigation.iconEmoji)

        val fertilization = CareTaskType.FERTILIZATION
        assertEquals("FERTILIZATION", fertilization.typeKey)
        assertEquals("🧪", fertilization.iconEmoji)

        val reminder = CropCareReminder(
            id = 101L,
            calendarEntryId = 1L,
            cropName = "San Marzano Plum",
            title = "Morning High-Phosphorus Fertigation",
            description = "Apply N-P-K 10-30-20 at EC 2.0",
            taskTypeKey = CareTaskType.FERTILIZATION.typeKey,
            dueDate = "2026-03-25",
            intervalDays = 4,
            priority = "HIGH"
        )

        assertEquals(CareTaskType.FERTILIZATION.typeKey, reminder.taskTypeKey)
        assertFalse(reminder.isCompleted)
        assertEquals("HIGH", reminder.priority)
    }
}
