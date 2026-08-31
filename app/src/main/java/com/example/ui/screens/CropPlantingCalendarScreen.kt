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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AgroViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class CalendarTabMode(val label: String, val iconEmoji: String) {
    TIMELINES("Crop Timelines", "🌱"),
    CARE_REMINDERS("Climate Care Tasks", "💧"),
    CALENDAR_VIEW("Calendar Matrix", "📅"),
    STAGE_ALMANAC("Stage Guide", "📖")
}

@Composable
fun CropPlantingCalendarScreen(
    viewModel: AgroViewModel,
    navController: NavController
) {
    val activeCrops by viewModel.activePlantingEntries.collectAsStateWithLifecycle()
    val activeReminders by viewModel.activeCareReminders.collectAsStateWithLifecycle()
    val allReminders by viewModel.allCareReminders.collectAsStateWithLifecycle()
    val liveWeather by viewModel.liveWeather.collectAsStateWithLifecycle()
    val climateRecs by viewModel.climateRecommendations.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(CalendarTabMode.TIMELINES) }
    var showAddCropDialog by remember { mutableStateOf(false) }
    var showAddCareTaskDialog by remember { mutableStateOf(false) }
    var selectedCropForDetail by remember { mutableStateOf<PlantingCalendarEntry?>(null) }
    var filterCategory by remember { mutableStateOf<CareTaskType?>(null) }

    val completedCount = remember(allReminders) { allReminders.count { it.isCompleted } }
    val pendingCount = remember(allReminders) { allReminders.count { !it.isCompleted } }
    val displayedTasks = remember(allReminders, filterCategory) {
        if (filterCategory == null) allReminders
        else allReminders.filter { it.taskTypeKey == filterCategory?.typeKey }
    }

    Scaffold(
        containerColor = DeepForestBg,
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick add task button
                SmallFloatingActionButton(
                    onClick = { showAddCareTaskDialog = true },
                    containerColor = SoftGrass,
                    contentColor = TextLight,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_add_care_task")
                ) {
                    Icon(Icons.Filled.AddAlert, "Add Care Task", modifier = Modifier.size(20.dp))
                }

                // Primary Add Crop Batch FAB
                FloatingActionButton(
                    onClick = { showAddCropDialog = true },
                    containerColor = GlowingEmerald,
                    contentColor = DeepForestBg,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_add_crop_batch")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Spa, "Add Crop Batch", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Crop Batch", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                // --- Top Hero Header & Microclimate Integration Bar ---
                CropCalendarHeroHeader(
                    weather = liveWeather,
                    activeCropCount = activeCrops.size,
                    pendingCareTaskCount = pendingCount,
                    onAdaptClimateClick = {
                        viewModel.applyClimateCareToActiveTasks()
                    }
                )
            }

            item {
                // --- Tab Mode Selector ---
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = CardSurfaceDark,
                    contentColor = GlowingEmerald,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = GlowingEmerald,
                            height = 3.dp
                        )
                    },
                    divider = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    CalendarTabMode.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(tab.iconEmoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tab.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == tab) GlowingEmerald else TextMuted
                                    )
                                }
                            },
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }

            // --- TAB 1: CROP TIMELINES & ACTIVE PHENOLOGICAL STAGES ---
            if (selectedTab == CalendarTabMode.TIMELINES) {
                if (activeCrops.isEmpty()) {
                    item {
                        EmptyCropsBanner(onAddCrop = { showAddCropDialog = true })
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACTIVE CROPS (${activeCrops.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Tap crop for stage care details",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    items(activeCrops, key = { it.id }) { crop ->
                        CropLifecycleCard(
                            crop = crop,
                            weather = liveWeather,
                            recommendation = climateRecs.firstOrNull { it.cropName == crop.cropName },
                            onClick = { selectedCropForDetail = crop },
                            onAdvanceStage = { newStage ->
                                viewModel.advanceCropStage(crop.id, newStage, 0)
                            }
                        )
                    }
                }
            }

            // --- TAB 2: CLIMATE CARE REMINDERS & TASK CHECKLIST ---
            if (selectedTab == CalendarTabMode.CARE_REMINDERS) {
                item {
                    // Category Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = filterCategory == null,
                                onClick = { filterCategory = null },
                                label = { Text("All Tasks (${allReminders.size})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GlowingEmerald,
                                    selectedLabelColor = DeepForestBg,
                                    containerColor = CardSurfaceDark,
                                    labelColor = TextLight
                                )
                            )
                        }
                        items(CareTaskType.entries) { type ->
                            FilterChip(
                                selected = filterCategory == type,
                                onClick = { filterCategory = if (filterCategory == type) null else type },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(type.iconEmoji, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(type.title.substringBefore("&").trim(), fontSize = 11.sp)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = type.color,
                                    selectedLabelColor = Color.Black,
                                    containerColor = CardSurfaceDark,
                                    labelColor = TextLight
                                )
                            )
                        }
                    }
                }

                item {
                    // Progress & completion counter
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SoftGrass),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Care Schedule Fulfillment", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 13.sp)
                                Text("$completedCount completed • $pendingCount upcoming/pending", color = TextMuted, fontSize = 11.sp)
                            }
                            val progress = if (allReminders.isNotEmpty()) completedCount.toFloat() / allReminders.size.toFloat() else 0f
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.size(36.dp),
                                    color = GlowingEmerald,
                                    trackColor = SoftGrass,
                                    strokeWidth = 3.5.dp
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlowingEmerald
                                )
                            }
                        }
                    }
                }

                if (displayedTasks.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎉", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("All caught up with care tasks!", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 14.sp)
                                Text("No pending reminders under selected filter.", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    items(displayedTasks, key = { it.id }) { reminder ->
                        CareReminderCard(
                            reminder = reminder,
                            onToggle = { isDone -> viewModel.toggleCareTaskCompleted(reminder.id, isDone) },
                            onDelete = { viewModel.deleteCareTask(reminder.id) }
                        )
                    }
                }
            }

            // --- TAB 3: CALENDAR MATRIX VIEW ---
            if (selectedTab == CalendarTabMode.CALENDAR_VIEW) {
                item {
                    CropCalendarMatrixView(
                        activeCrops = activeCrops,
                        reminders = allReminders,
                        onCropSelect = { selectedCropForDetail = it }
                    )
                }
            }

            // --- TAB 4: PHENOLOGICAL STAGE ALMANAC ---
            if (selectedTab == CalendarTabMode.STAGE_ALMANAC) {
                item {
                    Text(
                        text = "PHENOLOGICAL GROWTH STAGES ALMANAC",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }

                items(CropPhenologyStage.entries) { stage ->
                    PhenologyStageAlmanacCard(stage = stage)
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // --- DIALOG: ADD NEW CROP PLANTING BATCH ---
    if (showAddCropDialog) {
        AddCropBatchDialog(
            onDismiss = { showAddCropDialog = false },
            onConfirm = { name, variety, species, pDate, hDate, stage, plot, method, days, targetYield, notes ->
                viewModel.registerCropPlantingBatch(
                    cropName = name,
                    variety = variety,
                    speciesKey = species,
                    plantingDate = pDate,
                    estimatedHarvestDate = hDate,
                    currentStageKey = stage,
                    plotLocation = plot,
                    plantingMethod = method,
                    totalExpectedDurationDays = days,
                    targetYieldKg = targetYield,
                    notes = notes
                ) {
                    showAddCropDialog = false
                }
            }
        )
    }

    // --- DIALOG: ADD CUSTOM CARE TASK ---
    if (showAddCareTaskDialog) {
        AddCareTaskDialog(
            activeCrops = activeCrops,
            onDismiss = { showAddCareTaskDialog = false },
            onConfirm = { cropId, cropName, title, desc, type, dueDate, interval, priority ->
                viewModel.addCustomCareReminder(
                    calendarEntryId = cropId,
                    cropName = cropName,
                    title = title,
                    description = desc,
                    taskType = type,
                    dueDate = dueDate,
                    intervalDays = interval,
                    priority = priority
                )
                showAddCareTaskDialog = false
            }
        )
    }

    // --- BOTTOM SHEET / MODAL: CROP DETAIL & STAGE ADVANCEMENT ---
    selectedCropForDetail?.let { crop ->
        CropDetailBottomSheet(
            crop = crop,
            weather = liveWeather,
            recommendation = climateRecs.firstOrNull { it.cropName == crop.cropName },
            reminders = allReminders.filter { it.calendarEntryId == crop.id },
            onDismiss = { selectedCropForDetail = null },
            onAdvanceStage = { newStage ->
                viewModel.advanceCropStage(crop.id, newStage, 0)
                selectedCropForDetail = crop.copy(currentStageKey = newStage.stageKey, stageProgressPercent = 0)
            },
            onDeleteCrop = {
                viewModel.deleteCropPlantingBatch(crop.id)
                selectedCropForDetail = null
            },
            onToggleTask = { reminderId, isDone ->
                viewModel.toggleCareTaskCompleted(reminderId, isDone)
            }
        )
    }
}

// =============================================================================
// --- SUB-COMPONENTS: CARDS, TIMELINES, ALMANAC & DIALOGS ---
// =============================================================================

@Composable
fun CropCalendarHeroHeader(
    weather: LiveWeatherTelemetry?,
    activeCropCount: Int,
    pendingCareTaskCount: Int,
    onAdaptClimateClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GlowingEmerald.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("crop_calendar_hero_header")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F3823),
                            CardSurfaceDark
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = GlowingEmerald.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🌾", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Crop Planting Calendar",
                                fontWeight = FontWeight.Bold,
                                color = TextLight,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Growth Stages & Climate-Adaptive Care",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Adapt to Weather action chip
                    FilledTonalButton(
                        onClick = onAdaptClimateClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = GlowingEmerald,
                            contentColor = DeepForestBg
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_adapt_climate")
                    ) {
                        Icon(Icons.Filled.Sync, "Sync with climate", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Climate-Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SoftGrass)
                Spacer(modifier = Modifier.height(12.dp))

                // Live microclimate summary row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ClimateMetricPill(
                        icon = "🌡️",
                        label = "Air Temp",
                        value = "${String.format(Locale.US, "%.1f", weather?.temperatureCelsius ?: 24.5f)}°C"
                    )
                    ClimateMetricPill(
                        icon = "💧",
                        label = "Humidity",
                        value = "${String.format(Locale.US, "%.0f", weather?.relativeHumidity ?: 62f)}%"
                    )
                    ClimateMetricPill(
                        icon = "💨",
                        label = "VPD",
                        value = "${String.format(Locale.US, "%.2f", weather?.vaporPressureDeficitKPa ?: 1.15f)} kPa"
                    )
                    ClimateMetricPill(
                        icon = "🌱",
                        label = "Soil Moisture",
                        value = "${String.format(Locale.US, "%.0f", weather?.soilMoisturePercentage ?: 42f)}%"
                    )
                }
            }
        }
    }
}

