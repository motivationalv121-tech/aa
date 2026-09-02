package com.example.model

data class KeyFact(
    val title: String,
    val detail: String,
    val isVerified: Boolean = true,
    val sourceName: String = "Web Knowledge Graph"
)

data class SourceReference(
    val title: String,
    val url: String,
    val domain: String,
    val snippet: String,
    val reliabilityScore: Float = 0.95f
)

data class TimelineEvent(
    val yearOrDate: String,
    val eventTitle: String,
    val description: String
)

data class StatisticItem(
    val metric: String,
    val value: String,
    val context: String
)

data class ResearchData(
    val projectId: String,
    val topic: String,
    val summary: String,
    val keyFacts: List<KeyFact> = emptyList(),
    val sources: List<SourceReference> = emptyList(),
    val statistics: List<StatisticItem> = emptyList(),
    val timeline: List<TimelineEvent> = emptyList(),
    val entities: List<String> = emptyList(),
    val confidenceScore: Float = 0.98f,
    val uncertainNotes: String = ""
)
