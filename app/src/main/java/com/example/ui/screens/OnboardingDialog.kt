package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

data class OnboardingStep(
    val iconEmoji: String,
    val iconVector: ImageVector,
    val title: String,
    val subtitle: String,
    val keyHighlight: String,
    val badgeLabel: String
)

@Composable
fun AgroOnboardingDialog(
    onDismiss: () -> Unit,
    onFinish: () -> Unit
) {
    val steps = remember {
        listOf(
            OnboardingStep(
                iconEmoji = "🌿",
                iconVector = Icons.Filled.Spa,
                title = "Welcome to AgroSense-X",
                subtitle = "Monitor plant physiology and detect invisible cellular stress 48–72 hours before visible symptoms manifest.",
                keyHighlight = "Pre-symptomatic spectral fluorescence & transpiration index monitoring",
                badgeLabel = "PRECISION AGRI-TECH"
            ),
            OnboardingStep(
                iconEmoji = "📷",
                iconVector = Icons.Filled.PhotoCamera,
                title = "Scan Your Plants",
                subtitle = "Use your smartphone camera or drone sensors. Our built-in leaf framing and quality check guarantee clean data capture.",
                keyHighlight = "Real-time blur, illumination, and canopy segmentation heuristics",
                badgeLabel = "MULTIMODAL VISION"
            ),
            OnboardingStep(
                iconEmoji = "🧠",
                iconVector = Icons.Filled.Psychology,
                title = "AI-Powered Insights",
                subtitle = "Deconstruct multi-stress interactions across water deficit, nitrogen starvation, thermal spikes, and fungal pressure.",
                keyHighlight = "Transparent XAI explanations with feature contribution breakdown",
                badgeLabel = "EXPLAINABLE AI (XAI)"
            ),
            OnboardingStep(
                iconEmoji = "🚨",
                iconVector = Icons.Filled.NotificationsActive,
                title = "Act Early & Protect Yield",
                subtitle = "Receive climate-adaptive care schedules, automated irrigation triggers, and verified agronomic advisories.",
                keyHighlight = "Preventative intervention plans tailored to your specific micro-climate",
                badgeLabel = "DECISION SUPPORT"
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = steps[currentStepIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = CardSurfaceDark,
            border = BorderStroke(1.dp, GlowingEmerald.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .shadow(24.dp, RoundedCornerShape(28.dp))
                .testTag("onboarding_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge & Skip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SoftGrass,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = step.badgeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingEmerald,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            letterSpacing = 0.5.sp
                        )
                    }

                    TextButton(
                        onClick = onFinish,
                        modifier = Modifier.testTag("onboarding_skip_btn")
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animated step icon
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        fadeIn() + slideInHorizontally { width -> width } togetherWith
                                fadeOut() + slideOutHorizontally { width -> -width }
                    },
                    label = "onboarding_icon"
                ) { current ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        GlowingEmerald.copy(alpha = 0.25f),
                                        SoftGrass
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = current.iconEmoji,
                            fontSize = 44.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title & Subtitle
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "onboarding_text"
                ) { current ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = current.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = current.subtitle,
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DeepForestBg,
                            border = BorderStroke(1.dp, SoftGrass),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = GlowingEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = current.keyHighlight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextLight
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Step Indicators (Dots)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    steps.indices.forEach { index ->
                        val isSelected = index == currentStepIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(6.dp)
                                .width(if (isSelected) 24.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) GlowingEmerald else TextMuted.copy(alpha = 0.3f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { currentStepIndex-- },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SoftGrass),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("onboarding_back_btn")
                        ) {
                            Text("Back", color = TextLight, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStepIndex < steps.size - 1) {
                                currentStepIndex++
                            } else {
                                onFinish()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("onboarding_next_btn")
                    ) {
                        Text(
                            text = if (currentStepIndex < steps.size - 1) "Next →" else "Get Started 🚀",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
