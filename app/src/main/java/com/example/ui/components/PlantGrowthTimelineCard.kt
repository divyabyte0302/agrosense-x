package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropPhenologyStage
import com.example.data.model.Plant
import com.example.data.model.SensorData
import com.example.ui.theme.*
import com.example.ui.viewmodel.AgroViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Phenological model data structure for species-specific developmental modeling.
 */
data class CropPhenologyProfile(
    val speciesName: String,
    val baseTemperatureC: Float,
    val optimalTempMinC: Float,
    val optimalTempMaxC: Float,
    val standardDurationDays: Int,
    val totalGddRequired: Float,
    val stages: List<PhenologyStageDef>
)

data class PhenologyStageDef(
    val stage: CropPhenologyStage,
    val name: String,
    val emoji: String,
    val durationDays: Int,
    val gddFraction: Float,
    val waterDemand: String, // "Low", "Moderate", "High", "Critical"
    val nutrientFocus: String,
    val keyAction: String,
    val risks: String
)

data class DynamicCareReminderItem(
    val id: String,
    val title: String,
    val category: String, // "IRRIGATION", "NUTRIENT", "PRUNING", "PROTECTION"
    val iconEmoji: String,
    val description: String,
    val priority: String, // "HIGH", "MEDIUM", "LOW"
    val climateReason: String,
    var isDone: Boolean = false
)

object PhenologyCalculator {

