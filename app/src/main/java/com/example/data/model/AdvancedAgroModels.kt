package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

/**
 * Supported imaging modalities for multimodal plant sensing.
 */
enum class ImagingModality(
    val title: String,
    val wavelengthRange: String,
    val description: String,
    val primaryUse: String
) {
    RGB(
        title = "RGB Standard",
        wavelengthRange = "400 - 700 nm",
        description = "True-color visible spectrum for visible chlorosis, necrosis, and morphological spotting.",
        primaryUse = "Visible Symptom Detection"
    ),
    NIR(
        title = "NIR (Near-Infrared)",
        wavelengthRange = "700 - 1100 nm",
        description = "High mesophyll cell reflectance to reveal internal leaf structure breakdown before surface symptoms.",
        primaryUse = "Cellular Structural Health"
    ),
    MULTISPECTRAL(
        title = "Multispectral",
        wavelengthRange = "Green, Red, RedEdge, NIR",
        description = "Calibrated discrete wavebands for vegetation indices (NDVI, NDRE, VARI).",
        primaryUse = "Vegetation Index Analytics"
    ),
    HYPERSPECTRAL(
        title = "Hyperspectral",
        wavelengthRange = "380 - 1050 nm (Continuous)",
        description = "Continuous narrow spectral bands detecting micro-color shifts & biochemical absorption signatures.",
        primaryUse = "Pre-Symptomatic Signature Matching"
    ),
    THERMAL(
        title = "Thermal Infrared (TIR)",
        wavelengthRange = "8 - 14 µm (Long-Wave)",
        description = "Canopy and leaf surface temperature tracking stomatal closure and transpiration cooling.",
        primaryUse = "Stomatal Conductance & Water Stress"
    ),
    FLUORESCENCE(
        title = "Chlorophyll Fluorescence",
        wavelengthRange = "680 - 740 nm (Fv/Fm)",
        description = "Photochemical quantum yield of Photosystem II measuring light reaction stress.",
        primaryUse = "Photosynthetic Efficiency"
    )
}

/**
 * Major crop species profiles for Cross-Crop Intelligence.
 */
enum class CropSpecies(
    val commonName: String,
    val scientificName: String,
    val iconEmoji: String,
    val optimalTempMin: Float,
    val optimalTempMax: Float,
    val optimalSoilMoistureMin: Float,
    val optimalSoilMoistureMax: Float,
    val optimalVpdMin: Float,
    val optimalVpdMax: Float,
    val primaryStressRisks: List<String>
) {
    TOMATO("Tomato", "Solanum lycopersicum", "🍅", 20f, 27f, 40f, 65f, 0.8f, 1.2f, listOf("Early Blight", "Blossom End Rot", "Bacterial Canker", "Heat Stomatal Stress")),
    CHILLI("Chilli / Pepper", "Capsicum annuum", "🌶️", 22f, 30f, 35f, 60f, 0.9f, 1.4f, listOf("Anthracnose", "Mite Herbivory", "Powdery Mildew", "Drought Wilt")),
    RICE("Paddy Rice", "Oryza sativa", "🌾", 24f, 32f, 65f, 95f, 0.5f, 1.0f, listOf("Blast Disease", "Bacterial Leaf Streak", "Salinity Osmotic Stress", "Nitrogen Deficiency")),
    POTATO("Potato", "Solanum tuberosum", "🥔", 15f, 22f, 45f, 70f, 0.7f, 1.1f, listOf("Late Blight", "Potato Virus Y", "Heat Tuber Stress", "Phosphorus Stress")),
    MAIZE("Maize / Corn", "Zea mays", "🌽", 20f, 32f, 40f, 65f, 0.8f, 1.5f, listOf("Fall Armyworm", "Northern Corn Leaf Blight", "Nitrogen Chlorosis", "Drought Stress")),
    BEANS("Legume / Beans", "Phaseolus vulgaris", "🫘", 18f, 26f, 35f, 55f, 0.8f, 1.2f, listOf("Rust Disease", "Root Rot", "Rhizobia Nodulation Stress", "Iron Deficiency")),
    BANANA("Banana", "Musa acuminata", "🍌", 26f, 34f, 60f, 80f, 0.7f, 1.3f, listOf("Panama Disease (Fusarium)", "Black Sigatoka", "Potassium Depletion", "Cold Shock")),
    MANGO("Mango Canopy", "Mangifera indica", "🥭", 24f, 35f, 30f, 55f, 1.0f, 1.8f, listOf("Anthracnose Foliar Blight", "Powdery Mildew", "Zinc Deficiency", "Salt Burn")),
    GRAPES("Grapes / Vineyard", "Vitis vinifera", "🍇", 18f, 28f, 30f, 50f, 0.9f, 1.5f, listOf("Downy Mildew", "Grapevine Leafroll", "Water Deficit Defoliation", "Magnesium Chlorosis")),
    STRAWBERRY("Strawberry", "Fragaria ananassa", "🍓", 16f, 24f, 40f, 60f, 0.6f, 1.1f, listOf("Botrytis Grey Mould", "Spider Mite Scorching", "Calcium Tip Burn", "Heat Stress"))
}

