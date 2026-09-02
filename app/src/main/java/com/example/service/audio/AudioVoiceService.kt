package com.example.service.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID

class AudioVoiceService(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingTrack: AudioTrackItem? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            tts?.language = Locale("hi", "IN")
        }
    }

    fun setVoiceLanguage(language: String) {
        if (!isTtsInitialized || tts == null) return
        val locale = when {
            language.contains("hindi", ignoreCase = true) || language.contains("हिंदी", ignoreCase = true) -> Locale("hi", "IN")
            language.contains("hinglish", ignoreCase = true) -> Locale("hi", "IN")
            language.contains("uk", ignoreCase = true) -> Locale.UK
            else -> Locale("en", "IN")
        }
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.language = Locale.ENGLISH
        }
    }

    fun setSpeechParameters(rate: Float, pitch: Float) {
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
    }

    fun speakText(text: String, onComplete: (() -> Unit)? = null) {
        if (!isTtsInitialized || tts == null) {
            onComplete?.invoke()
            return
        }

        val utteranceId = UUID.randomUUID().toString()
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) {
                if (id == utteranceId) onComplete?.invoke()
            }
            override fun onError(id: String?) {
                if (id == utteranceId) onComplete?.invoke()
            }
        })

        val params = Bundle()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stopSpeech() {
        try {
            tts?.stop()
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

object SoundTrackLibrary {

    data class MusicTrack(
        val id: String,
        val title: String,
        val mood: String,
        val genre: String,
        val bpm: Int,
        val description: String
    )

    val availableTracks = listOf(
        MusicTrack("bgm_motivational_1", "Rising Triumph", "Motivational", "Cinematic Beats", 128, "Uplifting brass, driving drums and soaring strings for ambitious narratives"),
        MusicTrack("bgm_cinematic_1", "Epic Galactic Voyage", "Cinematic", "Orchestral", 110, "Majestic symphonic brass and heavy percussion for space and discovery"),
        MusicTrack("bgm_documentary_1", "Intrigue & Discovery", "Documentary", "Ambient Electronic", 95, "Warm synth pulses and minimalist piano for investigative journalism"),
        MusicTrack("bgm_emotional_1", "Unbreakable Spirit", "Emotional", "Piano & Cello", 80, "Deep emotional piano and cello layers for inspiring hardship-to-glory journeys"),
        MusicTrack("bgm_tech_1", "Cyber Matrix Grid", "Corporate", "Tech Synthwave", 120, "Crisp electronic groove and futuristic arpeggiators for AI & tech"),
        MusicTrack("bgm_suspense_1", "Countdown Tension", "Suspense", "Dark Hybrid", 135, "Ticking clock, rising sub-bass and heavy cinematic braams for tension")
    )

    fun getTrackForMood(mood: String): MusicTrack {
        return availableTracks.find { it.mood.contains(mood, ignoreCase = true) }
            ?: availableTracks.first()
    }
}