    fun getProfileForCrop(cropName: String, variety: String): CropPhenologyProfile {
        val lower = "$cropName $variety".lowercase()
        return when {
            lower.contains("chilli") || lower.contains("pepper") || lower.contains("capsicum") -> {
                CropPhenologyProfile(
                    speciesName = "Chilli / Pepper",
                    baseTemperatureC = 10.0f,
                    optimalTempMinC = 21.0f,
                    optimalTempMaxC = 29.0f,
                    standardDurationDays = 90,
                    totalGddRequired = 1350f,
                    stages = listOf(
                        PhenologyStageDef(CropPhenologyStage.GERMINATION, "Germination", "🌱", 10, 0.08f, "Moderate", "Warm seedbed, zero N", "Maintain warm rootzone 25°C", "Damping-off fungal root rot"),
                        PhenologyStageDef(CropPhenologyStage.EARLY_VEGETATIVE, "Seedling", "🌿", 18, 0.18f, "Moderate", "Balanced 19-19-19 NPK + Zinc", "Transplant & foliar zinc spray", "Thrips & whitefly vectors"),
                        PhenologyStageDef(CropPhenologyStage.RAPID_VEGETATIVE, "Vegetative", "☘️", 22, 0.25f, "High", "High Nitrogen & Magnesium", "Pinch early tip for bushy canopy", "Caterpillar defoliation"),
                        PhenologyStageDef(CropPhenologyStage.FLOWERING_ANTHESIS, "Flowering", "🌸", 16, 0.20f, "Critical", "Phosphorus & Boron", "Avoid overhead irrigation during bloom", "Blossom drop in heat >32°C"),
                        PhenologyStageDef(CropPhenologyStage.FRUIT_DEVELOPMENT, "Fruit Set", "🌶️", 16, 0.20f, "High", "High Potassium & Calcium", "Calcium foliar spray for wall thickness", "Sunscald on exposed pods"),
                        PhenologyStageDef(CropPhenologyStage.MATURATION_HARVEST, "Ripening", "🧺", 8, 0.09f, "Low", "Low N, K flush", "Harvest firm colored pods", "Post-harvest anthracnose")
                    )
                )
            }
            lower.contains("lettuce") || lower.contains("spinach") || lower.contains("greens") -> {
                CropPhenologyProfile(
                    speciesName = "Leafy Greens",
                    baseTemperatureC = 4.0f,
                    optimalTempMinC = 15.0f,
                    optimalTempMaxC = 22.0f,
                    standardDurationDays = 45,
                    totalGddRequired = 650f,
                    stages = listOf(
                        PhenologyStageDef(CropPhenologyStage.GERMINATION, "Germination", "🌱", 4, 0.08f, "Moderate", "Low EC water", "Provide cool moist cover", "Seed dormancy in high heat"),
                        PhenologyStageDef(CropPhenologyStage.EARLY_VEGETATIVE, "Cotyledon", "🌿", 10, 0.20f, "Moderate", "Dilute Calcium Nitrate", "Thin seedlings to 15cm", "Flea beetles"),
                        PhenologyStageDef(CropPhenologyStage.RAPID_VEGETATIVE, "Rosette / Head", "🥬", 20, 0.45f, "High", "High Nitrogen & Iron chelates", "Shade cloth if ambient >26°C", "Tipburn from Ca deficiency"),
                        PhenologyStageDef(CropPhenologyStage.MATURATION_HARVEST, "Harvest Window", "🧺", 11, 0.27f, "Moderate", "Clean water flush", "Harvest early morning for crispness", "Bolting & bitterness")
                    )
                )
            }
            lower.contains("cucumber") || lower.contains("gourd") || lower.contains("zucchini") -> {
                CropPhenologyProfile(
                    speciesName = "Cucumber",
                    baseTemperatureC = 12.0f,
                    optimalTempMinC = 22.0f,
                    optimalTempMaxC = 30.0f,
                    standardDurationDays = 58,
                    totalGddRequired = 950f,
                    stages = listOf(
                        PhenologyStageDef(CropPhenologyStage.GERMINATION, "Sprouting", "🌱", 5, 0.08f, "Moderate", "Warm moisture", "Keep temperature 26°C", "Pythium damping-off"),
                        PhenologyStageDef(CropPhenologyStage.EARLY_VEGETATIVE, "Vining", "🌿", 14, 0.22f, "Moderate", "High N & Phosphite", "Train main vine on vertical trellis", "Cucumber beetle vectors"),
                        PhenologyStageDef(CropPhenologyStage.FLOWERING_ANTHESIS, "Bloom & Pistillate", "🌸", 15, 0.28f, "High", "Potassium, Boron & Calcium", "Pollinator access or gynoecious care", "Downy mildew in high humidity"),
                        PhenologyStageDef(CropPhenologyStage.FRUIT_DEVELOPMENT, "Fruit Bulking", "🥒", 16, 0.30f, "Critical", "Potassium sulphate", "Consistent daily drip irrigation", "Crooked fruit from water stress"),
                        PhenologyStageDef(CropPhenologyStage.MATURATION_HARVEST, "Harvesting", "🧺", 8, 0.12f, "High", "Maintenance feed", "Pick every 2 days before seeds harden", "Over-mature yellowing")
                    )
                )
            }
            lower.contains("rice") || lower.contains("paddy") -> {
                CropPhenologyProfile(
                    speciesName = "Paddy Rice",
                    baseTemperatureC = 10.0f,
                    optimalTempMinC = 24.0f,
                    optimalTempMaxC = 32.0f,
                    standardDurationDays = 115,
                    totalGddRequired = 1850f,
                    stages = listOf(
                        PhenologyStageDef(CropPhenologyStage.GERMINATION, "Nursery", "🌱", 15, 0.12f, "High", "Basal P & organic manure", "Maintain 2cm standing nursery water", "Bakanae fungal disease"),
                        PhenologyStageDef(CropPhenologyStage.EARLY_VEGETATIVE, "Tillering", "🌿", 30, 0.28f, "High", "Urea top-dressing split 1", "Maintain saturated shallow flood", "Stem borer moth oviposition"),
                        PhenologyStageDef(CropPhenologyStage.RAPID_VEGETATIVE, "Panicle Initiation", "☘️", 25, 0.25f, "Critical", "Muriate of Potash + Urea", "Avoid water deficit at panicle primordium", "Rice blast lesions on foliage"),
                        PhenologyStageDef(CropPhenologyStage.FLOWERING_ANTHESIS, "Heading & Anthesis", "🌸", 15, 0.15f, "Critical", "Zinc sulphate foliar", "Ensure 5cm continuous water depth", "High heat spike pollen sterility"),
                        PhenologyStageDef(CropPhenologyStage.FRUIT_DEVELOPMENT, "Grain Milk & Dough", "🌾", 20, 0.15f, "Moderate", "No late nitrogen", "Gradual drain 10 days before maturity", "Brown planthopper infestation"),
                        PhenologyStageDef(CropPhenologyStage.MATURATION_HARVEST, "Golden Ripening", "🧺", 10, 0.05f, "Low", "Dry field", "Combine harvest at 20-22% moisture", "Grain shattering")
                    )
                )
            }
            lower.contains("strawberry") -> {
                CropPhenologyProfile(
                    speciesName = "Strawberry",
                    baseTemperatureC = 7.0f,
                    optimalTempMinC = 16.0f,
                    optimalTempMaxC = 24.0f,
                    standardDurationDays = 85,
                    totalGddRequired = 1100f,
                    stages = listOf(
                        PhenologyStageDef(CropPhenologyStage.GERMINATION, "Crown Anchorage", "🌱", 10, 0.10f, "Moderate", "Phosphorus root booster", "Bury crown at exact soil line", "Crown rot phytophthora"),
                        PhenologyStageDef(CropPhenologyStage.EARLY_VEGETATIVE, "Trifoliate Leaf", "🌿", 20, 0.22f, "Moderate", "Balanced 20-20-20 + Ca", "Remove early runner stolons", "Two-spotted spider mites"),
                        PhenologyStageDef(CropPhenologyStage.FLOWERING_ANTHESIS, "Inflorescence", "🌸", 20, 0.25f, "Moderate", "High Potassium & Boron", "Protect from frost & heavy rain", "Botrytis grey mould on petals"),
                        PhenologyStageDef(CropPhenologyStage.FRUIT_DEVELOPMENT, "Berry Swelling", "🍓", 25, 0.33f, "High", "Potassium Nitrate & Silica", "Straw mulch beneath fruit clusters", "Slug & snail damage"),
                        PhenologyStageDef(CropPhenologyStage.MATURATION_HARVEST, "Full Red Brix", "🧺", 10, 0.10f, "Moderate", "Flush with pure water", "Gently twist ripe berries with calyx", "Fruit bruising / leak")
                    )
                )
            }
            else -> {
                // Default: Tomato / Solanaceous crop
                CropPhenologyProfile(
                    speciesName = "Tomato (Solanum lycopersicum)",
                    baseTemperatureC = 10.0f,
                    optimalTempMinC = 20.0f,
                    optimalTempMaxC = 28.0f,
                    standardDurationDays = 80,
                    totalGddRequired = 1200f,
                    stages = listOf(
                        PhenologyStageDef(CropPhenologyStage.GERMINATION, "Germination", "🌱", 7, 0.07f, "Moderate", "Low EC water, root booster", "Warm soil bed (22-26°C)", "Damping-off fungal attack"),
                        PhenologyStageDef(CropPhenologyStage.EARLY_VEGETATIVE, "Seedling & Cotyledon", "🌿", 15, 0.18f, "Moderate", "Balanced N-P-K + Zinc", "Transplant & harden seedlings", "Transplant shock / Thrips"),
                        PhenologyStageDef(CropPhenologyStage.RAPID_VEGETATIVE, "Vegetative & Branching", "☘️", 22, 0.28f, "High", "High Nitrogen & Magnesium", "Support with trellis & prune suckers", "Nitrogen chlorosis & aphid colonies"),
                        PhenologyStageDef(CropPhenologyStage.FLOWERING_ANTHESIS, "Flowering & Truss Set", "🌸", 14, 0.20f, "Critical", "Phosphorus, Potassium & Boron", "Foliar Boron spray for pollen tube viability", "Blossom drop in heat >32°C"),
                        PhenologyStageDef(CropPhenologyStage.FRUIT_DEVELOPMENT, "Fruit Bulking & Color Break", "🍅", 16, 0.20f, "High", "Potassium & Calcium Nitrate", "Maintain steady soil moisture for Calcium uptake", "Blossom end rot & skin splitting"),
                        PhenologyStageDef(CropPhenologyStage.MATURATION_HARVEST, "Vine-Ripening & Harvest", "🧺", 6, 0.07f, "Low", "Potassium maintenance, flush", "Harvest at breaker or red-ripe stage", "Pre-harvest rain cracking & fruit flies")
                    )
                )
            }
        }
    }