/**
 * Plant phenological growth stages for Growth-Stage-Aware AI inference.
 */
enum class PlantGrowthStage(
    val stageName: String,
    val iconEmoji: String,
    val physiologicalFocus: String,
    val vulnerabilityNotes: String
) {
    SEEDLING(
        stageName = "Seedling / Emergence",
        iconEmoji = "🌱",
        physiologicalFocus = "Root establishment, cotyledon vigor, high damping-off vulnerability.",
        vulnerabilityNotes = "High sensitivity to waterlogging and salinity shock."
    ),
    VEGETATIVE(
        stageName = "Vegetative Growth",
        iconEmoji = "🌿",
        physiologicalFocus = "Canopy expansion, chlorophyll synthesis, rapid nitrogen uptake.",
        vulnerabilityNotes = "Susceptible to foliar nitrogen chlorosis and chewing pests."
    ),
    FLOWERING(
        stageName = "Flowering / Anthesis",
        iconEmoji = "🌸",
        physiologicalFocus = "Pollen viability, high phosphorus/potassium flux, stomatal sensitivity.",
        vulnerabilityNotes = "Extreme vulnerability to heat stress (>32°C causes flower drop)."
    ),
    FRUITING(
        stageName = "Fruit / Grain Filling",
        iconEmoji = "🍅",
        physiologicalFocus = "Sink-source carbohydrate translocation, calcium mobilization.",
        vulnerabilityNotes = "High water demand; fluctuations trigger blossom-end rot and cracking."
    ),
    MATURATION(
        stageName = "Maturation / Harvest",
        iconEmoji = "🌾",
        physiologicalFocus = "Programmed leaf senescence, sugar accumulation, desiccation.",
        vulnerabilityNotes = "Natural yellowing must not be confused with pathological chlorosis."
    )
}

/**
 * Multi-stress multi-label breakdown probabilities (0 - 100%).
 */
data class MultiStressBreakdown(
    val waterStressProb: Int = 18,
    val nutrientStressProb: Int = 22,
    val pathogenDiseaseProb: Int = 12,
    val pestHerbivoryProb: Int = 8,
    val heatStressProb: Int = 15,
    val coldStressProb: Int = 5,
    val lightPhotoinhibitionProb: Int = 10,
    val salinityOsmoticProb: Int = 6,
    val physiologicalSenescenceProb: Int = 4,
    val dominantStressType: String = "Balanced Growth",
    val primaryConfidence: Int = 92
)

/**
 * Micro-Color and Spectral indices calculated from optical sensing.
 */
