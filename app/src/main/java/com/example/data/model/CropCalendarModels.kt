package com.example.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.*
import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.*

/**
 * Phenological growth stages for crop tracking and care scheduling.
 */
enum class CropPhenologyStage(
    val stageKey: String,
    val displayName: String,
    val iconEmoji: String,
    val typicalDurationDays: Int,
    val physiologicalFocus: String,
    val waterDemandLevel: String, // "Low", "Moderate", "High", "Critical"
    val nutrientFocus: String,
    val commonRisks: List<String>
) {
    GERMINATION(
        stageKey = "GERMINATION",
        displayName = "Germination & Emergence",
        iconEmoji = "🌱",
        typicalDurationDays = 8,
        physiologicalFocus = "Radicle emergence, cotyledon expansion, root hair anchorage.",
        waterDemandLevel = "Moderate",
        nutrientFocus = "Low EC, gentle phosphorus for root elongation.",
        commonRisks = listOf("Damping-off fungal rot", "Crusting soil surface", "Moisture deficit")
    ),
    EARLY_VEGETATIVE(
        stageKey = "EARLY_VEGETATIVE",
        displayName = "Seedling & Early Leaf",
        iconEmoji = "🌿",
        typicalDurationDays = 16,
        physiologicalFocus = "True leaf formation, photosynthetic area establishment.",
        waterDemandLevel = "Moderate",
        nutrientFocus = "Balanced Nitrogen (N) & micronutrients (Zn, Fe).",
        commonRisks = listOf("Flea beetles / Thrips", "Waterlogging damping", "Transplant shock")
    ),
    RAPID_VEGETATIVE(
        stageKey = "RAPID_VEGETATIVE",
        displayName = "Canopy & Vegetative Growth",
        iconEmoji = "☘️",
        typicalDurationDays = 24,
        physiologicalFocus = "Main stem branching, heavy biomass synthesis, node stacking.",
        waterDemandLevel = "High",
        nutrientFocus = "High Nitrogen (N) & Magnesium (Mg) for chlorophyll synthesis.",
        commonRisks = listOf("Nitrogen chlorosis", "Chewing caterpillar herbivory", "Aphid infestation")
    ),
    FLOWERING_ANTHESIS(
        stageKey = "FLOWERING_ANTHESIS",
        displayName = "Flowering & Anthesis",
        iconEmoji = "🌸",
        typicalDurationDays = 18,
        physiologicalFocus = "Floral bud initiation, pollen viability, nectar secretion.",
        waterDemandLevel = "Critical",
        nutrientFocus = "High Phosphorus (P), Potassium (K), Boron (B) for pollen tube growth.",
        commonRisks = listOf("Heat-induced blossom drop (>32°C)", "Calcium deficiency", "Thrips floral damage")
    ),
    FRUIT_DEVELOPMENT(
        stageKey = "FRUIT_DEVELOPMENT",
        displayName = "Fruit Set & Grain Filling",
        iconEmoji = "🍅",
        typicalDurationDays = 28,
        physiologicalFocus = "Cell division & enlargement, carbohydrate translocation, seed filling.",
        waterDemandLevel = "High",
        nutrientFocus = "High Potassium (K) & Calcium (Ca) for skin firmness & sugar transport.",
        commonRisks = listOf("Blossom end rot (Calcium/water fluctuation)", "Fruit cracking", "Foliar blight")
    ),
    MATURATION_HARVEST(
        stageKey = "MATURATION_HARVEST",
        displayName = "Ripening & Harvest Window",
        iconEmoji = "🌾",
        typicalDurationDays = 14,
        physiologicalFocus = "Brix sugar accumulation, color pigmentation, desiccation.",
        waterDemandLevel = "Low",
        nutrientFocus = "Reduced Nitrogen, Potassium maintenance, dry-down flush.",
        commonRisks = listOf("Over-ripening / Fruit rot", "Pre-harvest rain splitting", "Pest piercing")
    );

    companion object {
        fun fromKey(key: String): CropPhenologyStage {
            return entries.firstOrNull { it.stageKey.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) }
                ?: RAPID_VEGETATIVE
        }
    }
}

/**
 * Categories of climate-adaptive care tasks.
 */
