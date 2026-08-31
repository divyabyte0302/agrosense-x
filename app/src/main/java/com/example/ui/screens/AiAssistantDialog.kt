package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppLanguage
import com.example.ui.theme.*
import com.example.ui.translation.AgroStrings
import com.example.ui.viewmodel.AgroViewModel
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String,
    val sender: String, // "USER" or "AI"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionSuggestion: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantDialog(
    viewModel: AgroViewModel,
    onDismiss: () -> Unit,
    onNavigateToScreen: (String) -> Unit
) {
    val plants by viewModel.plants.collectAsStateWithLifecycle()
    val selectedPlant by viewModel.selectedPlant.collectAsStateWithLifecycle()
    val latestSensor by viewModel.latestSensorData.collectAsStateWithLifecycle()
    val latestPrediction by viewModel.latestPrediction.collectAsStateWithLifecycle()
    val liveWeather by viewModel.liveWeather.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val isTamil = appLanguage == AppLanguage.TAMIL

    val quickQuestions = remember(isTamil) {
        if (isTamil) {
            listOf(
                "எனது பயிர் ஏன் அழுத்தத்தில் உள்ளது?",
                "இப்போது நான் என்ன செய்ய வேண்டும்?",
                "பாதிப்பு அதிகமுள்ள பயிர்களைக் காட்டு",
                "கடைசி AI கணிப்பை விளக்குக",
                "VPD ஆவியாதல் அழுத்தம் எவ்வாறு பாதிக்கிறது?"
            )
        } else {
            listOf(
                "Why is my plant stressed?",
                "What should I check right now?",
                "Show plants with high risk",
                "Explain my latest AI prediction",
                "How does VPD affect transpiration?"
            )
        }
    }

    val initialGreeting = remember(isTamil) {
        if (isTamil) {
            "வணக்கம்! நான் உங்கள் AgroSense-X வேளாண்மை AI உதவியாளர். உங்கள் பண்ணை சென்சார் தரவுகள், இலை ஸ்கேன் மற்றும் பயிர் வளர்ச்சி மாதிரிகளை ஆய்வு செய்து தங்களுக்கு அறிவியல் பூர்வ வழிகாட்டுகிறேன். இன்று உங்களுக்கு எவ்வாறு உதவ முடியும்?"
        } else {
            "Hello! I am your AgroSense-X Agronomic Assistant. I analyze your real-time sensor telemetry, multispectral scans, and phenological models to give evidence-based advice. How can I help you today?"
        }
    }

    var messages by remember(isTamil) {
        mutableStateOf(
            listOf(
                ChatMessage(
                    id = "init_welcome",
                    sender = "AI",
                    message = initialGreeting,
                    actionSuggestion = null
                )
            )
        )
    }

    var inputPrompt by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    fun respondToQuery(query: String) {
        val userMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            sender = "USER",
            message = query
        )
        messages = messages + userMsg

        isThinking = true
        coroutineScope.launch {
            kotlinx.coroutines.delay(500) // Realistic processing delay

            val plantName = selectedPlant?.name ?: if (isTamil) "செயலில் உள்ள பயிர்" else "active crop"
            val plantTag = selectedPlant?.tag ?: "T102"
            val soilM = latestSensor?.soilMoisture ?: 42f
            val temp = liveWeather?.temperatureC ?: latestSensor?.temperature ?: 28.5f
            val humidity = liveWeather?.humidityPercent ?: latestSensor?.humidity ?: 65f
            val vpd = String.format("%.2f", 0.61078 * Math.exp((17.27 * temp) / (temp + 237.3)) * (1.0 - (humidity / 100.0)))
            val rawStressIssue = latestPrediction?.issueName ?: "Water Deficit"
            val stressIssue = AgroStrings.translateIssue(rawStressIssue, isTamil)
            val confidence = latestPrediction?.confidence ?: 86f

            val (replyText, action) = if (isTamil) {
                when {
                    query.contains("ஏன்", ignoreCase = true) || query.contains("அழுத்தம்", ignoreCase = true) || query.contains("why", ignoreCase = true) || query.contains("stress", ignoreCase = true) -> {
                        Pair(
                            "$plantName ($plantTag) பயிருக்கான நேரலை சென்சார் பகுப்பாய்வு:\n• மண் ஈரப்பதம்: ${soilM}%\n• பண்ணை வெப்பநிலை: ${temp}°C, காற்று ஈரப்பதம்: ${humidity}%\n• கணக்கிடப்பட்ட VPD: $vpd kPa\n\nAI அமைப்பு ஆரம்பநிலை இலைத்துளை மூடுதலைக் கண்டறிந்துள்ளது. மண்ணின் ஈரப்பதம் உகந்த 55% அளவுக்குக் குறைவாகவும், VPD அழுத்தம் அதிகமாகவும் இருப்பதால், இலைகள் வாடுவதற்கு முன்பே நீர் உறிஞ்சும் திறன் குறைந்துள்ளது. உடனடியாக வேர்ப்பகுதிக்கு பாசனம் செய்யவும்.",
                            "scanner"
                        )
                    }
                    query.contains("என்ன செய்ய", ignoreCase = true) || query.contains("சரிபார்க்க", ignoreCase = true) || query.contains("check", ignoreCase = true) -> {
                        Pair(
                            "$plantName பயிருக்கான உடனடி சரிபார்ப்புப் பட்டியல்:\n1. 💧 வேர்ப்பகுதியின் மண் ஈரப்பதத்தை சரிபார்க்கவும் (Soil Moisture).\n2. 🌡️ பயிர் இலை மற்றும் காற்று வெப்பநிலையைக் கண்காணிக்கவும்.\n3. 🧪 சொட்டுநீர்ப் பாசனக் குழாய்களில் உப்பு அடைப்பு உள்ளதா எனப் பார்க்கவும்.\n4. 📷 அடுத்த 24 மணி நேரத்திற்குள் மீண்டும் இலை ஸ்கேன் செய்து முன்னேற்றத்தைக் கண்காணிக்கவும்.",
                            "crop_calendar"
                        )
                    }
                    query.contains("பாதிப்பு", ignoreCase = true) || query.contains("அபாயம்", ignoreCase = true) || query.contains("risk", ignoreCase = true) -> {
                        val highRiskCount = plants.count { it.healthScore < 70 }
                        Pair(
                            "தற்போது உங்கள் பண்ணையில் உள்ள ${plants.size} பயிர்களில், $highRiskCount பயிர்கள் அதிக அல்லது மிதமான அழுத்தத்தில் உள்ளதாகக் கண்டறியப்பட்டுள்ளது. பண்ணை வரைபடத்தில் (Farm Map) அவற்றின் நிலையைத் தெளிவாகப் பார்க்கலாம்.",
                            "farm_heatmap"
                        )
                    }
                    query.contains("கணிப்பு", ignoreCase = true) || query.contains("விளக்கு", ignoreCase = true) || query.contains("prediction", ignoreCase = true) -> {
                        Pair(
                            "$plantName பயிருக்கான சமீபத்திய AI கணிப்பு:\n• கண்டறியப்பட்ட பாதிப்பு: $stressIssue\n• நம்பகத்தன்மை (Confidence): ${String.format("%.1f", confidence)}%\n• முக்கிய காரணிகள்: இலை பச்சைய நிறமாலை குறைவு மற்றும் நீராவிப்போக்கு வேகம் குறைவு.\n\n*குறிப்பு: AI கணிப்புகள் முடிவெடுக்க உதவும் வழிகாட்டலே ஆகும்.*",
                            "history"
                        )
                    }
                    query.contains("vpd", ignoreCase = true) || query.contains("ஆவியாதல்", ignoreCase = true) -> {
                        Pair(
                            "ஆவியாதல் பற்றாக்குறை அழுத்தம் (VPD) என்பது காற்றின் உறிஞ்சும் திறனைக் குறிக்கும். தற்போது உங்கள் பண்ணையின் VPD அளவு $vpd kPa ஆகும். தழைப்பருவப் பயிர்களுக்கு 0.8–1.2 kPa உகந்தது. VPD அதிகரிக்கும் போது நீராவிப்போக்கு அதிகமாகும்; மண்ணில் போதுமான நீர் இல்லையென்றால் இலைத்துளைகள் மூடப்பட்டு வளர்ச்சி தடைபடும்.",
                            "biochemical_lab"
                        )
                    }
                    else -> {
                        Pair(
                            "தற்போதைய பசுமைக்குடில் தரவுகள் ஆய்வு செய்யப்பட்டன (மண் ஈரப்பதம்: ${soilM}%, வெப்பநிலை: ${temp}°C, VPD: $vpd kPa). $plantName பயிருக்கு மண் ஈரப்பதத்தை 45%-க்கு மேல் பராமரித்து, இலை வெப்பநிலையைக் கண்காணிக்கவும். புதிய இலை ஸ்கேன் செய்ய விரும்புகிறீர்களா?",
                            "scanner"
                        )
                    }
                }
            } else {
                when {
                    query.contains("Why is my plant stressed", ignoreCase = true) || query.contains("why", ignoreCase = true) -> {
                        Pair(
                            "Based on real telemetry for $plantName ($plantTag):\n• Substrate Moisture: ${soilM}%\n• Microclimate Temp: ${temp}°C, Humidity: ${humidity}%\n• Calculated VPD: $vpd kPa\n\nAI detected early stomatal conductance decline. Soil moisture is below the optimum 55% threshold while VPD is elevated, forcing guard cell closure before visible wilting occurs.",
                            "scanner"
                        )
                    }
                    query.contains("What should I check", ignoreCase = true) || query.contains("check", ignoreCase = true) -> {
                        Pair(
                            "Here is the priority diagnostic checklist for $plantName:\n1. 💧 Check volumetric soil moisture around the root zone.\n2. 🌡️ Verify canopy vs air temperature differential (ideal: canopy is 1–2°C cooler than air).\n3. 🧪 Inspect drip irrigation emitters for localized salt clogs.\n4. 📷 Perform an AI multispectral scan in 24 hours to track stress trajectory.",
                            "crop_calendar"
                        )
                    }
                    query.contains("high risk", ignoreCase = true) || query.contains("critical", ignoreCase = true) -> {
                        val highRiskCount = plants.count { it.healthScore < 70 }
                        Pair(
                            "Currently, $highRiskCount of ${plants.size} registered plants are flagged with high or moderate stress in your farm zone. You can inspect the Farm Heatmap to see their exact spatial layout.",
                            "farm_heatmap"
                        )
                    }
                    query.contains("prediction", ignoreCase = true) || query.contains("explain", ignoreCase = true) -> {
                        Pair(
                            "Latest AI Diagnosis for $plantName:\n• Issue: $stressIssue\n• Confidence: ${String.format("%.1f", confidence)}%\n• Primary Drivers: Red-edge spectral reflectance drop & localized thermal transpiration reduction.\n\n*Note: AI predictions are decision-support estimates and not laboratory confirmations.*",
                            "history"
                        )
                    }
                    query.contains("vpd", ignoreCase = true) -> {
                        Pair(
                            "Vapor Pressure Deficit (VPD) represents the drying power of the air. Current VPD in your zone is $vpd kPa. Ideal range for vegetative crops is 0.8–1.2 kPa. High VPD forces high transpiration demand; if soil moisture cannot supply it, stomata close and photosynthesis stops.",
                            "biochemical_lab"
                        )
                    }
                    else -> {
                        Pair(
                            "I analyzed your request against current greenhouse telemetry (Soil: ${soilM}%, Temp: ${temp}°C, VPD: $vpd kPa). For $plantName, maintain soil moisture above 45% and monitor canopy temperature closely. Would you like to launch a scan or review crop calendar tasks?",
                            "scanner"
                        )
                    }
                }
            }

            val aiMsg = ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                sender = "AI",
                message = replyText,
                actionSuggestion = action
            )
            messages = messages + aiMsg
            isThinking = false

            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .shadow(24.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, GlowingEmerald)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(DarkGreen40, CardSurfaceDark)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(GlowingEmerald.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Psychology,
                                    contentDescription = "AI Assistant",
                                    tint = GlowingEmerald,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isTamil) "AI உதவியாளர்" else "AgroSense AI Advisor",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextLight
                                )
                                Text(
                                    text = if (isTamil) "நேரலை • சென்சார் இணைக்கப்பட்டுள்ளது" else "Online • Live Agronomic Telemetry",
                                    fontSize = 11.sp,
                                    color = GlowingEmerald
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }
                }

                HorizontalDivider(color = SoftGrass, thickness = 1.dp)

                // Chat message stream
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isUser = msg.sender == "USER"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                ),
                                color = if (isUser) GlowingEmerald else DeepForestBg,
                                border = if (isUser) null else BorderStroke(1.dp, SoftGrass),
                                modifier = Modifier.widthIn(max = 340.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = msg.message,
                                        fontSize = 13.sp,
                                        color = if (isUser) Color.White else TextLight,
                                        lineHeight = 18.sp
                                    )

                                    if (msg.actionSuggestion != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                onDismiss()
                                                onNavigateToScreen(msg.actionSuggestion)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, GlowingEmerald),
                                            colors = ButtonDefaults.outlinedButtonColors(containerColor = CardSurfaceDark),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(
                                                text = when (msg.actionSuggestion) {
                                                    "scanner" -> if (isTamil) "📷 தாவரத்தை ஸ்கேன் செய்க" else "📷 Open AI Scanner"
                                                    "farm_heatmap" -> if (isTamil) "🗺️ பண்ணை வரைபடம் பார்க்க" else "🗺️ View Farm Map"
                                                    "crop_calendar" -> if (isTamil) "📅 பயிர் காலண்டர் திறக்க" else "📅 View Crop Calendar"
                                                    "history" -> if (isTamil) "📊 வரலாற்று வரைபடங்கள்" else "📊 View History & Trends"
                                                    else -> if (isTamil) "🔍 திறக்க" else "🔍 Open Module"
                                                },
                                                fontSize = 11.sp,
                                                color = GlowingEmerald,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isThinking) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = GlowingEmerald,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTamil) "சென்சார் மற்றும் பயிர் மாதிரிகள் ஆய்வு செய்யப்படுகின்றன..." else "Analyzing farm telemetry & plant models...",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                // Quick Prompt Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepForestBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickQuestions) { q ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = CardSurfaceDark,
                            border = BorderStroke(1.dp, SoftGrass),
                            modifier = Modifier
                                .clickable {
                                    respondToQuery(q)
                                }
                                .testTag("quick_prompt_chip")
                        ) {
                            Text(
                                text = q,
                                fontSize = 11.sp,
                                color = GlowingEmerald,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Input Bar
                Surface(
                    color = CardSurfaceDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputPrompt,
                            onValueChange = { inputPrompt = it },
                            placeholder = {
                                Text(
                                    text = if (isTamil) "பயிர் நலம், VPD, பாசனம் பற்றி கேளுங்கள்..." else "Ask about plant health, VPD, stress...",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_assistant_input"),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GlowingEmerald,
                                unfocusedBorderColor = SoftGrass,
                                focusedContainerColor = DeepForestBg,
                                unfocusedContainerColor = DeepForestBg,
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight
                            ),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (inputPrompt.isNotBlank()) {
                                    val text = inputPrompt
                                    inputPrompt = ""
                                    respondToQuery(text)
                                }
                            },
                            enabled = inputPrompt.isNotBlank() && !isThinking,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (inputPrompt.isNotBlank()) GlowingEmerald else SoftGrass)
                                .testTag("ai_assistant_send_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "Send",
                                tint = if (inputPrompt.isNotBlank()) Color.White else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
