package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AspectRatio
import com.example.model.StoryboardScene
import com.example.ui.components.NeonBadge
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioHeader
import com.example.ui.theme.*
import com.example.viewmodel.StudioViewModel
import kotlinx.coroutines.delay

@Composable
fun VideoPreviewPlayerScreen(
    viewModel: StudioViewModel,
    projectId: String,
    onBackClick: () -> Unit,
    onEditStoryboardClick: (String) -> Unit
) {
    LaunchedEffect(projectId) {
        viewModel.selectProject(projectId)
    }

    val project by viewModel.currentProject.collectAsState()
    val scenes by viewModel.currentStoryboard.collectAsState()
    val timeline by viewModel.currentTimeline.collectAsState()

    var isPlaying by remember { mutableStateOf(false) }
    var currentSceneIndex by remember { mutableStateOf(0) }
    var playbackProgressSec by remember { mutableStateOf(0f) }
    var selectedRatio by remember { mutableStateOf(project?.aspectRatio ?: AspectRatio.PORTRAIT_9_16) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showSubtitles by remember { mutableStateOf(true) }

    val totalDuration = scenes.sumOf { it.durationSec }.coerceAtLeast(1)

    // Playback ticker simulation
    LaunchedEffect(isPlaying, scenes) {
        if (isPlaying && scenes.isNotEmpty()) {
            while (isPlaying) {
                delay(200)
                playbackProgressSec += 0.2f
                if (playbackProgressSec >= totalDuration) {
                    playbackProgressSec = 0f
                    currentSceneIndex = 0
                    isPlaying = false
                } else {
                    // determine scene index
                    var accumulated = 0
                    for (i in scenes.indices) {
                        accumulated += scenes[i].durationSec
                        if (playbackProgressSec <= accumulated) {
                            if (currentSceneIndex != i) {
                                currentSceneIndex = i
                                // trigger TTS for current scene voiceover
                                viewModel.previewVoice(scenes[i].narration, project?.language ?: "Hindi")
                            }
                            break
                        }
                    }
                }
            }
        } else {
            viewModel.stopVoice()
        }
    }

    val activeScene: StoryboardScene? = scenes.getOrNull(currentSceneIndex)

    Scaffold(
        containerColor = StudioDarkCanvas,
        topBar = {
            StudioHeader(
                title = project?.title ?: "Video Studio Player",
                subtitle = "Multi-Track Master Preview • ${totalDuration}s",
                navigationIcon = Icons.Default.ArrowBack,
                onNavClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("export_video_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export", tint = StudioNeonCyan)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Video Preview Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(StudioDarkSurface)
                    .border(1.dp, StudioBorderColor, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Video frame container with Aspect Ratio simulation
                val frameAspectRatio = when (selectedRatio) {
                    AspectRatio.PORTRAIT_9_16 -> 9f / 16f
                    AspectRatio.LANDSCAPE_16_9 -> 16f / 9f
                    AspectRatio.SQUARE_1_1 -> 1f
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.92f)
                        .aspectRatio(frameAspectRatio)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioDarkSurfaceHighlight)
                        .border(1.dp, StudioNeonCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (activeScene != null && !activeScene.mediaUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = activeScene.mediaUrl,
                            contentDescription = "Active Scene Visual",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StudioNeonCyan, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Scene ${currentSceneIndex + 1}", style = MaterialTheme.typography.titleMedium, color = StudioTextPrimary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(activeScene?.cameraMovement?.displayName ?: "Cinematic Motion", style = MaterialTheme.typography.bodySmall, color = StudioNeonCyan)
                        }
                    }

                    // Watermark / Studio Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "AI Studio Master",
                            style = MaterialTheme.typography.labelSmall,
                            color = StudioNeonCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            fontSize = 10.sp
                        )
                    }

                    // Subtitles Overlay (Karaoke / Dynamic Caption Style)
                    if (showSubtitles && activeScene != null && activeScene.narration.isNotBlank()) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 12.dp, vertical = 20.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = Color.Black.copy(alpha = 0.75f)
                        ) {
                            Text(
                                text = activeScene.narration,
                                style = MaterialTheme.typography.bodyMedium,
                                color = StudioNeonAmber,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Timeline Scrubber & Controls
            StudioCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scene ${currentSceneIndex + 1} of ${scenes.size.coerceAtLeast(1)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = StudioNeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${playbackProgressSec.toInt()}s / ${totalDuration}s",
                        style = MaterialTheme.typography.labelMedium,
                        color = StudioTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = playbackProgressSec,
                    onValueChange = {
                        playbackProgressSec = it
                    },
                    valueRange = 0f..totalDuration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = StudioNeonCyan,
                        activeTrackColor = StudioNeonCyan,
                        inactiveTrackColor = StudioDarkSurfaceHighlight
                    ),
                    modifier = Modifier.testTag("player_timeline_slider")
                )

                // Playback Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            playbackProgressSec = 0f
                            currentSceneIndex = 0
                        }
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Restart", tint = StudioTextPrimary)
                    }

                    // Large Play / Pause button
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(StudioNeonViolet, StudioNeonCyan)))
                            .clickable {
                                isPlaying = !isPlaying
                                if (isPlaying && activeScene != null) {
                                    viewModel.previewVoice(activeScene.narration, project?.language ?: "Hindi")
                                }
                            }
                            .testTag("player_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (currentSceneIndex < scenes.size - 1) {
                                currentSceneIndex++
                            }
                        }
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next Scene", tint = StudioTextPrimary)
                    }

                    IconButton(
                        onClick = { showSubtitles = !showSubtitles }
                    ) {
                        Icon(
                            imageVector = if (showSubtitles) Icons.Default.Subtitles else Icons.Default.SubtitlesOff,
                            contentDescription = "Subtitles",
                            tint = if (showSubtitles) StudioNeonAmber else StudioTextTertiary
                        )
                    }
                }
            }

            // Quick Scene Thumbnails Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(scenes) { idx, scene ->
                    val isCurrent = idx == currentSceneIndex
                    Surface(
                        modifier = Modifier
                            .width(72.dp)
                            .height(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                2.dp,
                                if (isCurrent) StudioNeonCyan else StudioBorderColor,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                currentSceneIndex = idx
                                var acc = 0
                                for (i in 0 until idx) acc += scenes[i].durationSec
                                playbackProgressSec = acc.toFloat()
                            },
                        color = StudioDarkSurfaceHighlight
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (!scene.mediaUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = scene.mediaUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(2.dp),
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(3.dp)
                            ) {
                                Text(
                                    text = "${scene.durationSec}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Action Bar (Edit Storyboard / Export)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onEditStoryboardClick(projectId) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioNeonViolet)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = StudioNeonViolet, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Scenes", color = StudioNeonViolet)
                }

                PrimaryGradientButton(
                    text = "Export Master Video",
                    icon = Icons.Default.Share,
                    onClick = { showExportDialog = true },
                    modifier = Modifier.weight(1.3f),
                    testTag = "export_master_button"
                )
            }
        }
    }

    // Export / Share Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text("Export & Render Master Video", color = StudioTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Your complete video package has been compiled with HD visual frames, synced voiceover, ambient BGM ducking and dynamic subtitles.",
                        color = StudioTextSecondary,
                        fontSize = 13.sp
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = StudioDarkSurfaceHighlight
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("• Format: MP4 (H.264 / AAC)", style = MaterialTheme.typography.bodySmall, color = StudioTextPrimary)
                            Text("• Resolution: 1080x1920 (9:16 Shorts)", style = MaterialTheme.typography.bodySmall, color = StudioTextPrimary)
                            Text("• Audio: Stereo 48kHz Voice + BGM", style = MaterialTheme.typography.bodySmall, color = StudioTextPrimary)
                            Text("• Duration: ${totalDuration} Seconds", style = MaterialTheme.typography.bodySmall, color = StudioTextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.approveAndRenderFinal {
                            viewModel.showToast("Master Video Render Complete & Saved to Gallery!")
                        }
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioNeonCyan)
                ) {
                    Text("Save / Render Complete", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = StudioTextSecondary)
                }
            },
            containerColor = StudioDarkSurface
        )
    }
}
