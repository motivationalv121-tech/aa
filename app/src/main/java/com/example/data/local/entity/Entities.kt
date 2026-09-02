package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.*

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val userPrompt: String,
    val customInstructions: String,
    val aspectRatio: String,
    val targetDurationSec: Int,
    val language: String,
    val tone: String,
    val voiceStyle: String,
    val status: String,
    val resolution: String,
    val fps: String,
    val outputVideoUri: String?,
    val thumbnailUri: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "research_data")
data class ResearchEntity(
    @PrimaryKey val projectId: String,
    val topic: String,
    val summary: String,
    val keyFactsJson: String,
    val sourcesJson: String,
    val statisticsJson: String,
    val timelineJson: String,
    val entitiesJson: String,
    val confidenceScore: Float,
    val uncertainNotes: String
)

@Entity(tableName = "scripts")
data class ScriptEntity(
    @PrimaryKey val projectId: String,
    val title: String,
    val language: String,
    val tone: String,
    val audience: String,
    val totalDurationSec: Int,
    val fullScriptText: String,
    val scenesJson: String
)

@Entity(tableName = "storyboards")
data class StoryboardEntity(
    @PrimaryKey val projectId: String,
    val scenesJson: String
)

@Entity(tableName = "timelines")
data class TimelineEntity(
    @PrimaryKey val projectId: String,
    val totalDurationMs: Long,
    val videoClipsJson: String,
    val audioTracksJson: String,
    val subtitlesJson: String,
    val lastAiAutoEditNote: String
)
