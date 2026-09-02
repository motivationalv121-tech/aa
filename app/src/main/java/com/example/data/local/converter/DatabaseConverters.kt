package com.example.data.local.converter

import androidx.room.TypeConverter
import com.example.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class DatabaseConverters {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @TypeConverter
    fun fromKeyFactList(facts: List<KeyFact>?): String {
        if (facts == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, KeyFact::class.java)
        return moshi.adapter<List<KeyFact>>(type).toJson(facts)
    }

    @TypeConverter
    fun toKeyFactList(json: String?): List<KeyFact> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, KeyFact::class.java)
        return try {
            moshi.adapter<List<KeyFact>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromSourceList(sources: List<SourceReference>?): String {
        if (sources == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, SourceReference::class.java)
        return moshi.adapter<List<SourceReference>>(type).toJson(sources)
    }

    @TypeConverter
    fun toSourceList(json: String?): List<SourceReference> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, SourceReference::class.java)
        return try {
            moshi.adapter<List<SourceReference>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStatsList(stats: List<StatisticItem>?): String {
        if (stats == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, StatisticItem::class.java)
        return moshi.adapter<List<StatisticItem>>(type).toJson(stats)
    }

    @TypeConverter
    fun toStatsList(json: String?): List<StatisticItem> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, StatisticItem::class.java)
        return try {
            moshi.adapter<List<StatisticItem>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromTimelineEventList(events: List<TimelineEvent>?): String {
        if (events == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, TimelineEvent::class.java)
        return moshi.adapter<List<TimelineEvent>>(type).toJson(events)
    }

    @TypeConverter
    fun toTimelineEventList(json: String?): List<TimelineEvent> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, TimelineEvent::class.java)
        return try {
            moshi.adapter<List<TimelineEvent>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStringList(strings: List<String>?): String {
        if (strings == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).toJson(strings)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return try {
            moshi.adapter<List<String>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromScriptSceneList(scenes: List<ScriptScene>?): String {
        if (scenes == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, ScriptScene::class.java)
        return moshi.adapter<List<ScriptScene>>(type).toJson(scenes)
    }

    @TypeConverter
    fun toScriptSceneList(json: String?): List<ScriptScene> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, ScriptScene::class.java)
        return try {
            moshi.adapter<List<ScriptScene>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStoryboardSceneList(scenes: List<StoryboardScene>?): String {
        if (scenes == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, StoryboardScene::class.java)
        return moshi.adapter<List<StoryboardScene>>(type).toJson(scenes)
    }

    @TypeConverter
    fun toStoryboardSceneList(json: String?): List<StoryboardScene> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, StoryboardScene::class.java)
        return try {
            moshi.adapter<List<StoryboardScene>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromVideoClipList(clips: List<VideoClipItem>?): String {
        if (clips == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, VideoClipItem::class.java)
        return moshi.adapter<List<VideoClipItem>>(type).toJson(clips)
    }

    @TypeConverter
    fun toVideoClipList(json: String?): List<VideoClipItem> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, VideoClipItem::class.java)
        return try {
            moshi.adapter<List<VideoClipItem>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromAudioTrackList(tracks: List<AudioTrackItem>?): String {
        if (tracks == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, AudioTrackItem::class.java)
        return moshi.adapter<List<AudioTrackItem>>(type).toJson(tracks)
    }

    @TypeConverter
    fun toAudioTrackList(json: String?): List<AudioTrackItem> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, AudioTrackItem::class.java)
        return try {
            moshi.adapter<List<AudioTrackItem>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromSubtitleList(subtitles: List<SubtitleItem>?): String {
        if (subtitles == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, SubtitleItem::class.java)
        return moshi.adapter<List<SubtitleItem>>(type).toJson(subtitles)
    }

    @TypeConverter
    fun toSubtitleList(json: String?): List<SubtitleItem> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, SubtitleItem::class.java)
        return try {
            moshi.adapter<List<SubtitleItem>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