data class SpectralVegetationIndices(
    val ndvi: Float = 0.78f,        // Normalized Difference Vegetation Index: 0.2 to 0.9
    val ndre: Float = 0.62f,        // Normalized Difference Red Edge (chlorophyll density): 0.1 to 0.8
    val vari: Float = 0.45f,        // Visual Atmospheric Resistance Index (RGB visible greenness)
    val sipi: Float = 1.12f,        // Structure Insensitive Pigment Index (Carotenoid/Chlorophyll a)
    val pri: Float = 0.04f,         // Photochemical Reflectance Index (Photosynthetic light efficiency)
    val redEdgeInflectionWavelength: Float = 708.5f, // nm
    val microColorShiftDetected: Boolean = false,
    val shiftMagnitudePercent: Float = 3.2f
)

/**
 * Plant Physiology and Biotechnology biochemical estimates.
 */
data class PlantPhysiologyVitals(
    val chlorophyllSpad: Float = 46.2f,        // SPAD units (30 - 65 optimal)
    val chlorophyllContentUgCm2: Float = 38.5f,// µg/cm²
    val carotenoidIndex: Float = 0.28f,        // Stress photoprotection pigment ratio
    val anthocyaninIndex: Float = 0.15f,       // Cold / UV / phosphate stress pigment
    val photosyntheticEfficiencyFvFm: Float = 0.81f, // Optimal ~0.83, < 0.70 is stressed
    val relativeWaterContentRwc: Float = 84.5f,// % (Optimal 80-90%)
    val nitrogenBalanceIndexNbi: Float = 1.42f // NBI ratio
)

/**
 * Thermal Stress and Stomatal Transpiration Conductance telemetry.
 */
data class ThermalConductanceVitals(
    val leafTemperatureC: Float = 23.8f,
    val ambientAirTemperatureC: Float = 24.5f,
    val deltaTC: Float = -0.7f,                // T_leaf - T_air (negative indicates active transpiration cooling)
    val cwsi: Float = 0.18f,                   // Crop Water Stress Index (0.0 to 1.0)
    val stomatalStatus: String = "Open (Active Transpiration)",
    val transpirationRateMmolM2S: Float = 4.2f
)

/**
 * Explainable AI (XAI) feature attribution breakdown (e.g. SHAP / Integrated Gradients).
 */
data class XaiFeatureAttribution(
    val featureName: String,
    val contributionPercent: Int,
    val description: String
)

/**
 * 7-Day Stress Progression & Trajectory forecast.
 */
data class StressTrajectoryForecast(
    val day0Health: Int = 84,
    val day1Health: Int = 81,
    val day3Health: Int = 74,
    val day5Health: Int = 63,
    val day7Health: Int = 51,
    val trajectoryRiskLevel: String = "Moderate Progression Risk", // Low, Moderate, High, Critical
    val actionableInterventionWindowHours: Int = 48,
    val forecastSummary: String = "Stress progression detected. Estimated risk of acute foliar chlorosis in 3-5 days if irrigation is unadjusted."
)

/**
 * Digital Plant Twin representation.
 */
data class DigitalPlantTwin(
    val plantId: Long,
    val plantTagId: String = "T-102",
    val cropSpecies: CropSpecies = CropSpecies.TOMATO,
    val growthStage: PlantGrowthStage = PlantGrowthStage.VEGETATIVE,
    val ageDays: Int = 38,
    val ashiScore: Int = 82, // AgroSense Health Index (0 - 100)
    val healthTier: AshiHealthTier = AshiHealthTier.HEALTHY,
    val multiStressBreakdown: MultiStressBreakdown = MultiStressBreakdown(),
    val spectralIndices: SpectralVegetationIndices = SpectralVegetationIndices(),
    val physiologyVitals: PlantPhysiologyVitals = PlantPhysiologyVitals(),
    val thermalVitals: ThermalConductanceVitals = ThermalConductanceVitals(),
    val trajectoryForecast: StressTrajectoryForecast = StressTrajectoryForecast(),
    val historicalHealthPoints: List<Int> = listOf(94, 91, 88, 86, 82),
    val lastScanModality: ImagingModality = ImagingModality.MULTISPECTRAL,
    val lastUpdateTimestamp: Long = System.currentTimeMillis(),
    val zoneLocation: String = "Polyhouse A - Bed 3"
)

