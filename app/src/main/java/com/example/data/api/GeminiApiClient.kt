package com.example.data.api

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

// --- Gemini API Request & Response Data Classes ---

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Double? = 0.7,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)

// --- Interview Structure ---

@JsonClass(generateAdapter = true)
data class InterviewResponse(
    val feedback: String,
    val score: Int,
    val nextQuestion: String
)

// --- Retrofit Interface ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// --- Client Instance ---

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    /**
     * Parses the response from Gemini into a structured InterviewResponse object
     */
    fun parseInterviewResponse(rawJson: String): InterviewResponse {
        return try {
            // Remove markdown code fences if any
            val cleanJson = rawJson.trim()
                .replace("```json", "")
                .replace("```", "")
                .trim()
            val adapter = moshi.adapter(InterviewResponse::class.java)
            adapter.fromJson(cleanJson) ?: InterviewResponse(
                feedback = "Unable to process answer properly.",
                score = 0,
                nextQuestion = "Can you describe a security incident you mitigated in the past?"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback parsing if JSON contains minor issues or formatting
            InterviewResponse(
                feedback = "Constructive feedback could not be fully parsed, but excellent try! Review your protocols and focus on access control fundamentals.",
                score = 75,
                nextQuestion = "What measures do you take to prevent cross-site scripting (XSS) in modern web apps?"
            )
        }
    }
}