enum class CareTaskType(
    val typeKey: String,
    val title: String,
    val iconEmoji: String,
    val color: Color
) {
    IRRIGATION(
        typeKey = "IRRIGATION",
        title = "Irrigation & Hydration",
        iconEmoji = "💧",
        color = Color(0xFF29B6F6)
    ),
    FERTILIZATION(
        typeKey = "FERTILIZATION",
        title = "Nutrients & Fertigation",
        iconEmoji = "🧪",
        color = Color(0xFF66BB6A)
    ),
    PEST_SCOUTING(
        typeKey = "PEST_SCOUTING",
        title = "Pest & Pathogen Scouting",
        iconEmoji = "🛡️",
        color = Color(0xFFFFB74D)
    ),
    CANOPY_PRUNING(
        typeKey = "CANOPY_PRUNING",
        title = "Pruning, Sucker & Trellis",
        iconEmoji = "✂️",
        color = Color(0xFFAB47BC)
    ),
    CLIMATE_SHIELD(
        typeKey = "CLIMATE_SHIELD",
        title = "Climate Protection & Shielding",
        iconEmoji = "☀️",
        color = Color(0xFFFF7043)
    ),
    HARVEST_CHECK(
        typeKey = "HARVEST_CHECK",
        title = "Brix & Harvest Readiness",
        iconEmoji = "🧺",
        color = GlowingEmerald
    );

    companion object {
        fun fromKey(key: String): CareTaskType {
            return entries.firstOrNull { it.typeKey.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) }
                ?: IRRIGATION
        }
    }
}

/**
 * Room Database entity representing a registered crop lifecycle planting entry.
 */
@Entity(tableName = "planting_calendar")
@JsonClass(generateAdapter = true)
data class PlantingCalendarEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cropName: String,
    val variety: String,
    val speciesKey: String = "TOMATO", // TOMATO, CHILLI, RICE, POTATO, MAIZE, BEANS, BANANA, MANGO, GRAPES, STRAWBERRY, CUSTOM
    val plantingDate: String, // e.g. "2026-08-01"
    val plantingTimestamp: Long = System.currentTimeMillis(),
    val estimatedHarvestDate: String, // e.g. "2026-11-15"
    val estimatedHarvestTimestamp: Long = System.currentTimeMillis() + (90L * 86400000L),
    val currentStageKey: String = CropPhenologyStage.EARLY_VEGETATIVE.stageKey,
    val stageProgressPercent: Int = 35, // 0 - 100% within current stage
    val plotLocation: String = "Polyhouse A - Row 2",
    val plantingMethod: String = "Transplant", // "Direct Seed", "Transplant", "Hydroponic Channel", "Raised Bed"
    val totalExpectedDurationDays: Int = 95,
    val currentAgeDays: Int = 22,
    val targetYieldKg: Float = 45.0f,
    val notes: String = "",
    val accumulatedGdd: Float = 340.5f, // Growing Degree Days
    val isArchived: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Room Database entity representing a scheduled or climate-triggered care task reminder.
 */
@Entity(tableName = "care_reminders")
@JsonClass(generateAdapter = true)
data class CropCareReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val calendarEntryId: Long = 0,
    val cropName: String,
    val title: String,
    val description: String,
    val taskTypeKey: String = CareTaskType.IRRIGATION.typeKey,
    val dueDate: String, // e.g. "2026-08-26"
    val dueTimestamp: Long = System.currentTimeMillis(),
    val intervalDays: Int = 3,
    val isCompleted: Boolean = false,
    val completedTimestamp: Long? = null,
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val climateTriggerReason: String = "Calculated from local microclimate telemetry",
    val growthStageApplicable: String = "Vegetative",
    val suggestedAction: String = "Verify soil moisture before application",
    val isAutoAdaptive: Boolean = true
)

/**
 * Climate Adaptive Recommendation summary for the calendar dashboard.
 */
data class ClimateCareRecommendation(
    val cropName: String,
    val currentStage: CropPhenologyStage,
    val irrigationAdjustment: String,
    val nutrientRecommendation: String,
    val diseaseRiskAlert: String?,
    val heatOrColdAdvisory: String?,
    val calculatedGddRate: Float,
    val nextKeyMilestone: String,
    val daysToNextStage: Int
)
