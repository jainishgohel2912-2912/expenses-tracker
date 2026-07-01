package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

// Response parsing helper class for natural language logging
@JsonClass(generateAdapter = true)
data class ParsedTransaction(
    val amount: Double,
    val category: String, // Matching default category names
    val description: String,
    val type: String, // "EXPENSE" or "INCOME"
    val paymentMethod: String // "Cash", "Card", "UPI", "Bank Transfer"
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun askGemini(prompt: String, systemPrompt: String? = null, jsonOutput: Boolean = false): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Please configure your GEMINI_API_KEY in the Secrets Panel of AI Studio to activate AI features."
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = if (jsonOutput) GenerationConfig(responseMimeType = "application/json", temperature = 0.1f) else GenerationConfig(temperature = 0.7f),
            systemInstruction = systemPrompt?.let { Content(parts = listOf(Part(text = it))) }
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No response from AI."
        } catch (e: Exception) {
            "AI error: ${e.message}"
        }
    }

    // Helper to parse natural language transaction input (Voice or Text quick log)
    suspend fun parseNaturalLanguageTransaction(input: String): ParsedTransaction? {
        val systemPrompt = """
            You are a transaction extraction assistant. Parse the user's input representing a financial transaction (income or expense) and output a JSON object with these EXACT keys:
            - "amount": Double (numeric amount, e.g. 250.0)
            - "category": String (must be one of: "Food", "Travel", "Shopping", "College", "Books", "Hostel", "Rent", "Entertainment", "Fuel", "Healthcare", "Gym", "Bills", "Investment", "Others")
            - "description": String (short description, e.g., "Lunch with roommates", "Monthly stipend")
            - "type": String (either "EXPENSE" or "INCOME")
            - "paymentMethod": String (either "Cash", "Card", "UPI", "Bank Transfer", select based on context or default to "Cash" or "UPI")

            Example inputs:
            "I spent 250 rupees on lunch with friends" -> {"amount": 250.0, "category": "Food", "description": "Lunch with friends", "type": "EXPENSE", "paymentMethod": "UPI"}
            "Scholarship received 15000" -> {"amount": 15000.0, "category": "College", "description": "Scholarship", "type": "INCOME", "paymentMethod": "Bank Transfer"}
            "Paid rent of 3000 by card" -> {"amount": 3000.0, "category": "Rent", "description": "Paid rent", "type": "EXPENSE", "paymentMethod": "Card"}

            ONLY output valid JSON matching this schema. Do not include markdown codeblocks or other chat text.
        """.trimIndent()

        val responseText = askGemini(prompt = input, systemPrompt = systemPrompt, jsonOutput = true)
        return try {
            // Remove potential markdown fences just in case Gemini includes them despite instructions
            val cleanJson = responseText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val adapter = moshi.adapter(ParsedTransaction::class.java)
            adapter.fromJson(cleanJson)
        } catch (e: Exception) {
            null
        }
    }
}
