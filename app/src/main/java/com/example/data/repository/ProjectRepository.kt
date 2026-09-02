package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.converter.DatabaseConverters
import com.example.data.local.entity.*
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class ProjectRepository(private val db: AppDatabase) {
    private val converters = DatabaseConverters()

    fun observeAllProjects(): Flow<List<Project>> {
        return db.projectDao().getAllProjects().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun observeProject(id: String): Flow<Project?> {
        return db.projectDao().observeProjectById(id).map { it?.toDomain() }
    }

    suspend fun getProject(id: String): Project? = withContext(Dispatchers.IO) {
        db.projectDao().getProjectById(id)?.toDomain()
    }

    suspend fun saveProject(project: Project) = withContext(Dispatchers.IO) {
        db.projectDao().insertProject(project.toEntity())
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        db.projectDao().deleteProjectById(id)
        db.researchDao().deleteResearchByProjectId(id)
        db.scriptDao().deleteScriptByProjectId(id)
        db.storyboardDao().deleteStoryboardByProjectId(id)
        db.timelineDao().deleteTimelineByProjectId(id)
    }

    suspend fun duplicateProject(originalId: String): String = withContext(Dispatchers.IO) {
        val original = getProject(originalId) ?: return@withContext ""
        val newId = UUID.randomUUID().toString()
        val duplicatedProject = original.copy(
            id = newId,
            title = "${original.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        saveProject(duplicatedProject)

        val research = getResearch(originalId)
        if (research != null) {
            saveResearch(research.copy(projectId = newId))
        }

        val script = getScript(originalId)
        if (script != null) {
            saveScript(script.copy(projectId = newId))
        }

        val storyboard = getStoryboard(originalId)
        if (storyboard.isNotEmpty()) {
            saveStoryboard(newId, storyboard.map { it.copy(id = UUID.randomUUID().toString(), projectId = newId) })
        }

        val timeline = getTimeline(originalId)
        if (timeline != null) {
            saveTimeline(timeline.copy(projectId = newId))
        }

        newId
    }

    // Research
    fun observeResearch(projectId: String): Flow<ResearchData?> {
        return db.researchDao().observeResearchByProjectId(projectId).map { entity ->
            entity?.let {
                ResearchData(
                    projectId = it.projectId,
                    topic = it.topic,
                    summary = it.summary,
                    keyFacts = converters.toKeyFactList(it.keyFactsJson),
                    sources = converters.toSourceList(it.sourcesJson),
                    statistics = converters.toStatsList(it.statisticsJson),
                    timeline = converters.toTimelineEventList(it.timelineJson),
                    entities = converters.toStringList(it.entitiesJson),
                    confidenceScore = it.confidenceScore,
                    uncertainNotes = it.uncertainNotes
                )
            }
        }
    }

    suspend fun getResearch(projectId: String): ResearchData? = withContext(Dispatchers.IO) {
        val entity = db.researchDao().getResearchByProjectId(projectId) ?: return@withContext null
        ResearchData(
            projectId = entity.projectId,
            topic = entity.topic,
            summary = entity.summary,
            keyFacts = converters.toKeyFactList(entity.keyFactsJson),
            sources = converters.toSourceList(entity.sourcesJson),
            statistics = converters.toStatsList(entity.statisticsJson),
            timeline = converters.toTimelineEventList(entity.timelineJson),
            entities = converters.toStringList(entity.entitiesJson),
            confidenceScore = entity.confidenceScore,
            uncertainNotes = entity.uncertainNotes
        )
    }

    suspend fun saveResearch(research: ResearchData) = withContext(Dispatchers.IO) {
        val entity = ResearchEntity(
            projectId = research.projectId,
            topic = research.topic,
            summary = research.summary,
            keyFactsJson = converters.fromKeyFactList(research.keyFacts),
            sourcesJson = converters.fromSourceList(research.sources),
            statisticsJson = converters.fromStatsList(research.statistics),
            timelineJson = converters.fromTimelineEventList(research.timeline),
            entitiesJson = converters.fromStringList(research.entities),
            confidenceScore = research.confidenceScore,
            uncertainNotes = research.uncertainNotes
        )
        db.researchDao().insertResearch(entity)
    }

    // Script
    fun observeScript(projectId: String): Flow<Script?> {
        return db.scriptDao().observeScriptByProjectId(projectId).map { entity ->
            entity?.let {
                Script(
                    projectId = it.projectId,
                    title = it.title,
                    language = it.language,
                    tone = it.tone,
                    audience = it.audience,
                    totalDurationSec = it.totalDurationSec,
                    fullScriptText = it.fullScriptText,
                    scenes = converters.toScriptSceneList(it.scenesJson)
                )
            }
        }
    }

    suspend fun getScript(projectId: String): Script? = withContext(Dispatchers.IO) {
        val entity = db.scriptDao().getScriptByProjectId(projectId) ?: return@withContext null
        Script(
            projectId = entity.projectId,
            title = entity.title,
            language = entity.language,
            tone = entity.tone,
            audience = entity.audience,
            totalDurationSec = entity.totalDurationSec,
            fullScriptText = entity.fullScriptText,
            scenes = converters.toScriptSceneList(entity.scenesJson)
        )
    }

    suspend fun saveScript(script: Script) = withContext(Dispatchers.IO) {
        val entity = ScriptEntity(
            projectId = script.projectId,
            title = script.title,
            language = script.language,
            tone = script.tone,
            audience = script.audience,
            totalDurationSec = script.totalDurationSec,
            fullScriptText = script.fullScriptText,
            scenesJson = converters.fromScriptSceneList(script.scenes)
        )
        db.scriptDao().insertScript(entity)
    }

    // Storyboard
    fun observeStoryboard(projectId: String): Flow<List<StoryboardScene>> {
        return db.storyboardDao().observeStoryboardByProjectId(projectId).map { entity ->
            entity?.let { converters.toStoryboardSceneList(it.scenesJson) } ?: emptyList()
        }
    }

    suspend fun getStoryboard(projectId: String): List<StoryboardScene> = withContext(Dispatchers.IO) {
        val entity = db.storyboardDao().getStoryboardByProjectId(projectId) ?: return@withContext emptyList()
        converters.toStoryboardSceneList(entity.scenesJson)
    }

    suspend fun saveStoryboard(projectId: String, scenes: List<StoryboardScene>) = withContext(Dispatchers.IO) {
        val entity = StoryboardEntity(
            projectId = projectId,
            scenesJson = converters.fromStoryboardSceneList(scenes)
        )
        db.storyboardDao().insertStoryboard(entity)
    }

    // Timeline
    fun observeTimeline(projectId: String): Flow<TimelineProject?> {
        return db.timelineDao().observeTimelineByProjectId(projectId).map { entity ->
            entity?.let {
                TimelineProject(
                    projectId = it.projectId,
                    totalDurationMs = it.totalDurationMs,
                    videoClips = converters.toVideoClipList(it.videoClipsJson),
                    audioTracks = converters.toAudioTrackList(it.audioTracksJson),
                    subtitles = converters.toSubtitleList(it.subtitlesJson),
                    lastAiAutoEditNote = it.lastAiAutoEditNote
                )
            }
        }
    }

    suspend fun getTimeline(projectId: String): TimelineProject? = withContext(Dispatchers.IO) {
        val entity = db.timelineDao().getTimelineByProjectId(projectId) ?: return@withContext null
        TimelineProject(
            projectId = entity.projectId,
            totalDurationMs = entity.totalDurationMs,
            videoClips = converters.toVideoClipList(entity.videoClipsJson),
            audioTracks = converters.toAudioTrackList(entity.audioTracksJson),
            subtitles = converters.toSubtitleList(entity.subtitlesJson),
            lastAiAutoEditNote = entity.lastAiAutoEditNote
        )
    }

    suspend fun saveTimeline(timeline: TimelineProject) = withContext(Dispatchers.IO) {
        val entity = TimelineEntity(
            projectId = timeline.projectId,
            totalDurationMs = timeline.totalDurationMs,
            videoClipsJson = converters.fromVideoClipList(timeline.videoClips),
            audioTracksJson = converters.fromAudioTrackList(timeline.audioTracks),
            subtitlesJson = converters.fromSubtitleList(timeline.subtitles),
            lastAiAutoEditNote = timeline.lastAiAutoEditNote
        )
        db.timelineDao().insertTimeline(entity)
    }

    private fun ProjectEntity.toDomain(): Project {
        return Project(
            id = id,
            title = title,
            userPrompt = userPrompt,
            customInstructions = customInstructions,
            aspectRatio = try { AspectRatio.valueOf(aspectRatio) } catch (e: Exception) { AspectRatio.PORTRAIT_9_16 },
            targetDurationSec = targetDurationSec,
            language = language,
            tone = tone,
            voiceStyle = voiceStyle,
            status = try { ProjectStatus.valueOf(status) } catch (e: Exception) { ProjectStatus.DRAFT },
            resolution = try { VideoResolution.valueOf(resolution) } catch (e: Exception) { VideoResolution.RES_1080P },
            fps = try { VideoFps.valueOf(fps) } catch (e: Exception) { VideoFps.FPS_30 },
            outputVideoUri = outputVideoUri,
            thumbnailUri = thumbnailUri,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun Project.toEntity(): ProjectEntity {
        return ProjectEntity(
            id = id,
            title = title,
            userPrompt = userPrompt,
            customInstructions = customInstructions,
            aspectRatio = aspectRatio.name,
            targetDurationSec = targetDurationSec,
            language = language,
            tone = tone,
            voiceStyle = voiceStyle,
            status = status.name,
            resolution = resolution.name,
            fps = fps.name,
            outputVideoUri = outputVideoUri,
            thumbnailUri = thumbnailUri,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
