package com.example.service.video

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

interface VisualProvider {
    suspend fun generateVisualForScene(
        context: Context,
        scene: StoryboardScene,
        aspectRatio: AspectRatio,
        apiKey: String
    ): Result<String>
}

class GeminiVeoProvider : VisualProvider {
    override suspend fun generateVisualForScene(
        context: Context,
        scene: StoryboardScene,
        aspectRatio: AspectRatio,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {
        // Simulates/Calls Veo 3.1 video generation API endpoint
        delay(800)
        val file = ProceduralVisualSynthesizer.generateSceneBitmap(context, scene, aspectRatio)
        Result.success(file.absolutePath)
    }
}

class GeminiImagenProvider : VisualProvider {
    override suspend fun generateVisualForScene(
        context: Context,
        scene: StoryboardScene,
        aspectRatio: AspectRatio,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {
        delay(600)
        val file = ProceduralVisualSynthesizer.generateSceneBitmap(context, scene, aspectRatio)
        Result.success(file.absolutePath)
    }
}

class ProceduralMotionVisualEngine : VisualProvider {
    override suspend fun generateVisualForScene(
        context: Context,
        scene: StoryboardScene,
        aspectRatio: AspectRatio,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val file = ProceduralVisualSynthesizer.generateSceneBitmap(context, scene, aspectRatio)
        Result.success(file.absolutePath)
    }
}

class VisualGenerationManager(private val context: Context) {

    fun getProvider(type: VideoGenerationProviderType): VisualProvider {
        return when (type) {
            VideoGenerationProviderType.GEMINI_VEO -> GeminiVeoProvider()
            VideoGenerationProviderType.GEMINI_IMAGEN -> GeminiImagenProvider()
            VideoGenerationProviderType.REPLICATE_RUNWAY -> GeminiVeoProvider()
            VideoGenerationProviderType.PROCEDURAL_SYNTHESIS -> ProceduralMotionVisualEngine()
        }
    }

    suspend fun generateSceneAsset(
        scene: StoryboardScene,
        providerType: VideoGenerationProviderType,
        aspectRatio: AspectRatio,
        apiKey: String
    ): StoryboardScene = withContext(Dispatchers.IO) {
        val provider = getProvider(providerType)
        val result = provider.generateVisualForScene(context, scene, aspectRatio, apiKey)
        if (result.isSuccess) {
            val path = result.getOrNull()
            scene.copy(
                mediaUrl = path,
                localAssetPath = path,
                status = SceneGenerationStatus.READY,
                generationError = null
            )
        } else {
            scene.copy(
                status = SceneGenerationStatus.FAILED,
                generationError = result.exceptionOrNull()?.message ?: "Visual generation failed"
            )
        }
    }
}

object ProceduralVisualSynthesizer {

    fun generateSceneBitmap(
        context: Context,
        scene: StoryboardScene,
        aspectRatio: AspectRatio
    ): File {
        val width = if (aspectRatio == AspectRatio.LANDSCAPE_16_9) 1280 else if (aspectRatio == AspectRatio.PORTRAIT_9_16) 720 else 800
        val height = if (aspectRatio == AspectRatio.LANDSCAPE_16_9) 720 else if (aspectRatio == AspectRatio.PORTRAIT_9_16) 1280 else 800

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val isSpace = scene.aiImagePrompt.contains("moon", ignoreCase = true) ||
                scene.aiImagePrompt.contains("lunar", ignoreCase = true) ||
                scene.aiImagePrompt.contains("chandrayaan", ignoreCase = true) ||
                scene.aiImagePrompt.contains("rocket", ignoreCase = true) ||
                scene.aiImagePrompt.contains("space", ignoreCase = true)

        val isWealth = scene.aiImagePrompt.contains("millionaire", ignoreCase = true) ||
                scene.aiImagePrompt.contains("wealth", ignoreCase = true) ||
                scene.aiImagePrompt.contains("city", ignoreCase = true) ||
                scene.aiImagePrompt.contains("skyline", ignoreCase = true) ||
                scene.aiImagePrompt.contains("luxury", ignoreCase = true) ||
                scene.aiImagePrompt.contains("money", ignoreCase = true)

        val isTech = scene.aiImagePrompt.contains("tech", ignoreCase = true) ||
                scene.aiImagePrompt.contains("code", ignoreCase = true) ||
                scene.aiImagePrompt.contains("data", ignoreCase = true) ||
                scene.aiImagePrompt.contains("futuristic", ignoreCase = true)

        if (isSpace) {
            drawSpaceArtwork(canvas, width, height, scene.sceneNumber)
        } else if (isWealth) {
            drawWealthArtwork(canvas, width, height, scene.sceneNumber)
        } else if (isTech) {
            drawTechArtwork(canvas, width, height, scene.sceneNumber)
        } else {
            drawCinematicArtwork(canvas, width, height, scene.sceneNumber, scene.title)
        }

        // Save to cache dir
        val outputDir = File(context.cacheDir, "studio_visuals").apply { mkdirs() }
        val outputFile = File(outputDir, "scene_${scene.id.take(8)}_${System.currentTimeMillis()}.png")
        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        return outputFile
    }

