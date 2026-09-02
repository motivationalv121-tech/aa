package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.ProjectRepository
import com.example.model.*
import com.example.service.ai.AiAutoEditEngine
import com.example.service.ai.AiOrchestrator
import com.example.service.ai.ResearchAgent
import com.example.service.ai.ScriptAgent
import com.example.service.ai.StoryboardAgent
import com.example.service.audio.AudioVoiceService
import com.example.service.video.VisualGenerationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = ProjectRepository(db)
    val orchestrator = AiOrchestrator(application, repository)
    val voiceService = AudioVoiceService(application)
    private val researchAgent = ResearchAgent()
    private val scriptAgent = ScriptAgent()
    private val storyboardAgent = StoryboardAgent()
    private val autoEditEngine = AiAutoEditEngine()
    private val visualManager = VisualGenerationManager(application)

    // Projects Flow
    val allProjects: StateFlow<List<Project>> = repository.observeAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Active Project ID
    private val _currentProjectId = MutableStateFlow<String?>(null)
    val currentProjectId: StateFlow<String?> = _currentProjectId.asStateFlow()

    // Current Project Flow
    val currentProject: StateFlow<Project?> = _currentProjectId.flatMapLatest { id ->
        if (id != null) repository.observeProject(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Project Research Data
    val currentResearch: StateFlow<ResearchData?> = _currentProjectId.flatMapLatest { id ->
        if (id != null) repository.observeResearch(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Project Script
    val currentScript: StateFlow<Script?> = _currentProjectId.flatMapLatest { id ->
        if (id != null) repository.observeScript(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Project Storyboard
    val currentStoryboard: StateFlow<List<StoryboardScene>> = _currentProjectId.flatMapLatest { id ->
        if (id != null) repository.observeStoryboard(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Project Timeline
    val currentTimeline: StateFlow<TimelineProject?> = _currentProjectId.flatMapLatest { id ->
        if (id != null) repository.observeTimeline(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Agent Live State
    val agentState: StateFlow<AgentState> = orchestrator.agentState

    // Global Settings
    private val _settings = MutableStateFlow(ProviderSettings())
    val settings: StateFlow<ProviderSettings> = _settings.asStateFlow()

    // UI Message / Snack State
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun selectProject(projectId: String) {
        _currentProjectId.value = projectId
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showToast(msg: String) {
        _userMessage.value = msg
    }

    fun updateSettings(newSettings: ProviderSettings) {
        _settings.value = newSettings
    }

    // 1. Create New Project & Run Full Workflow
    fun createProjectFromPrompt(
        prompt: String,
        customInstructions: String = "",
        aspectRatio: AspectRatio = AspectRatio.PORTRAIT_9_16,
        targetDurationSec: Int = 60,
        language: String = "Hindi",
        tone: String = "Motivational",
        voiceStyle: String = "Male Energetic",
        onProjectCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            val projectId = UUID.randomUUID().toString()
            val title = if (prompt.length > 35) prompt.take(32) + "..." else prompt
            val project = Project(
                id = projectId,
                title = title,
                userPrompt = prompt,
                customInstructions = customInstructions,
                aspectRatio = aspectRatio,
                targetDurationSec = targetDurationSec,
                language = language,
                tone = tone,
                voiceStyle = voiceStyle,
                status = ProjectStatus.RESEARCHING
            )

            repository.saveProject(project)
            _currentProjectId.value = projectId
            onProjectCreated(projectId)

            orchestrator.executeMasterWorkflow(project, _settings.value)
        }
    }

    // 2. Standalone Research Agent
    fun runResearchOnly(topic: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val projectId = UUID.randomUUID().toString()
            val project = Project(
                id = projectId,
                title = "Research: $topic",
                userPrompt = topic,
                status = ProjectStatus.DRAFT
            )
            repository.saveProject(project)
            _currentProjectId.value = projectId

            val research = researchAgent.executeResearch(topic, projectId, _settings.value.customGeminiApiKey)
            repository.saveResearch(research)
            onComplete(projectId)
        }
    }

    // 3. Standalone Script Generation
    fun generateScriptOnly(
        topic: String,
        language: String,
        tone: String,
        durationSec: Int,
        onComplete: (String) -> Unit
    ) {
        viewModelScope.launch {
            val projectId = _currentProjectId.value ?: UUID.randomUUID().toString()
            var project = repository.getProject(projectId)
            if (project == null) {
                project = Project(
                    id = projectId,
                    title = "Script: $topic",
                    userPrompt = topic,
                    language = language,
                    tone = tone,
                    targetDurationSec = durationSec
                )
                repository.saveProject(project)
                _currentProjectId.value = projectId
            }

            var research = repository.getResearch(projectId)
            if (research == null) {
                research = researchAgent.executeResearch(topic, projectId, _settings.value.customGeminiApiKey)
                repository.saveResearch(research)
            }

            val script = scriptAgent.generateScript(
                prompt = topic,
                research = research,
                durationSec = durationSec,
                language = language,
                tone = tone,
                audience = "General",
                projectId = projectId,
                apiKey = _settings.value.customGeminiApiKey
            )
            repository.saveScript(script)
            onComplete(projectId)
        }
    }

    // 4. Regenerate a Specific Storyboard Scene
    fun regenerateSceneVisual(sceneId: String) {
        viewModelScope.launch {
            val projectId = _currentProjectId.value ?: return@launch
            val scenes = repository.getStoryboard(projectId).toMutableList()
            val index = scenes.indexOfFirst { it.id == sceneId }
            if (index >= 0) {
                val scene = scenes[index]
                scenes[index] = scene.copy(status = SceneGenerationStatus.GENERATING)
                repository.saveStoryboard(projectId, scenes)

                val project = repository.getProject(projectId)
                val updatedScene = visualManager.generateSceneAsset(
                    scene = scene,
                    providerType = _settings.value.selectedVideoProvider,
                    aspectRatio = project?.aspectRatio ?: AspectRatio.PORTRAIT_9_16,
                    apiKey = _settings.value.customGeminiApiKey
                )
                scenes[index] = updatedScene
                repository.saveStoryboard(projectId, scenes)
                _userMessage.value = "Scene ${updatedScene.sceneNumber} visual regenerated!"
            }
        }
    }

    // 5. Update a Storyboard Scene
    fun updateStoryboardScene(updatedScene: StoryboardScene) {
        viewModelScope.launch {
            val projectId = _currentProjectId.value ?: return@launch
            val scenes = repository.getStoryboard(projectId).toMutableList()
            val index = scenes.indexOfFirst { it.id == updatedScene.id }
            if (index >= 0) {
                scenes[index] = updatedScene
                repository.saveStoryboard(projectId, scenes)
            }
        }
    }

    // 6. AI Auto Edit with Custom Instructions
    fun triggerAiAutoEdit(customInstructionText: String) {
        viewModelScope.launch {
            val projectId = _currentProjectId.value ?: return@launch
            val project = repository.getProject(projectId) ?: return@launch
            val storyboard = repository.getStoryboard(projectId)
            val script = repository.getScript(projectId)
            val timeline = repository.getTimeline(projectId) ?: TimelineProject(projectId = projectId)

            val updatedProject = project.copy(customInstructions = customInstructionText)
            repository.saveProject(updatedProject)

            val (editedTimeline, editPlan) = autoEditEngine.applyAutoEdit(
                timeline = timeline,
                storyboard = storyboard,
                script = script,
                customInstructions = customInstructionText,
                apiKey = _settings.value.customGeminiApiKey
            )
            repository.saveTimeline(editedTimeline)
            _userMessage.value = "AI Auto-Edit applied: ${editPlan.targetStyle}"
        }
    }

    // 7. Approve & Final Render
    fun approveAndRenderFinal(onRenderComplete: () -> Unit) {
        viewModelScope.launch {
            val projectId = _currentProjectId.value ?: return@launch
            val project = repository.getProject(projectId) ?: return@launch
            val success = orchestrator.executeFinalRender(projectId, project)
            if (success) {
                onRenderComplete()
            }
        }
    }

    // 8. Delete Project
    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_currentProjectId.value == projectId) {
                _currentProjectId.value = null
            }
            _userMessage.value = "Project deleted."
        }
    }

    // 9. Duplicate Project
    fun duplicateProject(projectId: String) {
        viewModelScope.launch {
            val newId = repository.duplicateProject(projectId)
            if (newId.isNotBlank()) {
                _userMessage.value = "Project duplicated!"
            }
        }
    }

    // 10. Voice Preview
    fun previewVoice(text: String, language: String) {
        voiceService.setVoiceLanguage(language)
        voiceService.setSpeechParameters(_settings.value.ttsSpeechRate, _settings.value.ttsPitch)
        voiceService.speakText(text)
    }

    fun stopVoice() {
        voiceService.stopSpeech()
    }

    override fun onCleared() {
        super.onCleared()
        voiceService.release()
    }
}
