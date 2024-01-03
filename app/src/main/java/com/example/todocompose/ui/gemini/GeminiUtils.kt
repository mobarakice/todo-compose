package com.example.todocompose.ui.gemini

import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content

object GeminiUtils {
    const val MODEL_GEMINI_PRO: String = "gemini-pro"
    const val TEMPERATURE = 0.9f
    const val TOP_K = 1
    const val TOP_P = 1f
    const val MAX_OUTPUT_TOKENS = 2048

    fun getSafetySettings() = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(
            HarmCategory.SEXUALLY_EXPLICIT,
            BlockThreshold.MEDIUM_AND_ABOVE
        ),
        SafetySetting(
            HarmCategory.DANGEROUS_CONTENT,
            BlockThreshold.MEDIUM_AND_ABOVE
        ),
    )

    fun getStructuredPrompt(text: String) = content {
        text(
            """English trainer, refine my advanced English!
                    |-Correct & explain my mistakes.
                    |-Offer advanced alternatives.
                    |-Output a maximum of 80 words.
                |Hello, how are you?""".trimMargin("|")
        )
        text("Request: Hello, how are you?")
        text(
            """Response: Your sentence is grammatically correct! However, depending on the context and desired formality, here are some advanced alternatives:
            |"Good morning/afternoon/evening, I hope you are well."
            |"Great to see you! How's it going?"
            |"Hello! Are you tackling any exciting projects nowadays?"""".trimMargin("|")
        )
        text("Request: What are you done?")
        text(
            """Response: "What are you done?" is grammatically incorrect. The correct way to ask would be 
            |"What have you done?" or "What are you doing?" depending on the context and time frame.
            |Alternatives in advanced English:
            |"What have you accomplished recently?" 
            |"What have you been up to lately?" 
            |"What progress have you made on [project/task]?"""".trimMargin("|")
        )
        text("Request: $text")
        text("Response: ")
    }

    fun getChatPrompt() = listOf(
        content("user") {
            text(
                """English trainer, refine my advanced English!
                    |-Correct & explain my mistakes.
                    |-Offer advanced alternatives.
                    |-Output a maximum of 80 words.
                |Hello, how are you?""".trimMargin("|")
            )
        },
        content("model") {
            text(
                """Your sentence is grammatically correct! However, depending on the context and desired formality,
                |here are some advanced alternatives:
                |"Good morning/afternoon/evening, I hope you are well."
                |"Great to see you! How's it going?"
                |"Hello! Are you tackling any exciting projects nowadays?"""".trimMargin("|")
            )
        },
        content("user") {
            text(
                """English trainer, refine my advanced English!
                    |-Correct & explain my mistakes.
                    |-Offer advanced alternatives.
                    |-Output a maximum of 80 words.
                |What are you done?""".trimMargin("|")
            )
        },
        content("model") {
            text(
                """"What are you done?" is grammatically incorrect. The correct way to ask would be "What have you done?" or "What are you doing?" depending on the context and time frame.
                |Alternatives in advanced English:
                |"What have you accomplished recently?
                |"What have you been up to lately?"
                |"What progress have you made on [project/task]?" (Tailored to a specific context)"
                """.trimMargin("|")
            )
        }
    )
}