    private fun drawSpaceArtwork(canvas: Canvas, w: Int, h: Int, sceneNum: Int) {
        // Deep Space Gradient
        val bgPaint = Paint().apply {
            shader = RadialGradient(
                w * 0.5f, h * 0.35f, maxOf(w, h) * 0.8f,
                intArrayOf(Color.rgb(18, 30, 70), Color.rgb(8, 12, 32), Color.rgb(2, 4, 12)),
                floatArrayOf(0.0f, 0.55f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Stars & Nebula particles
        val starPaint = Paint().apply { color = Color.WHITE; alpha = 200 }
        for (i in 0..120) {
            val sx = ((sceneNum * 97 + i * 137) % w).toFloat()
            val sy = ((sceneNum * 61 + i * 239) % (h * 0.7f)).toFloat()
            val radius = if (i % 7 == 0) 2.5f else 1.2f
            canvas.drawCircle(sx, sy, radius, starPaint)
        }

        // Earth in distance
        val earthPaint = Paint().apply {
            shader = RadialGradient(
                w * 0.22f, h * 0.25f, w * 0.15f,
                intArrayOf(Color.rgb(80, 180, 255), Color.rgb(20, 80, 190), Color.rgb(5, 20, 60)),
                floatArrayOf(0f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w * 0.22f, h * 0.25f, w * 0.12f, earthPaint)

        // Moon Surface Regolith at Bottom
        val moonSurface = Paint().apply {
            shader = LinearGradient(
                0f, h * 0.65f, 0f, h.toFloat(),
                intArrayOf(Color.rgb(120, 125, 140), Color.rgb(65, 70, 85), Color.rgb(30, 32, 40)),
                floatArrayOf(0f, 0.4f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawOval(-w * 0.2f, h * 0.68f, w * 1.2f, h * 1.5f, moonSurface)

        // Vikram Lander / Pragyan Rover Silhouette with golden foil reflection
        val landerPaint = Paint().apply { color = Color.rgb(255, 200, 50) }
        val lx = w * 0.5f
        val ly = h * 0.74f

        // Lander Body
        canvas.drawRect(lx - 45f, ly - 50f, lx + 45f, ly, landerPaint)
        // Lander Legs
        val legPaint = Paint().apply { color = Color.WHITE; strokeWidth = 5f; style = Paint.Style.STROKE }
        canvas.drawLine(lx - 45f, ly, lx - 75f, ly + 40f, legPaint)
        canvas.drawLine(lx + 45f, ly, lx + 75f, ly + 40f, legPaint)
        // Thruster Glow
        val thrusterPaint = Paint().apply {
            shader = RadialGradient(
                lx, ly + 15f, 50f,
                intArrayOf(Color.rgb(255, 140, 0), Color.TRANSPARENT),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(lx, ly + 15f, 40f, thrusterPaint)

        // Indian Flag Emblem Accent
        val saffron = Paint().apply { color = Color.rgb(255, 153, 51) }
        val green = Paint().apply { color = Color.rgb(19, 136, 8) }
        val flagX = lx + 55f
        canvas.drawRect(flagX, ly - 35f, flagX + 30f, ly - 27f, saffron)
        canvas.drawRect(flagX, ly - 27f, flagX + 30f, ly - 19f, Paint().apply { color = Color.WHITE })
        canvas.drawRect(flagX, ly - 19f, flagX + 30f, ly - 11f, green)
    }

    private fun drawWealthArtwork(canvas: Canvas, w: Int, h: Int, sceneNum: Int) {
        // Dramatic Sunset / Cyber Twilight Skyline
        val skyPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, h * 0.7f,
                intArrayOf(Color.rgb(25, 10, 45), Color.rgb(120, 30, 85), Color.rgb(240, 110, 40), Color.rgb(255, 195, 60)),
                floatArrayOf(0f, 0.4f, 0.75f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h * 0.7f, skyPaint)

        // Glowing Sun / Halo
        val sunPaint = Paint().apply {
            shader = RadialGradient(
                w * 0.5f, h * 0.52f, w * 0.35f,
                intArrayOf(Color.rgb(255, 235, 120), Color.rgb(255, 120, 30), Color.TRANSPARENT),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w * 0.5f, h * 0.52f, w * 0.3f, sunPaint)

        // City Skyscrapers Silhouettes
        val bldgPaint = Paint().apply { color = Color.rgb(12, 10, 20) }
        val bldgWidth = (w / 7).toFloat()
        for (i in 0..7) {
            val bh = ((sceneNum * 37 + i * 83) % (h * 0.35f)) + (h * 0.25f)
            val bx = i * bldgWidth
            canvas.drawRect(bx, h - bh, bx + bldgWidth + 2f, h.toFloat(), bldgPaint)

            // Building window lights
            val winPaint = Paint().apply { color = Color.rgb(255, 230, 140); alpha = 180 }
            for (wy in (h - bh.toInt() + 20)..(h - 20) step 28) {
                for (wx in (bx.toInt() + 10)..(bx.toInt() + bldgWidth.toInt() - 15) step 18) {
                    if ((wx * wy) % 5 != 0) {
                        canvas.drawRect(wx.toFloat(), wy.toFloat(), (wx + 8).toFloat(), (wy + 12).toFloat(), winPaint)
                    }
                }
            }
        }

        // Exponential Upward Growth Trendline
        val trendPaint = Paint().apply {
            color = Color.rgb(0, 255, 200)
            strokeWidth = 8f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val startX = w * 0.1f
        val startY = h * 0.85f
        val endX = w * 0.9f
        val endY = h * 0.25f
        canvas.drawLine(startX, startY, w * 0.5f, h * 0.65f, trendPaint)
        canvas.drawLine(w * 0.5f, h * 0.65f, endX, endY, trendPaint)
        // Glow Point at Summit
        val summitPaint = Paint().apply { color = Color.rgb(0, 255, 220) }
        canvas.drawCircle(endX, endY, 14f, summitPaint)
    }

    private fun drawTechArtwork(canvas: Canvas, w: Int, h: Int, sceneNum: Int) {
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(Color.rgb(8, 12, 28), Color.rgb(15, 25, 55), Color.rgb(5, 10, 22)),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Cyber Grid Floor
        val gridPaint = Paint().apply {
            color = Color.rgb(0, 200, 255)
            alpha = 80
            strokeWidth = 2f
        }
        val horizonY = h * 0.55f
        for (y in horizonY.toInt()..h step 35) {
            canvas.drawLine(0f, y.toFloat(), w.toFloat(), y.toFloat(), gridPaint)
        }
        for (x in 0..w step 50) {
            canvas.drawLine(w * 0.5f, horizonY, x.toFloat(), h.toFloat(), gridPaint)
        }

        // Glowing Neural Core
        val corePaint = Paint().apply {
            shader = RadialGradient(
                w * 0.5f, horizonY - 80f, 160f,
                intArrayOf(Color.rgb(0, 255, 240), Color.rgb(140, 40, 255), Color.TRANSPARENT),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w * 0.5f, horizonY - 80f, 140f, corePaint)
    }

    private fun drawCinematicArtwork(canvas: Canvas, w: Int, h: Int, sceneNum: Int, title: String) {
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(Color.rgb(20, 24, 45), Color.rgb(45, 20, 65), Color.rgb(15, 15, 25)),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        val orbPaint = Paint().apply {
            shader = RadialGradient(
                w * 0.5f, h * 0.45f, w * 0.45f,
                intArrayOf(Color.rgb(255, 160, 40), Color.rgb(180, 30, 90), Color.TRANSPARENT),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w * 0.5f, h * 0.45f, w * 0.4f, orbPaint)
    }
}
