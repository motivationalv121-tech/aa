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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Project
import com.example.model.ProjectStatus
import com.example.ui.components.NeonBadge
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioHeader
import com.example.ui.theme.*
import com.example.viewmodel.StudioViewModel

@Composable
fun ProjectsListScreen(
    viewModel: StudioViewModel,
    onBackClick: () -> Unit,
    onProjectClick: (String) -> Unit,
    onCreateNewClick: () -> Unit
) {
    val projects by viewModel.allProjects.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredProjects = projects.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.userPrompt.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = StudioDarkCanvas,
        topBar = {
            StudioHeader(
                title = "All Video Projects",
                subtitle = "${projects.size} AI Generated Projects",
                navigationIcon = Icons.Default.ArrowBack,
                onNavClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNewClick,
                containerColor = StudioNeonCyan,
                contentColor = Color.Black,
                modifier = Modifier.testTag("fab_create_project")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Video")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_projects_input"),
                    placeholder = { Text("Search projects...", color = StudioTextTertiary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = StudioTextSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = StudioTextSecondary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StudioTextPrimary,
                        unfocusedTextColor = StudioTextPrimary,
                        focusedBorderColor = StudioNeonCyan,
                        unfocusedBorderColor = StudioBorderColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (filteredProjects.isEmpty()) {
                item {
                    StudioCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = StudioTextTertiary, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No projects found", style = MaterialTheme.typography.titleMedium, color = StudioTextPrimary)
                        }
                    }
                }
            } else {
                items(filteredProjects) { project ->
                    ProjectRowCard(
                        project = project,
                        onClick = { onProjectClick(project.id) },
                        onDuplicate = { viewModel.duplicateProject(project.id) },
                        onDelete = { viewModel.deleteProject(project.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
fun ProjectRowCard(
    project: Project,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, StudioBorderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = StudioDarkSurface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NeonBadge(text = project.aspectRatio.ratioString, color = StudioNeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "${project.targetDurationSec}s", style = MaterialTheme.typography.labelSmall, color = StudioTextSecondary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = StudioTextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StudioNeonPink, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = project.title,
                style = MaterialTheme.typography.titleMedium,
                color = StudioTextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = project.userPrompt,
                style = MaterialTheme.typography.bodySmall,
                color = StudioTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lang: ${project.language} • ${project.tone}",
                    style = MaterialTheme.typography.labelSmall,
                    color = StudioTextTertiary
                )
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
}
