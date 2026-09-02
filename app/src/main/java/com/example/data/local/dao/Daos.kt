package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    fun observeProjectById(id: String): Flow<ProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}

@Dao
interface ResearchDao {
    @Query("SELECT * FROM research_data WHERE projectId = :projectId LIMIT 1")
    suspend fun getResearchByProjectId(projectId: String): ResearchEntity?

    @Query("SELECT * FROM research_data WHERE projectId = :projectId LIMIT 1")
    fun observeResearchByProjectId(projectId: String): Flow<ResearchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResearch(research: ResearchEntity)

    @Query("DELETE FROM research_data WHERE projectId = :projectId")
    suspend fun deleteResearchByProjectId(projectId: String)
}

@Dao
interface ScriptDao {
    @Query("SELECT * FROM scripts WHERE projectId = :projectId LIMIT 1")
    suspend fun getScriptByProjectId(projectId: String): ScriptEntity?

    @Query("SELECT * FROM scripts WHERE projectId = :projectId LIMIT 1")
    fun observeScriptByProjectId(projectId: String): Flow<ScriptEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: ScriptEntity)

    @Query("DELETE FROM scripts WHERE projectId = :projectId")
    suspend fun deleteScriptByProjectId(projectId: String)
}

@Dao
interface StoryboardDao {
    @Query("SELECT * FROM storyboards WHERE projectId = :projectId LIMIT 1")
    suspend fun getStoryboardByProjectId(projectId: String): StoryboardEntity?

    @Query("SELECT * FROM storyboards WHERE projectId = :projectId LIMIT 1")
    fun observeStoryboardByProjectId(projectId: String): Flow<StoryboardEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryboard(storyboard: StoryboardEntity)

    @Query("DELETE FROM storyboards WHERE projectId = :projectId")
    suspend fun deleteStoryboardByProjectId(projectId: String)
}

@Dao
interface TimelineDao {
    @Query("SELECT * FROM timelines WHERE projectId = :projectId LIMIT 1")
    suspend fun getTimelineByProjectId(projectId: String): TimelineEntity?

    @Query("SELECT * FROM timelines WHERE projectId = :projectId LIMIT 1")
    fun observeTimelineByProjectId(projectId: String): Flow<TimelineEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeline(timeline: TimelineEntity)

    @Query("DELETE FROM timelines WHERE projectId = :projectId")
    suspend fun deleteTimelineByProjectId(projectId: String)
}
