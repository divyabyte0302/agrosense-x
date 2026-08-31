package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.Plant
import com.example.ui.theme.*
import com.example.ui.translation.AgroStrings
import com.example.ui.translation.CommonCropsRegistry
import com.example.ui.translation.CropPredictionCandidate

@Composable
fun ScannedPlantIdentificationCard(
    currentPlant: Plant?,
    predictedIssue: String,
    appLanguage: AppLanguage,
    onUpdatePlantName: (newName: String) -> Unit,
    onCreateNewPlant: (name: String, variety: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTamil = appLanguage == AppLanguage.TAMIL

    // Extract recommendations based on detected issue or current plant name
    val candidates = remember(predictedIssue, currentPlant) {
        val query = if (predictedIssue.isNotBlank()) predictedIssue else currentPlant?.name ?: "Tomato"
        CommonCropsRegistry.getBestMatchesFor(query)
    }

    var selectedCandidate by remember(candidates) {
        mutableStateOf<CropPredictionCandidate?>(candidates.firstOrNull())
    }

    var showManualEntry by remember { mutableStateOf(false) }
    var manualNameInput by remember { mutableStateOf("") }
    var manualVarietyInput by remember { mutableStateOf("") }
    var lastUpdatedName by remember { mutableStateOf<String?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.5.dp, GlowingEmerald.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("scanned_plant_identification_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with AI badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(GlowingEmerald.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "AI Prediction",
                            tint = GlowingEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isTamil) "AI கணிக்கப்பட்ட பயிர் அடையாளம்" else "AI PREDICTED PLANT IDENTITY",
                            color = GlowingEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isTamil) "பரிந்துரைகளில் இருந்து தேர்வு செய்யவும்" else "Tap a recommendation or name manually",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    color = GlowingEmerald.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GlowingEmerald.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (isTamil) "AI துல்லியம்" else "AI MATCH",
                        color = GlowingEmerald,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current Registered Crop Banner
            Surface(
                color = DeepForestBg,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, SoftGrass),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🌿", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = if (isTamil) "பதிவு செய்யப்பட்ட பயிர்" else "Current Active Crop",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                            Text(
                                text = currentPlant?.name ?: "Unknown Crop",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (lastUpdatedName != null) {
                        Surface(
                            color = GlowingEmerald.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isTamil) "புதுப்பிக்கப்பட்டது ✓" else "Updated ✓",
                                color = GlowingEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Candidate Recommendations Row
            Text(
                text = if (isTamil) "AI பரிந்துரைக்கும் பயிர் பெயர்கள்:" else "AI Recommended Crop Candidates:",
                color = TextLight,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(candidates.take(5)) { candidate ->
                    val isSelected = selectedCandidate == candidate
                    Surface(
                        onClick = {
                            selectedCandidate = candidate
                            val chosenName = if (isTamil) "${candidate.tamilName} (${candidate.englishName})" else "${candidate.englishName} (${candidate.tamilName})"
                            onUpdatePlantName(chosenName)
                            lastUpdatedName = chosenName
                            val msg = if (isTamil) "பயிர் பெயர் புதுப்பிக்கப்பட்டது: $chosenName" else "Crop name updated to: $chosenName"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        color = if (isSelected) GlowingEmerald.copy(alpha = 0.2f) else DeepForestBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) GlowingEmerald else SoftGrass
                        ),
                        modifier = Modifier
                            .width(130.dp)
                            .testTag("crop_candidate_${candidate.englishName.lowercase().replace(" ", "_")}")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = candidate.iconEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isTamil) candidate.tamilName else candidate.englishName,
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isTamil) candidate.englishName else candidate.tamilName,
                                color = TextMuted,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GlowingEmerald.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "${candidate.defaultConfidence}% Match",
                                    color = GlowingEmerald,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Candidate Details Card
            selectedCandidate?.let { candidate ->
                Surface(
                    color = DeepForestBg.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SoftGrass.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = candidate.iconEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${candidate.englishName} • ${candidate.tamilName}",
                                    color = GlowingEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = candidate.botanicalName,
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isTamil) candidate.descriptionTa else candidate.descriptionEn,
                            color = TextLight,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isTamil) "பரிந்துரைக்கப்படும் ரகம்: ${candidate.varietySuggestion}" else "Suggested Variety: ${candidate.varietySuggestion}",
                                color = AlertWarning,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isTamil) "மண் ஈரப்பதம்: ${candidate.typicalSoilMoistureTarget}" else "Target Moisture: ${candidate.typicalSoilMoistureTarget}",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Toggle for Manual Naming
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showManualEntry = !showManualEntry },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (showManualEntry) GlowingEmerald else TextLight
                    ),
                    border = BorderStroke(1.dp, if (showManualEntry) GlowingEmerald else SoftGrass),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("toggle_manual_naming_btn")
                ) {
                    Icon(
                        imageVector = if (showManualEntry) Icons.Filled.ExpandLess else Icons.Filled.Edit,
                        contentDescription = "Manual naming",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTamil) "✍️ கைகளால் பெயரிடுங்கள் (Manual Name)" else "✍️ Name Manually / Custom",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (selectedCandidate != null && !showManualEntry) {
                    Button(
                        onClick = {
                            val candidate = selectedCandidate ?: return@Button
                            val chosenName = if (isTamil) "${candidate.tamilName} (${candidate.englishName})" else "${candidate.englishName} (${candidate.tamilName})"
                            onUpdatePlantName(chosenName)
                            lastUpdatedName = chosenName
                            val msg = if (isTamil) "பயிர் பெயர் புதுப்பிக்கப்பட்டது: $chosenName" else "Plant name updated to: $chosenName"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("confirm_crop_recommendation_btn")
                    ) {
                        Icon(Icons.Filled.Check, null, tint = DeepForestBg, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTamil) "இப்பெயரை உறுதிசெய்" else "Confirm Name",
                            color = DeepForestBg,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Expandable Manual Naming Field
            AnimatedVisibility(
                visible = showManualEntry,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Surface(
                        color = DeepForestBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GlowingEmerald.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isTamil) "பயிரின் பெயரை உள்ளிடவும்:" else "Enter Custom Crop Name:",
                                color = GlowingEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = manualNameInput,
                                onValueChange = { manualNameInput = it },
                                placeholder = {
                                    Text(
                                        text = if (isTamil) "எ.கா: நாட்டு கத்திரி தோட்டம் 2 / Hybrid Tomato" else "e.g. Hybrid Tomato Plot 2 / நாட்டு கத்திரி",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight,
                                    focusedBorderColor = GlowingEmerald,
                                    unfocusedBorderColor = SoftGrass
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("manual_plant_name_input"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = manualVarietyInput,
                                onValueChange = { manualVarietyInput = it },
                                placeholder = {
                                    Text(
                                        text = if (isTamil) "விருப்பத்தேர்வு: பயிர் ரகம் (எ.கா: PKM-1 / F1 Hybrid)" else "Optional: Variety (e.g. Arka Rakshak / F1)",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight,
                                    focusedBorderColor = GlowingEmerald,
                                    unfocusedBorderColor = SoftGrass
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("manual_plant_variety_input"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (manualNameInput.isNotBlank()) {
                                            onUpdatePlantName(manualNameInput.trim())
                                            lastUpdatedName = manualNameInput.trim()
                                            showManualEntry = false
                                            val msg = if (isTamil) "பயிர் பெயர் மாற்றப்பட்டது: $manualNameInput" else "Plant name updated to: $manualNameInput"
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, if (isTamil) "தயவுசெய்து பெயரை உள்ளிடவும்" else "Please enter a valid name", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_manual_plant_name_btn")
                                ) {
                                    Icon(Icons.Filled.Save, null, tint = DeepForestBg, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isTamil) "இப்பயிரைப் புதுப்பி" else "Update Active Crop",
                                        color = DeepForestBg,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (manualNameInput.isNotBlank()) {
                                            onCreateNewPlant(
                                                manualNameInput.trim(),
                                                if (manualVarietyInput.isNotBlank()) manualVarietyInput.trim() else "Farmer Cultivar"
                                            )
                                            lastUpdatedName = manualNameInput.trim()
                                            showManualEntry = false
                                            val msg = if (isTamil) "புதிய பயிர் பதிவு செய்யப்பட்டது: $manualNameInput" else "Registered new crop: $manualNameInput"
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, if (isTamil) "தயவுசெய்து பெயரை உள்ளிடவும்" else "Please enter a valid name", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GlowingEmerald),
                                    border = BorderStroke(1.dp, GlowingEmerald),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("create_new_plant_from_scan_btn")
                                ) {
                                    Icon(Icons.Filled.Add, null, tint = GlowingEmerald, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isTamil) "புதிய பயிராகச் சேர்" else "Add As New Crop",
                                        color = GlowingEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
