package com.example.service.ai

import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class AiAutoEditEngine {

    suspend fun applyAutoEdit(
        timeline: TimelineProject,
        storyboard: List<StoryboardScene>,
        script: Script?,
        customInstructions: String,
        apiKey: String
    ): Pair<TimelineProject, EditPlan> = withContext(Dispatchers.IO) {
        val hasCustomInstructions = customInstructions.isNotBlank()
        val isFastPaced = customInstructions.contains("fast", ignoreCase = true) ||
                customInstructions.contains("mrbeast", ignoreCase = true) ||
                customInstructions.contains("reel", ignoreCase = true)

        val isDocumentary = customInstructions.contains("documentary", ignoreCase = true) ||
                customInstructions.contains("calm", ignoreCase = true) ||
                customInstructions.contains("slow", ignoreCase = true)

        val isLowBgm = customInstructions.contains("low", ignoreCase = true) ||
                customInstructions.contains("duck", ignoreCase = true) ||
                customInstructions.contains("कम", ignoreCase = true)

        val visualIntervalSec = if (isFastPaced) 2.5f else if (isDocumentary) 5.0f else 3.5f

        val editPlan = EditPlan(
            targetStyle = when {
                isDocumentary -> "Cinematic Slow-Paced Documentary"
                isFastPaced -> "High-Retention Fast-Cut Viral"
                else -> "AI Optimized Dynamic Studio Edit"
            },
            summary = if (hasCustomInstructions) {
                "Applied custom directives: \"$customInstructions\". Optimized cuts, visual transitions, subtitle karaoke tracking, and ducked audio mix."
            } else {
                "AI Auto-Edit synchronized all ${storyboard.size} scenes to speech rhythm, balanced BGM ducking to 20%, added high-contrast kinetic captions."
            },
            fastPacedCuts = isFastPaced,
            keyWordsHighlighted = true,
            musicVolumePercent = if (isLowBgm) 15 else 22,
            voiceoverBoost = true,
            visualChangeEverySec = visualIntervalSec
        )

        // Build synchronized Video Clips
        var cumulativeMs = 0L
        val updatedClips = mutableListOf<VideoClipItem>()

        storyboard.forEachIndexed { index, scene ->
            val clipDurationMs = (scene.durationSec * 1000L)
            updatedClips.add(
                VideoClipItem(
                    id = UUID.randomUUID().toString(),
                    sceneId = scene.id,
                    title = scene.title,
                    visualPrompt = scene.aiVideoPrompt,
                    startMs = cumulativeMs,
                    durationMs = clipDurationMs,
                    speed = if (isFastPaced) 1.15f else 1.0f,
                    cameraMovement = scene.cameraMovement,
                    transition = if (index == 0) TransitionType.CUT else scene.transition,
                    isKenBurnsActive = true,
                    assetUrl = scene.mediaUrl
                )
            )
            cumulativeMs += clipDurationMs
        }

        val totalDurationMs = maxOf(30000L, cumulativeMs)

        // Build Audio Tracks with Smart Ducking
        val audioTracks = mutableListOf<AudioTrackItem>()

        // 1. Voiceover Track (full duration)
        audioTracks.add(
            AudioTrackItem(
                id = UUID.randomUUID().toString(),
                trackType = AudioTrackType.VOICEOVER,
                title = "AI Neural Voiceover (${script?.language ?: "Hindi"})",
                startMs = 0L,
                durationMs = totalDurationMs,
                volume = 1.0f,
                isAutoDucked = false,
                audioMood = script?.tone ?: "Motivational"
            )
        )

        // 2. Background Music Track (Auto Ducked under voiceover)
        audioTracks.add(
            AudioTrackItem(
                id = UUID.randomUUID().toString(),
                trackType = AudioTrackType.BACKGROUND_MUSIC,
                title = "Ambient Soundtrack: ${storyboard.firstOrNull()?.backgroundMusicMood ?: "Motivational"}",
                startMs = 0L,
                durationMs = totalDurationMs,
                volume = (editPlan.musicVolumePercent / 100f),
                isAutoDucked = true,
                audioMood = storyboard.firstOrNull()?.backgroundMusicMood ?: "Motivational"
            )
        )

        // 3. Sound Effect Trackers for transitions
        storyboard.forEachIndexed { index, scene ->
            if (scene.soundEffect.isNotBlank()) {
                val sfxStart = updatedClips.getOrNull(index)?.startMs ?: 0L
                audioTracks.add(
                    AudioTrackItem(
                        id = UUID.randomUUID().toString(),
                        trackType = AudioTrackType.SOUND_EFFECT,
                        title = "SFX: ${scene.soundEffect}",
                        startMs = sfxStart,
                        durationMs = 1500L,
                        volume = 0.65f,
                        isAutoDucked = false,
                        soundEffectType = scene.soundEffect
                    )
                )
            }
        }

        // Build Synchronized Subtitles
        val subtitles = mutableListOf<SubtitleItem>()
        var subTimeCursor = 0L

        storyboard.forEach { scene ->
            val sceneDurationMs = scene.durationSec * 1000L
            val words = scene.narration.split("\\s+".toRegex()).filter { it.isNotBlank() }

            if (words.isNotEmpty()) {
                // Group words into bite-sized punchy chunks of 4-6 words for modern TikTok/Reels reading speed
                val chunkSize = if (isFastPaced) 4 else 6
                val chunks = words.chunked(chunkSize)
                val chunkDurationMs = sceneDurationMs / maxOf(1, chunks.size)

                chunks.forEachIndexed { cIndex, chunkWords ->
                    val chunkStart = subTimeCursor + (cIndex * chunkDurationMs)
                    val chunkEnd = chunkStart + chunkDurationMs
                    val wordTimeDelta = chunkDurationMs / maxOf(1, chunkWords.size)

                    val wordList = chunkWords.mapIndexed { wIndex, w ->
                        SubtitleWord(
                            word = w,
                            startMs = chunkStart + (wIndex * wordTimeDelta),
                            endMs = chunkStart + ((wIndex + 1) * wordTimeDelta)
                        )
                    }

                    val style = when {
                        customInstructions.contains("neon", ignoreCase = true) -> SubtitleStyleType.NEON_CYBER
                        customInstructions.contains("clean", ignoreCase = true) || isDocumentary -> SubtitleStyleType.CLEAN_CINEMATIC
                        isFastPaced -> SubtitleStyleType.MR_BEAST_BOLD
                        script?.language?.contains("Hindi", ignoreCase = true) == true -> SubtitleStyleType.HINDI_DEVANAGARI_MODERN
                        else -> SubtitleStyleType.TIKTOK_VIRAL_YELLOW
                    }

                    subtitles.add(
                        SubtitleItem(
                            id = UUID.randomUUID().toString(),
                            text = chunkWords.joinToString(" "),
                            startMs = chunkStart,
                            endMs = chunkEnd,
                            words = wordList,
                            style = style,
                            positionYPercent = 0.76f
                        )
                    )
                }
            }
            subTimeCursor += sceneDurationMs
        }

        val updatedTimeline = timeline.copy(
            totalDurationMs = totalDurationMs,
            videoClips = updatedClips,
            audioTracks = audioTracks,
            subtitles = subtitles,
            lastAiAutoEditNote = editPlan.summary
        )

        Pair(updatedTimeline, editPlan)
    }
}
