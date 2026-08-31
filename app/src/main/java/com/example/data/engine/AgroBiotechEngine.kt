package com.example.data.engine

import com.example.data.model.*
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * AgroBiotechEngine: Core multimodal synthesis & biotechnology analytics engine
 * for AgroSense-X.
 */
object AgroBiotechEngine {

    /**
     * Compute Vapor Pressure Deficit (VPD) in kPa given temperature (°C) and relative humidity (%).
     */
    fun calculateVpd(tempC: Float, relativeHumidityPercent: Float): Float {
        // Tetens equation for saturation vapor pressure (es in kPa)
        val es = 0.61078f * exp((17.27f * tempC) / (tempC + 237.3f))
        val ea = es * (relativeHumidityPercent / 100f)
        val vpd = es - ea
        return max(0.1f, (vpd * 100).roundToInt() / 100f)
    }

    /**
     * Compute Crop Water Stress Index (CWSI) from leaf and air temperatures.
     */
    fun calculateCwsi(
        leafTempC: Float,
        airTempC: Float,
        vpdKpa: Float
    ): Float {
        val deltaT = leafTempC - airTempC
        // Non-water-stressed baseline lower limit
        val lowerLimit = -2.5f + (0.5f * vpdKpa)
        // Completely stressed upper limit
        val upperLimit = 4.0f
        val cwsiRaw = (deltaT - lowerLimit) / (upperLimit - lowerLimit)
        return cwsiRaw.coerceIn(0.0f, 1.0f)
    }

