package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Project
import com.example.model.ProjectStatus
import com.example.ui.components.NeonBadge
import com.example.ui.components.PrimaryGradientButton
import com.example.ui.components.StudioCard
import com.example.ui.theme.*
import com.example.viewmodel.StudioViewModel

@Composable
fun HomeScreen(
    viewModel: StudioViewModel,
    onCreateNewClick: () -> Unit,
    onProjectClick: (String) -> Unit,
    onQuickPromptClick: (String, String, String) -> Unit,
    onSettingsClick: () -> Unit,
    onProjectsListClick: () -> Unit
) {
    val projects by viewModel.allProjects.collectAsState()
    val agentState by viewModel.agentState.collectAsState()

    Scaffold(
        containerColor = StudioDarkCanvas,
        topBar = {
            Surface(
                color = StudioDarkSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(StudioNeonViolet, StudioNeonCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AI Video Studio",
                                style = MaterialTheme.typography.titleLarge,
                                color = StudioTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Autonomous Video Generation Agent",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = StudioTextSecondary
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Hero One-Prompt Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            1.dp,
                            Brush.horizontalGradient(
                                listOf(StudioNeonViolet.copy(alpha = 0.6f), StudioNeonCyan.copy(alpha = 0.6f))
                            ),
                            RoundedCornerShape(20.dp)
                        ),
                    color = StudioDarkSurfaceVariant
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NeonBadge(text = "AUTONOMOUS PIPELINE", color = StudioNeonCyan)
                            if (agentState.isRunning) {
                                NeonBadge(text = "AGENT RUNNING (${agentState.progressPercent}%)", color = StudioNeonAmber)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "One Prompt to Master Video",
                            style = MaterialTheme.typography.displayMedium,
                            color = StudioTextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 30.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Research • Script • Storyboard • AI Visuals • Voiceover • Dynamic Captions • Auto Edit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StudioTextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PrimaryGradientButton(
                            text = "Create New AI Video",
                            icon = Icons.Default.Add,
                            onClick = onCreateNewClick,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "home_create_video_button"
                        )
                    }
                }
            }

            // Quick Studio Hub
            item {
                Column {
                    Text(
                        text = "Studio Agent Agents",
                        style = MaterialTheme.typography.titleMedium,
                        color = StudioTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickAgentCard(
                            title = "Deep Research",
                            subtitle = "Fact Checking",
                            icon = Icons.Default.Search,
                            color = StudioNeonCyan,
                            modifier = Modifier.weight(1f),
                            onClick = onCreateNewClick
                        )
                        QuickAgentCard(
                            title = "Viral Script",
                            subtitle = "Scene Writer",
                            icon = Icons.Default.EditNote,
                            color = StudioNeonPink,
                            modifier = Modifier.weight(1f),
                            onClick = onCreateNewClick
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickAgentCard(
                            title = "Visual Engine",
                            subtitle = "AI Storyboard",
                            icon = Icons.Default.MovieCreation,
                            color = StudioNeonAmber,
                            modifier = Modifier.weight(1f),
                            onClick = onCreateNewClick
                        )
                        QuickAgentCard(
                            title = "Auto Editor",
                            subtitle = "Multi-Track Sync",
                            icon = Icons.Default.Tune,
                            color = StudioNeonGreen,
                            modifier = Modifier.weight(1f),
                            onClick = onCreateNewClick
                        )
                    }
                }
            }

            // Quick-Start Prompt Templates
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Instant Trending Templates",
                            style = MaterialTheme.typography.titleMedium,
                            color = StudioTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        NeonBadge(text = "ONE-CLICK", color = StudioNeonViolet)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        item {
                            PromptTemplateCard(
                                title = "चंद्रयान-3 मिशन",
                                subtitle = "India's Lunar South Pole Triumph",
                                language = "Hindi",
                                tone = "Motivational & Patriotic",
                                prompt = "चंद्रयान-3 मिशन: इसरो का ऐतिहासिक चंद्रमा पर सफल लैंडिंग का पूरा सफर",
                                onClick = { onQuickPromptClick(it, "Hindi", "Motivational") }
                            )
                        }
                        item {
                            PromptTemplateCard(
                                title = "गरीब से करोड़पति बनने का सफर",
                                subtitle = "Zero to Wealth Mindset & High Income Skills",
                                language = "Hindi",
                                tone = "Motivational",
                                prompt = "गरीब से करोड़पति बनने का सफर: कैसे 0 से शुरू करके अमीर बनें",
                                onClick = { onQuickPromptClick(it, "Hindi", "Motivational") }
                            )
                        }
                        item {
                            PromptTemplateCard(
                                title = "The AI Revolution 2026",
                                subtitle = "How Autonomous Agents Change Everything",
                                language = "English",
                                tone = "Cinematic & Tech",
                                prompt = "The AI Revolution 2026: Autonomous Agents, Robotics and the Future of Work",
                                onClick = { onQuickPromptClick(it, "English", "Cinematic") }
                            )
                        }
                    }
                }
            }

            // Recent Projects
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Projects (${projects.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = StudioTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (projects.isNotEmpty()) {
                        TextButton(
                            onClick = onProjectsListClick,
                            modifier = Modifier.testTag("view_all_projects_button")
                        ) {
                            Text("View All", color = StudioNeonCyan)
                        }
                    }
                }
            }

            if (projects.isEmpty()) {
                item {
                    StudioCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = StudioDarkSurface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = StudioTextTertiary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Projects Yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = StudioTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Type any prompt to generate a full video with research, script & storyboard.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StudioTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(projects.take(4)) { project ->
                    ProjectItemCard(
                        project = project,
                        onClick = { onProjectClick(project.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun QuickAgentCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = StudioDarkSurface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = StudioTextPrimary, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = StudioTextSecondary)
            }
        }
    }
}

@Composable
fun PromptTemplateCard(
    title: String,
    subtitle: String,
    language: String,
    tone: String,
    prompt: String,
    onClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, StudioBorderColor, RoundedCornerShape(16.dp))
            .clickable { onClick(prompt) },
        color = StudioDarkSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonBadge(text = language, color = StudioNeonCyan)
                Text(text = tone, style = MaterialTheme.typography.labelSmall, color = StudioTextTertiary)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = StudioTextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = StudioTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Now", style = MaterialTheme.typography.labelSmall, color = StudioNeonViolet, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = StudioNeonViolet, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun ProjectItemCard(
    project: Project,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, StudioBorderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = StudioDarkSurface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudioDarkSurfaceHighlight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleOutline,
                    contentDescription = null,
                    tint = StudioNeonCyan,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = StudioTextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${project.aspectRatio.ratioString} • ${project.targetDurationSec}s • ${project.language}",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            val statusColor = when (project.status) {
                ProjectStatus.COMPLETED -> StudioNeonGreen
                ProjectStatus.AWAITING_APPROVAL -> StudioNeonAmber
                ProjectStatus.FAILED -> StudioNeonPink
                else -> StudioNeonCyan
            }

            NeonBadge(text = project.status.name.replace("_", " "), color = statusColor)
        }
    }
}
