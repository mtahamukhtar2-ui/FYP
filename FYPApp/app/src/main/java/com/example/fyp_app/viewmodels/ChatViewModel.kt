package com.example.fyp_app.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp_app.BuildConfig
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Groq API Models ---
data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>
)

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: GroqMessage
)

interface GroqApi {
    @POST("chat/completions")
    suspend fun getCompletion(
        @Header("Authorization") token: String,
        @Body request: GroqRequest
    ): GroqResponse
}

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
)

class ChatViewModel : ViewModel() {

    private val apiKey = BuildConfig.GROQ_API_KEY // Reading from local.properties
    private val groqModel = "llama-3.3-70b-versatile"
    
    private val api = Retrofit.Builder()
        .baseUrl("https://api.groq.com/openai/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GroqApi::class.java)

    val chatHistory = mutableStateListOf<ChatMessage>()
    private var initializedLanguage: String? = null
    private var systemPrompt: String = ""

    fun initialize(language: String, greeting: String, systemInstruction: String) {
        if (initializedLanguage != language) {
            systemPrompt = systemInstruction
            chatHistory.clear()
            chatHistory.add(ChatMessage(greeting, isFromUser = false))
            initializedLanguage = language
        }
    }

    fun sendMessage(userText: String) {
        chatHistory.add(ChatMessage(userText, isFromUser = true))

        viewModelScope.launch {
            try {
                // Build message history for context
                val messages = mutableListOf<GroqMessage>()
                messages.add(GroqMessage("system", systemPrompt))
                
                // Add last few messages for context (optional, but good for chat)
                chatHistory.takeLast(10).forEach { 
                    messages.add(GroqMessage(if (it.isFromUser) "user" else "assistant", it.text))
                }

                val request = GroqRequest(
                    model = groqModel,
                    messages = messages
                )

                val response = api.getCompletion("Bearer $apiKey", request)
                val botReply = response.choices.firstOrNull()?.message?.content ?: "No response from assistant."
                
                chatHistory.add(ChatMessage(botReply, isFromUser = false))
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Groq API Error", e)
                chatHistory.add(
                    ChatMessage("Error: ${e.localizedMessage}. Make sure your API key is valid for Groq.", isFromUser = false)
                )
            }
        }
    }
}
