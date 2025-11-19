package com.quickhire.candidate.network

import android.util.Log
import com.quickhire.candidate.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// Internally powered by Groq (Llama 3), but keeping name to avoid breaking UI code
object GeminiClient {

    // Groq Model (Fast & Smart)
    private const val MODEL_ID = "llama-3.3-70b-versatile"

    suspend fun parseVoiceInput(spokenText: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.groq.com/openai/v1/chat/completions")

                // 1. Construct OpenAI-compatible JSON Body for Groq
                val jsonBody = JSONObject()

                // Llama 3 System Prompt (Enforcing JSON)
                val systemMessage = JSONObject()
                systemMessage.put("role", "system")
                systemMessage.put("content", """
                    You are a recruiter AI.
                    Extract 'role' (job title) and 'skills' (array of strings) from the user's spoken text.
                    Output MUST be valid JSON object.
                    Example: { "role": "Driver", "skills": ["License", "Navigation"] }
                """.trimIndent())

                val userMessage = JSONObject()
                userMessage.put("role", "user")
                userMessage.put("content", spokenText)

                val messagesArray = JSONArray()
                messagesArray.put(systemMessage)
                messagesArray.put(userMessage)

                jsonBody.put("model", MODEL_ID)
                jsonBody.put("messages", messagesArray)

                // Force JSON mode (Groq Feature)
                val responseFormat = JSONObject()
                responseFormat.put("type", "json_object")
                jsonBody.put("response_format", responseFormat)

                Log.d("GROQ_CLIENT", "Sending to Groq: $spokenText")

                // 2. Send Request
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer ${Config.GROQ_API_KEY}")
                conn.doOutput = true
                conn.connectTimeout = 10000
                conn.readTimeout = 10000

                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(jsonBody.toString())
                writer.flush()
                writer.close()

                // 3. Read Response
                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val rawJson = response.toString()
                    Log.d("GROQ_CLIENT", "Response: $rawJson")

                    // Parse OpenAI/Groq Format
                    // { "choices": [ { "message": { "content": "{ ... }" } } ] }
                    val responseObj = JSONObject(rawJson)
                    val choices = responseObj.getJSONArray("choices")
                    if (choices.length() > 0) {
                        val content = choices.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")

                        return@withContext content
                    }
                    return@withContext "Error: Empty choices from Groq"
                } else {
                    // Read Error
                    val errorStream = conn.errorStream
                    val errorMsg = StringBuilder()
                    if (errorStream != null) {
                        val reader = BufferedReader(InputStreamReader(errorStream))
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            errorMsg.append(line)
                        }
                    }
                    Log.e("GROQ_ERROR", "Error $responseCode: $errorMsg")
                    return@withContext "Server Error $responseCode: $errorMsg"
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext "Crash: ${e.localizedMessage}"
            }
        }
    }
}