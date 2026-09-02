package com.example.model

enum class AspectRatio(val label: String, val ratioString: String, val width: Int, val height: Int) {
    LANDSCAPE_16_9("16:9 (YouTube)", "16:9", 1920, 1080),
    PORTRAIT_9_16("9:16 (Reels/Shorts)", "9:16", 1080, 1920),
    SQUARE_1_1("1:1 (Square)", "1:1", 1080, 1080)
}

enum class VideoResolution(val label: String, val height: Int) {
    RES_720P("720p HD", 720),
    RES_1080P("1080p Full HD", 1080),
    RES_4K("4K Ultra HD", 2160)
}

enum class VideoFps(val label: String, val fps: Int) {
    FPS_24("24 fps (Cinematic)", 24),
    FPS_30("30 fps (Standard)", 30),
    FPS_60("60 fps (Smooth)", 60)
}

enum class ProjectStatus {
    DRAFT,
    RESEARCHING,
    SCRIPTING,
    STORYBOARDING,
    GENERATING_ASSETS,
    EDITING,
    AWAITING_APPROVAL,
    RENDERING,
    COMPLETED,
    FAILED
}

data class Project(
    val id: String,
    val title: String,
    val userPrompt: String,
    val customInstructions: String = "",
    val aspectRatio: AspectRatio = AspectRatio.PORTRAIT_9_16,
    val targetDurationSec: Int = 60,
    val language: String = "Hindi",
    val tone: String = "Motivational",
    val voiceStyle: String = "Male Energetic",
    val status: ProjectStatus = ProjectStatus.DRAFT,
    val resolution: VideoResolution = VideoResolution.RES_1080P,
    val fps: VideoFps = VideoFps.FPS_30,
    val outputVideoUri: String? = null,
    val thumbnailUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
