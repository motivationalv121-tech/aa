package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.model.AgentPipelineStage
import com.example.model.AgentStepLog
import com.example.ui.components.NeonBadge
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioHeader
import com.example.ui.theme.*
import com.example.viewmodel.StudioViewModel

@Composable
fun PipelineScreen(
    viewModel: StudioViewModel,
    projectId: String,
    onBackClick: () -> Unit,
    onViewStoryboardClick: (String) -> Unit,
    onViewPreviewClick: (String) -> Unit,
    onViewScriptClick: (String) -> Unit
) {
    LaunchedEffect(projectId) {
        viewModel.selectProject(projectId)
    }

    val project by viewModel.currentProject.collectAsState()
    val agentState by viewModel.agentState.collectAsState()

    Scaffold(
        containerColor = StudioDarkCanvas,
        topBar = {
            StudioHeader(
                title = "AI Autonomous Agent",
                subtitle = project?.title ?: "Multi-Agent Pipeline",
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

            // Progress Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            if (agentState.isRunning) StudioNeonCyan.copy(alpha = 0.5f) else StudioNeonGreen.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        ),
                    color = StudioDarkSurfaceVariant
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (agentState.isRunning) StudioNeonCyan else StudioNeonGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (agentState.isRunning) "Agent: ${agentState.activeAgent}" else "Generation Ready",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = StudioTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            NeonBadge(
                                text = "${agentState.progressPercent}%",
                                color = if (agentState.isRunning) StudioNeonCyan else StudioNeonGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = agentState.progressPercent / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .testTag("agent_progress_bar"),
                            color = StudioNeonCyan,
                            trackColor = StudioDarkSurfaceHighlight
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = agentState.stepDetail.ifBlank { "Processing autonomous generation..." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = StudioTextSecondary
                        )
                    }
                }
            }

            // Quick Navigation Hub
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onViewScriptClick(projectId) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pipeline_view_script_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioDarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderColor)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = StudioNeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Script & Facts", color = StudioTextPrimary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onViewStoryboardClick(projectId) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pipeline_view_storyboard_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioDarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderColor)
                    ) {
                        Icon(Icons.Default.ViewCarousel, contentDescription = null, tint = StudioNeonPink, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Storyboard", color = StudioTextPrimary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onViewPreviewClick(projectId) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pipeline_view_preview_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioDarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioNeonViolet.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = StudioNeonViolet, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Player", color = StudioNeonViolet, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Agents Pipeline Stages
            item {
                Text(
                    text = "Autonomous Multi-Agent Workflow",
                    style = MaterialTheme.typography.titleMedium,
                    color = StudioTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Display key pipeline stages
            val mainStages = listOf(
                AgentPipelineStage.UNDERSTANDING_PROMPT,
                AgentPipelineStage.RESEARCHING_TOPIC,
                AgentPipelineStage.WRITING_SCRIPT,
                AgentPipelineStage.CREATING_STORYBOARD,
                AgentPipelineStage.GENERATING_VISUALS,
                AgentPipelineStage.GENERATING_VOICEOVER,
                AgentPipelineStage.EDITING_VIDEO,
                AgentPipelineStage.COMPLETED
            )

            items(mainStages) { stage ->
                val currentOrdinal = agentState.currentStage.stageNumber
                val stageOrdinal = stage.stageNumber

                val isPassed = currentOrdinal > stageOrdinal
                val isCurrent = agentState.currentStage == stage
                val isPending = currentOrdinal < stageOrdinal

                AgentStageCard(
                    stage = stage,
                    isPassed = isPassed,
                    isCurrent = isCurrent,
                    isPending = isPending
                )
            }

            // Live Agent Logs
            item {
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = StudioDarkSurface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Agent Logs & Reasoning",
                            style = MaterialTheme.typography.titleMedium,
                            color = StudioTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        NeonBadge(text = "${agentState.logs.size} EVENTS", color = StudioNeonViolet)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (agentState.logs.isEmpty()) {
                        Text(
                            text = "Agent initializing autonomous task queue...",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextTertiary
                        )
                    } else {
                        agentState.logs.takeLast(6).reversed().forEach { log ->
                            AgentStepLogLine(log = log)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Bottom Navigation
            item {
                PrimaryGradientButton(
                    text = if (agentState.isRunning) "Open Video Studio Player" else "Open Final Video Preview & Export",
                    icon = Icons.Default.Movie,
                    onClick = { onViewPreviewClick(projectId) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "pipeline_open_player_button"
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AgentStageCard(
    stage: AgentPipelineStage,
    isPassed: Boolean,
    isCurrent: Boolean,
    isPending: Boolean
) {
    val stageColor = when {
        isPassed -> StudioNeonGreen
        isCurrent -> StudioNeonCyan
        else -> StudioTextTertiary
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isCurrent) StudioNeonCyan.copy(alpha = 0.5f) else StudioBorderColor,
                RoundedCornerShape(12.dp)
            ),
        color = if (isCurrent) StudioDarkSurfaceVariant else StudioDarkSurface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(stageColor.copy(alpha = 0.15f))
                    .border(1.dp, stageColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isPassed) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = StudioNeonGreen, modifier = Modifier.size(18.dp))
                } else if (isCurrent) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = StudioNeonCyan
                    )
                } else {
                    Text(
                        text = "${stage.stageNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = StudioTextTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stage.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isPending) StudioTextSecondary else StudioTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stage.agentName} • ${stage.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioTextTertiary
                )
            }

            if (isCurrent) {
                NeonBadge(text = "ACTIVE", color = StudioNeonCyan)
            } else if (isPassed) {
                NeonBadge(text = "DONE", color = StudioNeonGreen)
            }
        }
    }
}

@Composable
fun AgentStepLogLine(log: AgentStepLog) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = StudioDarkSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "[${log.stage.agentName}]",
                style = MaterialTheme.typography.labelSmall,
                color = if (log.isError) StudioNeonPink else StudioNeonCyan,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall,
                color = StudioTextPrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