    /**
     * Run full Multimodal Data Fusion to produce an integrated Digital Plant Twin state.
     */
    fun runMultimodalFusion(
        species: CropSpecies,
        stage: PlantGrowthStage,
        ageDays: Int,
        imagingModality: ImagingModality,
        sensorTempC: Float,
        sensorHumidity: Float,
        sensorSoilMoisture: Float,
        visualHealthScore: Int,
        simulatedLeafTemp: Float? = null
    ): DigitalPlantTwin {
        val vpd = calculateVpd(sensorTempC, sensorHumidity)
        
        // 1. Calculate Thermal & Stomatal Conductance
        val leafTemp = simulatedLeafTemp ?: run {
            // Leaf temperature is normally 1-3°C below air temp when well watered (transpiration cooling)
            val moistureDeficit = max(0f, 45f - sensorSoilMoisture) / 45f
            sensorTempC - 1.8f + (moistureDeficit * 3.5f)
        }
        val deltaT = leafTemp - sensorTempC
        val cwsi = calculateCwsi(leafTemp, sensorTempC, vpd)
        val stomatalStatus = when {
            cwsi < 0.25f -> "Open (Active Transpiration Cooling)"
            cwsi < 0.60f -> "Partially Restricted (Early Water Conservation)"
            else -> "Closed / Impaired (High Transpirational Stress)"
        }
        val transpirationRate = max(0.4f, 5.0f - (cwsi * 4.2f))

        val thermalVitals = ThermalConductanceVitals(
            leafTemperatureC = (leafTemp * 10).roundToInt() / 10f,
            ambientAirTemperatureC = (sensorTempC * 10).roundToInt() / 10f,
            deltaTC = (deltaT * 10).roundToInt() / 10f,
            cwsi = (cwsi * 100).roundToInt() / 100f,
            stomatalStatus = stomatalStatus,
            transpirationRateMmolM2S = (transpirationRate * 10).roundToInt() / 10f
        )

        // 2. Calculate Spectral Indices (Adjusted by Modality & Growth Stage)
        val baseNdvi = (visualHealthScore / 100f * 0.45f + 0.42f).coerceIn(0.20f, 0.92f)
        val ndre = (baseNdvi * 0.82f).coerceIn(0.15f, 0.78f)
        val vari = (baseNdvi * 0.58f).coerceIn(0.10f, 0.65f)
        val sipi = 1.0f + ((1f - baseNdvi) * 0.4f)
        val pri = 0.05f - (cwsi * 0.06f)
        val redEdgeShift = 705f + (baseNdvi * 12f)
        val microShiftDetected = (1f - baseNdvi) > 0.18f || cwsi > 0.35f

        val spectralIndices = SpectralVegetationIndices(
            ndvi = (baseNdvi * 100).roundToInt() / 100f,
            ndre = (ndre * 100).roundToInt() / 100f,
            vari = (vari * 100).roundToInt() / 100f,
            sipi = (sipi * 100).roundToInt() / 100f,
            pri = (pri * 1000).roundToInt() / 1000f,
            redEdgeInflectionWavelength = (redEdgeShift * 10).roundToInt() / 10f,
            microColorShiftDetected = microShiftDetected,
            shiftMagnitudePercent = ((1f - baseNdvi) * 20f).coerceIn(1.0f, 35.0f)
        )

        // 3. Estimate Plant Physiology Vitals
        val chlorophyllSpad = (baseNdvi * 62f).coerceIn(18f, 68f)
        val chlorophyllUgCm2 = (chlorophyllSpad * 0.85f).coerceIn(14f, 58f)
        val carotenoidIdx = 0.20f + ((1f - baseNdvi) * 0.25f)
        val anthocyaninIdx = if (sensorTempC < 16f || cwsi > 0.5f) 0.38f else 0.12f
        val fvFm = (0.83f - (cwsi * 0.18f) - ((1f - baseNdvi) * 0.12f)).coerceIn(0.45f, 0.84f)
        val rwc = (92f - (cwsi * 35f)).coerceIn(52f, 96f)
        val nbi = (chlorophyllSpad / (10f + (anthocyaninIdx * 20f))).coerceIn(0.5f, 3.2f)

        val physiologyVitals = PlantPhysiologyVitals(
            chlorophyllSpad = (chlorophyllSpad * 10).roundToInt() / 10f,
            chlorophyllContentUgCm2 = (chlorophyllUgCm2 * 10).roundToInt() / 10f,
            carotenoidIndex = (carotenoidIdx * 100).roundToInt() / 100f,
            anthocyaninIndex = (anthocyaninIdx * 100).roundToInt() / 100f,
            photosyntheticEfficiencyFvFm = (fvFm * 100).roundToInt() / 100f,
            relativeWaterContentRwc = (rwc * 10).roundToInt() / 10f,
            nitrogenBalanceIndexNbi = (nbi * 100).roundToInt() / 100f
        )

        // 4. Multi-Stress Multi-Label Risk Estimation
        val waterStressRaw = (cwsi * 85f + (if (sensorSoilMoisture < species.optimalSoilMoistureMin) 30f else 0f)).coerceIn(5f, 95f).roundToInt()
        val heatStressRaw = (if (sensorTempC > species.optimalTempMax) (sensorTempC - species.optimalTempMax) * 12f else 5f).coerceIn(5f, 90f).roundToInt()
        val coldStressRaw = (if (sensorTempC < species.optimalTempMin) (species.optimalTempMin - sensorTempC) * 14f else 4f).coerceIn(4f, 85f).roundToInt()
        val nutrientStressRaw = (if (chlorophyllSpad < 35f) (35f - chlorophyllSpad) * 2.8f + 15f else 12f).coerceIn(8f, 88f).roundToInt()
        val pathogenRiskRaw = (if (sensorHumidity > 80f && sensorTempC in 18f..28f) 45f else 14f).coerceIn(6f, 82f).roundToInt()
        val pestRiskRaw = (if (sensorTempC > 26f && sensorHumidity < 55f) 35f else 10f).coerceIn(5f, 75f).roundToInt()
        val lightStressRaw = (if (fvFm < 0.72f) 42f else 8f).coerceIn(5f, 70f).roundToInt()
        val salinityRiskRaw = (if (sensorSoilMoisture < 25f && sensorTempC > 30f) 38f else 7f).coerceIn(4f, 65f).roundToInt()
        val senescenceProb = if (stage == PlantGrowthStage.MATURATION) 65 else 8

        val stressMap = mapOf(
            "Water Deficit Stress" to waterStressRaw,
            "Nutrient Deficiency" to nutrientStressRaw,
            "Pathogen / Foliar Disease" to pathogenRiskRaw,
            "Pest / Herbivory Activity" to pestRiskRaw,
            "Thermal Heat Stress" to heatStressRaw,
            "Cold / Chilling Stress" to coldStressRaw,
            "Photosynthetic Light Stress" to lightStressRaw,
            "Salinity Osmotic Stress" to salinityRiskRaw,
            "Physiological Senescence" to senescenceProb
        )
        val dominantEntry = stressMap.maxByOrNull { it.value }
        val dominantStressName = dominantEntry?.key ?: "Balanced Growth"
        val dominantStressVal = dominantEntry?.value ?: 15

        val multiStress = MultiStressBreakdown(
            waterStressProb = waterStressRaw,
            nutrientStressProb = nutrientStressRaw,
            pathogenDiseaseProb = pathogenRiskRaw,
            pestHerbivoryProb = pestRiskRaw,
            heatStressProb = heatStressRaw,
            coldStressProb = coldStressRaw,
            lightPhotoinhibitionProb = lightStressRaw,
            salinityOsmoticProb = salinityRiskRaw,
            physiologicalSenescenceProb = senescenceProb,
            dominantStressType = if (dominantStressVal > 30) dominantStressName else "Optimal Equilibrium",
            primaryConfidence = (82 + (visualHealthScore * 0.12f)).roundToInt().coerceIn(75, 98)
        )

        // 5. Composite AgroSense Health Index (ASHI)
        // Weighted: 30% Spectral NDVI/NDRE + 25% Thermal CWSI + 20% Visual + 15% Photosystem Fv/Fm + 10% Soil Environment
        val spectralScore = (baseNdvi * 100f).coerceIn(0f, 100f)
        val thermalScore = ((1f - cwsi) * 100f).coerceIn(0f, 100f)
        val fvFmScore = ((fvFm / 0.83f) * 100f).coerceIn(0f, 100f)
        val envScore = (if (sensorSoilMoisture in species.optimalSoilMoistureMin..species.optimalSoilMoistureMax) 95f else 60f)

        val ashiRaw = (spectralScore * 0.30f + thermalScore * 0.25f + visualHealthScore * 0.20f + fvFmScore * 0.15f + envScore * 0.10f).roundToInt().coerceIn(1, 100)
        
        val healthTier = when {
            ashiRaw >= 90 -> AshiHealthTier.EXCELLENT
            ashiRaw >= 75 -> AshiHealthTier.HEALTHY
            ashiRaw >= 50 -> AshiHealthTier.MODERATE
            ashiRaw >= 25 -> AshiHealthTier.HIGH_RISK
            else -> AshiHealthTier.CRITICAL
        }

        // 6. 7-Day Stress Progression Forecasting
        val degradationRate = if (ashiRaw < 75) max(3, ((75 - ashiRaw) * 0.35f).roundToInt()) else 1
        val day0 = ashiRaw
        val day1 = max(10, day0 - degradationRate)
        val day3 = max(10, day0 - (degradationRate * 3))
        val day5 = max(5, day0 - (degradationRate * 5))
        val day7 = max(5, day0 - (degradationRate * 7))

        val riskLevel = when {
            day5 < 40 -> "Critical Rapid Degradation"
            day5 < 60 -> "High Progression Risk"
            day5 < 75 -> "Moderate Progression Risk"
            else -> "Stable Physiological State"
        }
        val interventionWindow = when {
            ashiRaw < 50 -> 24
            ashiRaw < 75 -> 48
            else -> 96
        }

        val trajectory = StressTrajectoryForecast(
            day0Health = day0,
            day1Health = day1,
            day3Health = day3,
            day5Health = day5,
            day7Health = day7,
            trajectoryRiskLevel = riskLevel,
            actionableInterventionWindowHours = interventionWindow,
            forecastSummary = if (ashiRaw < 75) {
                "Pre-symptomatic physiological stress progression detected. Without intervention within $interventionWindow hours, projected health will drop to ${day5}% by Day 5."
            } else {
                "Physiological parameters stable. Plant trajectory projected to maintain ${day7}% health over the next 7 days."
            }
        )

        return DigitalPlantTwin(
            plantId = 1L,
            plantTagId = "T-102",
            cropSpecies = species,
            growthStage = stage,
            ageDays = ageDays,
            ashiScore = ashiRaw,
            healthTier = healthTier,
            multiStressBreakdown = multiStress,
            spectralIndices = spectralIndices,
            physiologyVitals = physiologyVitals,
            thermalVitals = thermalVitals,
            trajectoryForecast = trajectory,
            historicalHealthPoints = listOf(
                min(100, day0 + 10),
                min(100, day0 + 7),
                min(100, day0 + 4),
                min(100, day0 + 2),
                day0
            ),
            lastScanModality = imagingModality,
            lastUpdateTimestamp = System.currentTimeMillis()
        )
    }

