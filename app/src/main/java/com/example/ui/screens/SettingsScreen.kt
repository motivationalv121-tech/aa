package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VideoGenerationProviderType
import com.example.model.VoiceProviderType
import com.example.ui.components.NeonBadge
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioHeader
import com.example.ui.theme.*
import com.example.viewmodel.StudioViewModel

@Composable
fun SettingsScreen(
    viewModel: StudioViewModel,
    onBackClick: () -> Unit
) {
    val currentSettings by viewModel.settings.collectAsState()

    var geminiApiKey by remember(currentSettings.customGeminiApiKey) { mutableStateOf(currentSettings.customGeminiApiKey) }
    var selectedVideoProvider by remember(currentSettings.selectedVideoProvider) { mutableStateOf(currentSettings.selectedVideoProvider) }
    var selectedVoiceProvider by remember(currentSettings.selectedVoiceProvider) { mutableStateOf(currentSettings.selectedVoiceProvider) }
    var speechRate by remember(currentSettings.ttsSpeechRate) { mutableStateOf(currentSettings.ttsSpeechRate) }
    var pitch by remember(currentSettings.ttsPitch) { mutableStateOf(currentSettings.ttsPitch) }
    var enableWatermark by remember(currentSettings.enableAiWatermarkLabel) { mutableStateOf(currentSettings.enableAiWatermarkLabel) }

    Scaffold(
        containerColor = StudioDarkCanvas,
        topBar = {
            StudioHeader(
                title = "Studio Settings & AI Models",
                subtitle = "Configure Multi-Agent Engines & Voice Synthesizer",
                navigationIcon = Icons.Default.ArrowBack,
                onNavClick = onBackClick
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
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Gemini AI Key Card
            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Google Gemini 2.5 Flash",
                            style = MaterialTheme.typography.titleMedium,
                            color = StudioTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        NeonBadge(text = "PRIMARY BRAIN", color = StudioNeonCyan)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Orchestrates research, writes high-retention viral scripts, and powers autonomous timeline editing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = geminiApiKey,
                        onValueChange = { geminiApiKey = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_api_key_input"),
                        label = { Text("Custom Gemini API Key (Optional)") },
                        placeholder = { Text("Uses integrated server-side Gemini key if blank", color = StudioTextTertiary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = StudioTextPrimary,
                            unfocusedTextColor = StudioTextPrimary,
                            focusedBorderColor = StudioNeonCyan,
                            unfocusedBorderColor = StudioBorderColor
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Visual Generation Engine Card
            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Visual & Video Generation Provider",
                        style = MaterialTheme.typography.titleMedium,
                        color = StudioTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    VideoGenerationProviderType.values().forEach { provider ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedVideoProvider == provider,
                                onClick = { selectedVideoProvider = provider },
                                colors = RadioButtonDefaults.colors(selectedColor = StudioNeonCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = provider.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StudioTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = provider.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StudioTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Voice Provider Card
            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Voice Synthesis Engine",
                        style = MaterialTheme.typography.titleMedium,
                        color = StudioTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    VoiceProviderType.values().forEach { provider ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedVoiceProvider == provider,
                                onClick = { selectedVoiceProvider = provider },
                                colors = RadioButtonDefaults.colors(selectedColor = StudioNeonViolet)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = provider.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StudioTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = provider.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StudioTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Speech Tuning
            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Voice Synthesis Tuning",
                        style = MaterialTheme.typography.titleMedium,
                        color = StudioTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Speech Rate: ${String.format("%.2f", speechRate)}x", style = MaterialTheme.typography.bodySmall, color = StudioTextSecondary)
                    Slider(
                        value = speechRate,
                        onValueChange = { speechRate = it },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = StudioNeonCyan, activeTrackColor = StudioNeonCyan)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Voice Pitch: ${String.format("%.2f", pitch)}x", style = MaterialTheme.typography.bodySmall, color = StudioTextSecondary)
                    Slider(
                        value = pitch,
                        onValueChange = { pitch = it },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = StudioNeonAmber, activeTrackColor = StudioNeonAmber)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.previewVoice("नमस्ते! यह AI वीडियो स्टूडियो की आवाज़ का नमूना है।", "Hindi")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioDarkSurfaceHighlight)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = StudioNeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Voiceover Sample", color = StudioNeonCyan)
                    }
                }
            }

            // Studio Watermark Switch
            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("AI Studio Watermark Overlay", style = MaterialTheme.typography.bodyMedium, color = StudioTextPrimary, fontWeight = FontWeight.Bold)
                            Text("Adds non-intrusive creator watermark during export", style = MaterialTheme.typography.bodySmall, color = StudioTextSecondary)
                        }
                        Switch(
                            checked = enableWatermark,
                            onCheckedChange = { enableWatermark = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = StudioNeonCyan, checkedTrackColor = StudioNeonCyan.copy(alpha = 0.4f))
                        )
                    }
                }
            }

            // Save Settings Button
            item {
                PrimaryGradientButton(
                    text = "Save Settings Configuration",
                    icon = Icons.Default.Save,
                    onClick = {
                        viewModel.updateSettings(
                            currentSettings.copy(
                                customGeminiApiKey = geminiApiKey,
                                selectedVideoProvider = selectedVideoProvider,
                                selectedVoiceProvider = selectedVoiceProvider,
                                ttsSpeechRate = speechRate,
                                ttsPitch = pitch,
                                enableAiWatermarkLabel = enableWatermark
                            )
                        )
                        viewModel.showToast("Settings updated successfully!")
                        onBackClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "save_settings_button"
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
