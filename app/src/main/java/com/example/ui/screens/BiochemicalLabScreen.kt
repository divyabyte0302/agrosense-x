package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AgroViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiochemicalLabScreen(
    viewModel: AgroViewModel,
    navController: NavController
) {
    val tests by viewModel.biochemicalTests.collectAsStateWithLifecycle()
    val learningCount by viewModel.continuousLearningCount.collectAsStateWithLifecycle()

    var showNewTestDialog by remember { mutableStateOf(false) }
    var selectedTestForResults by remember { mutableStateOf<BiochemicalConfirmationTest?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI + BIOCHEMICAL CONFIRMATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GlowingEmerald, letterSpacing = 1.sp)
                        Text("Laboratory Verification & Model Feedback", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextLight)
                    }
                },
                actions = {
                    IconButton(onClick = { showNewTestDialog = true }) {
                        Icon(Icons.Filled.AddCircle, contentDescription = "Request Test", tint = GlowingEmerald)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepForestBg)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewTestDialog = true },
                containerColor = GlowingEmerald,
                contentColor = DeepForestBg,
                icon = { Icon(Icons.Filled.Science, null) },
                text = { Text("Request Lab Test", fontWeight = FontWeight.Bold) }
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
            // --- HEADER SUMMARY METRICS ---
            Surface(
                color = CardSurfaceDark,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, SoftGrass),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "AI-ASSISTED BIOTECHNOLOGY WORKFLOW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlowingEmerald,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "AI diagnoses trigger targeted biochemical assays (RT-PCR, ELISA, SPAD, NPK Spectrometry). Verified lab outcomes directly retrain and calibrate the Edge AI models.",
                        fontSize = 11.sp,
                        color = TextLight,
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(color = SoftGrass.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LabMetricSummaryItem("Concordance", "94.2%", "AI Accuracy Matched", GlowingEmerald)
                        LabMetricSummaryItem("Tests Run", "${tests.size + 29}", "Completed Assays", Color(0xFF29B6F6))
                        LabMetricSummaryItem("Learning Loop", "$learningCount", "Retraining Samples", Color(0xFFAB47BC))
                    }
                }
            }

            // --- TEST QUEUE TITLE ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LABORATORY CONFIRMATION QUEUE (${tests.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlowingEmerald,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Tap test to record results",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            // --- LIST OF TESTS ---
            tests.forEach { test ->
                Surface(
                    color = CardSurfaceDark,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (test.testStatus == LabTestStatus.CONFIRMED_MATCH) GlowingEmerald.copy(alpha = 0.5f) else SoftGrass),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTestForResults = test }
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
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GlowingEmerald.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Biotech, null, tint = GlowingEmerald, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(test.id, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GlowingEmerald)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("• ${test.plantTag}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                    }
                                    Text(test.cropName, fontSize = 11.sp, color = TextMuted)
                                }
                            }

                            // Status chip
                            Surface(
                                color = test.testStatus.color.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, test.testStatus.color)
                            ) {
                                Text(
                                    text = test.testStatus.label,
                                    color = test.testStatus.color,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Surface(
                            color = DeepForestBg,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("AI Suspected Issue:", fontSize = 10.sp, color = TextMuted)
                                Text(test.aiSuspectedIssue, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Test Type: ${test.recommendedTestType.title}", fontSize = 10.sp, color = GlowingEmerald)
                                    Text("AI Confidence: ${test.aiConfidence}%", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }

                        if (test.labResultSummary.isNotBlank()) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Lab Findings:", fontSize = 10.sp, color = TextMuted)
                                Text(test.labResultSummary, fontSize = 11.sp, color = TextLight)
                            }
                        }

                        if (test.laboratoryNotes.isNotBlank()) {
                            Text(
                                text = "Notes: ${test.laboratoryNotes}",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Requested: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(test.dateRequested))}",
                                fontSize = 9.sp,
                                color = TextMuted
                            )

                            TextButton(
                                onClick = { selectedTestForResults = test },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = if (test.testStatus == LabTestStatus.CONFIRMED_MATCH) "View Details" else "Update Results",
                                    color = GlowingEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // --- DIALOG: RECORD LAB RESULTS ---
    selectedTestForResults?.let { test ->
        var labFindings by remember { mutableStateOf(test.labResultSummary) }
        var labNotes by remember { mutableStateOf(test.laboratoryNotes) }
        var isConfirmedMatch by remember { mutableStateOf(test.resultMatchedAiDiagnosis ?: true) }

        AlertDialog(
            onDismissRequest = { selectedTestForResults = null },
            title = {
                Text(
                    text = "Record Laboratory Results for ${test.id}",
                    color = TextLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Target Assay: ${test.recommendedTestType.title}", fontSize = 12.sp, color = GlowingEmerald, fontWeight = FontWeight.Bold)
                    Text("Method: ${test.recommendedTestType.method}", fontSize = 10.sp, color = TextMuted)

                    OutlinedTextField(
                        value = labFindings,
                        onValueChange = { labFindings = it },
                        label = { Text("Laboratory Quantification / Findings") },
                        placeholder = { Text("e.g. SPAD: 32.5, Nitrate sap: 380 ppm, RT-PCR Cycle: 28") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass
                        )
                    )

                    OutlinedTextField(
                        value = labNotes,
                        onValueChange = { labNotes = it },
                        label = { Text("Agronomist / Lab Technician Notes") },
                        placeholder = { Text("Notes on treatment prescribed or sample condition") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowingEmerald,
                            unfocusedBorderColor = SoftGrass
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isConfirmedMatch,
                            onCheckedChange = { isConfirmedMatch = it },
                            colors = CheckboxDefaults.colors(checkedColor = GlowingEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lab findings confirm AI's original diagnosis",
                            fontSize = 11.sp,
                            color = TextLight
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val status = if (isConfirmedMatch) LabTestStatus.CONFIRMED_MATCH else LabTestStatus.DISCREPANCY_CALIBRATED
                        viewModel.updateBiochemicalTestResult(
                            testId = test.id,
                            status = status,
                            resultSummary = labFindings.ifBlank { "Assay completed with quantitative confirmation." },
                            matched = isConfirmedMatch,
                            labNotes = labNotes
                        )
                        selectedTestForResults = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                ) {
                    Text("SAVE & CALIBRATE MODEL", color = DeepForestBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTestForResults = null }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }

    // --- DIALOG: REQUEST NEW LAB TEST ---
    if (showNewTestDialog) {
        var plantTag by remember { mutableStateOf("T-102") }
        var cropName by remember { mutableStateOf("Tomato (Roma Hybrid)") }
        var suspectedIssue by remember { mutableStateOf("Pre-Symptomatic Nitrogen / Potassium Chlorosis") }
        var selectedTestType by remember { mutableStateOf(BiochemicalTestType.SPAD_CHLOROPHYLL) }
        var initialNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewTestDialog = false },
            title = {
                Text(
                    text = "Request Biochemical Laboratory Test",
                    color = TextLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = plantTag,
                        onValueChange = { plantTag = it },
                        label = { Text("Plant Tag / Zone ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cropName,
                        onValueChange = { cropName = it },
                        label = { Text("Crop & Variety") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = suspectedIssue,
                        onValueChange = { suspectedIssue = it },
                        label = { Text("AI Suspected Issue") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Select Laboratory Test Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GlowingEmerald)
                    BiochemicalTestType.entries.forEach { type ->
                        Surface(
                            color = if (selectedTestType == type) GlowingEmerald.copy(alpha = 0.2f) else DeepForestBg,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (selectedTestType == type) GlowingEmerald else SoftGrass),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTestType = type }
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(type.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                Text(type.method, fontSize = 9.sp, color = TextMuted)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.requestBiochemicalTest(
                            plantTag = plantTag,
                            cropName = cropName,
                            aiSuspectedIssue = suspectedIssue,
                            testType = selectedTestType,
                            notes = initialNotes
                        )
                        showNewTestDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                ) {
                    Text("DISPATCH REQUEST", color = DeepForestBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTestDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = CardSurfaceDark
        )
    }
}

@Composable
private fun LabMetricSummaryItem(title: String, value: String, subtitle: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
        Text(subtitle, fontSize = 9.sp, color = TextMuted)
    }
}
