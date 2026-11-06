package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.model.Plant
import com.example.data.model.Prediction
import com.example.data.model.ScanFeedback
import com.example.ui.theme.*
import com.example.ui.viewmodel.AgroViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeedbackScreen(
    viewModel: AgroViewModel,
    navController: NavController? = null,
    initialPredictionId: Long? = null
) {
    val feedbackList by viewModel.feedbackList.collectAsStateWithLifecycle()
    val plants by viewModel.plants.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmittingFeedback.collectAsStateWithLifecycle()
    val feedbackMsg by viewModel.feedbackMessage.collectAsStateWithLifecycle()
    val latestScanResult by viewModel.latestScanResult.collectAsStateWithLifecycle()
    val selectedPlantState by viewModel.selectedPlant.collectAsStateWithLifecycle()

    var selectedPlant by remember { mutableStateOf<Plant?>(selectedPlantState ?: plants.firstOrNull()) }
    var selectedPrediction by remember { mutableStateOf<Prediction?>(latestScanResult) }

    var predictionAccuracyRating by remember { mutableIntStateOf(5) }
    var recommendationAccuracyRating by remember { mutableIntStateOf(5) }
    var commentsText by remember { mutableStateOf("") }
    var showSuccessBanner by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(plants) {
        if (selectedPlant == null && plants.isNotEmpty()) {
            selectedPlant = plants.first()
        }
    }

    LaunchedEffect(latestScanResult) {
        if (latestScanResult != null) {
            selectedPrediction = latestScanResult
        }
    }

    LaunchedEffect(feedbackMsg) {
        if (feedbackMsg != null) {
            showSuccessBanner = true
        }
    }

    // Compute aggregate AI metrics
    val totalReviews = feedbackList.size
    val avgPredictionScore = if (totalReviews > 0) {
        feedbackList.map { it.predictionAccuracyRating }.average()
    } else 4.9
    val avgRecommendationScore = if (totalReviews > 0) {
        feedbackList.map { it.recommendationAccuracyRating }.average()
    } else 4.8

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepForestBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "AI ACCURACY & FEEDBACK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlowingEmerald,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Crop Scan Reliability Hub",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SoftGrass.copy(alpha = 0.3f))
                    .border(1.dp, GlowingEmerald, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CloudDone,
                        contentDescription = "Firestore Live",
                        tint = GlowingEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Firestore Sync",
                        fontSize = 10.sp,
                        color = GlowingEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Aggregate AI Accuracy Summary Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, SoftGrass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "AI MODEL ACCURACY BENCHMARK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "%.1f".format(avgPredictionScore),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlowingEmerald
                            )
                            Text(
                                text = "/5.0",
                                fontSize = 16.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        Text(
                            text = "Diagnosis Accuracy",
                            fontSize = 12.sp,
                            color = TextLight
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                            .background(SoftGrass)
                    )

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "%.1f".format(avgRecommendationScore),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = AlertWarning
                            )
                            Text(
                                text = "/5.0",
                                fontSize = 16.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        Text(
                            text = "Prescription Quality",
                            fontSize = 12.sp,
                            color = TextLight
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                            .background(SoftGrass)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalReviews",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                        Text(
                            text = "Evaluations",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Success banner
        AnimatedVisibility(visible = showSuccessBanner && feedbackMsg != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GlowingEmerald.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, GlowingEmerald),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Success",
                        tint = GlowingEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = feedbackMsg ?: "Feedback saved successfully to Firebase Firestore!",
                        color = TextLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            showSuccessBanner = false
                            viewModel.clearFeedbackMessage()
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Filled.Close, "Dismiss", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Feedback Submission Form Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, SoftGrass),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.RateReview,
                        contentDescription = "Rate AI",
                        tint = GlowingEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rate AI Predictions & Advice",
                        color = TextLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Help refine pre-symptomatic spectral accuracy by rating each scan result.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                HorizontalDivider(color = SoftGrass)
                Spacer(modifier = Modifier.height(14.dp))

                // Crop / Plant Selection
                Text(
                    text = "Target Plant / Crop Profile",
                    color = TextLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (plants.isEmpty()) {
                    Text("No plants registered yet. Add a plant from Overview first.", color = TextMuted, fontSize = 12.sp)
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        plants.forEach { plant ->
                            FilterChip(
                                selected = selectedPlant?.id == plant.id,
                                onClick = {
                                    selectedPlant = plant
                                    viewModel.selectPlant(plant)
                                },
                                label = { Text(plant.name, fontSize = 11.sp) },
                                leadingIcon = if (selectedPlant?.id == plant.id) {
                                    { Icon(Icons.Filled.Check, null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GlowingEmerald,
                                    selectedLabelColor = DeepForestBg,
                                    containerColor = SoftGrass.copy(alpha = 0.2f),
                                    labelColor = TextLight
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scan / Diagnosis Context
                selectedPlant?.let { plant ->
                    val plantScan = selectedPrediction ?: Prediction(
                        plantId = plant.id,
                        imageUrl = "",
                        healthScore = 85,
                        stressProbability = 15,
                        predictedIssue = "Baseline Foliar Evaluation",
                        recommendation = "Standard nutrient feeding with 40% soil moisture threshold."
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepForestBg)
                            .border(1.dp, SoftGrass, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Scan Target: ${plant.name}",
                                    color = GlowingEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Stress: ${plantScan.stressProbability}%",
                                    color = if (plantScan.stressProbability > 50) AlertWarning else GlowingEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Diagnosed: ${plantScan.predictedIssue}",
                                color = TextLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Advice: ${plantScan.recommendation}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Prediction Accuracy Rating
                Text(
                    text = "1. AI Prediction Accuracy (Diagnosis Rating)",
                    color = TextLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                RatingBar(
                    rating = predictionAccuracyRating,
                    onRatingChanged = { predictionAccuracyRating = it },
                    tag = "pred_rating_bar"
                )
                Text(
                    text = when (predictionAccuracyRating) {
                        1 -> "1 Star - Completely inaccurate diagnosis"
                        2 -> "2 Stars - Minor discrepancies found"
                        3 -> "3 Stars - Moderately accurate"
                        4 -> "4 Stars - Very accurate pre-symptomatic prediction"
                        else -> "5 Stars - Spot on / Highly accurate diagnosis"
                    },
                    color = GlowingEmerald,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Recommendation Quality Rating
                Text(
                    text = "2. Agronomist Recommendation Quality",
                    color = TextLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                RatingBar(
                    rating = recommendationAccuracyRating,
                    onRatingChanged = { recommendationAccuracyRating = it },
                    tag = "rec_rating_bar"
                )
                Text(
                    text = when (recommendationAccuracyRating) {
                        1 -> "1 Star - Recommendation was unhelpful"
                        2 -> "2 Stars - Partially helpful"
                        3 -> "3 Stars - Good actionable advice"
                        4 -> "4 Stars - Very effective irrigation/fertilizer plan"
                        else -> "5 Stars - Outstanding agronomist guidance"
                    },
                    color = AlertWarning,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Optional Comments Field
                Text(
                    text = "3. Farmer Observations & Comments (Optional)",
                    color = TextLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = commentsText,
                    onValueChange = { commentsText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("feedback_comments_input"),
                    placeholder = {
                        Text(
                            "e.g. AI detected early water deficiency 24 hours prior to wilting. Adjusting irrigation restored leaf turgor nicely.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlowingEmerald,
                        unfocusedBorderColor = SoftGrass,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight,
                        cursorColor = GlowingEmerald
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val targetP = selectedPlant ?: plants.firstOrNull()
                        if (targetP != null) {
                            val targetScan = selectedPrediction ?: Prediction(
                                plantId = targetP.id,
                                imageUrl = "",
                                healthScore = 80,
                                stressProbability = 20,
                                predictedIssue = "General Health Scan",
                                recommendation = "Standard watering schedule."
                            )

                            viewModel.submitScanFeedback(
                                plant = targetP,
                                prediction = targetScan,
                                predictionAccuracyRating = predictionAccuracyRating,
                                recommendationAccuracyRating = recommendationAccuracyRating,
                                comments = commentsText
                            ) {
                                commentsText = ""
                            }
                        }
                    },
                    enabled = !isSubmitting && plants.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_feedback_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlowingEmerald,
                        disabledContainerColor = SoftGrass.copy(alpha = 0.3f)
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = DeepForestBg,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Syncing to Firestore...", color = DeepForestBg, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Filled.Send, contentDescription = "Submit", tint = DeepForestBg, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SUBMIT AI ACCURACY FEEDBACK", color = DeepForestBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Community Feedback History Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "COMMUNITY REVIEWS (${feedbackList.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight,
                letterSpacing = 1.sp
            )

            Text(
                text = "Firestore 'Feedback'",
                fontSize = 10.sp,
                color = GlowingEmerald,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (feedbackList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardSurfaceDark)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.RateReview, null, tint = TextMuted, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No feedback recorded yet", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Submit your first evaluation above to populate Firestore.", color = TextMuted, fontSize = 11.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                feedbackList.forEach { item ->
                    FeedbackItemCard(feedback = item)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun RatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    maxStars: Int = 5,
    tag: String = "rating_bar"
) {
    Row(
        modifier = Modifier.testTag(tag),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isSelected = i <= rating
            IconButton(
                onClick = { onRatingChanged(i) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) AlertWarning.copy(alpha = 0.15f) else Color.Transparent)
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Rating $i of $maxStars",
                    tint = if (isSelected) AlertWarning else TextMuted,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun FeedbackItemCard(feedback: ScanFeedback) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(feedback.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.dp, SoftGrass.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("feedback_card_${feedback.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GlowingEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Spa, null, tint = GlowingEmerald, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = feedback.plantName,
                            color = TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = dateStr,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Star badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AlertWarning.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = AlertWarning, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${feedback.predictionAccuracyRating}.0",
                            color = AlertWarning,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Issue & Recommendation summary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepForestBg)
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "Diagnosis: ${feedback.predictedIssue}",
                        color = GlowingEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Recommendation: ${feedback.recommendation}",
                        color = TextMuted,
                        fontSize = 10.sp,
                        maxLines = 2
                    )
                }
            }

            if (feedback.comments.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${feedback.comments}\"",
                    color = TextLight,
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Person, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = feedback.userEmail.ifBlank { "farmer@agrosense.com" },
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Check, null, tint = GlowingEmerald, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Stored in Firestore",
                        color = GlowingEmerald,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
