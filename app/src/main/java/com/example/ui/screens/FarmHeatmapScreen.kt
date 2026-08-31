package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun FarmHeatmapScreen(
    viewModel: AgroViewModel,
    navController: NavController
) {
    val heatmapNodes by viewModel.farmHeatmapNodes.collectAsStateWithLifecycle()
    val selectedZone by viewModel.selectedHeatmapZone.collectAsStateWithLifecycle()
    val latestSensor by viewModel.latestSensorData.collectAsStateWithLifecycle()

    var selectedNode by remember { mutableStateOf<FarmHeatmapNode?>(heatmapNodes.firstOrNull()) }

    val currentTemp = latestSensor?.temperature ?: 24.2f
    val currentHumidity = latestSensor?.humidity ?: 62f
    val currentSoilMoisture = latestSensor?.soilMoisture ?: 39f
    val currentVpd = AgroBiotechEngine.calculateVpd(currentTemp, currentHumidity)

    val zones = listOf("Polyhouse Alpha", "Polyhouse Beta", "Open Field South", "Drone UAV Aerial")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("FARM STRESS HEATMAP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GlowingEmerald, letterSpacing = 1.sp)
                        Text("Geospatial & Canopy Hotspot Matrix", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextLight)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("digital_twin") }) {
                        Icon(Icons.Filled.Psychology, contentDescription = "Digital Twin", tint = GlowingEmerald)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepForestBg)
            )
        },
        containerColor = DeepForestBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- ZONE SELECTOR PILLS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                zones.forEach { zone ->
                    FilterChip(
                        selected = selectedZone == zone,
                        onClick = { viewModel.setHeatmapZone(zone) },
                        label = { Text(zone, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (zone.contains("Drone")) Icons.Filled.Flight else Icons.Filled.GridOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GlowingEmerald,
                            selectedLabelColor = DeepForestBg,
                            selectedLeadingIconColor = DeepForestBg,
                            containerColor = CardSurfaceDark,
                            labelColor = TextLight,
                            iconColor = TextMuted
                        ),
                        border = BorderStroke(1.dp, if (selectedZone == zone) GlowingEmerald else SoftGrass)
                    )
                }
            }

            // --- FEATURE 8: ENVIRONMENTAL CORRELATION ENGINE STRIP ---
            Surface(
                color = CardSurfaceDark,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SoftGrass),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ENVIRONMENTAL CORRELATION ENGINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingEmerald,
                            letterSpacing = 0.8.sp
                        )
                        Text("Real-Time IoT Telemetry", fontSize = 10.sp, color = TextMuted)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        EnvTelemetryItem("Temp", "$currentTemp°C", Icons.Filled.Thermostat, Color(0xFFFF7043))
                        EnvTelemetryItem("Humidity", "$currentHumidity%", Icons.Filled.WaterDrop, Color(0xFF29B6F6))
                        EnvTelemetryItem("Moisture", "$currentSoilMoisture%", Icons.Filled.Grass, GlowingEmerald)
                        EnvTelemetryItem("VPD", "$currentVpd kPa", Icons.Filled.Air, Color(0xFFAB47BC))
                        EnvTelemetryItem("Soil pH", "6.8", Icons.Filled.Science, Color(0xFFFFB74D))
                    }
                }
            }

            // --- FEATURE 9: 5x5 INTERACTIVE HEATMAP MATRIX ---
            Surface(
                color = CardSurfaceDark,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, SoftGrass),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CANOPY STRESS MATRIX (5x5 GRID)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GlowingEmerald)
                            Text("Click any node to inspect plant physiology", fontSize = 11.sp, color = TextMuted)
                        }

                        // Color legend
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            LegendDot(GlowingEmerald, "Healthy")
                            LegendDot(AlertWarning, "Warning")
                            LegendDot(AlertCritical, "Hotspot")
                        }
                    }

                    // 5x5 Grid Render
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(DeepForestBg, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(heatmapNodes) { node ->
                                val nodeColor = when {
                                    node.ashiHealthScore >= 75 -> GlowingEmerald
                                    node.ashiHealthScore >= 50 -> AlertWarning
                                    else -> AlertCritical
                                }
                                val isSelected = selectedNode?.nodeId == node.nodeId

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(nodeColor.copy(alpha = if (isSelected) 0.45f else 0.20f))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color.White else nodeColor,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedNode = node },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = node.plantTag,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextLight
                                        )
                                        Text(
                                            text = "${node.ashiHealthScore}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = nodeColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- SELECTED NODE DETAIL CARD ---
            selectedNode?.let { node ->
                val nodeHealthColor = when {
                    node.ashiHealthScore >= 75 -> GlowingEmerald
                    node.ashiHealthScore >= 50 -> AlertWarning
                    else -> AlertCritical
                }

                Surface(
                    color = CardSurfaceDark,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, nodeHealthColor.copy(alpha = 0.6f)),
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
                                Text(node.cropSpecies.iconEmoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "PLANT NODE ${node.plantTag}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextLight
                                    )
                                    Text(
                                        text = "${node.cropSpecies.commonName} • Grid [${node.row}, ${node.col}]",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Surface(
                                color = nodeHealthColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "ASHI ${node.ashiHealthScore}/100",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = nodeHealthColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = SoftGrass.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Dominant Stress", fontSize = 10.sp, color = TextMuted)
                                Text(node.dominantStress, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Leaf Temp / Moisture", fontSize = 10.sp, color = TextMuted)
                                Text("${node.leafTempC}°C • ${node.soilMoisturePercent}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GlowingEmerald)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GPS: ${String.format("%.4f", node.gpsLat)}, ${String.format("%.4f", node.gpsLng)}",
                                fontSize = 10.sp,
                                color = TextMuted
                            )

                            Button(
                                onClick = {
                                    viewModel.setCropSpecies(node.cropSpecies)
                                    navController.navigate("digital_twin")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open Digital Twin", color = DeepForestBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun EnvTelemetryItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
        Text(label, fontSize = 9.sp, color = TextMuted)
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 9.sp, color = TextMuted)
    }
}