    /**
     * Compute Explainable AI (XAI) feature attribution breakdown (SHAP).
     */
    fun computeXaiFeatureAttributions(twin: DigitalPlantTwin): List<XaiFeatureAttribution> {
        val thermalWeight = (twin.thermalVitals.cwsi * 45 + 15).roundToInt().coerceIn(10, 42)
        val spectralWeight = ((1f - twin.spectralIndices.ndvi) * 40 + 18).roundToInt().coerceIn(12, 38)
        val fvFmWeight = ((0.83f - twin.physiologyVitals.photosyntheticEfficiencyFvFm) * 60 + 14).roundToInt().coerceIn(8, 28)
        val envWeight = 100 - thermalWeight - spectralWeight - fvFmWeight

        return listOf(
            XaiFeatureAttribution(
                featureName = "Leaf Thermal Transpiration (ΔT & CWSI)",
                contributionPercent = thermalWeight,
                description = "Stomatal conductance deficit and altered canopy cooling detected in thermal infrared spectrum."
            ),
            XaiFeatureAttribution(
                featureName = "Spectral Red-Edge & NDVI Shift",
                contributionPercent = spectralWeight,
                description = "Micro-color reflectance alteration in 705nm inflection band and mesophyll cell light scattering."
            ),
            XaiFeatureAttribution(
                featureName = "Photosystem II Efficiency (Fv/Fm)",
                contributionPercent = fvFmWeight,
                description = "Photochemical light energy dissipation indicating early non-photochemical quenching."
            ),
            XaiFeatureAttribution(
                featureName = "Microclimate & IoT Correlation",
                contributionPercent = envWeight.coerceAtLeast(6),
                description = "Ambient Vapor Pressure Deficit (VPD) and root-zone soil moisture matrix potential."
            )
        )
    }