@Composable
fun ClimateMetricPill(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(2.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextLight)
        }
        Text(label, fontSize = 9.sp, color = TextMuted)
    }
}

@Composable
fun CropLifecycleCard(
    crop: PlantingCalendarEntry,
    weather: LiveWeatherTelemetry?,
    recommendation: ClimateCareRecommendation?,
    onClick: () -> Unit,
    onAdvanceStage: (CropPhenologyStage) -> Unit
) {
    val stage = remember(crop.currentStageKey) { CropPhenologyStage.fromKey(crop.currentStageKey) }
    val stageIndex = remember(stage) { CropPhenologyStage.entries.indexOf(stage) }
    val totalStages = CropPhenologyStage.entries.size

    val species = remember(crop.speciesKey) {
        try { CropSpecies.valueOf(crop.speciesKey) } catch (e: Exception) { CropSpecies.TOMATO }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SoftGrass),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("crop_card_${crop.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Emoji, Name, Plot & Stage Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SoftGrass,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(species.iconEmoji, fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = crop.cropName,
                            fontWeight = FontWeight.Bold,
                            color = TextLight,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${crop.variety} • ${crop.plotLocation}",
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                // Stage Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GlowingEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, GlowingEmerald.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stage.iconEmoji, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stage.displayName.substringBefore("&").trim(),
                            color = GlowingEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Phenological Stage Step Indicator Bar (6 segments)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CropPhenologyStage.entries.forEachIndexed { index, phenoStage ->
                    val isPast = index < stageIndex
                    val isCurrent = index == stageIndex
                    val color = when {
                        isPast -> GlowingEmerald
                        isCurrent -> Color(0xFF64B5F6)
                        else -> SoftGrass
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stage details and stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Day ${crop.currentAgeDays} of ${crop.totalExpectedDurationDays}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
                    Text("Planted: ${crop.plantingDate}", fontSize = 10.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Est. Harvest: ${crop.estimatedHarvestDate}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFB74D))
                    Text("Accum. GDD: ${String.format(Locale.US, "%.0f", crop.accumulatedGdd)}°D", fontSize = 10.sp, color = TextMuted)
                }
            }

            // Climate adaptive callout if recommendation exists
            recommendation?.let { rec ->
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F2618),
                    border = BorderStroke(1.dp, GlowingEmerald.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌦️", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Climate Optimal Care:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlowingEmerald
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = rec.irrigationAdjustment,
                            fontSize = 10.sp,
                            color = TextLight,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        rec.diseaseRiskAlert?.let { alert ->
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = alert,
                                fontSize = 10.sp,
                                color = AlertWarning,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Quick Actions: Advance Stage & View Sheet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Next: ${if (stageIndex < totalStages - 1) CropPhenologyStage.entries[stageIndex + 1].displayName else "Harvest Finished"}",
                    fontSize = 10.sp,
                    color = TextMuted
                )

                if (stageIndex < totalStages - 1) {
                    val nextStage = CropPhenologyStage.entries[stageIndex + 1]
                    TextButton(
                        onClick = { onAdvanceStage(nextStage) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("btn_advance_stage_${crop.id}")
                    ) {
                        Text("Advance to ${nextStage.displayName.substringBefore("&").trim()} →", fontSize = 11.sp, color = GlowingEmerald, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CareReminderCard(
    reminder: CropCareReminder,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val taskType = remember(reminder.taskTypeKey) { CareTaskType.fromKey(reminder.taskTypeKey) }
    val isPastDue = remember(reminder.dueDate, reminder.isCompleted) {
        if (reminder.isCompleted) false
        else {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Date())
                reminder.dueDate < todayStr
            } catch (e: Exception) { false }
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isCompleted) Color(0xFF14241B) else CardSurfaceDark
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (reminder.isCompleted) GlowingEmerald.copy(alpha = 0.3f)
            else if (isPastDue) AlertCritical.copy(alpha = 0.6f)
            else SoftGrass
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("care_reminder_${reminder.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Completion Checkbox
            Checkbox(
                checked = reminder.isCompleted,
                onCheckedChange = { onToggle(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = GlowingEmerald,
                    uncheckedColor = TextMuted,
                    checkmarkColor = DeepForestBg
                ),
                modifier = Modifier.testTag("chk_reminder_${reminder.id}")
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title and Crop Name Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reminder.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (reminder.isCompleted) TextMuted else TextLight,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Task Type Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = taskType.color.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, taskType.color.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(taskType.iconEmoji, fontSize = 9.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = taskType.title.substringBefore("&").trim(),
                                color = taskType.color,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "${reminder.cropName} • Due: ${reminder.dueDate}",
                    color = if (isPastDue) AlertCritical else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = if (isPastDue) FontWeight.Bold else FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reminder.description,
                    color = TextLight.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // Climate trigger callout chip
                if (reminder.climateTriggerReason.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F2618),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🌦️", fontSize = 9.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = reminder.climateTriggerReason,
                                fontSize = 10.sp,
                                color = GlowingEmerald,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Delete Action
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("btn_del_reminder_${reminder.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete task",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CropCalendarMatrixView(
    activeCrops: List<PlantingCalendarEntry>,
    reminders: List<CropCareReminder>,
    onCropSelect: (PlantingCalendarEntry) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SoftGrass),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "CROP SCHEDULE & MILESTONE TIMELINE",
                fontWeight = FontWeight.Bold,
                color = TextLight,
                fontSize = 13.sp
            )
            Text(
                text = "Tracking phenological milestones across active plantings",
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (activeCrops.isEmpty()) {
                Text("No active crops scheduled.", color = TextMuted, fontSize = 12.sp)
            } else {
                activeCrops.forEach { crop ->
                    val stage = CropPhenologyStage.fromKey(crop.currentStageKey)
                    val cropReminders = reminders.filter { it.calendarEntryId == crop.id }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DeepForestBg,
                        border = BorderStroke(1.dp, SoftGrass),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onCropSelect(crop) }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(stage.iconEmoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(crop.cropName, fontWeight = FontWeight.Bold, color = TextLight, fontSize = 12.sp)
                                }
                                Text(
                                    text = "Harvest: ${crop.estimatedHarvestDate}",
                                    fontSize = 10.sp,
                                    color = GlowingEmerald,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Mini Progress Bar
                            LinearProgressIndicator(
                                progress = { (crop.currentAgeDays.toFloat() / crop.totalExpectedDurationDays.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = GlowingEmerald,
                                trackColor = SoftGrass
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Stage: ${stage.displayName}", fontSize = 10.sp, color = TextMuted)
                                Text("${cropReminders.count { !it.isCompleted }} care tasks pending", fontSize = 10.sp, color = Color(0xFF64B5F6))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhenologyStageAlmanacCard(stage: CropPhenologyStage) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SoftGrass),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stage.iconEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stage.displayName,
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SoftGrass
                ) {
                    Text(
                        text = "~${stage.typicalDurationDays} Days",
                        color = GlowingEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stage.physiologicalFocus,
                fontSize = 11.sp,
                color = TextLight,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = SoftGrass)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("💧 Water Demand", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF29B6F6))
                    Text(stage.waterDemandLevel, fontSize = 10.sp, color = TextMuted)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("🧪 Nutrient Target", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF66BB6A))
                    Text(stage.nutrientFocus.substringBefore("."), fontSize = 10.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "🛡️ Key Risks: ${stage.commonRisks.joinToString(" • ")}",
                fontSize = 10.sp,
                color = Color(0xFFFFB74D)
            )
        }
    }
}

@Composable
fun EmptyCropsBanner(onAddCrop: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SoftGrass),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🌱", fontSize = 42.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No Active Crop Plantings",
                fontWeight = FontWeight.Bold,
                color = TextLight,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Start tracking your crop lifecycle with phenological growth stages and climate-adaptive care schedules.",
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddCrop,
                colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald, contentColor = DeepForestBg),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_empty_add_crop")
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Schedule First Crop Batch", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// --- DIALOG: ADD CROP PLANTING BATCH ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCropBatchDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        variety: String,
        species: String,
        plantingDate: String,
        harvestDate: String,
        stage: String,
        plot: String,
        method: String,
        durationDays: Int,
        targetYield: Float,
        notes: String
    ) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = remember { sdf.format(Date()) }
    val defaultHarvest = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 90) }
        sdf.format(cal.time)
    }

    var selectedSpecies by remember { mutableStateOf(CropSpecies.TOMATO) }
    var cropName by remember { mutableStateOf(selectedSpecies.commonName) }
    var variety by remember { mutableStateOf("Heirloom Standard") }
    var plotLocation by remember { mutableStateOf("Polyhouse Bed 1") }
    var plantingDate by remember { mutableStateOf(today) }
    var harvestDate by remember { mutableStateOf(defaultHarvest) }
    var selectedStage by remember { mutableStateOf(CropPhenologyStage.GERMINATION) }
    var plantingMethod by remember { mutableStateOf("Transplant") }
    var totalDurationDays by remember { mutableStateOf("90") }
    var targetYieldKg by remember { mutableStateOf("45.0") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌱", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Schedule New Crop Batch", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Preset species selector
                Text("Select Crop Species:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CropSpecies.entries) { sp ->
                        FilterChip(
                            selected = selectedSpecies == sp,
                            onClick = {
                                selectedSpecies = sp
                                cropName = "${sp.commonName} Crop"
                            },
                            label = { Text("${sp.iconEmoji} ${sp.commonName}", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GlowingEmerald,
                                selectedLabelColor = DeepForestBg,
                                containerColor = DeepForestBg,
                                labelColor = TextLight
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = cropName,
                    onValueChange = { cropName = it },
                    label = { Text("Crop Batch Name", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlowingEmerald,
                        unfocusedBorderColor = SoftGrass,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_crop_name")
                )

                OutlinedTextField(
                    value = variety,
                    onValueChange = { variety = it },
                    label = { Text("Variety / Hybrid", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlowingEmerald,
                        unfocusedBorderColor = SoftGrass,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = plantingDate,
                        onValueChange = { plantingDate = it },
                        label = { Text("Plant Date (YYYY-MM-DD)", fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = harvestDate,
                        onValueChange = { harvestDate = it },
                        label = { Text("Harvest Date", fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = plotLocation,
                    onValueChange = { plotLocation = it },
                    label = { Text("Plot / Greenhouse Location", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlowingEmerald,
                        unfocusedBorderColor = SoftGrass,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Current Growth Stage:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CropPhenologyStage.entries) { stage ->
                        FilterChip(
                            selected = selectedStage == stage,
                            onClick = { selectedStage = stage },
                            label = { Text("${stage.iconEmoji} ${stage.displayName.substringBefore("&").trim()}", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GlowingEmerald,
                                selectedLabelColor = DeepForestBg,
                                containerColor = DeepForestBg,
                                labelColor = TextLight
                            )
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalDurationDays,
                        onValueChange = { totalDurationDays = it },
                        label = { Text("Total Days", fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetYieldKg,
                        onValueChange = { targetYieldKg = it },
                        label = { Text("Target Yield (kg)", fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Agronomic Notes (Optional)", fontSize = 11.sp) },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlowingEmerald,
                        unfocusedBorderColor = SoftGrass,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cropName.isNotBlank()) {
                        onConfirm(
                            cropName.trim(),
                            variety.trim(),
                            selectedSpecies.name,
                            plantingDate.trim(),
                            harvestDate.trim(),
                            selectedStage.stageKey,
                            plotLocation.trim(),
                            plantingMethod,
                            totalDurationDays.toIntOrNull() ?: 90,
                            targetYieldKg.toFloatOrNull() ?: 45.0f,
                            notes.trim()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald, contentColor = DeepForestBg),
                modifier = Modifier.testTag("btn_confirm_add_crop")
            ) {
                Text("Schedule Planting", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

// --- DIALOG: ADD CUSTOM CARE TASK ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCareTaskDialog(
    activeCrops: List<PlantingCalendarEntry>,
    onDismiss: () -> Unit,
    onConfirm: (
        cropId: Long,
        cropName: String,
        title: String,
        desc: String,
        type: CareTaskType,
        dueDate: String,
        interval: Int,
        priority: String
    ) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = remember { sdf.format(Date()) }

    var selectedCrop by remember { mutableStateOf(activeCrops.firstOrNull()) }
    var selectedType by remember { mutableStateOf(CareTaskType.IRRIGATION) }
    var title by remember { mutableStateOf("Hydration Cycle") }
    var description by remember { mutableStateOf("Standard micro-drip irrigation.") }
    var dueDate by remember { mutableStateOf(today) }
    var intervalDays by remember { mutableStateOf("3") }
    var priority by remember { mutableStateOf("MEDIUM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💧", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Schedule Care Task Reminder", fontWeight = FontWeight.Bold, color = TextLight, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (activeCrops.isNotEmpty()) {
                    Text("Select Target Crop:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(activeCrops) { crop ->
                            FilterChip(
                                selected = selectedCrop?.id == crop.id,
                                onClick = { selectedCrop = crop },
                                label = { Text(crop.cropName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GlowingEmerald,
                                    selectedLabelColor = DeepForestBg,
                                    containerColor = DeepForestBg,
                                    labelColor = TextLight
                                )
                            )
                        }
                    }
                }

                Text("Care Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CareTaskType.entries) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                title = "${type.title.substringBefore("&").trim()} Routine"
                            },
                            label = { Text("${type.iconEmoji} ${type.title.substringBefore("&").trim()}", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = type.color,
                                selectedLabelColor = Color.Black,
                                containerColor = DeepForestBg,
                                labelColor = TextLight
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlowingEmerald,
                        unfocusedBorderColor = SoftGrass,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Care Instructions", fontSize = 11.sp) },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlowingEmerald,
                        unfocusedBorderColor = SoftGrass,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date (YYYY-MM-DD)", fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = intervalDays,
                        onValueChange = { intervalDays = it },
                        label = { Text("Repeat (Days)", fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Priority Level:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("HIGH", "MEDIUM", "LOW").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (p == "HIGH") AlertCritical else if (p == "MEDIUM") AlertWarning else GlowingEmerald,
                                selectedLabelColor = Color.White,
                                containerColor = DeepForestBg,
                                labelColor = TextLight
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val cId = selectedCrop?.id ?: 0L
                        val cName = selectedCrop?.cropName ?: "General Crop"
                        onConfirm(
                            cId,
                            cName,
                            title.trim(),
                            description.trim(),
                            selectedType,
                            dueDate.trim(),
                            intervalDays.toIntOrNull() ?: 3,
                            priority
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald, contentColor = DeepForestBg),
                modifier = Modifier.testTag("btn_confirm_add_task")
            ) {
                Text("Schedule Task", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

// --- BOTTOM SHEET: CROP DETAIL & STAGE CARE DEEP DIVE ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropDetailBottomSheet(
    crop: PlantingCalendarEntry,
    weather: LiveWeatherTelemetry?,
    recommendation: ClimateCareRecommendation?,
    reminders: List<CropCareReminder>,
    onDismiss: () -> Unit,
    onAdvanceStage: (CropPhenologyStage) -> Unit,
    onDeleteCrop: () -> Unit,
    onToggleTask: (Long, Boolean) -> Unit
) {
    val stage = remember(crop.currentStageKey) { CropPhenologyStage.fromKey(crop.currentStageKey) }
    val stageIndex = remember(stage) { CropPhenologyStage.entries.indexOf(stage) }
    val totalStages = CropPhenologyStage.entries.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardSurfaceDark,
        dragHandle = { BottomSheetDefaults.DragHandle(color = SoftGrass) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = crop.cropName,
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${crop.variety} • ${crop.plotLocation}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = onDeleteCrop,
                    modifier = Modifier.testTag("btn_delete_crop_batch")
                ) {
                    Icon(Icons.Filled.DeleteOutline, "Delete Crop", tint = AlertCritical)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stage Banner Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DeepForestBg,
                border = BorderStroke(1.dp, GlowingEmerald.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stage.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(stage.displayName, fontWeight = FontWeight.Bold, color = TextLight, fontSize = 14.sp)
                                Text("Stage ${stageIndex + 1} of $totalStages", color = GlowingEmerald, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stage.physiologicalFocus, fontSize = 11.sp, color = TextLight, lineHeight = 15.sp)

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = SoftGrass)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("💧 Water Demand", fontSize = 10.sp, color = Color(0xFF29B6F6), fontWeight = FontWeight.Bold)
                            Text(stage.waterDemandLevel, fontSize = 11.sp, color = TextLight)
                        }
                        Column {
                            Text("🧪 Nutrient Target", fontSize = 10.sp, color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold)
                            Text(stage.nutrientFocus.substringBefore("."), fontSize = 11.sp, color = TextLight)
                        }
                        Column {
                            Text("🌡️ GDD Acc.", fontSize = 10.sp, color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold)
                            Text("${String.format(Locale.US, "%.0f", crop.accumulatedGdd)}°D", fontSize = 11.sp, color = TextLight)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stage Advancement Selector
            Text("Advance Growth Stage:", fontWeight = FontWeight.Bold, color = TextMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(CropPhenologyStage.entries) { phenoStage ->
                    val isCurrent = phenoStage == stage
                    FilterChip(
                        selected = isCurrent,
                        onClick = { onAdvanceStage(phenoStage) },
                        label = { Text("${phenoStage.iconEmoji} ${phenoStage.displayName.substringBefore("&").trim()}", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GlowingEmerald,
                            selectedLabelColor = DeepForestBg,
                            containerColor = DeepForestBg,
                            labelColor = TextLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Associated Care Tasks Checklist for this crop
            Text("Scheduled Care Tasks (${reminders.size}):", fontWeight = FontWeight.Bold, color = TextMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))

            if (reminders.isEmpty()) {
                Text("No tasks scheduled for this crop.", color = TextMuted, fontSize = 11.sp)
            } else {
                reminders.forEach { reminder ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DeepForestBg,
                        border = BorderStroke(1.dp, if (reminder.isCompleted) GlowingEmerald.copy(alpha = 0.3f) else SoftGrass),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = reminder.isCompleted,
                                onCheckedChange = { isChecked -> onToggleTask(reminder.id, isChecked) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = GlowingEmerald,
                                    uncheckedColor = TextMuted
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reminder.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (reminder.isCompleted) TextMuted else TextLight
                                )
                                Text(
                                    text = "Due: ${reminder.dueDate} • ${reminder.description}",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
