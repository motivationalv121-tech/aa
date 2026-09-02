package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.KeyFact
import com.example.model.ScriptScene
import com.example.model.SourceReference
import com.example.ui.components.NeonBadge
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioHeader
import com.example.ui.theme.*
import com.example.viewmodel.StudioViewModel

@Composable
fun ResearchScriptScreen(
    viewModel: StudioViewModel,
    projectId: String,
    onBackClick: () -> Unit,
    onOpenStoryboardClick: (String) -> Unit
) {
    LaunchedEffect(projectId) {
        viewModel.selectProject(projectId)
    }

    val project by viewModel.currentProject.collectAsState()
    val research by viewModel.currentResearch.collectAsState()
    val script by viewModel.currentScript.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Script, 1: Deep Research

    Scaffold(
        containerColor = StudioDarkCanvas,
        topBar = {
            StudioHeader(
                title = "Research & Script Agent",
                subtitle = project?.title ?: "AI Script Breakdown",
                navigationIcon = Icons.Default.ArrowBack,
                onNavClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = StudioDarkSurface,
                contentColor = StudioNeonCyan,
                divider = { Divider(color = StudioBorderColor) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Script Breakdown", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Deep Research & Facts", fontWeight = FontWeight.Bold) }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                if (selectedTab == 0) {
                    // Script View
                    if (script == null) {
                        item {
                            StudioCard(modifier = Modifier.fillMaxWidth()) {
                                Text("No script generated yet.", color = StudioTextSecondary)
                            }
                        }
                    } else {
                        val s = script!!
                        item {
                            StudioCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = StudioNeonPink.copy(alpha = 0.5f),
                                backgroundColor = StudioDarkSurfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NeonBadge(text = "TOTAL RUNTIME: ${s.totalDurationSec}s", color = StudioNeonPink)
                                    Text(
                                        text = "${s.language} • ${s.tone}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StudioTextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = s.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = StudioTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Scenes Breakdown
                        items(s.scenes) { scene ->
                            StudioCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NeonBadge(text = "SCENE ${scene.sceneNumber}: ${scene.sceneType.displayName}", color = StudioNeonCyan)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${scene.estimatedDurationSec}s",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StudioTextSecondary
                                        )
                                        IconButton(
                                            onClick = {
                                                viewModel.previewVoice(scene.narrationText, project?.language ?: "Hindi")
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.VolumeUp, contentDescription = "Play", tint = StudioNeonCyan, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = scene.narrationText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = StudioTextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp)),
                                    color = StudioDarkSurfaceHighlight
                                ) {
                                    Text(
                                        text = "🎬 Visual: ${scene.visualSummary}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StudioNeonAmber,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Deep Research View
                    if (research == null) {
                        item {
                            StudioCard(modifier = Modifier.fillMaxWidth()) {
                                Text("No research data found.", color = StudioTextSecondary)
                            }
                        }
                    } else {
                        val r = research!!
                        item {
                            StudioCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = StudioNeonCyan.copy(alpha = 0.5f)
                            ) {
                                NeonBadge(text = "RESEARCH EXECUTIVE SUMMARY", color = StudioNeonCyan)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = r.summary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = StudioTextPrimary
                                )
                            }
                        }

                        item {
                            Text(
                                text = "Verified Key Facts (${r.keyFacts.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = StudioTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(r.keyFacts) { fact: KeyFact ->
                            StudioCard(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StudioNeonGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = fact.title, style = MaterialTheme.typography.labelLarge, color = StudioTextPrimary, fontWeight = FontWeight.Bold)
                                        Text(text = fact.detail, style = MaterialTheme.typography.bodyMedium, color = StudioTextSecondary)
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Verified Sources & References (${r.sources.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = StudioTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(r.sources) { source: SourceReference ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp)),
                                color = StudioDarkSurface
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Link, contentDescription = null, tint = StudioNeonCyan, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = source.title, style = MaterialTheme.typography.bodyMedium, color = StudioTextPrimary, fontWeight = FontWeight.SemiBold)
                                        Text(text = source.domain, style = MaterialTheme.typography.bodySmall, color = StudioTextTertiary)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { onOpenStoryboardClick(projectId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("research_to_storyboard_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioNeonViolet),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ViewCarousel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Proceed to Storyboard & Visuals", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
