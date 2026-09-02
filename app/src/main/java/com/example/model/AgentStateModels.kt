package com.example.model

enum class AgentPipelineStage(
    val stageNumber: Int,
    val title: String,
    val description: String,
    val agentName: String
) {
    IDLE(0, "Ready", "Waiting for prompt input", "AI Orchestrator"),
    UNDERSTANDING_PROMPT(1, "Understanding Prompt...", "Analyzing topic, target platform, tone & intent", "AI Orchestrator"),
    RESEARCHING_TOPIC(2, "Researching Topic...", "Querying web sources, validating facts & statistics", "AI Research Agent"),
    ANALYZING_SOURCES(3, "Analyzing Sources...", "Synthesizing cross-references & key timeline milestones", "AI Research Agent"),
    WRITING_SCRIPT(4, "Writing Script...", "Drafting scene-by-scene script with viral hook and CTA", "AI Script Agent"),
    CREATING_STORYBOARD(5, "Creating Storyboard...", "Generating visual prompts, camera directions & SFX cues", "AI Storyboard Agent"),
    GENERATING_VISUALS(6, "Generating Visuals...", "Synthesizing AI video clips & dynamic motion assets", "AI Video & Visual Engine"),
    GENERATING_VOICEOVER(7, "Generating Voiceover...", "Synthesizing natural AI speech & pacing in chosen language", "AI Voice Service"),
    ADDING_CAPTIONS(8, "Adding Captions...", "Synchronizing word-level animated subtitles", "AI Subtitle Engine"),
    EDITING_VIDEO(9, "Editing Video...", "Assembling multi-track timeline, beat matching & ducking", "AI Auto-Editor"),
    AWAITING_APPROVAL(10, "Awaiting Approval", "Preview ready for human review and custom instructions", "AI Orchestrator"),
    RENDERING_FINAL_VIDEO(11, "Rendering Final Video...", "Encoding high-bitrate MP4 with color grading", "AI Render Agent"),
    COMPLETED(12, "Completed ✓", "Final video rendered and ready for export", "AI Orchestrator"),
    ERROR(99, "Error Encountered", "Pipeline stopped with an issue", "AI Orchestrator")
}

data class AgentStepLog(
    val timestamp: Long = System.currentTimeMillis(),
    val stage: AgentPipelineStage,
    val message: String,
    val isWarning: Boolean = false,
    val isError: Boolean = false
)

data class AgentState(
    val currentStage: AgentPipelineStage = AgentPipelineStage.IDLE,
    val progressPercent: Int = 0,
    val activeAgent: String = "AI Orchestrator",
    val stepDetail: String = "Ready to start new video production",
    val logs: List<AgentStepLog> = emptyList(),
    val isRunning: Boolean = false,
    val error: String? = null
)
