package com.example.model

enum class CameraMovement(val displayName: String, val iconName: String) {
    SLOW_ZOOM_IN("Slow Zoom In", "zoom_in"),
    FAST_ZOOM_IN("Fast Push-In", "zoom_in_map"),
    SLOW_ZOOM_OUT("Zoom Out / Reveal", "zoom_out"),
    PAN_LEFT("Pan Left", "arrow_left"),
    PAN_RIGHT("Pan Right", "arrow_right"),
    TILT_UP("Tilt Up", "arrow_upward"),
    KEN_BURNS_DYNAMIC("Dynamic Ken Burns", "animation"),
    FPV_DRONE("Cinematic Drone Flythrough", "flight"),
    STATIC_CLOSEUP("Static Focused Close-up", "center_focus_strong"),
    ORBIT_360("360 Orbit Rotation", "rotate_90_degrees_ccw")
}

enum class TransitionType(val displayName: String) {
    CUT("Hard Cut"),
    CROSS_DISSOLVE("Cross Dissolve"),
    WHIP_PAN("Whip Pan"),
    GLITCH("Cyber Glitch"),
    ZOOM_WARP("Zoom Warp"),
    FADE_TO_BLACK("Fade to Black"),
    LIGHT_LEAK("Cinematic Light Leak")
}

enum class MediaAssetType {
    AI_VIDEO,
    AI_IMAGE_MOTION,
    USER_VIDEO_CLIP,
    MOTION_GRAPHIC_TITLE
}

enum class SceneGenerationStatus {
    PENDING,
    GENERATING,
    READY,
    FAILED
}

data class StoryboardScene(
    val id: String,
    val projectId: String,
    val sceneNumber: Int,
    val title: String,
    val narration: String,
    val visualDescription: String,
    val aiImagePrompt: String,
    val aiVideoPrompt: String,
    val cameraMovement: CameraMovement = CameraMovement.SLOW_ZOOM_IN,
    val durationSec: Int = 5,
    val transition: TransitionType = TransitionType.CROSS_DISSOLVE,
    val backgroundMusicMood: String = "Motivational",
    val soundEffect: String = "Whoosh",
    val mediaType: MediaAssetType = MediaAssetType.AI_VIDEO,
    val mediaUrl: String? = null,
    val localAssetPath: String? = null,
    val status: SceneGenerationStatus = SceneGenerationStatus.READY,
    val generationError: String? = null
)