    /**
     * Generate Pre-Symptomatic Early Warnings with actionable recommendations.
     */
    fun generatePreSymptomaticAlert(twin: DigitalPlantTwin): String {
        return when {
            twin.thermalVitals.cwsi > 0.45f -> {
                "⚠️ PRE-SYMPTOMATIC WARNING [Plant #${twin.plantTagId}]: Stomatal conductance restricted (CWSI ${twin.thermalVitals.cwsi}). Leaf temperature is ${twin.thermalVitals.leafTemperatureC}°C. Water deficit stress is progressing before visual wilting occurs. Recommend 1.5L precision drip irrigation within ${twin.trajectoryForecast.actionableInterventionWindowHours}h."
            }
            twin.spectralIndices.microColorShiftDetected -> {
                "⚠️ EARLY SPECTRAL ANOMALY [Plant #${twin.plantTagId}]: Micro-color shift of ${twin.spectralIndices.shiftMagnitudePercent}% detected in Red-Edge (708nm). Possible early nitrogen/chlorophyll alteration. Optimal scan cadence: 24h."
            }
            twin.physiologyVitals.photosyntheticEfficiencyFvFm < 0.72f -> {
                "⚠️ PHOTOCHEMICAL STRESS [Plant #${twin.plantTagId}]: Photosystem II yield down to ${twin.physiologyVitals.photosyntheticEfficiencyFvFm} (Optimal 0.83). High photoinhibition risk. Recommend shade screen deployment."
            }
            else -> {
                "✅ NORMAL EQUILIBRIUM [Plant #${twin.plantTagId}]: ASHI score is ${twin.ashiScore}/100 (${twin.healthTier.label}). All physiological markers within optimal species baseline."
            }
        }
    }
}
