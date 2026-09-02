package com.example.model

enum class VideoGenerationProviderType(val displayName: String, val description: String) {
    GEMINI_VEO("Google Veo 3.1", "High-fidelity cinematic generative video (Fast & Ultra)"),
    GEMINI_IMAGEN("Gemini 2.5/3.1 Flash Image + Motion FX", "Ultra high-res 1K/2K frames with Ken Burns camera motion"),
    REPLICATE_RUNWAY("Replicate / Runway Gen-3", "Compatible adapter for Replicate and Runway ML API endpoints"),
    PROCEDURAL_SYNTHESIS("Studio Procedural Motion Engine", "Built-in zero-latency cinematic shader, particle & visual motion engine")
}

enum class VoiceProviderType(val displayName: String, val description: String) {
    ANDROID_DEVICE_TTS("High-Quality On-Device TTS", "Fast native multi-language voice engine (Hindi, English, Hinglish)"),
    GEMINI_VOICE_TTS("Gemini Native Audio TTS", "Expressive neural voiceover with prebuilt voices (Kore, Fenrir, Puck, Aoede)"),
    ELEVENLABS_ADAPTER("ElevenLabs AI Voice", "Compatible adapter for ElevenLabs ultra-realistic multilingual clones")
}

data class ProviderSettings(
    val customGeminiApiKey: String = "",
    val selectedVideoProvider: VideoGenerationProviderType = VideoGenerationProviderType.GEMINI_IMAGEN,
    val selectedVoiceProvider: VoiceProviderType = VoiceProviderType.ANDROID_DEVICE_TTS,
    val ttsSpeechRate: Float = 1.05f,
    val ttsPitch: Float = 1.0f,
    val selectedVoiceName: String = "hi-in-x-hie-local",
    val defaultAspect: AspectRatio = AspectRatio.PORTRAIT_9_16,
    val defaultResolution: VideoResolution = VideoResolution.RES_1080P,
    val defaultFps: VideoFps = VideoFps.FPS_30,
    val enableAiWatermarkLabel: Boolean = true,
    val enableEthicalSafetyFilters: Boolean = true,
    val isDarkMode: Boolean = true
)
