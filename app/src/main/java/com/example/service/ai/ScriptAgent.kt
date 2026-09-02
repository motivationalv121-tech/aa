package com.example.service.ai

import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ScriptAgent {

    suspend fun generateScript(
        prompt: String,
        research: ResearchData,
        durationSec: Int,
        language: String,
        tone: String,
        audience: String,
        projectId: String,
        apiKey: String
    ): Script = withContext(Dispatchers.IO) {
        val effectiveKey = GeminiClient.getEffectiveApiKey(apiKey)

        if (effectiveKey.isBlank()) {
            return@withContext generateFallbackScript(prompt, research, durationSec, language, tone, audience, projectId)
        }

        try {
            val systemPrompt = """
                You are a master viral YouTube and Reels Video Scriptwriter.
                Your job is to write a high-retention, cinematic video script based on the user's prompt and research facts.
                
                Language: $language (Use natural, engaging phrasing appropriate for $language - if Hindi, write natural Hindi/Hinglish as requested).
                Tone: $tone
                Target Duration: $durationSec seconds
                Audience: $audience
                
                Research Summary: ${research.summary}
                Key Facts: ${research.keyFacts.joinToString("; ") { it.title + ": " + it.detail }}
                
                Structure the script into clear scenes:
                - Scene 1: Strong Hook (Captures attention in first 3 seconds)
                - Scene 2: Introduction & Stakes
                - Scene 3: Core Narrative / Deep Info
                - Scene 4: Shocking Fact or Key Milestone
                - Scene 5: Climax / Emotional Peak
                - Scene 6: Call To Action (CTA) & Conclusion
                
                Return JSON format ONLY:
                {
                   "title": "Compelling Title",
                   "fullScriptText": "Complete narration text...",
                   "scenes": [
                      {
                         "sceneNumber": 1,
                         "sceneType": "HOOK",
                         "title": "The Moon Shot Hook",
                         "narrationText": "क्या आप जानते हैं कि...",
                         "visualSummary": "Cinematic rocket thrust with fiery smoke",
                         "estimatedDurationSec": 5,
                         "onScreenText": "ISRO DID WHAT?!",
                         "toneSuggestion": "High Energy"
                      }
                   ]
                }
                
                Allowed sceneType values: HOOK, INTRO, MAIN_INFO, FACT, CLIMAX, CONCLUSION, CTA
            """.trimIndent()

            val request = GeminiGenerateRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = "User Prompt: $prompt. Write video script. Return ONLY JSON."))
                    )
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7f,
                    responseMimeType = "application/json"
                )
            )

            val response = GeminiClient.apiService.generateContent(effectiveKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!jsonText.isNullOrBlank()) {
                parseScriptJson(jsonText, projectId, language, tone, audience, durationSec)
            } else {
                generateFallbackScript(prompt, research, durationSec, language, tone, audience, projectId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            generateFallbackScript(prompt, research, durationSec, language, tone, audience, projectId)
        }
    }

    private fun parseScriptJson(
        jsonString: String,
        projectId: String,
        language: String,
        tone: String,
        audience: String,
        targetDurationSec: Int
    ): Script {
        return try {
            val cleanJson = jsonString.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val obj = JSONObject(cleanJson)

            val title = obj.optString("title", "AI Video Script")
            val fullScriptText = obj.optString("fullScriptText", "")

            val scenes = mutableListOf<ScriptScene>()
            val scenesArray = obj.optJSONArray("scenes") ?: JSONArray()

            for (i in 0 until scenesArray.length()) {
                val sc = scenesArray.optJSONObject(i) ?: continue
                val typeStr = sc.optString("sceneType", "MAIN_INFO")
                val sceneType = try {
                    SceneType.valueOf(typeStr)
                } catch (e: Exception) {
                    when (i) {
                        0 -> SceneType.HOOK
                        1 -> SceneType.INTRO
                        scenesArray.length() - 1 -> SceneType.CTA
                        else -> SceneType.MAIN_INFO
                    }
                }

                scenes.add(
                    ScriptScene(
                        sceneNumber = sc.optInt("sceneNumber", i + 1),
                        sceneType = sceneType,
                        title = sc.optString("title", "Scene ${i + 1}"),
                        narrationText = sc.optString("narrationText", ""),
                        visualSummary = sc.optString("visualSummary", ""),
                        estimatedDurationSec = sc.optInt("estimatedDurationSec", targetDurationSec / maxOf(1, scenesArray.length())),
                        onScreenText = sc.optString("onScreenText", ""),
                        toneSuggestion = sc.optString("toneSuggestion", "Energetic")
                    )
                )
            }

            Script(
                projectId = projectId,
                title = title,
                language = language,
                tone = tone,
                audience = audience,
                totalDurationSec = targetDurationSec,
                fullScriptText = if (fullScriptText.isNotBlank()) fullScriptText else scenes.joinToString(" ") { it.narrationText },
                scenes = if (scenes.isNotEmpty()) scenes else generateDefaultScenes(language, targetDurationSec)
            )
        } catch (e: Exception) {
            generateDefaultScriptFallback(projectId, language, tone, audience, targetDurationSec)
        }
    }

    private fun generateFallbackScript(
        prompt: String,
        research: ResearchData,
        durationSec: Int,
        language: String,
        tone: String,
        audience: String,
        projectId: String
    ): Script {
        val isHindi = language.contains("Hindi", ignoreCase = true) || language.contains("हिंदी", ignoreCase = true) || prompt.contains("चंद्रयान", ignoreCase = true) || prompt.contains("करोड़पति", ignoreCase = true)
        val isSpace = prompt.contains("चंद्रयान", ignoreCase = true) || prompt.contains("chandrayaan", ignoreCase = true) || prompt.contains("space", ignoreCase = true)
        val isMotivation = prompt.contains("motivat", ignoreCase = true) || prompt.contains("करोड़पति", ignoreCase = true) || prompt.contains("गरीब", ignoreCase = true)

        val title: String
        val scenes: List<ScriptScene>

        if (isSpace) {
            title = if (isHindi) "चंद्रयान-3: भारत का ऐतिहासिक चंद्रमा मिशन" else "Chandrayaan-3: India's Historic Moon Landing"
            scenes = if (isHindi) {
                listOf(
                    ScriptScene(
                        sceneNumber = 1,
                        sceneType = SceneType.HOOK,
                        title = "Strong Opening Hook",
                        narrationText = "23 अगस्त 2023 की शाम, पूरी दुनिया की नज़रें भारत के चंद्रयान-3 पर टिकी थीं।",
                        visualSummary = "Cinematic launchpad view with fiery exhaust and slow-motion smoke billowing",
                        estimatedDurationSec = 8,
                        onScreenText = "23 AUGUST 2023: THE MOMENT",
                        toneSuggestion = "Dramatic & High Stakes"
                    ),
                    ScriptScene(
                        sceneNumber = 2,
                        sceneType = SceneType.INTRO,
                        title = "The High Stakes Mission",
                        narrationText = "जहाँ दुनिया के सबसे बड़े देश असफल हुए, वहां इसरो के वैज्ञानिकों ने एक नया इतिहास रचने की ठानी।",
                        visualSummary = "Mission control room with ISRO scientists watching telemetry monitors in intense focus",
                        estimatedDurationSec = 10,
                        onScreenText = "WHERE OTHERS FAILED...",
                        toneSuggestion = "Suspenseful & Inspiring"
                    ),
                    ScriptScene(
                        sceneNumber = 3,
                        sceneType = SceneType.MAIN_INFO,
                        title = "The Historic Descent",
                        narrationText = "विक्रम लैंडर ने चंद्रमा के दुर्गम दक्षिणी ध्रुव पर सफलतापूर्वक कदम रखा। 'शिव शक्ति पॉइंट' पर भारत का तिरंगा लहराया।",
                        visualSummary = "Vikram lander thrusters firing as it touches down on cratered lunar south pole surface",
                        estimatedDurationSec = 12,
                        onScreenText = "TOUCHDOWN: SHIV SHAKTI POINT",
                        toneSuggestion = "Grand & Triumphant"
                    ),
                    ScriptScene(
                        sceneNumber = 4,
                        sceneType = SceneType.FACT,
                        title = "Pragyan Rover Scientific Discovery",
                        narrationText = "प्रज्ञान रोवर ने 100 मीटर से ज्यादा चलकर चांद की ज़मीन में सल्फर, ऑक्सीजन और खनिजों की पुष्टि की!",
                        visualSummary = "Pragyan 6-wheeled rover rolling down ramp, carving tracks into lunar regolith",
                        estimatedDurationSec = 12,
                        onScreenText = "SULPHUR & MINERALS CONFIRMED!",
                        toneSuggestion = "Excited & Scientific"
                    ),
                    ScriptScene(
                        sceneNumber = 5,
                        sceneType = SceneType.CLIMAX,
                        title = "Budget Triumph & Global Respect",
                        narrationText = "हॉलीवुड फिल्मों से भी कम बजट में भारत बना चांद के दक्षिणी ध्रुव पर पहुंचने वाला दुनिया का पहला देश!",
                        visualSummary = "Globe with glowing golden trajectory to Moon, ISRO logo illuminated with pride",
                        estimatedDurationSec = 10,
                        onScreenText = "WORLD'S #1 TO SOUTH POLE",
                        toneSuggestion = "Patriotic & Powerful"
                    ),
                    ScriptScene(
                        sceneNumber = 6,
                        sceneType = SceneType.CTA,
                        title = "Call to Action",
                        narrationText = "भारत के इस गर्व के लिए एक लाइक और चैनल को सब्सक्राइब ज़रूर करें। जय हिन्द!",
                        visualSummary = "Animated YouTube subscribe button, bell icon with Indian Flag waving in cinematic lighting",
                        estimatedDurationSec = 8,
                        onScreenText = "SUBSCRIBE FOR MORE SPACE INSIGHTS",
                        toneSuggestion = "Warm Call-to-Action"
                    )
                )
            } else {
                listOf(
                    ScriptScene(1, SceneType.HOOK, "The 3-Second Hook", "On August 23rd, 2023, the world watched India make space history.", "High-speed cinematic liftoff with trailing golden flames", 8, "HISTORY IN THE MAKING", "Dramatic"),
                    ScriptScene(2, SceneType.INTRO, "The Impossible South Pole", "Where world superpowers failed, ISRO chose the most treacherous terrain on the Moon.", "Lunar craters shrouded in permanent shadows", 10, "LUNAR SOUTH POLE", "Intense"),
                    ScriptScene(3, SceneType.MAIN_INFO, "Pinpoint Touchdown", "The Vikram lander touched down flawlessly at Shiv Shakti Point.", "Lander landing pads touching dust with thruster plume", 12, "TOUCHDOWN CONFIRMED", "Inspiring"),
                    ScriptScene(4, SceneType.FACT, "Groundbreaking Discovery", "Pragyan rover detected sulphur and crucial elements on the lunar regolith.", "Rover headlights scanning lunar rocks with laser spectrum", 12, "KEY ELEMENTS FOUND", "Curious"),
                    ScriptScene(5, SceneType.CLIMAX, "Engineering Marvel", "Achieved at a fraction of Hollywood budgets, India became #1 at the lunar pole.", "Golden trophy graphics and cheering engineers", 10, "INDIA IS #1", "Proud"),
                    ScriptScene(6, SceneType.CTA, "Follow & Subscribe", "Hit follow and subscribe for more extraordinary space stories!", "Clean animated subscribe badge with cinematic glow", 8, "SUBSCRIBE NOW", "Upbeat")
                )
            }
        } else if (isMotivation) {
            title = if (isHindi) "गरीब से करोड़पति बनने का सफर" else "From Zero to Millionaire Mindset"
            scenes = if (isHindi) {
                listOf(
                    ScriptScene(1, SceneType.HOOK, "The Hook", "अगर आप आज गरीब हैं, तो यह आपकी गलती नहीं है। लेकिन 5 साल बाद भी गरीब रहे, तो यह आपकी पसंद होगी!", "High contrast portrait of determined individual in city rain transition to luxury skyline", 8, "POVERTY IS TEMPORARY", "Electrifying"),
                    ScriptScene(2, SceneType.INTRO, "The Shift", "करोड़पति बनने का पहला नियम: पैसे के लिए काम करना बंद करो और ऐसी स्किल्स सीखो जो सोते समय भी पैसा बनाएं।", "Close up of clock ticking and fingers coding on glowing neon laptop", 10, "HIGH-INCOME SKILLS", "Authoritative"),
                    ScriptScene(3, SceneType.MAIN_INFO, "Compounding Power", "हर दिन 30 मिनट नई किताबें पढ़ो, अपने समय को इन्वेस्ट करो और कंपाउंडिंग की ताकत को समझो।", "Visual of compounding bar chart shooting to the sky with golden sparkles", 12, "1% BETTER EVERY DAY", "Motivating"),
                    ScriptScene(4, SceneType.FACT, "The Producer Rule", "99% लोग सिर्फ खर्च करते हैं। अमीर वह बनते हैं जो वैल्यू और सिस्टम्स क्रिएट करते हैं।", "Split screen of consumer scrolling vs creator building digital empire", 12, "BE A CREATOR, NOT CONSUMER", "Direct"),
                    ScriptScene(5, SceneType.CLIMAX, "Breakthrough Moment", "मुसीबतें आएंगी, लेकिन आपका अनुशासन आपको भीड़ से 10 गुना आगे ले जाएगा!", "Hero running up illuminated golden staircase to the summit", 10, "DISCIPLINE > MOTIVATION", "Peak Energy"),
                    ScriptScene(6, SceneType.CTA, "Share & Save", "अगर इस सोच से सहमत हैं, तो वीडियो को सेव करें और अपने सपनों के लिए सब्सक्राइब करें!", "Pulsing save & follow icon with fiery outline", 8, "SAVE & SHARE THIS REEL", "Inspiring")
                )
            } else {
                listOf(
                    ScriptScene(1, SceneType.HOOK, "Viral Hook", "Being born without privilege is not your fault, but staying broke is a choice.", "Intense cinematic portrait transition to modern penthouse view", 8, "CHANGE YOUR TRAJECTORY", "Urgent"),
                    ScriptScene(2, SceneType.INTRO, "High-Income Leverage", "Stop trading time for pennies. Master high-leverage digital assets.", "Digital networks expanding across glowing holographic map", 10, "BUILD DIGITAL LEVERAGE", "Bold"),
                    ScriptScene(3, SceneType.MAIN_INFO, "The 1% Rule", "Dedicate 1 hour every single morning to building scalable systems.", "Morning sunrise over city with entrepreneur planning roadmap", 12, "COMPOUNDING EFFORT", "Motivational"),
                    ScriptScene(4, SceneType.FACT, "Asset Mindset", "True wealth is built when your investments make more than your living expenses.", "Asset portfolio visualization multiplying exponentially", 12, "ASSETS OVER LIABILITIES", "Direct"),
                    ScriptScene(5, SceneType.CLIMAX, "The Victory", "Consistency beats talent every single day. Keep showing up!", "Cinematic runner crossing the finish line in glory", 10, "NEVER GIVE UP", "Heroic"),
                    ScriptScene(6, SceneType.CTA, "Call to Action", "Save this video for daily motivation and follow for more!", "Modern animated follow button with neon accent", 8, "FOLLOW FOR MORE", "Friendly")
                )
            }
        } else {
            title = "$prompt - AI Video Production"
            scenes = generateDefaultScenes(language, durationSec)
        }

        return Script(
            projectId = projectId,
            title = title,
            language = language,
            tone = tone,
            audience = audience,
            totalDurationSec = durationSec,
            fullScriptText = scenes.joinToString(" ") { it.narrationText },
            scenes = scenes
        )
    }

    private fun generateDefaultScenes(language: String, durationSec: Int): List<ScriptScene> {
        val count = 5
        val perScene = durationSec / count
        return listOf(
            ScriptScene(1, SceneType.HOOK, "Opening Hook", "Here is something incredible that will change how you see this topic forever.", "Fast dynamic motion graphics with neon particles", perScene, "WATCH TILL THE END", "Engaging"),
            ScriptScene(2, SceneType.INTRO, "The Core Context", "Let's explore why this innovation is capturing worldwide attention right now.", "Sleek 3D isometric overview diagram", perScene, "THE BIG PICTURE", "Informative"),
            ScriptScene(3, SceneType.MAIN_INFO, "Key Breakthrough", "At its core, this breakthrough solves one of the most critical challenges.", "Detailed animated infographic highlighting mechanics", perScene, "HOW IT WORKS", "Clear"),
            ScriptScene(4, SceneType.FACT, "Remarkable Evidence", "Data shows an exponential growth pattern that experts did not anticipate.", "High contrast statistical chart with glowing milestones", perScene, "UNPRECEDENTED RESULTS", "Insightful"),
            ScriptScene(5, SceneType.CTA, "Final Takeaway & CTA", "What do you think about this future? Drop your comment below and subscribe!", "Modern sleek endcard with interactive subscribe badge", perScene, "SUBSCRIBE & SHARE", "Warm")
        )
    }

    private fun generateDefaultScriptFallback(
        projectId: String,
        language: String,
        tone: String,
        audience: String,
        durationSec: Int
    ): Script {
        val scenes = generateDefaultScenes(language, durationSec)
        return Script(
            projectId = projectId,
            title = "AI Studio Produced Video",
            language = language,
            tone = tone,
            audience = audience,
            totalDurationSec = durationSec,
            fullScriptText = scenes.joinToString(" ") { it.narrationText },
            scenes = scenes
        )
    }
}
