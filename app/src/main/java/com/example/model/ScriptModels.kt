package com.example.model

enum class SceneType(val displayName: String, val badgeColorHex: Long) {
    HOOK("Hook / Viral Intro", 0xFFFF5722),
    INTRO("Introduction", 0xFF2196F3),
    MAIN_INFO("Core Narrative", 0xFF9C27B0),
    FACT("Shocking Fact / Stat", 0xFFFF9800),
    CLIMAX("Emotional Peak / Climax", 0xFFE91E63),
    CONCLUSION("Conclusion", 0xFF009688),
    CTA("Call to Action (Subscribe/Follow)", 0xFF4CAF50)
}

data class ScriptScene(
    val sceneNumber: Int,
    val sceneType: SceneType,
    val title: String,
    val narrationText: String,
    val visualSummary: String,
    val estimatedDurationSec: Int,
    val onScreenText: String = "",
    val toneSuggestion: String = "Energetic"
)

data class Script(
    val projectId: String,
    val title: String,
    val language: String,
    val tone: String,
    val audience: String,
    val totalDurationSec: Int,
    val fullScriptText: String,
    val scenes: List<ScriptScene> = emptyList()
)