    /**
     * Calculates days elapsed since planting date.
     */
    fun calculateDaysElapsed(dateStr: String): Int {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val plantedDate = format.parse(dateStr) ?: return 28
            val diffMillis = System.currentTimeMillis() - plantedDate.time
            max(1, (diffMillis / (1000 * 60 * 60 * 24)).toInt())
        } catch (e: Exception) {
            28
        }
    }

    /**
     * Determines current phenological stage index based on elapsed days.
     */
    fun getCurrentStageIndex(stages: List<PhenologyStageDef>, elapsedDays: Int): Pair<Int, Float> {
        var accumulatedDays = 0
        stages.forEachIndexed { index, stageDef ->
            val stageStart = accumulatedDays
            val stageEnd = accumulatedDays + stageDef.durationDays
            if (elapsedDays in stageStart until stageEnd) {
                val progressInStage = (elapsedDays - stageStart).toFloat() / stageDef.durationDays.toFloat()
                return Pair(index, progressInStage.coerceIn(0f, 1f))
            }
            accumulatedDays = stageEnd
        }
        return Pair(stages.size - 1, 1f)
    }

    /**
     * Calculates Climate Impact on Maturity Date using local temperature & GDD.
     */
    fun calculateClimateMaturityForecast(
        profile: CropPhenologyProfile,
        plantedDateStr: String,
        currentTempC: Float,
        currentHumidity: Float,
        simulatedTempDelta: Float = 0f
    ): ClimateMaturityResult {
        val effectiveTemp = currentTempC + simulatedTempDelta
        val baseTemp = profile.baseTemperatureC
        val optimalMin = profile.optimalTempMinC
        val optimalMax = profile.optimalTempMaxC
        
        // Standard expected GDD per day at reference 24°C
        val standardGddPerDay = max(1f, 24.0f - baseTemp)
        // Current local daily GDD accumulation
        val localGddPerDay = max(0.5f, effectiveTemp - baseTemp)

        // Development velocity factor
        val velocityFactor = when {
            effectiveTemp < baseTemp -> 0.4f
            effectiveTemp in optimalMin..optimalMax -> (localGddPerDay / standardGddPerDay).coerceIn(0.8f, 1.45f)
            effectiveTemp > optimalMax + 5f -> 0.85f // Extreme heat stress slows development
            else -> (localGddPerDay / standardGddPerDay).coerceIn(0.7f, 1.3f)
        }

        val elapsedDays = calculateDaysElapsed(plantedDateStr)
        val standardTotalDays = profile.standardDurationDays
        
        // Adjusted total days to maturity
        val adjustedTotalDays = (standardTotalDays / velocityFactor).roundToInt().coerceIn(standardTotalDays / 2, standardTotalDays * 2)
        val daysShift = standardTotalDays - adjustedTotalDays

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        val plantedDate = try {
            dateFormat.parse(plantedDateStr) ?: Date(System.currentTimeMillis() - (elapsedDays * 86400000L))
        } catch (e: Exception) {
            Date(System.currentTimeMillis() - (elapsedDays * 86400000L))
        }

        val calStd = Calendar.getInstance().apply {
            time = plantedDate
            add(Calendar.DAY_OF_YEAR, standardTotalDays)
        }

        val calAdj = Calendar.getInstance().apply {
            time = plantedDate
            add(Calendar.DAY_OF_YEAR, adjustedTotalDays)
        }

        val daysRemaining = max(0, adjustedTotalDays - elapsedDays)
        val accumulatedGdd = elapsedDays * localGddPerDay

        val climateEffectSummary = when {
            daysShift > 2 -> "⚡ Harvest Accelerated by +$daysShift Days (Optimal GDD accumulation rate at ${"%.1f".format(effectiveTemp)}°C)"
            daysShift < -2 -> "⏳ Harvest Delayed by ${-daysShift} Days (Sub-optimal thermal accumulation below ideal ${optimalMin}°C)"
            else -> "⚖️ On-track for standard schedule (Local temperature aligns with optimal growth curve)"
        }

        return ClimateMaturityResult(
            elapsedDays = elapsedDays,
            standardTotalDays = standardTotalDays,
            adjustedTotalDays = adjustedTotalDays,
            daysShift = daysShift,
            standardMaturityDate = displayFormat.format(calStd.time),
            climateAdjustedMaturityDate = displayFormat.format(calAdj.time),
            daysRemaining = daysRemaining,
            accumulatedGdd = accumulatedGdd,
            totalGddRequired = profile.totalGddRequired,
            velocityFactor = velocityFactor,
            climateEffectSummary = climateEffectSummary,
            effectiveTemp = effectiveTemp
        )
    }

    /**
     * Synthesizes actionable, climate-driven care reminders for the active stage.
     */
    fun generateDynamicCareReminders(
        profile: CropPhenologyProfile,
        stage: PhenologyStageDef,
        currentTempC: Float,
        currentHumidity: Float,
        soilMoisture: Float
    ): List<DynamicCareReminderItem> {
        val reminders = mutableListOf<DynamicCareReminderItem>()

        // 1. Irrigation Task
        val wateringAdvice = when {
            currentTempC >= 30f && stage.waterDemand in listOf("High", "Critical") -> {
                "High Transpiration Alert (${"%.1f".format(currentTempC)}°C): Apply 2.2L/m² daily irrigation via drip lines before 08:30 AM to prevent flower abortion and wilting."
            }
            currentTempC < 18f -> {
                "Cool Ambient Condition (${"%.1f".format(currentTempC)}°C): Reduce irrigation volume by 25% to avoid root hypoxia and cold fungal damping."
            }
            soilMoisture < 40f -> {
                "Low Substrate Moisture (${"%.0f".format(soilMoisture)}%): Initiate deep root soaking pulse to restore volumetric rootzone moisture to 60%."
            }
            else -> {
                "Standard ${stage.waterDemand} Water Demand: Maintain regular 1.2L daily drip fertigation cycle for active ${stage.name} stage."
            }
        }
        reminders.add(
            DynamicCareReminderItem(
                id = "rem_irr_${stage.stage.name}",
                title = "Smart Irrigation Scheduling",
                category = "IRRIGATION",
                iconEmoji = "💧",
                description = wateringAdvice,
                priority = if (currentTempC > 30f || soilMoisture < 35f) "HIGH" else "MEDIUM",
                climateReason = "Calculated from ${"%.1f".format(currentTempC)}°C ambient & ${"%.0f".format(soilMoisture)}% soil moisture"
            )
        )

        // 2. Fertigation / Nutrient Task
        reminders.add(
            DynamicCareReminderItem(
                id = "rem_nut_${stage.stage.name}",
                title = "Stage Nutrient Protocol: ${stage.nutrientFocus}",
                category = "NUTRIENT",
                iconEmoji = "🧪",
                description = "Apply recommended ${stage.nutrientFocus}. ${stage.keyAction}.",
                priority = if (stage.stage == CropPhenologyStage.FLOWERING_ANTHESIS || stage.stage == CropPhenologyStage.FRUIT_DEVELOPMENT) "HIGH" else "MEDIUM",
                climateReason = "Phenology requirement for ${stage.name} developmental phase"
            )
        )

        // 3. Canopy & Crop Management
        reminders.add(
            DynamicCareReminderItem(
                id = "rem_can_${stage.stage.name}",
                title = "Canopy & Agronomic Architecture",
                category = "PRUNING",
                iconEmoji = "✂️",
                description = stage.keyAction,
                priority = "MEDIUM",
                climateReason = "Optimizes light interception and vertical airflow in polyhouse"
            )
        )

        // 4. Disease / Climate Shielding Risk
        val diseaseAdvice = when {
            currentHumidity >= 75f -> {
                "High Foliar Humidity (${"%.0f".format(currentHumidity)}%): Risk of ${stage.risks}. Activate polyhouse side-ventilation and apply preventative bio-fungicide (Trichoderma)."
            }
            currentTempC >= 32f -> {
                "Thermal Stress Warning (${"%.1f".format(currentTempC)}°C): Deploy 35% aluminet shade screen to mitigate floral heat sterility."
            }
            else -> {
                "Scout Canopy: Monitor lower leaf undersides for ${stage.risks} under current ${"%.0f".format(currentHumidity)}% humidity."
            }
        }
        reminders.add(
            DynamicCareReminderItem(
                id = "rem_prot_${stage.stage.name}",
                title = "Foliar Protection & Climate Shield",
                category = "PROTECTION",
                iconEmoji = "🛡️",
                description = diseaseAdvice,
                priority = if (currentHumidity >= 75f || currentTempC >= 32f) "HIGH" else "LOW",
                climateReason = "Microclimate risk factor based on ${"%.0f".format(currentHumidity)}% RH & ${"%.1f".format(currentTempC)}°C"
            )
        )

        return reminders
    }
}

