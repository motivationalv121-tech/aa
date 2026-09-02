package com.example.service.ai

import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class StoryboardAgent {

    suspend fun createStoryboard(
        script: Script,
        projectId: String,
        aspectRatio: AspectRatio,
        apiKey: String
    ): List<StoryboardScene> = withContext(Dispatchers.IO) {
        val effectiveKey = GeminiClient.getEffectiveApiKey(apiKey)

        if (effectiveKey.isBlank()) {
            return@withContext generateFallbackStoryboard(script, projectId, aspectRatio)
        }

        try {
            val systemPrompt = """
                You are an elite Hollywood Storyboard & AI Cinematography Director.
                Transform the provided video script into a scene-by-scene visual storyboard optimized for AI Video/Image generation models (such as Veo 3.1, Gemini Imagen, Midjourney).
                
                Aspect Ratio: ${aspectRatio.ratioString}
                Video Tone: ${script.tone}
                
                For EACH scene in the script, provide:
                1. "sceneNumber": matching script scene number
                2. "title": descriptive title
                3. "visualDescription": cinematic visual framing, lighting, atmosphere
                4. "aiImagePrompt": hyper-detailed diffusion prompt (8k resolution, cinematic lighting, photorealistic, Unreal Engine 5 render style, chromatic aberration, ray tracing)
                5. "aiVideoPrompt": dynamic video prompt describing camera motion, subject movement, physics, and atmospheric smoke/lens flares
                6. "cameraMovement": ONE OF [SLOW_ZOOM_IN, FAST_ZOOM_IN, SLOW_ZOOM_OUT, PAN_LEFT, PAN_RIGHT, TILT_UP, KEN_BURNS_DYNAMIC, FPV_DRONE, STATIC_CLOSEUP, ORBIT_360]
                7. "durationSec": scene duration in seconds
                8. "transition": ONE OF [CUT, CROSS_DISSOLVE, WHIP_PAN, GLITCH, ZOOM_WARP, FADE_TO_BLACK, LIGHT_LEAK]
                9. "backgroundMusicMood": e.g. "Motivational", "Cinematic Orchestral", "Suspense", "Documentary", "Energetic Electronic"
                10. "soundEffect": e.g. "Rocket Thruster Blast", "Sub Bass Drop", "Whoosh Transition", "Camera Shutter", "Cheering Crowd"
                
                Respond ONLY with JSON:
                [
                   {
                      "sceneNumber": 1,
                      "title": "...",
                      "visualDescription": "...",
                      "aiImagePrompt": "...",
                      "aiVideoPrompt": "...",
                      "cameraMovement": "SLOW_ZOOM_IN",
                      "durationSec": 8,
                      "transition": "CROSS_DISSOLVE",
                      "backgroundMusicMood": "Cinematic Orchestral",
                      "soundEffect": "Rocket Blast"
                   }
                ]
            """.trimIndent()

            val scriptText = script.scenes.joinToString("\n\n") {
                "Scene ${it.sceneNumber} (${it.sceneType}): Narration: ${it.narrationText} | Visual: ${it.visualSummary} | Duration: ${it.estimatedDurationSec}s"
            }

            val request = GeminiGenerateRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = "Script to Storyboard:\n$scriptText\nReturn JSON array only."))
                    )
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.5f,
                    responseMimeType = "application/json"
                )
            )

            val response = GeminiClient.apiService.generateContent(effectiveKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!jsonText.isNullOrBlank()) {
                parseStoryboardJson(jsonText, script, projectId)
            } else {
                generateFallbackStoryboard(script, projectId, aspectRatio)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            generateFallbackStoryboard(script, projectId, aspectRatio)
        }
    }

    private fun parseStoryboardJson(
        jsonString: String,
        script: Script,
        projectId: String
    ): List<StoryboardScene> {
        return try {
            val cleanJson = jsonString.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val array = JSONArray(cleanJson)
            val result = mutableListOf<StoryboardScene>()

            for (i in 0 until array.length()) {
                val sc = array.optJSONObject(i) ?: continue
                val scriptScene = script.scenes.getOrNull(i)

                val camStr = sc.optString("cameraMovement", "SLOW_ZOOM_IN")
                val cameraMovement = try {
                    CameraMovement.valueOf(camStr)
                } catch (e: Exception) {
                    CameraMovement.SLOW_ZOOM_IN
                }

                val transStr = sc.optString("transition", "CROSS_DISSOLVE")
                val transition = try {
                    TransitionType.valueOf(transStr)
                } catch (e: Exception) {
                    TransitionType.CROSS_DISSOLVE
                }

                result.add(
                    StoryboardScene(
                        id = UUID.randomUUID().toString(),
                        projectId = projectId,
                        sceneNumber = sc.optInt("sceneNumber", i + 1),
                        title = sc.optString("title", scriptScene?.title ?: "Scene ${i + 1}"),
                        narration = scriptScene?.narrationText ?: sc.optString("narration", ""),
                        visualDescription = sc.optString("visualDescription", scriptScene?.visualSummary ?: ""),
                        aiImagePrompt = sc.optString("aiImagePrompt", "Cinematic 8k photorealistic scene of ${scriptScene?.visualSummary}"),
                        aiVideoPrompt = sc.optString("aiVideoPrompt", "Smooth camera motion showing ${scriptScene?.visualSummary}"),
                        cameraMovement = cameraMovement,
                        durationSec = sc.optInt("durationSec", scriptScene?.estimatedDurationSec ?: 6),
                        transition = transition,
                        backgroundMusicMood = sc.optString("backgroundMusicMood", "Motivational"),
                        soundEffect = sc.optString("soundEffect", "Whoosh"),
                        mediaType = MediaAssetType.AI_VIDEO,
                        status = SceneGenerationStatus.READY
                    )
                )
            }

            if (result.isNotEmpty()) result else generateFallbackStoryboard(script, projectId, AspectRatio.PORTRAIT_9_16)
        } catch (e: Exception) {
            generateFallbackStoryboard(script, projectId, AspectRatio.PORTRAIT_9_16)
        }
    }

    private fun generateFallbackStoryboard(
        script: Script,
        projectId: String,
        aspectRatio: AspectRatio
    ): List<StoryboardScene> {
        return script.scenes.mapIndexed { index, scene ->
            val cameraMove = when (index % 5) {
                0 -> CameraMovement.FAST_ZOOM_IN
                1 -> CameraMovement.SLOW_ZOOM_IN
                2 -> CameraMovement.KEN_BURNS_DYNAMIC
                3 -> CameraMovement.PAN_RIGHT
                else -> CameraMovement.ORBIT_360
            }

            val transition = when (index % 4) {
                0 -> TransitionType.CROSS_DISSOLVE
                1 -> TransitionType.WHIP_PAN
                2 -> TransitionType.ZOOM_WARP
                else -> TransitionType.CUT
            }

            val sfx = when (scene.sceneType) {
                SceneType.HOOK -> "Sub Bass Drop & Heavy Impact"
                SceneType.INTRO -> "Cinematic Riser Whoosh"
                SceneType.MAIN_INFO -> "Camera Shutter & Click"
                SceneType.FACT -> "High-Tech Sci-Fi Data Chime"
                SceneType.CLIMAX -> "Orchestral Brass Swell"
                SceneType.CTA -> "Warm Bell Ding & Follow Pop"
                SceneType.CONCLUSION -> "Ambient Sub Reverb"
            }

            StoryboardScene(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                sceneNumber = scene.sceneNumber,
                title = scene.title,
                narration = scene.narrationText,
                visualDescription = scene.visualSummary,
                aiImagePrompt = "Cinematic 8k masterpiece, ${scene.visualSummary}, dramatic volumetric lighting, photorealistic octane render, high dynamic range, sharp focus, 35mm lens, golden ratio composition",
                aiVideoPrompt = "Smooth 60fps cinematic video, camera ${cameraMove.displayName.lowercase()} capturing ${scene.visualSummary} with subtle atmospheric particles, realistic motion blur, shallow depth of field",
                cameraMovement = cameraMove,
                durationSec = scene.estimatedDurationSec,
                transition = transition,
                backgroundMusicMood = if (script.tone.contains("moti", ignoreCase = true)) "Motivational" else "Cinematic Orchestral",
                soundEffect = sfx,
                mediaType = MediaAssetType.AI_VIDEO,
                status = SceneGenerationStatus.READY
            )
        }
    }
}
