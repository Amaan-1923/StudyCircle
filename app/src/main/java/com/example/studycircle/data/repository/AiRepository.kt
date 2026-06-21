package com.example.studycircle.data.repository

import com.example.studycircle.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class AiRepository {
    private val apiKey = BuildConfig.GROQ_API_KEY
    private val baseUrl = "https://api.groq.com/openai/v1/chat/completions"
    private val conversationHistory = mutableListOf<JSONObject>()

    init {
        // Add system message
        conversationHistory.add(
            JSONObject().apply {
                put("role", "system")
                put("content", """
                    You are StudyBot, an AI study assistant built into StudyCircle.
                    Help students understand difficult concepts clearly.
                    Solve academic problems step by step.
                    Explain topics in simple, friendly language.
                    Give examples and analogies when needed.
                    Support subjects like Math, Physics, Chemistry, CS, OS, DBMS, DSA.
                    Always be encouraging, patient, and helpful.
                    Keep answers focused and educational.
                """.trimIndent())
            }
        )
    }

    suspend fun sendMessage(message: String): String = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("AiRepository", "API Key length: ${apiKey.length}")
            android.util.Log.d("AiRepository", "Sending: $message")

            if (apiKey.isEmpty()) {
                return@withContext "API Key is empty! Check local.properties"
            }

            // Add user message
            conversationHistory.add(
                JSONObject().apply {
                    put("role", "user")
                    put("content", message)
                }
            )

            val requestBody = JSONObject().apply {
                put("model", "llama-3.3-70b-versatile")
                put("messages", JSONArray(conversationHistory))
                put("max_tokens", 1024)
                put("temperature", 0.7)
            }

            val url = URL(baseUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("Accept", "application/json")
                doOutput = true
                doInput = true
                connectTimeout = 30000
                readTimeout = 30000
            }

            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val responseCode = connection.responseCode
            android.util.Log.d("AiRepository", "Response code: $responseCode")

            val responseText = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader(Charsets.UTF_8).readText()
            } else {
                val errorText = connection.errorStream
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.readText() ?: "No error body"
                android.util.Log.e("AiRepository", "Error: $errorText")
                return@withContext "Error $responseCode: $errorText"
            }

            val jsonResponse = JSONObject(responseText)
            val text = jsonResponse
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            // Add assistant response to history
            conversationHistory.add(
                JSONObject().apply {
                    put("role", "assistant")
                    put("content", text)
                }
            )

            text
        } catch (e: Exception) {
            android.util.Log.e("AiRepository", "Exception: ${e.message}", e)
            "Error: ${e::class.java.simpleName} — ${e.message}"
        }
    }

    fun sendMessageStream(message: String): Flow<String> = flow {
        emit(sendMessage(message))
    }

    fun clearHistory() {
        val systemMessage = conversationHistory.firstOrNull()
        conversationHistory.clear()
        systemMessage?.let { conversationHistory.add(it) }
    }
}