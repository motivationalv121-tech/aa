package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CameraMovement
import com.example.model.SceneGenerationStatus
import com.example.model.StoryboardScene
import com.example.ui.components.NeonBadge
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioHeader
import com.example.ui.theme.*
import com.example.viewmodel.StudioViewModel

@Composable
fun StoryboardEditorScreen(
    viewModel: StudioViewModel,
    projectId: String,
    onBackClick: () -> Unit,
    onOpenPlayerClick: (String) -> Unit
) {
    LaunchedEffect(projectId) {
        viewModel.selectProject(projectId)
    }

    val project by viewModel.currentProject.collectAsState()
    val scenes by viewModel.currentStoryboard.collectAsState()
    var editingSceneId by remember { mutableStateOf<String?>(null) }
    var autoEditDialogVisible by remember { mutableStateOf(false) }
    var customEditPrompt by remember { mutableStateOf("") }

    Scaffold(
        containerColor = StudioDarkCanvas,
        topBar = {
            StudioHeader(
                title = "AI Storyboard Studio",
                subtitle = "${scenes.size} Generated Scenes • ${project?.aspectRatio?.ratioString ?: "9:16"}",
                navigationIcon = Icons.Default.ArrowBack,
                onNavClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { autoEditDialogVisible = true },
                        modifier = Modifier.testTag("storyboard_auto_edit_button")
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = "AI Auto-Edit", tint = StudioNeonViolet)
                    }
                }
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

            // AI Director Header
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, StudioNeonViolet.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                    color = StudioDarkSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Autonomous Visual Storyboard",
                                style = MaterialTheme.typography.titleMedium,
                                color = StudioTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Click any scene to customize prompt, camera motion or regenerate.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioTextSecondary
                            )
                        }

                        Button(
                            onClick = { autoEditDialogVisible = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioNeonViolet),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Auto-Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (scenes.isEmpty()) {
                item {
                    StudioCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = StudioNeonCyan, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Generating visual scenes...", color = StudioTextSecondary)
                        }
                    }
                }
            } else {
                itemsIndexed(scenes) { index, scene ->
                    SceneCard(
                        scene = scene,
                        index = index,
                        isExpanded = editingSceneId == scene.id,
                        onExpandToggle = {
                            editingSceneId = if (editingSceneId == scene.id) null else scene.id
                        },
                        onRegenerate = {
                            viewModel.regenerateSceneVisual(scene.id)
                        },
                        onSaveScene = { updated ->
                            viewModel.updateStoryboardScene(updated)
                            editingSceneId = null
                        },
                        onPlayVoice = {
                            viewModel.previewVoice(scene.narration, project?.language ?: "Hindi")
                        }
                    )
                }
            }

            item {
                PrimaryGradientButton(
                    text = "Launch Player & Render Video",
                    icon = Icons.Default.PlayCircle,
                    onClick = { onOpenPlayerClick(projectId) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "storyboard_to_player_button"
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (autoEditDialogVisible) {
        AlertDialog(
            onDismissRequest = { autoEditDialogVisible = false },
            title = { Text("AI Auto-Director Command", color = StudioTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Give high-level creative directions (e.g., 'Make pacing 20% faster', 'Add dramatic zooms to hooks', 'Change aesthetic to cyberpunk neon').",
                        color = StudioTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customEditPrompt,
                        onValueChange = { customEditPrompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter prompt instructions...", color = StudioTextTertiary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = StudioTextPrimary,
                            unfocusedTextColor = StudioTextPrimary,
                            focusedBorderColor = StudioNeonCyan
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.triggerAiAutoEdit(customEditPrompt)
                        autoEditDialogVisible = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioNeonCyan)
                ) {
                    Text("Apply AI Edit", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { autoEditDialogVisible = false }) {
                    Text("Cancel", color = StudioTextSecondary)
                }
            },
            containerColor = StudioDarkSurface
        )
    }
}

@Composable
fun SceneCard(
    scene: StoryboardScene,
    index: Int,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onRegenerate: () -> Unit,
    onSaveScene: (StoryboardScene) -> Unit,
    onPlayVoice: () -> Unit
) {
    var promptInput by remember(scene.aiImagePrompt) { mutableStateOf(scene.aiImagePrompt) }
    var voiceInput by remember(scene.narration) { mutableStateOf(scene.narration) }

    StudioCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isExpanded) StudioNeonCyan.copy(alpha = 0.6f) else StudioBorderColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NeonBadge(text = "SCENE ${scene.sceneNumber}", color = StudioNeonCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${scene.durationSec}s • ${scene.transition.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = StudioTextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPlayVoice, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Voiceover", tint = StudioNeonCyan, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onExpandToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = StudioTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visual Preview Thumbnail Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(StudioDarkSurfaceHighlight),
            contentAlignment = Alignment.Center
        ) {
            if (!scene.mediaUrl.isNullOrBlank()) {
                AsyncImage(
                    model = scene.mediaUrl,
                    contentDescription = "Scene visual",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = StudioTextTertiary, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("AI Visual Generated Asset", style = MaterialTheme.typography.bodySmall, color = StudioTextSecondary)
                }
            }

            // Camera Motion Overlay Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Text(
                    text = "🎥 ${scene.cameraMovement.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = StudioNeonCyan,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            // Status badge
            if (scene.status == SceneGenerationStatus.GENERATING) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp),
                    color = StudioNeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Voiceover Script line
        Text(
            text = "Voiceover: \"${scene.narration}\"",
            style = MaterialTheme.typography.bodyMedium,
            color = StudioTextPrimary,
            fontWeight = FontWeight.Medium
        )

        // Expanded Editor Controls
        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Divider(color = StudioBorderColor, modifier = Modifier.padding(vertical = 8.dp))

                Text("Visual AI Prompt", style = MaterialTheme.typography.labelSmall, color = StudioNeonCyan, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StudioTextPrimary,
                        unfocusedTextColor = StudioTextPrimary,
                        focusedBorderColor = StudioNeonCyan
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Voiceover Script", style = MaterialTheme.typography.labelSmall, color = StudioNeonAmber, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = voiceInput,
                    onValueChange = { voiceInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StudioTextPrimary,
                        unfocusedTextColor = StudioTextPrimary,
                        focusedBorderColor = StudioNeonAmber
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRegenerate,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioDarkSurfaceHighlight),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = StudioNeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Regenerate", color = StudioNeonCyan, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onSaveScene(scene.copy(aiImagePrompt = promptInput, narration = voiceInput))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioNeonViolet),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Scene", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
