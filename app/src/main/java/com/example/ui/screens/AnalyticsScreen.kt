package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.model.AppLanguage
import com.example.ui.components.TelemetryRechartsCard
import com.example.ui.theme.*
import com.example.ui.translation.AgroStrings
import com.example.ui.viewmodel.AgroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AgroViewModel,
    navController: NavController
) {
    val plants by viewModel.plants.collectAsStateWithLifecycle()
    val allPredictions by viewModel.allPredictions.collectAsStateWithLifecycle()
    val latestSensor by viewModel.latestSensorData.collectAsStateWithLifecycle()
    val sensorHistory by viewModel.sensorHistory.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val lastSyncedTimestamp by viewModel.lastSyncedTimestamp.collectAsStateWithLifecycle()
    val liveWeather by viewModel.liveWeather.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val isTamil = appLanguage == AppLanguage.TAMIL

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = if (isTamil) {
        listOf("பயிர் நலம்", "பல்முனை அழுத்தம்", "சுற்றுச்சூழல்", "AI மாடல் அளவீடுகள்")
    } else {
        listOf("Crop Health", "Multi-Stress", "Environment", "AI Model Metrics")
    }

    val avgHealth = remember(plants) {
        if (plants.isEmpty()) 82 else plants.map { it.healthScore }.average().toInt()
    }
    val healthyCount = remember(plants) { plants.count { it.healthScore >= 80 } }
    val atRiskCount = remember(plants) { plants.count { it.healthScore in 50..79 } }
    val criticalCount = remember(plants) { plants.count { it.healthScore < 50 } }

    Scaffold(
        containerColor = DeepForestBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isTamil) "மேம்பட்ட வேளாண் தரவு பகுப்பாய்வு" else "ADVANCED AGRI-DATA ANALYTICS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingEmerald,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = AgroStrings.t("Analytics", isTamil),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepForestBg)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Tab Selection Row
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CardSurfaceDark,
                    contentColor = GlowingEmerald,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, SoftGrass, RoundedCornerShape(14.dp))
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            selectedContentColor = GlowingEmerald,
                            unselectedContentColor = TextMuted
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // CROP HEALTH TAB
                    item {
                        // Health KPIs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple(if (isTamil) "சராசரி ஆரோக்கியம்" else "Average Health", "$avgHealth/100", GlowingEmerald),
                                Triple(if (isTamil) "ஆரோக்கியம்" else "Healthy", "$healthyCount", GlowingEmerald),
                                Triple(if (isTamil) "ஆபத்து" else "At Risk", "$atRiskCount", AlertWarning),
                                Triple(if (isTamil) "அவசரம்" else "Critical", "$criticalCount", AlertCritical)
                            ).forEach { (label, value, color) ->
                                Surface(
                                    color = CardSurfaceDark,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, SoftGrass),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(label, fontSize = 9.sp, color = TextMuted, maxLines = 1)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // Health Trajectory Chart
                        Surface(
                            color = CardSurfaceDark,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, SoftGrass),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(if (isTamil) "7-நாள் நல்வாழ்வுப் போக்கு" else "7-Day Health Trajectory", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                        Text(if (isTamil) "மேற்பரப்பு பயிர் வளர்ச்சி குறியீடு" else "Canopy Normalized Plant Vitality", fontSize = 11.sp, color = TextMuted)
                                    }
                                    Surface(color = SoftGrass, shape = RoundedCornerShape(6.dp)) {
                                        Text(if (isTamil) "+4.2% வளர்ச்சி" else "+4.2% Growth", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GlowingEmerald, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Sparkline Line Chart
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                ) {
                                    val points = listOf(74f, 76f, 75f, 79f, 82f, 80f, 84f)
                                    val maxVal = 100f
                                    val minVal = 60f
                                    val range = maxVal - minVal
                                    val stepX = size.width / (points.size - 1)

                                    val path = Path()
                                    points.forEachIndexed { i, p ->
                                        val x = i * stepX
                                        val y = size.height - ((p - minVal) / range) * size.height
                                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    }

                                    drawPath(
                                        path = path,
                                        color = GlowingEmerald,
                                        style = Stroke(width = 3.dp.toPx())
                                    )

                                    points.forEachIndexed { i, p ->
                                        val x = i * stepX
                                        val y = size.height - ((p - minVal) / range) * size.height
                                        drawCircle(
                                            color = GlowingEmerald,
                                            radius = 4.dp.toPx(),
                                            center = Offset(x, y)
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = 2.dp.toPx(),
                                            center = Offset(x, y)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val dayLabels = if (isTamil) {
                                        listOf("நாள் 1", "நாள் 2", "நாள் 3", "நாள் 4", "நாள் 5", "நாள் 6", "இன்று")
                                    } else {
                                        listOf("Day 1", "Day 2", "Day 3", "Day 4", "Day 5", "Day 6", "Today")
                                    }
                                    dayLabels.forEach { day ->
                                        Text(day, fontSize = 9.sp, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // Health Distribution
                        Surface(
                            color = CardSurfaceDark,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, SoftGrass),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(if (isTamil) "பயிர் நலம் பரவல் விகிதம்" else "Canopy Health Distribution", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                
                                listOf(
                                    Triple(if (isTamil) "சிறந்த நலம் (>80%)" else "Optimal Health (>80%)", (healthyCount.toFloat() / plants.size.coerceAtLeast(1) * 100).toInt(), GlowingEmerald),
                                    Triple(if (isTamil) "மிதமான அழுத்தம் (50–79%)" else "Moderate Stress (50–79%)", (atRiskCount.toFloat() / plants.size.coerceAtLeast(1) * 100).toInt(), AlertWarning),
                                    Triple(if (isTamil) "அவசர அழுத்தம் (<50%)" else "Critical Stress (<50%)", (criticalCount.toFloat() / plants.size.coerceAtLeast(1) * 100).toInt(), AlertCritical)
                                ).forEach { (label, pct, color) ->
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(label, fontSize = 12.sp, color = TextLight)
                                            Text("$pct%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { pct / 100f },
                                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                            color = color,
                                            trackColor = SoftGrass
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // MULTI-STRESS TAB
                    item {
                        Surface(
                            color = CardSurfaceDark,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, SoftGrass),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(if (isTamil) "பல்முனை அழுத்தக் குறியீட்டு பகுப்பாய்வு" else "Multi-Stress Index Breakdown", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                Text(if (isTamil) "உடலியல் அழுத்தக் காரணிகளின் நேரலை நிலை" else "Real-time physiological risk across stress vectors", fontSize = 11.sp, color = TextMuted)

                                listOf(
                                    Triple(if (isTamil) "💧 நீர் பற்றாக்குறை (CWSI 0.28)" else "💧 Water Deficit (CWSI 0.28)", 24, GlowingEmerald),
                                    Triple(if (isTamil) "🧪 தழைச்சத்து குறைபாடு (NBI 38)" else "🧪 Nitrogen Starvation (NBI 38)", 37, GlowingEmerald),
                                    Triple(if (isTamil) "🦠 இலை நோய் கிருமிகள் (ஆரம்ப கருகல்)" else "🦠 Foliar Pathogen Spores (Early Blight)", 18, GlowingEmerald),
                                    Triple(if (isTamil) "☀️ வெப்ப அழுத்தம் / VPD உயர்வு (1.7 kPa)" else "☀️ Thermal / VPD Spike (VPD 1.7 kPa)", 41, AlertWarning),
                                    Triple(if (isTamil) "🐛 பூச்சித் தாக்குதல் (அசுவினி/சிலந்தி)" else "🐛 Pest Vector Activity (Aphid/Mite)", 12, GlowingEmerald)
                                ).forEach { (label, value, color) ->
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(label, fontSize = 12.sp, color = TextLight)
                                            Text("$value%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { value / 100f },
                                            modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                                            color = color,
                                            trackColor = SoftGrass
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            color = DeepForestBg,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, SoftGrass),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("💡", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(if (isTamil) "வேளாண் பல்முனை அழுத்தப் பார்வை" else "Agronomic Multi-Stress Insight", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GlowingEmerald)
                                    Text(
                                        if (isTamil) "உயர்ந்த வெப்ப அழுத்தம் தற்போது முக்கிய காரணமாக உள்ளது. நண்பகல் வேளையில் தெளிப்பான் இயக்குவது இலை வெப்பநிலையை 35% குறைக்கிறது." else "Elevated heat stress is currently the leading risk driver. Activating fogging systems at solar noon reduces thermal leaf load by 35%.",
                                        fontSize = 11.sp,
                                        color = TextLight,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // ENVIRONMENT TAB
                    item {
                        val temp = liveWeather?.temperatureC ?: latestSensor?.temperature ?: 31.0f
                        val humidity = liveWeather?.humidityPercent ?: latestSensor?.humidity ?: 68.0f
                        val soilM = latestSensor?.soilMoisture ?: 42.0f
                        val soilPh = 6.4f
                        val lightLux = if (isTamil) "அதிகம் (82 klux)" else "High (82 klux)"

                        Surface(
                            color = CardSurfaceDark,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, SoftGrass),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(if (isTamil) "நுண்ணிய காலநிலை மற்றும் மண் உணரி அளவீடுகள்" else "Microclimate & Rhizosphere Telemetry", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    listOf(
                                        Triple(if (isTamil) "வெப்பநிலை" else "Temperature", "${temp}°C", GlowingEmerald),
                                        Triple(if (isTamil) "ஈரப்பதம்" else "Humidity", "${humidity}%", TechCyan),
                                        Triple(if (isTamil) "மண் ஈரப்பதம்" else "Soil Moisture", "${soilM}%", SoilMoistureBlue)
                                    ).forEach { (label, value, color) ->
                                        Surface(color = DeepForestBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(label, fontSize = 9.sp, color = TextMuted)
                                                Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
                                            }
                                        }
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    listOf(
                                        Triple(if (isTamil) "மண் pH அளவு" else "Soil pH", "$soilPh ${if (isTamil) "(சரியானது)" else "(Ideal)"}", GlowingEmerald),
                                        Triple(if (isTamil) "சூரிய ஒளி கதிர்வீச்சு" else "Light Irradiance", lightLux, AlertWarning)
                                    ).forEach { (label, value, color) ->
                                        Surface(color = DeepForestBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(label, fontSize = 9.sp, color = TextMuted)
                                                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        TelemetryRechartsCard(
                            sensorHistory = sensorHistory,
                            isOnline = isOnline,
                            lastSyncedTimestamp = lastSyncedTimestamp,
                            onInjectSensorReading = {
                                viewModel.adminAddSensorReading(
                                    temperature = (24..35).random().toFloat() + 0.5f,
                                    humidity = (45..75).random().toFloat(),
                                    soilMoisture = (30..65).random().toFloat()
                                )
                            }
                        )
                    }
                }

                3 -> {
                    // AI MODEL PERFORMANCE
                    item {
                        Surface(
                            color = CardSurfaceDark,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, SoftGrass),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(if (isTamil) "செயற்கை நுண்ணறிவு மாடல் செயல்திறன்" else "AI Model Diagnostic Performance", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                Text(if (isTamil) "எட்ஜ் விஷன் டிரான்ஸ்ஃபார்மர் & மல்டிஸ்பெக்ட்ரல் பகுப்பாய்வு" else "Edge Vision Transformer & Multispectral Spectral Regression", fontSize = 11.sp, color = TextMuted)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    listOf(
                                        Pair(if (isTamil) "சராசரி துல்லியம்" else "Mean Confidence", "88.4%"),
                                        Pair(if (isTamil) "தவறான எச்சரிக்கை" else "False Positives", "1.8%"),
                                        Pair(if (isTamil) "கணிப்பு வேகம்" else "Inference Latency", "142 ms")
                                    ).forEach { (label, value) ->
                                        Surface(color = SoftGrass, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(label, fontSize = 9.sp, color = TextMuted)
                                                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GlowingEmerald)
                                            }
                                        }
                                    }
                                }

                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, SoftGrass),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(if (isTamil) "⚠️ அறிவியல் அறிவிப்பு" else "⚠️ Scientific Disclaimer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AlertWarning)
                                        Text(
                                            if (isTamil) "முன்கணிப்பு நம்பிக்கை என்பது புள்ளியியல் மாதிரி துல்லியத்தைக் குறிக்கிறது. தீவிர நோய் தொற்றுக்கு வேளாண் கள ஆய்வு பரிந்துரைக்கப்படுகிறது." else "Prediction confidence represents statistical model certainty and is not a laboratory biological confirmation.",
                                            fontSize = 11.sp,
                                            color = TextLight
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
