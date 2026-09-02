package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatio
import com.example.ui.components.NeonBadge
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioHeader
import com.example.ui.theme.*
import com.example.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePromptScreen(
    viewModel: StudioViewModel,
    initialPrompt: String = "",
    initialLanguage: String = "Hindi",
    initialTone: String = "Motivational",
    onBackClick: () -> Unit,
    onPipelineStarted: (String) -> Unit
) {
    var promptText by remember { mutableStateOf(initialPrompt) }
    var selectedLanguage by remember { mutableStateOf(initialLanguage) }
    var selectedAspectRatio by remember { mutableStateOf(AspectRatio.PORTRAIT_9_16) }
    var selectedDuration by remember { mutableStateOf(60) }
    var selectedTone by remember { mutableStateOf(initialTone) }
    var customDirectives by remember { mutableStateOf("") }
    var voiceStyle by remember { mutableStateOf("Deep Cinematic Male (Dev)") }
    var includeSubtitles by remember { mutableStateOf(true) }
    var includeBgm by remember { mutableStateOf(true) }

    val languages = listOf("Hindi", "English", "Hinglish", "Spanish", "German", "French", "Japanese")
    val tones = listOf("Motivational", "Cinematic", "Educational", "Casual & Humorous", "Dramatic & Mystery", "Fast Tech Hype")
    val durations = listOf(15, 30, 60, 90, 180)
    val voiceStyles = listOf(
        "Deep Cinematic Male (Dev)",
        "Energetic YouTube Creator (Arya)",
        "Warm Storyteller (Priya)",
        "Docu-Narrator (Vikram)",
        "Modern Fast-Paced (Kabir)"
    )

    Scaffold(
        containerColor = StudioDarkCanvas,
        topBar = {
            StudioHeader(
                title = "Create AI Video",
                subtitle = "Autonomous agent research & generation",
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

            // Master Prompt Input Card
            item {
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = StudioNeonViolet.copy(alpha = 0.5f),
                    backgroundColor = StudioDarkSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Describe Your Video Topic / Idea",
                            style = MaterialTheme.typography.titleMedium,
                            color = StudioTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        NeonBadge(text = "NATURAL PROMPT", color = StudioNeonViolet)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp)
                            .testTag("prompt_input_field"),
                        placeholder = {
                            Text(
                                text = "e.g., चंद्रयान-3 मिशन पर 1 मिनट की YouTube Shorts बनाओ। शुरुआत में strong hook हो, cinematic visuals, background music, AI voiceover और subtitles भी हों।",
                                color = StudioTextTertiary,
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioNeonViolet,
                            unfocusedBorderColor = StudioBorderColor,
                            focusedTextColor = StudioTextPrimary,
                            unfocusedTextColor = StudioTextPrimary,
                            focusedContainerColor = StudioDarkSurface,
                            unfocusedContainerColor = StudioDarkSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Prompt chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val quickIdeas = listOf(
                            "Chandrayaan-3 Journey",
                            "Stock Market Psychology",
                            "Quantum Computing Explained",
                            "Ancient Indian Architecture",
                            "AI Agents Revolution 2026"
                        )
                        items(quickIdeas) { idea ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { promptText = idea },
                                color = StudioDarkSurfaceHighlight
                            ) {
                                Text(
                                    text = "+ $idea",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StudioTextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Aspect Ratio Selector
            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Video Format & Aspect Ratio",
                        style = MaterialTheme.typography.titleMedium,
                        color = StudioTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AspectRatioCard(
                            ratio = AspectRatio.PORTRAIT_9_16,
                            label = "9:16 Shorts/Reels",
                            icon = Icons.Default.Smartphone,
                            isSelected = selectedAspectRatio == AspectRatio.PORTRAIT_9_16,
                            modifier = Modifier.weight(1f),
                            onSelect = { selectedAspectRatio = it }
                        )
                        AspectRatioCard(
                            ratio = AspectRatio.LANDSCAPE_16_9,
                            label = "16:9 YouTube",
                            icon = Icons.Default.Tv,
                            isSelected = selectedAspectRatio == AspectRatio.LANDSCAPE_16_9,
                            modifier = Modifier.weight(1f),
                            onSelect = { selectedAspectRatio = it }
                        )
                        AspectRatioCard(
                            ratio = AspectRatio.SQUARE_1_1,
                            label = "1:1 Square",
                            icon = Icons.Default.CropSquare,
                            isSelected = selectedAspectRatio == AspectRatio.SQUARE_1_1,
                            modifier = Modifier.weight(1f),
                            onSelect = { selectedAspectRatio = it }
                        )
                    }
                }
            }

            // Language & Duration Selection
            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Language & Tone",
                        style = MaterialTheme.typography.titleMedium,
                        color = StudioTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Script Language", style = MaterialTheme.typography.labelMedium, color = StudioTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(languages) { lang ->
                            FilterChip(
                                selected = selectedLanguage == lang,
                                onClick = { selectedLanguage = lang },
                                label = { Text(lang) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioNeonCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = StudioNeonCyan,
                                    containerColor = StudioDarkSurfaceVariant,
                                    labelColor = StudioTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedLanguage == lang,
                                    borderColor = StudioBorderColor,
                                    selectedBorderColor = StudioNeonCyan
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Target Duration", style = MaterialTheme.typography.labelMedium, color = StudioTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(durations) { dur ->
                            FilterChip(
                                selected = selectedDuration == dur,
                                onClick = { selectedDuration = dur },
                                label = { Text("${dur}s (${dur / 60}m ${dur % 60}s)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioNeonViolet.copy(alpha = 0.2f),
                                    selectedLabelColor = StudioNeonViolet,
                                    containerColor = StudioDarkSurfaceVariant,
                                    labelColor = StudioTextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Tone & Narrative Style", style = MaterialTheme.typography.labelMedium, color = StudioTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tones) { t ->
                            FilterChip(
                                selected = selectedTone == t,
                                onClick = { selectedTone = t },
                                label = { Text(t) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioNeonAmber.copy(alpha = 0.2f),
                                    selectedLabelColor = StudioNeonAmber,
                                    containerColor = StudioDarkSurfaceVariant,
                                    labelColor = StudioTextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // AI Voice & Audio Options
            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Voiceover & Audio Studio",
                        style = MaterialTheme.typography.titleMedium,
                        color = StudioTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    voiceStyles.forEach { style ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { voiceStyle = style }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = voiceStyle == style,
                                onClick = { voiceStyle = style },
                                colors = RadioButtonDefaults.colors(selectedColor = StudioNeonCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = style, style = MaterialTheme.typography.bodyMedium, color = StudioTextPrimary)
                        }
                    }

                    Divider(color = StudioBorderColor, modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dynamic Subtitles / Captions", style = MaterialTheme.typography.bodyMedium, color = StudioTextPrimary)
                            Text("Animated word-by-word highlight", style = MaterialTheme.typography.bodySmall, color = StudioTextSecondary)
                        }
                        Switch(
                            checked = includeSubtitles,
                            onCheckedChange = { includeSubtitles = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = StudioNeonCyan, checkedTrackColor = StudioNeonCyan.copy(alpha = 0.4f))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("AI Dynamic Background Music", style = MaterialTheme.typography.bodyMedium, color = StudioTextPrimary)
                            Text("Automatic ducking during voiceover", style = MaterialTheme.typography.bodySmall, color = StudioTextSecondary)
                        }
                        Switch(
                            checked = includeBgm,
                            onCheckedChange = { includeBgm = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = StudioNeonAmber, checkedTrackColor = StudioNeonAmber.copy(alpha = 0.4f))
                        )
                    }
                }
            }

            // Custom Directives
            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Special Agent Directives (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        color = StudioTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customDirectives,
                        onValueChange = { customDirectives = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("e.g., Include specific quote from APJ Abdul Kalam, fast paced cuts under 2s, cyberpunk lighting", color = StudioTextTertiary, fontSize = 13.sp)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioNeonCyan,
                            unfocusedBorderColor = StudioBorderColor,
                            focusedTextColor = StudioTextPrimary,
                            unfocusedTextColor = StudioTextPrimary,
                            focusedContainerColor = StudioDarkSurface,
                            unfocusedContainerColor = StudioDarkSurface
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Launch Button
            item {
                PrimaryGradientButton(
                    text = "Launch AI Autonomous Studio",
                    icon = Icons.Default.AutoMode,
                    onClick = {
                        val effectivePrompt = promptText.ifBlank { "चंद्रयान-3 मिशन: इसरो का ऐतिहासिक चंद्रमा पर सफल लैंडिंग" }
                        viewModel.createProjectFromPrompt(
                            prompt = effectivePrompt,
                            customInstructions = customDirectives,
                            aspectRatio = selectedAspectRatio,
                            targetDurationSec = selectedDuration,
                            language = selectedLanguage,
                            tone = selectedTone,
                            voiceStyle = voiceStyle,
                            onProjectCreated = { createdId ->
                                onPipelineStarted(createdId)
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "launch_pipeline_button"
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AspectRatioCard(
    ratio: AspectRatio,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: (AspectRatio) -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isSelected) StudioNeonCyan else StudioBorderColor,
                RoundedCornerShape(12.dp)
            )
            .clickable { onSelect(ratio) },
        color = if (isSelected) StudioNeonCyan.copy(alpha = 0.12f) else StudioDarkSurface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) StudioNeonCyan else StudioTextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) StudioTextPrimary else StudioTextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
