package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.engine.AgroBiotechEngine
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AgroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalTwinScreen(
    viewModel: AgroViewModel,
    navController: NavController
) {
    val twin by viewModel.digitalPlantTwin.collectAsStateWithLifecycle()
    val selectedSpecies by viewModel.selectedCropSpecies.collectAsStateWithLifecycle()
    val selectedStage by viewModel.selectedGrowthStage.collectAsStateWithLifecycle()
    val plantAge by viewModel.plantAgeDays.collectAsStateWithLifecycle()
    val selectedModality by viewModel.selectedModality.collectAsStateWithLifecycle()
    val xaiAttributions by viewModel.xaiFeatureAttributions.collectAsStateWithLifecycle()
    val showAttentionMap by viewModel.xaiShowAttentionMap.collectAsStateWithLifecycle()

    var showCropSelectorDialog by remember { mutableStateOf(false) }
    var showStageSelectorDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepForestBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HEADER BANNER: DIGITAL PLANT TWIN ---
        Surface(
            color = CardSurfaceDark,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, GlowingEmerald.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                GlowingEmerald.copy(alpha = 0.12f),
                                CardSurfaceDark
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlowingEmerald.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedSpecies.iconEmoji,
                                    fontSize = 24.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "DIGITAL PLANT TWIN",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlowingEmerald,
                                        letterSpacing = 1.2.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = GlowingEmerald.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "ID: ${twin.plantTagId}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GlowingEmerald,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${selectedSpecies.commonName} (${selectedSpecies.scientificName})",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextLight
                                )
                                Text(
                                    text = "${selectedStage.iconEmoji} ${selectedStage.stageName} • Age: $plantAge Days",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        // Quick species & stage switch button
                        FilledTonalButton(
                            onClick = { showCropSelectorDialog = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = GlowingEmerald.copy(alpha = 0.15f),
                                contentColor = GlowingEmerald
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Edit, "Edit Twin", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tune", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = SoftGrass.copy(alpha = 0.4f))

                    // Location and last scan modality chip bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Place, "Location", tint = TextMuted, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(twin.zoneLocation, fontSize = 11.sp, color = TextMuted)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Sensors, "Modality", tint = GlowingEmerald, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(selectedModality.title, fontSize = 11.sp, color = GlowingEmerald, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // --- FEATURE 6: AGROSENSE HEALTH INDEX (ASHI) HERO GAUGE ---
        Surface(
            color = CardSurfaceDark,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SoftGrass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AGROSENSE HEALTH INDEX — ASHI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingEmerald,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Multimodal Physiological Vitality Composite",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    // Health Tier Badge
                    Surface(
                        color = twin.healthTier.color.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, twin.healthTier.color)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(twin.healthTier.color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = twin.healthTier.label.uppercase(),
                                color = twin.healthTier.color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Big Score Metric with Progress Arc
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${twin.ashiScore}",
                                fontSize = 54.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = twin.healthTier.color,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = "/100",
                                fontSize = 18.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                            )
                        }
                        Text(
                            text = "Composite Vitality",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Column(
                        modifier = Modifier.width(180.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AshiSubScoreRow("Spectral (NDVI/NDRE)", "${(twin.spectralIndices.ndvi * 100).toInt()}%", GlowingEmerald)
                        AshiSubScoreRow("Thermal (CWSI Cooling)", "${((1f - twin.thermalVitals.cwsi) * 100).toInt()}%", Color(0xFF29B6F6))
                        AshiSubScoreRow("Photosystem II (Fv/Fm)", "${((twin.physiologyVitals.photosyntheticEfficiencyFvFm / 0.83f) * 100).toInt()}%", Color(0xFFAB47BC))
                        AshiSubScoreRow("Relative Water (RWC)", "${twin.physiologyVitals.relativeWaterContentRwc.toInt()}%", Color(0xFF4CAF50))
                    }
                }

                Text(
                    text = twin.healthTier.statusSummary,
                    fontSize = 12.sp,
                    color = TextLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepForestBg.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                )
            }
        }

        // --- FEATURE 10: PRE-SYMPTOMATIC ALERT WARNING CARD ---
        Surface(
            color = if (twin.ashiScore < 75) AlertWarning.copy(alpha = 0.12f) else CardSurfaceDark,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, if (twin.ashiScore < 75) AlertWarning else SoftGrass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (twin.ashiScore < 75) Icons.Filled.WarningAmber else Icons.Filled.CheckCircle,
                            contentDescription = "Alert",
                            tint = if (twin.ashiScore < 75) AlertWarning else GlowingEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (twin.ashiScore < 75) "PRE-SYMPTOMATIC EARLY WARNING" else "PRE-SYMPTOMATIC STATUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (twin.ashiScore < 75) AlertWarning else GlowingEmerald,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Text(
                        text = "Confidence: ${twin.multiStressBreakdown.primaryConfidence}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }

                Text(
                    text = AgroBiotechEngine.generatePreSymptomaticAlert(twin),
                    fontSize = 12.sp,
                    color = TextLight,
                    lineHeight = 18.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next Optimal Scan: In 24 Hours",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    TextButton(
                        onClick = { navController.navigate("scanner") },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Trigger Immediate Scan", fontSize = 11.sp, color = GlowingEmerald, fontWeight = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = GlowingEmerald, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // --- FEATURE 3: MULTI-STRESS DETECTION (MULTI-LABEL BREAKDOWN) ---
        Surface(
            color = CardSurfaceDark,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, SoftGrass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MULTI-STRESS CLASSIFICATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingEmerald,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Simultaneous multi-label risk probability breakdown",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Surface(
                        color = DeepForestBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Dominant: ${twin.multiStressBreakdown.dominantStressType}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingEmerald,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                StressRiskBar("💧 Water Deficit Stress", twin.multiStressBreakdown.waterStressProb, Color(0xFF29B6F6))
                StressRiskBar("🧪 Nutrient Deficiency (N/P/K/Fe)", twin.multiStressBreakdown.nutrientStressProb, Color(0xFFFFB74D))
                StressRiskBar("🦠 Pathogen / Foliar Disease Risk", twin.multiStressBreakdown.pathogenDiseaseProb, Color(0xFFE57373))
                StressRiskBar("🐛 Pest / Herbivory Activity", twin.multiStressBreakdown.pestHerbivoryProb, Color(0xFFFF8A65))
                StressRiskBar("☀️ Thermal Heat Stress", twin.multiStressBreakdown.heatStressProb, Color(0xFFFF7043))
                StressRiskBar("❄️ Cold / Chilling Stress", twin.multiStressBreakdown.coldStressProb, Color(0xFF81D4FA))
                StressRiskBar("💡 Photoinhibition / Light Stress", twin.multiStressBreakdown.lightPhotoinhibitionProb, Color(0xFFFFF176))
                StressRiskBar("🧂 Salinity / Osmotic Stress", twin.multiStressBreakdown.salinityOsmoticProb, Color(0xFFCE93D8))
                StressRiskBar("🌱 Physiological Senescence", twin.multiStressBreakdown.physiologicalSenescenceProb, Color(0xFFAED581))
            }
        }

        // --- FEATURE 4: THERMAL STRESS & STOMATAL CONDUCTANCE ---
        Surface(
            color = CardSurfaceDark,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, SoftGrass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "THERMAL STOMATAL CONDUCTANCE (TIR)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingEmerald,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Transpirational cooling & Crop Water Stress Index",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Icon(Icons.Filled.Thermostat, "Thermal", tint = Color(0xFFFF7043), modifier = Modifier.size(24.dp))
                }

                // Grid of thermal vitals
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThermalMetricCard(
                        title = "Leaf Temp (T_leaf)",
                        value = "${twin.thermalVitals.leafTemperatureC}°C",
                        subtitle = "Target: <26°C",
                        modifier = Modifier.weight(1f)
                    )
                    ThermalMetricCard(
                        title = "Temp Diff (ΔT)",
                        value = "${if (twin.thermalVitals.deltaTC > 0) "+" else ""}${twin.thermalVitals.deltaTC}°C",
                        subtitle = if (twin.thermalVitals.deltaTC <= 0) "Cooling Active" else "Stomata Closed",
                        modifier = Modifier.weight(1f),
                        valueColor = if (twin.thermalVitals.deltaTC <= 0) GlowingEmerald else AlertWarning
                    )
                    ThermalMetricCard(
                        title = "CWSI Index",
                        value = "${twin.thermalVitals.cwsi}",
                        subtitle = "0.0 (Wet) - 1.0 (Dry)",
                        modifier = Modifier.weight(1f),
                        valueColor = if (twin.thermalVitals.cwsi < 0.3f) GlowingEmerald else AlertWarning
                    )
                }

                Surface(
                    color = DeepForestBg,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Stomatal Status", fontSize = 10.sp, color = TextMuted)
                            Text(twin.thermalVitals.stomatalStatus, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Transpiration Rate", fontSize = 10.sp, color = TextMuted)
                            Text("${twin.thermalVitals.transpirationRateMmolM2S} mmol/m²s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GlowingEmerald)
                        }
                    }
                }
            }
        }

        // --- FEATURE 2 & 5: PLANT PHYSIOLOGY & SPECTRAL INDICES ---
        Surface(
            color = CardSurfaceDark,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, SoftGrass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PLANT PHYSIOLOGY & SPECTRAL SIGNATURES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingEmerald,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Biochemical pigments & optical vegetation indices",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Icon(Icons.Filled.Biotech, "Biotech", tint = GlowingEmerald, modifier = Modifier.size(24.dp))
                }

                // Grid 2x3 of physiological markers
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BiotechMetricCard("Chlorophyll SPAD", "${twin.physiologyVitals.chlorophyllSpad}", "Opt: 35-65", Modifier.weight(1f))
                    BiotechMetricCard("Chlorophyll Content", "${twin.physiologyVitals.chlorophyllContentUgCm2} µg/cm²", "Foliar Mass", Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BiotechMetricCard("Photosystem II (Fv/Fm)", "${twin.physiologyVitals.photosyntheticEfficiencyFvFm}", "Opt: ~0.83", Modifier.weight(1f),
                        highlight = twin.physiologyVitals.photosyntheticEfficiencyFvFm >= 0.75f)
                    BiotechMetricCard("Relative Water (RWC)", "${twin.physiologyVitals.relativeWaterContentRwc}%", "Opt: >80%", Modifier.weight(1f),
                        highlight = twin.physiologyVitals.relativeWaterContentRwc >= 75f)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BiotechMetricCard("NDVI (Canopy)", "${twin.spectralIndices.ndvi}", "0.2 to 0.9", Modifier.weight(1f))
                    BiotechMetricCard("NDRE (Red-Edge)", "${twin.spectralIndices.ndre}", "Chlorophyll Density", Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BiotechMetricCard("VARI (Visible Green)", "${twin.spectralIndices.vari}", "RGB Greenness", Modifier.weight(1f))
                    BiotechMetricCard("Red-Edge Inflection", "${twin.spectralIndices.redEdgeInflectionWavelength} nm", "Shift: ${twin.spectralIndices.shiftMagnitudePercent}%", Modifier.weight(1f))
                }
            }
        }

        // --- FEATURE 7: STRESS PROGRESSION FORECASTING (7-DAY TRAJECTORY) ---
        Surface(
            color = CardSurfaceDark,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, SoftGrass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "7-DAY STRESS PROGRESSION PREDICTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingEmerald,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Predictive degradation trajectory vs inaction",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.ShowChart, "Forecast", tint = GlowingEmerald, modifier = Modifier.size(24.dp))
                }

                // 5-Point Horizontal Forecast Stepper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepForestBg, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrajectoryStepNode("Today", "${twin.trajectoryForecast.day0Health}%", GlowingEmerald)
                    TrajectoryStepArrow()
                    TrajectoryStepNode("+1 Day", "${twin.trajectoryForecast.day1Health}%", if (twin.trajectoryForecast.day1Health >= 75) GlowingEmerald else AlertWarning)
                    TrajectoryStepArrow()
                    TrajectoryStepNode("+3 Days", "${twin.trajectoryForecast.day3Health}%", if (twin.trajectoryForecast.day3Health >= 70) GlowingEmerald else AlertWarning)
                    TrajectoryStepArrow()
                    TrajectoryStepNode("+5 Days", "${twin.trajectoryForecast.day5Health}%", if (twin.trajectoryForecast.day5Health >= 60) AlertWarning else AlertCritical)
                    TrajectoryStepArrow()
                    TrajectoryStepNode("+7 Days", "${twin.trajectoryForecast.day7Health}%", if (twin.trajectoryForecast.day7Health >= 50) AlertWarning else AlertCritical)
                }

                Text(
                    text = twin.trajectoryForecast.forecastSummary,
                    fontSize = 11.sp,
                    color = TextLight,
                    lineHeight = 16.sp
                )

                Surface(
                    color = AlertWarning.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AlertWarning.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.HourglassTop, null, tint = AlertWarning, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Actionable Intervention Window: ${twin.trajectoryForecast.actionableInterventionWindowHours} Hours to avert foliar damage.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlertWarning
                        )
                    }
                }
            }
        }

        // --- FEATURE 11: EXPLAINABLE AI (XAI) & SHAP FEATURE ATTRIBUTION ---
        Surface(
            color = CardSurfaceDark,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, SoftGrass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EXPLAINABLE AI (XAI) INTERPRETABILITY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingEmerald,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "SHAP feature contribution weights to diagnosis",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    IconButton(onClick = { viewModel.toggleXaiAttentionMap() }) {
                        Icon(
                            imageVector = if (showAttentionMap) Icons.Filled.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = "Toggle Saliency",
                            tint = if (showAttentionMap) GlowingEmerald else TextMuted
                        )
                    }
                }

                xaiAttributions.forEach { attr ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(attr.featureName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            Text("${attr.contributionPercent}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GlowingEmerald)
                        }
                        LinearProgressIndicator(
                            progress = { attr.contributionPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = GlowingEmerald,
                            trackColor = DeepForestBg
                        )
                        Text(attr.description, fontSize = 10.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // --- NAVIGATION HUBS: LAB CONFIRMATION & HEATMAP ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { navController.navigate("biochemical_lab") },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GlowingEmerald),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GlowingEmerald),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Science, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lab Testing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { navController.navigate("farm_heatmap") },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GlowingEmerald),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GlowingEmerald),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.GridOn, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Farm Heatmap", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // --- CROP SELECTOR DIALOG ---
    if (showCropSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showCropSelectorDialog = false },
            title = { Text("Select Crop Species & Growth Stage", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Crop Species (Cross-Crop Intelligence):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GlowingEmerald)
                    CropSpecies.entries.forEach { species ->
                        Surface(
                            color = if (selectedSpecies == species) GlowingEmerald.copy(alpha = 0.2f) else CardSurfaceDark,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (selectedSpecies == species) GlowingEmerald else SoftGrass),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCropSpecies(species)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(species.iconEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(species.commonName, fontWeight = FontWeight.Bold, color = TextLight, fontSize = 13.sp)
                                    Text(species.scientificName, fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Phenological Growth Stage:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GlowingEmerald)
                    PlantGrowthStage.entries.forEach { stage ->
                        Surface(
                            color = if (selectedStage == stage) GlowingEmerald.copy(alpha = 0.2f) else CardSurfaceDark,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (selectedStage == stage) GlowingEmerald else SoftGrass),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setGrowthStage(stage)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stage.iconEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(stage.stageName, fontWeight = FontWeight.Bold, color = TextLight, fontSize = 12.sp)
                                    Text(stage.physiologicalFocus, fontSize = 10.sp, color = TextMuted, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCropSelectorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                ) {
                    Text("DONE", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CardSurfaceDark
        )
    }
}

@Composable
private fun AshiSubScoreRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, color = TextMuted)
        }
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun StressRiskBar(label: String, percent: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, color = TextLight)
            Text("$percent%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = DeepForestBg
        )
    }
}

@Composable
private fun ThermalMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextLight
) {
    Surface(
        color = DeepForestBg,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 9.sp, color = TextMuted, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 9.sp, color = TextMuted, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun BiotechMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = true
) {
    Surface(
        color = DeepForestBg,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(title, fontSize = 10.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (highlight) GlowingEmerald else AlertWarning)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 9.sp, color = TextMuted)
        }
    }
}

@Composable
private fun TrajectoryStepNode(dayLabel: String, healthVal: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(dayLabel, fontSize = 9.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(healthVal, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun TrajectoryStepArrow() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = null,
        tint = TextMuted,
        modifier = Modifier.size(12.dp)
    )
}