data class ClimateMaturityResult(
    val elapsedDays: Int,
    val standardTotalDays: Int,
    val adjustedTotalDays: Int,
    val daysShift: Int,
    val standardMaturityDate: String,
    val climateAdjustedMaturityDate: String,
    val daysRemaining: Int,
    val accumulatedGdd: Float,
    val totalGddRequired: Float,
    val velocityFactor: Float,
    val climateEffectSummary: String,
    val effectiveTemp: Float
)

/**
 * Visual Growth Stage Timeline & Climate-Adaptive Maturity Component.
 */
@Composable
fun PlantGrowthStageTimelineComponent(
    plant: Plant,
    viewModel: AgroViewModel,
    modifier: Modifier = Modifier
) {
    val liveWeather by viewModel.liveWeather.collectAsState()
    val sensorData by viewModel.latestSensorData.collectAsState()

    // Determine environmental parameters
    val currentTemp = liveWeather?.temperatureCelsius ?: sensorData?.temperature ?: 27.5f
    val currentHumidity = liveWeather?.relativeHumidity ?: sensorData?.humidity ?: 64.0f
    val currentSoilMoisture = sensorData?.soilMoisture ?: 48.0f

    // Interactive What-If Climate Simulation State
    var simulatedTempDelta by remember { mutableFloatStateOf(0f) }
    var showSimulationPanel by remember { mutableStateOf(false) }

    // Resolve Crop Profile & Timeline Calculation
    val cropProfile = remember(plant.name, plant.variety) {
        PhenologyCalculator.getProfileForCrop(plant.name, plant.variety)
    }

    val maturityResult = remember(cropProfile, plant.plantedDate, currentTemp, currentHumidity, simulatedTempDelta) {
        PhenologyCalculator.calculateClimateMaturityForecast(
            profile = cropProfile,
            plantedDateStr = plant.plantedDate,
            currentTempC = currentTemp,
            currentHumidity = currentHumidity,
            simulatedTempDelta = simulatedTempDelta
        )
    }

    val (currentStageIdx, currentStageProgress) = remember(cropProfile.stages, maturityResult.elapsedDays) {
        PhenologyCalculator.getCurrentStageIndex(cropProfile.stages, maturityResult.elapsedDays)
    }

    var selectedStageIdx by remember(currentStageIdx) { mutableIntStateOf(currentStageIdx) }
    val activeDisplayStage = cropProfile.stages.getOrElse(selectedStageIdx) { cropProfile.stages.first() }

    // Dynamic Care Reminders State
    val initialReminders = remember(cropProfile, activeDisplayStage, currentTemp, currentHumidity, currentSoilMoisture) {
        PhenologyCalculator.generateDynamicCareReminders(
            profile = cropProfile,
            stage = activeDisplayStage,
            currentTempC = currentTemp + simulatedTempDelta,
            currentHumidity = currentHumidity,
            soilMoisture = currentSoilMoisture
        )
    }

    var careReminders by remember(initialReminders) { mutableStateOf(initialReminders) }
    var reminderFeedbackMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        color = CardSurfaceDark,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SoftGrass),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // 1. HEADER: Title, Crop Species & Climate Badge
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PHENOLOGICAL GROWTH TIMELINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingEmerald,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = SoftGrass,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(GlowingEmerald, CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Climate-Calibrated", color = GlowingEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = "${cropProfile.speciesName} • Day ${maturityResult.elapsedDays}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                }

                IconButton(
                    onClick = { showSimulationPanel = !showSimulationPanel },
                    modifier = Modifier.testTag("btn_toggle_climate_sim")
                ) {
                    Icon(
                        imageVector = if (showSimulationPanel) Icons.Filled.Tune else Icons.Outlined.Tune,
                        contentDescription = "Simulate Climate",
                        tint = if (showSimulationPanel) GlowingEmerald else TextMuted
                    )
                }
            }

            // ==========================================
            // 2. WHAT-IF CLIMATE SIMULATION SLIDER PANEL (Expandable)
            // ==========================================
            AnimatedVisibility(visible = showSimulationPanel) {
                Surface(
                    color = DeepForestBg,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GlowingEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🌡️ What-If Climate Simulation",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                            Text(
                                text = "${if (simulatedTempDelta > 0) "+" else ""}${"%.1f".format(simulatedTempDelta)}°C (${"%.1f".format(maturityResult.effectiveTemp)}°C total)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (simulatedTempDelta > 0) AlertWarning else if (simulatedTempDelta < 0) SoilMoistureBlue else GlowingEmerald
                            )
                        }

                        Text(
                            text = "Slide to simulate microclimate shifts (heatwave or cold spell) on phenological velocity and maturity timeline:",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Slider(
                            value = simulatedTempDelta,
                            onValueChange = { simulatedTempDelta = it },
                            valueRange = -6.0f..6.0f,
                            steps = 24,
                            colors = SliderDefaults.colors(
                                thumbColor = GlowingEmerald,
                                activeTrackColor = GlowingEmerald,
                                inactiveTrackColor = SoftGrass
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("-6°C Cold Snap", fontSize = 10.sp, color = TextMuted)
                            TextButton(
                                onClick = { simulatedTempDelta = 0f },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Reset to Real Weather", fontSize = 10.sp, color = GlowingEmerald, fontWeight = FontWeight.Bold)
                            }
                            Text("+6°C Heatwave", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }

            // ==========================================
            // 3. MATURITY FORECAST & GDD METRICS CARD
            // ==========================================
            Surface(
                color = DeepForestBg,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SoftGrass),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "EXPECTED HARVEST DATE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = maturityResult.climateAdjustedMaturityDate,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = GlowingEmerald
                            )
                        }

                        Surface(
                            color = if (maturityResult.daysShift > 0) SoftGrass else if (maturityResult.daysShift < 0) SoilBg else TempBg,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "${maturityResult.daysRemaining} Days Left",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (maturityResult.daysShift > 0) GlowingEmerald else TextLight
                                )
                                Text(
                                    text = "Std: ${maturityResult.standardMaturityDate}",
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    // Progress bar for total lifecycle
                    val overallProgress = (maturityResult.elapsedDays.toFloat() / maturityResult.adjustedTotalDays.toFloat()).coerceIn(0f, 1f)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Lifecycle Progress: ${(overallProgress * 100).roundToInt()}%",
                                fontSize = 11.sp,
                                color = TextLight,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${maturityResult.elapsedDays} / ${maturityResult.adjustedTotalDays} days",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = GlowingEmerald,
                            trackColor = SoftGrass
                        )
                    }

                    // GDD Accumulation & Climate Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = CardSurfaceDark,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SoftGrass),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Thermal Heat Units (GDD)", fontSize = 9.sp, color = TextMuted)
                                Text(
                                    text = "${maturityResult.accumulatedGdd.roundToInt()} / ${maturityResult.totalGddRequired.roundToInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextLight
                                )
                            }
                        }

                        Surface(
                            color = CardSurfaceDark,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SoftGrass),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Dev Velocity Index", fontSize = 9.sp, color = TextMuted)
                                Text(
                                    text = "${"%.2f".format(maturityResult.velocityFactor)}x Rate",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (maturityResult.velocityFactor >= 1.0f) GlowingEmerald else AlertWarning
                                )
                            }
                        }
                    }

                    // Climate effect banner
                    Surface(
                        color = if (maturityResult.daysShift > 0) SoftGrass else DeepForestBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (maturityResult.daysShift > 0) Icons.Filled.Bolt else Icons.Filled.Info,
                                contentDescription = null,
                                tint = GlowingEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = maturityResult.climateEffectSummary,
                                fontSize = 10.sp,
                                color = TextLight,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 4. HORIZONTAL GROWTH STAGE STEPPER TIMELINE
            // ==========================================
            Text(
                text = "PHENOLOGY STAGES (Tap to inspect care protocols)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GlowingEmerald,
                letterSpacing = 0.8.sp
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("growth_stages_stepper_row"),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(cropProfile.stages) { index, stageDef ->
                    val isPast = index < currentStageIdx
                    val isCurrent = index == currentStageIdx
                    val isSelected = index == selectedStageIdx

                    val nodeBorderColor = when {
                        isSelected -> GlowingEmerald
                        isCurrent -> LeafGreen
                        isPast -> SoftGrass
                        else -> SoftGrass.copy(alpha = 0.5f)
                    }

                    val nodeBgColor = when {
                        isSelected -> GlowingEmerald.copy(alpha = 0.12f)
                        isCurrent -> SoftGrass
                        isPast -> DeepForestBg
                        else -> DeepForestBg.copy(alpha = 0.5f)
                    }

                    Surface(
                        color = nodeBgColor,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(if (isSelected || isCurrent) 1.5.dp else 1.dp, nodeBorderColor),
                        modifier = Modifier
                            .width(132.dp)
                            .clickable { selectedStageIdx = index }
                            .testTag("stage_node_$index")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stageDef.emoji, fontSize = 20.sp)
                                if (isPast) {
                                    Surface(
                                        color = SoftGrass,
                                        shape = CircleShape,
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(Icons.Filled.Check, null, tint = GlowingEmerald, modifier = Modifier.padding(3.dp))
                                    }
                                } else if (isCurrent) {
                                    Surface(
                                        color = GlowingEmerald,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Text("~${stageDef.durationDays}d", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = stageDef.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (isCurrent) {
                                LinearProgressIndicator(
                                    progress = { currentStageProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = GlowingEmerald,
                                    trackColor = SoftGrass
                                )
                                Text(
                                    text = "${(currentStageProgress * 100).roundToInt()}% done",
                                    fontSize = 9.sp,
                                    color = GlowingEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Water: ${stageDef.waterDemand}",
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 5. SELECTED STAGE DETAIL & AGRONOMIC FOCUS
            // ==========================================
            Surface(
                color = DeepForestBg,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SoftGrass),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(activeDisplayStage.emoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${activeDisplayStage.name} (Duration: ~${activeDisplayStage.durationDays} days)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                        }
                        if (selectedStageIdx == currentStageIdx) {
                            Surface(
                                color = SoftGrass,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Current Stage",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlowingEmerald,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Biological parameters grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("💧 Water Demand", activeDisplayStage.waterDemand, if (activeDisplayStage.waterDemand == "Critical") AlertCritical else GlowingEmerald),
                            Triple("🧪 Nutrient Focus", activeDisplayStage.nutrientFocus, TextLight),
                            Triple("⚠️ Primary Risk", activeDisplayStage.risks, AlertWarning)
                        ).forEach { (label, value, col) ->
                            Surface(
                                color = CardSurfaceDark,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, SoftGrass),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text(label, fontSize = 8.sp, color = TextMuted, maxLines = 1)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = col, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 6. CLIMATE-ADAPTIVE CARE REMINDERS SECTION
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DYNAMIC CLIMATE CARE REMINDERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlowingEmerald,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Synchronized with current ${"%.1f".format(currentTemp + simulatedTempDelta)}°C microclimate & ${activeDisplayStage.name}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                TextButton(
                    onClick = {
                        careReminders = careReminders.map { it.copy(isDone = true) }
                        reminderFeedbackMessage = "All care tasks marked completed!"
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Complete All", fontSize = 11.sp, color = GlowingEmerald, fontWeight = FontWeight.Bold)
                }
            }

            // Reminder Feedback Banner if set
            AnimatedVisibility(visible = reminderFeedbackMessage != null) {
                Surface(
                    color = SoftGrass,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(reminderFeedbackMessage ?: "", fontSize = 11.sp, color = GlowingEmerald, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { reminderFeedbackMessage = null },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(Icons.Filled.Close, null, tint = GlowingEmerald, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // List of Reminders
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                careReminders.forEachIndexed { index, reminder ->
                    Surface(
                        color = if (reminder.isDone) DeepForestBg.copy(alpha = 0.6f) else DeepForestBg,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (reminder.isDone) SoftGrass else if (reminder.priority == "HIGH") AlertWarning.copy(alpha = 0.6f) else SoftGrass
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updated = careReminders.toMutableList()
                                updated[index] = reminder.copy(isDone = !reminder.isDone)
                                careReminders = updated
                            }
                            .testTag("care_reminder_item_$index")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Checkbox(
                                checked = reminder.isDone,
                                onCheckedChange = { checked ->
                                    val updated = careReminders.toMutableList()
                                    updated[index] = reminder.copy(isDone = checked)
                                    careReminders = updated
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = GlowingEmerald,
                                    uncheckedColor = TextMuted
                                ),
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(reminder.iconEmoji, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = reminder.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (reminder.isDone) TextMuted else TextLight
                                        )
                                    }

                                    Surface(
                                        color = if (reminder.priority == "HIGH") AlertWarning.copy(alpha = 0.15f) else SoftGrass,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = reminder.priority,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (reminder.priority == "HIGH") AlertWarning else GlowingEmerald,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = reminder.description,
                                    fontSize = 11.sp,
                                    color = if (reminder.isDone) TextMuted else TextLight,
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "📊 ${reminder.climateReason}",
                                    fontSize = 9.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Sync to Calendar Notification Button
            OutlinedButton(
                onClick = {
                    viewModel.applyClimateCareToActiveTasks()
                    reminderFeedbackMessage = "Care reminders synchronized with system push notifications & farm calendar!"
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GlowingEmerald),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_sync_care_calendar")
            ) {
                Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = GlowingEmerald, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sync Reminders to Farm Calendar & Push Alerts",
                    color = GlowingEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
