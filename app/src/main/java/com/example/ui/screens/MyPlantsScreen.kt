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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.model.AppLanguage
import com.example.data.model.Plant
import com.example.ui.components.PlantGrowthStageTimelineComponent
import com.example.ui.theme.*
import com.example.ui.translation.AgroStrings
import com.example.ui.viewmodel.AgroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlantsScreen(
    viewModel: AgroViewModel,
    navController: NavController
) {
    val plants by viewModel.plants.collectAsStateWithLifecycle()
    val selectedPlant by viewModel.selectedPlant.collectAsStateWithLifecycle()
    val healthHistory by viewModel.healthHistory.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val isTamil = appLanguage == AppLanguage.TAMIL

    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("ALL") } // ALL, HEALTHY, AT_RISK, CRITICAL
    var showAddPlantDialog by remember { mutableStateOf(false) }
    var activeDetailPlant by remember { mutableStateOf<Plant?>(null) }

    // Add Plant Dialog State
    var newCropName by remember { mutableStateOf("") }
    var newCropVariety by remember { mutableStateOf("") }
    var newCropPlantedDate by remember { mutableStateOf("") }
    var newCropLocation by remember { mutableStateOf("") }
    var newCropNotes by remember { mutableStateOf("") }

    val filteredPlants = remember(plants, searchQuery, filterStatus) {
        plants.filter { plant ->
            val matchesSearch = searchQuery.isBlank() ||
                    plant.name.contains(searchQuery, ignoreCase = true) ||
                    plant.tag.contains(searchQuery, ignoreCase = true) ||
                    plant.variety.contains(searchQuery, ignoreCase = true) ||
                    plant.location.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (filterStatus) {
                "HEALTHY" -> plant.healthScore >= 80
                "AT_RISK" -> plant.healthScore in 50..79
                "CRITICAL" -> plant.healthScore < 50
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        containerColor = DeepForestBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = AgroStrings.t("PLANT HEALTH REPOSITORY", isTamil),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingEmerald,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${AgroStrings.t("My Plants", isTamil)} (${plants.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddPlantDialog = true },
                        modifier = Modifier.testTag("add_plant_top_btn")
                    ) {
                        Icon(Icons.Filled.AddCircle, contentDescription = "Add Plant", tint = GlowingEmerald, modifier = Modifier.size(28.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepForestBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPlantDialog = true },
                containerColor = GlowingEmerald,
                contentColor = DeepForestBg,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_plant")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Register Plant")
            }
        }
    ) { padding ->
        if (activeDetailPlant != null) {
            // Detailed Plant Profile View
            PlantProfileDetailView(
                plant = activeDetailPlant!!,
                viewModel = viewModel,
                navController = navController,
                isTamil = isTamil,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onBack = { activeDetailPlant = null }
            )
        } else {
            // List View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("plants_search_input"),
                        placeholder = {
                            Text(
                                if (isTamil) "பயிர் பெயர், குறிச்சொல், ரகம் தேடவும்..." else "Search by crop, tag, variety, row...",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = GlowingEmerald) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Close, null, tint = TextMuted)
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass,
                            focusedContainerColor = CardSurfaceDark,
                            unfocusedContainerColor = CardSurfaceDark,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        singleLine = true
                    )
                }

                item {
                    // Filter Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("ALL", if (isTamil) "அனைத்தும் (${plants.size})" else "All (${plants.size})", TextLight),
                            Triple("HEALTHY", if (isTamil) "🟢 ஆரோக்கியம் (${plants.count { it.healthScore >= 80 }})" else "🟢 Healthy (${plants.count { it.healthScore >= 80 }})", GlowingEmerald),
                            Triple("AT_RISK", if (isTamil) "🟡 ஆபத்து (${plants.count { it.healthScore in 50..79 }})" else "🟡 At Risk (${plants.count { it.healthScore in 50..79 }})", AlertWarning),
                            Triple("CRITICAL", if (isTamil) "🔴 அவசரம் (${plants.count { it.healthScore < 50 }})" else "🔴 Critical (${plants.count { it.healthScore < 50 }})", AlertCritical)
                        ).forEach { (key, label, color) ->
                            val isSelected = filterStatus == key
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) color.copy(alpha = 0.15f) else CardSurfaceDark,
                                border = BorderStroke(1.dp, if (isSelected) color else SoftGrass),
                                modifier = Modifier
                                    .clickable { filterStatus = key }
                                    .testTag("filter_status_$key")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) color else TextLight,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                if (filteredPlants.isEmpty()) {
                    item {
                        Surface(
                            color = CardSurfaceDark,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, SoftGrass),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🌱", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) {
                                        if (isTamil) "பொருந்தும் பயிர்கள் எதுவும் கிடைக்கவில்லை" else "No matching plants found"
                                    } else {
                                        if (isTamil) "பயிர்கள் எதுவும் இன்னும் பதிவு செய்யப்படவில்லை" else "No registered crops yet"
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextLight
                                )
                                Text(
                                    text = if (searchQuery.isNotEmpty()) {
                                        if (isTamil) "வேறு பயிர் பெயர் அல்லது குறிச்சொல்லைத் தேட முயற்சிக்கவும்." else "Try searching for a different crop name or tag."
                                    } else {
                                        if (isTamil) "முன் அறிகுறி அழுத்தத்தைக் கண்காணிக்க உங்கள் முதல் பயிரைப் பதிவு செய்யுங்கள்." else "Register your first plant to begin tracking pre-symptomatic stress."
                                    },
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showAddPlantDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Add, null, tint = DeepForestBg, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isTamil) "புதிய பயிரைப் பதிவு செய்" else "Add Your First Plant", color = DeepForestBg, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(filteredPlants) { plant ->
                        val isHealthy = plant.healthScore >= 80
                        val isModerate = plant.healthScore in 50..79
                        val statusColor = when {
                            isHealthy -> GlowingEmerald
                            isModerate -> AlertWarning
                            else -> AlertCritical
                        }
                        val statusLabel = when {
                            isHealthy -> if (isTamil) "🟢 ஆரோக்கியம்" else "🟢 Healthy"
                            isModerate -> if (isTamil) "🟡 மிதமான ஆபத்து" else "🟡 Moderate Risk"
                            else -> if (isTamil) "🔴 அவசரக் கவனம்" else "🔴 Critical Risk"
                        }

                        Surface(
                            color = CardSurfaceDark,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, if (selectedPlant?.id == plant.id) GlowingEmerald else SoftGrass),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectPlant(plant)
                                    activeDetailPlant = plant
                                }
                                .testTag("plant_card_${plant.tag}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
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
                                                .background(statusColor.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Spa,
                                                contentDescription = null,
                                                tint = statusColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = plant.name,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextLight
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = SoftGrass,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = plant.tag,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GlowingEmerald,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "${plant.variety} • ${plant.location}",
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${plant.healthScore}%",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor
                                        )
                                        Text(
                                            text = statusLabel,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = statusColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = SoftGrass)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Schedule, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${AgroStrings.t("Planted", isTamil)}: ${plant.plantedDate}",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isTamil) "விவரங்களைக் காண்க" else "View Details",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GlowingEmerald
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(Icons.Filled.ChevronRight, null, tint = GlowingEmerald, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }

        // Add Plant Dialog
        if (showAddPlantDialog) {
            AlertDialog(
                onDismissRequest = { showAddPlantDialog = false },
                title = {
                    Text(
                        text = if (isTamil) "புதிய பயிரைப் பதிவு செய்" else "Register New Plant / Batch",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextLight
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newCropName,
                            onValueChange = { newCropName = it },
                            label = { Text(if (isTamil) "பயிர் பெயர் (எ.கா. தக்காளி)" else "Crop Name (e.g. Tomato)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newCropVariety,
                            onValueChange = { newCropVariety = it },
                            label = { Text(if (isTamil) "பயிர் ரகம் (எ.கா. ஹைபிரிட் F1)" else "Variety / Cultivar (e.g. San Marzano)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newCropLocation,
                            onValueChange = { newCropLocation = it },
                            label = { Text(if (isTamil) "பண்ணை இடம் (எ.கா. பாத்தி 2, வரிசை 4)" else "Location / Polyhouse (e.g. Bay 2, Row 4)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newCropPlantedDate,
                            onValueChange = { newCropPlantedDate = it },
                            label = { Text(if (isTamil) "நடவு செய்த தேதி (YYYY-MM-DD)" else "Planted Date (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newCropNotes,
                            onValueChange = { newCropNotes = it },
                            label = { Text(if (isTamil) "குறிப்புகள் / மண் வகை" else "Notes / Substrate Type") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCropName.isNotBlank()) {
                                viewModel.addPlant(
                                    name = newCropName,
                                    variety = if (newCropVariety.isNotBlank()) newCropVariety else "Standard",
                                    plantedDate = if (newCropPlantedDate.isNotBlank()) newCropPlantedDate else "2026-03-01",
                                    location = if (newCropLocation.isNotBlank()) newCropLocation else "Polyhouse Bay 1",
                                    notes = newCropNotes
                                )
                                showAddPlantDialog = false
                                newCropName = ""
                                newCropVariety = ""
                                newCropLocation = ""
                                newCropPlantedDate = ""
                                newCropNotes = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                    ) {
                        Text(if (isTamil) "பயிரைப் பதிவு செய்" else "Register Crop", color = DeepForestBg, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddPlantDialog = false }) {
                        Text(if (isTamil) "ரத்துசெய்" else "Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun PlantProfileDetailView(
    plant: Plant,
    viewModel: AgroViewModel,
    navController: NavController,
    isTamil: Boolean = false,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isHealthy = plant.healthScore >= 80
    val isModerate = plant.healthScore in 50..79
    val statusColor = when {
        isHealthy -> GlowingEmerald
        isModerate -> AlertWarning
        else -> AlertCritical
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = GlowingEmerald, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isTamil) "அனைத்து பயிர்களுக்கும் திரும்பு" else "Back to All Plants", color = GlowingEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        // Header Card
        Surface(
            color = CardSurfaceDark,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SoftGrass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(statusColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Spa, null, tint = statusColor, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(plant.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = SoftGrass,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = plant.tag,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlowingEmerald,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text("${plant.variety} • ${plant.location}", fontSize = 12.sp, color = TextMuted)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("${plant.healthScore}%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = statusColor)
                        Text(if (isTamil) "ஒட்டுமொத்த மதிப்பெண்" else "Overall Score", fontSize = 10.sp, color = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Pair(if (isTamil) "நடவு தேதி" else "Planted Date", plant.plantedDate),
                        Pair(if (isTamil) "நிலை" else "Status", if (isHealthy) (if (isTamil) "ஆரோக்கியம்" else "Healthy") else (if (isTamil) "ஆபத்து" else "At Risk")),
                        Pair(if (isTamil) "அடுத்த ஸ்கேன்" else "Next Scan", if (isTamil) "24 மணி நேரத்தில்" else "In 24h")
                    ).forEach { (label, value) ->
                        Surface(
                            color = DeepForestBg,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SoftGrass),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(label, fontSize = 9.sp, color = TextMuted)
                                Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.selectPlant(plant)
                            navController.navigate("scanner")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PhotoCamera, null, tint = DeepForestBg, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTamil) "பயிரை ஸ்கேன் செய்" else "Scan Plant Now", color = DeepForestBg, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.selectPlant(plant)
                            navController.navigate("digital_twin")
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GlowingEmerald),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Psychology, null, tint = GlowingEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTamil) "டிஜிட்டல் இரட்டை" else "Digital Twin", color = GlowingEmerald, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Phenological Growth Stage Timeline & Climate-Calibrated Maturity Forecast
        PlantGrowthStageTimelineComponent(
            plant = plant,
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth()
        )

        // Stress Breakdown Card
        Surface(
            color = CardSurfaceDark,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SoftGrass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isTamil) "உடலியல் அழுத்தக் குறியீடுகள்" else "Physiological Stress Indices",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight
                )

                listOf(
                    Triple(if (isTamil) "💧 நீர் அழுத்தம்" else "💧 Water Stress", 24, GlowingEmerald),
                    Triple(if (isTamil) "🧪 ஊட்டச்சத்து அழுத்தம்" else "🧪 Nutrient Stress", 37, GlowingEmerald),
                    Triple(if (isTamil) "🦠 இலை நோய் ஆபத்து" else "🦠 Foliar Disease Risk", 18, GlowingEmerald),
                    Triple(if (isTamil) "☀️ வெப்ப அழுத்தம் / VPD" else "☀️ Heat / VPD Stress", 41, AlertWarning),
                    Triple(if (isTamil) "🐛 பூச்சித் தாக்குதல் ஆபத்து" else "🐛 Pest Defoliation Risk", 12, GlowingEmerald)
                ).forEach { (label, value, color) ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, fontSize = 12.sp, color = TextLight, fontWeight = FontWeight.Medium)
                            Text("$value%", fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { value / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = color,
                            trackColor = SoftGrass
                        )
                    }
                }
            }
        }

        // Action Recommendation Card
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SoftGrass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.TipsAndUpdates, null, tint = GlowingEmerald, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isTamil) "${plant.name} பயிருக்கான வேளாண் ஆலோசனை" else "Agronomic Advisory For ${plant.name}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlowingEmerald
                    )
                }
                Text(
                    text = if (isTamil) "மண் ஈரப்பதத்தை 45% முதல் 55% வரை சீராகப் பராமரிக்கவும். சிவப்பு-விளிம்பு நிறமாலை பகுப்பாய்வு படி இலைத்துளைகள் ஆரோக்கியமாகவும் நோய் அறிகுறிகள் இன்றியும் உள்ளன." else "Maintain volumetric soil moisture between 45% and 55%. Red-edge spectrum response indicates robust stomatal conductance with zero visible symptoms of early blight.",
                    fontSize = 12.sp,
                    color = TextLight,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
