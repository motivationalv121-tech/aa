package com.example.model

enum class SubtitleStyleType(
    val displayName: String,
    val textColor: Long,
    val highlightColor: Long,
    val backgroundColor: Long,
    val fontSizeSp: Int
) {
    TIKTOK_VIRAL_YELLOW("Viral Highlight (Yellow)", 0xFFFFFFFF, 0xFFFFEB3B, 0x99000000, 22),
    MR_BEAST_BOLD("Bold Comic Punch (Cyan/Green)", 0xFF00E5FF, 0xFF76FF03, 0xCC000000, 24),
    CLEAN_CINEMATIC("Minimalist Cinematic", 0xFFFFFFFF, 0xFFE0E0E0, 0x66000000, 18),
    NEON_CYBER("Neon Cyber Glow", 0xFF00FFFF, 0xFFFF007F, 0xAA110022, 20),
    HINDI_DEVANAGARI_MODERN("Hindi High-Contrast", 0xFFFFF9C4, 0xFFFF5722, 0xBB000000, 22)
}

data class SubtitleWord(
    val word: String,
    val startMs: Long,
    val endMs: Long
)

data class SubtitleItem(
    val id: String,
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val words: List<SubtitleWord> = emptyList(),
    val style: SubtitleStyleType = SubtitleStyleType.TIKTOK_VIRAL_YELLOW,
    val positionYPercent: Float = 0.78f
)

data class VideoClipItem(
    val id: String,
    val sceneId: String,
    val title: String,
    val visualPrompt: String,
    val startMs: Long,
    val durationMs: Long,
    val speed: Float = 1.0f,
    val cameraMovement: CameraMovement = CameraMovement.SLOW_ZOOM_IN,
    val transition: TransitionType = TransitionType.CROSS_DISSOLVE,
    val filterColorOverlayHex: Long = 0x00000000,
    val isKenBurnsActive: Boolean = true,
    val assetUrl: String? = null
)

enum class AudioTrackType {
    VOICEOVER,
    BACKGROUND_MUSIC,
    SOUND_EFFECT
}

data class AudioTrackItem(
    val id: String,
    val trackType: AudioTrackType,
    val title: String,
    val startMs: Long,
    val durationMs: Long,
    val volume: Float = 1.0f,
    val isAutoDucked: Boolean = false,
    val audioMood: String = "Motivational",
    val soundEffectType: String = ""
)

data class TimelineProject(
    val projectId: String,
    val totalDurationMs: Long = 60000L,
    val videoClips: List<VideoClipItem> = emptyList(),
    val audioTracks: List<AudioTrackItem> = emptyList(),
    val subtitles: List<SubtitleItem> = emptyList(),
    val currentPlayheadMs: Long = 0L,
    val isPlaying: Boolean = false,
    val lastAiAutoEditNote: String = ""
)

data class EditPlan(
    val targetStyle: String,
    val summary: String,
    val fastPacedCuts: Boolean = true,
    val keyWordsHighlighted: Boolean = true,
    val musicVolumePercent: Int = 20,
    val voiceoverBoost: Boolean = true,
    val visualChangeEverySec: Float = 3.5f
)