/**
 * AgroSense Health Index (ASHI) Tiers.
 */
enum class AshiHealthTier(
    val label: String,
    val minScore: Int,
    val maxScore: Int,
    val color: Color,
    val statusSummary: String
) {
    EXCELLENT("Excellent", 90, 100, GlowingEmerald, "Peak physiological vitality with optimal photosynthetic rate."),
    HEALTHY("Healthy", 75, 89, Color(0xFF66BB6A), "Normal physiological parameters with stable transpiration."),
    MODERATE("Moderate Warning", 50, 74, AlertWarning, "Early pre-symptomatic stress detected. Countermeasures recommended."),
    HIGH_RISK("High Risk", 25, 49, Color(0xFFFF5722), "Acute physiological stress. Visible chlorosis or wilting imminent."),
    CRITICAL("Critical Failure", 0, 24, AlertCritical, "Severe cellular breakdown or pathogen necrosis requiring immediate rescue.")
}

/**
 * AI + Biochemical Confirmation Laboratory Test Workflow entity.
 */
data class BiochemicalConfirmationTest(
    val id: String,
    val plantId: Long,
    val plantTag: String,
    val cropName: String,
    val aiSuspectedIssue: String,
    val aiConfidence: Int,
    val recommendedTestType: BiochemicalTestType,
    val testStatus: LabTestStatus = LabTestStatus.PENDING_SAMPLE,
    val labResultSummary: String = "",
    val resultMatchedAiDiagnosis: Boolean? = null,
    val dateRequested: Long = System.currentTimeMillis(),
    val dateCompleted: Long? = null,
    val laboratoryNotes: String = ""
)

enum class BiochemicalTestType(val title: String, val method: String, val turnaround: String) {
    PCR_DIAGNOSTIC("Foliar Pathogen RT-PCR", "DNA/RNA Amplification for viral & fungal strains", "4 - 8 Hours"),
    ELISA_ASSAY("Enzyme-Linked Immunosorbent Assay", "Serological viral & bacterial protein binding", "12 - 24 Hours"),
    SPAD_CHLOROPHYLL("SPAD-502 Laboratory Optical Meter", "Transmittance at 650nm & 940nm", "Instant (Field Lab)"),
    LEAF_SAP_NPK("Leaf Sap & Soil Nutrient ICP-OES", "Elemental spectrometry (N, P, K, Ca, Mg, Fe)", "24 - 48 Hours"),
    STOMATAL_MICROSCOPY("Nail Polish Stomatal Impression Microscopy", "Optical aperture ratio measurement", "2 - 4 Hours"),
    SOIL_EC_PH_ANALYSIS("1:2 Saturated Paste Soil Extract", "Conductivity & Hydrogen ion ionometry", "1 Hour")
}

enum class LabTestStatus(val label: String, val color: Color) {
    PENDING_SAMPLE("Sample Pending", AlertWarning),
    IN_ANALYSIS("Under Laboratory Analysis", Color(0xFF29B6F6)),
    CONFIRMED_MATCH("Confirmed (AI Matched)", GlowingEmerald),
    DISCREPANCY_CALIBRATED("Discrepancy (Model Calibrated)", Color(0xFFAB47BC))
}

/**
 * Farm Stress Heatmap Node for geospatial canopy visualization.
 */
data class FarmHeatmapNode(
    val nodeId: String,
    val row: Int,
    val col: Int,
    val plantTag: String,
    val cropSpecies: CropSpecies,
    val ashiHealthScore: Int,
    val dominantStress: String,
    val leafTempC: Float,
    val soilMoisturePercent: Float,
    val gpsLat: Double,
    val gpsLng: Double,
    val zone: String
)
