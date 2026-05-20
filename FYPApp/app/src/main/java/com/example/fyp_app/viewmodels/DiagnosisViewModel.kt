package com.example.fyp_app.viewmodels
import com.example.fyp_app.utils.ImageProcessor
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp_app.utils.LanguageManager
import com.example.fyp_app.network.DiagnosisResponse
import com.example.fyp_app.network.RetrofitClient
import com.example.fyp_app.network.GroqClient
import com.example.fyp_app.network.GroqRequest
import com.example.fyp_app.network.GroqMessage
import com.example.fyp_app.BuildConfig
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.*

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(
        val data: DiagnosisResponse,
        val treatment: String? = null,
        val translatedPrediction: String? = null
    ) : UiState()
    data class Error(val message: String) : UiState()
}

class DiagnosisViewModel : ViewModel() {

    var uiState by mutableStateOf<UiState>(UiState.Idle)
        private set

    fun diagnoseImage(context: Context, imageUri: Uri, cropType: String) {
        viewModelScope.launch {
            uiState = UiState.Loading
            Log.d("ViewModel", "Diagnosing crop: $cropType")

            try {
                // 1. Process the Image URI into a MultipartBody.Part
                val processedUri = ImageProcessor.processLeafImage(context, imageUri)

                if (processedUri == null) {
                    uiState = UiState.Error("Failed to isolate the leaf from the background")
                    return@launch
                }
                val imagePart = uriToMultipartBodyPart(context, processedUri)
                if (imagePart==null){
                    uiState = UiState.Error("Failed to prepare image for upload.")
                    return@launch

                }

                // 2. Normalize crop string and determine the correct endpoint
                val endpoint = when (cropType.lowercase(Locale.ROOT)) {
                    "corn" -> "predict_corn"
                    "mango" -> "predict_mango"
                    "potato" -> "predict_potato"
                    "apple" -> "predict_apple"
                    "lemon" -> "predict_lemon"
                    "orange", "citrus" -> "predict"
                    else -> null // Unsupported crop
                }

                // 🚨 CRITICAL CHECK: Stop processing if the crop is not supported
                if (endpoint == null) {
                    uiState = UiState.Error("Unsupported crop type: $cropType")
                    Log.e("ViewModel", "Attempted to diagnose unsupported crop: $cropType")
                    return@launch
                }

                Log.d("ViewModel", "Routing to endpoint: /$endpoint")

                // 3. Make the API Call
                val response = RetrofitClient.instance.uploadImage(
                    endpoint = endpoint,
                    file = imagePart,
                )

                // 4. Handle the Response
                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && (body.prediction != null)) {
                        // Handle "uncertain" response gracefully
                        if (body.prediction.contains("uncertain", ignoreCase = true)) {
                            uiState = UiState.Error("Please take a clearer image of the leaf.")
                        } else {
                            val language = LanguageManager.getSavedLanguage(context)
                            uiState = UiState.Success(body)
                            // Only fetch AI treatment if the leaf is NOT healthy
                            if (!body.prediction.contains("healthy", ignoreCase = true)) {
                                fetchTreatmentAdvice(body.prediction, cropType, language)
                            } else if (language == "ur") {
                                uiState = (uiState as UiState.Success).copy(translatedPrediction = "صحت مند")
                            }
                        }
                        Log.d("ViewModel", "Prediction: ${body.prediction}, Confidence: ${body.confidence}")
                    } else {
                        uiState = UiState.Error("Invalid response from server.")
                    }
                } else {
                    uiState = UiState.Error("Server error (${response.code()})")
                    Log.e("ViewModel", "Error Body: ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                uiState = UiState.Error("Network error. Please check your connection.")
                Log.e("ViewModel", "Network Exception", e)
            }
        }
    }

    private fun fetchTreatmentAdvice(disease: String, crop: String, language: String) {
        viewModelScope.launch {
            try {
                val prompt = if (language == "ur") {
                    """
                    Provide a professional and detailed treatment advice for the plant disease: '$disease' in '$crop' crop.
                    The user wants specific chemical and fertilizer suggestions.
                    The ENTIRE response MUST be in Urdu (Urdu script).
                    
                    Please structure your response as follows (DO NOT use markdown formatting like **, *, or #):
                    
                    بیماری کا نام:
                    (Translate '$disease' to Urdu here)
                    
                    کیمیائی تجاویز:
                    (List specific fungicides/pesticides with their exact dosages and frequency in Urdu)
                    
                    کھاد کی سفارشات:
                    (Suggest specific fertilizers in Urdu)
                    
                    عملی منصوبہ:
                    (Provide 2-3 practical steps in Urdu)
                    
                    Keep the response concise, expert-level, and formatted for easy reading on a mobile screen.
                    """.trimIndent()
                } else {
                    """
                    Provide a professional and detailed treatment advice for the plant disease: '$disease' in '$crop' crop.
                    The user wants specific chemical and fertilizer suggestions.
                    
                    Please structure your response as follows (DO NOT use markdown formatting like **, *, or #):
                    
                    CHEMICAL SUGGESTIONS:
                    List specific fungicides/pesticides with their exact dosages (e.g., 2g/L) and frequency of use.
                    
                    FERTILIZER RECOMMENDATIONS:
                    Suggest specific fertilizers (like NPK ratios or micronutrients) to help the plant recover and specify the application rate.
                    
                    ACTION PLAN:
                    Provide 2-3 practical steps the farmer should take immediately.
                    
                    Keep the response concise, expert-level, and formatted for easy reading on a mobile screen.
                    """.trimIndent()
                }

                val request = GroqRequest(
                    messages = listOf(
                        GroqMessage(role = "system", content = if (language == "ur") "آپ ایک ماہر زرعی سائنسدان ہیں۔ ہمیشہ اردو میں جواب دیں۔" else "You are an expert agricultural scientist."),
                        GroqMessage(role = "user", content = prompt)
                    )
                )

                val response = GroqClient.instance.getChatCompletion(
                    apiKey = "Bearer ${BuildConfig.GROQ_API_KEY}",
                    request = request
                )

                if (response.isSuccessful) {
                    var treatment = response.body()?.choices?.firstOrNull()?.message?.content
                    var translatedName: String? = null
                    
                    if (language == "ur" && treatment != null) {
                        // Attempt to extract the translated disease name from the response
                        if (treatment.contains("بیماری کا نام:", ignoreCase = true)) {
                            val parts = treatment.split("کیمیائی تجاویز:", ignoreCase = true)
                            val headerPart = parts[0]
                            translatedName = headerPart.replace("بیماری کا نام:", "", ignoreCase = true).trim()
                            if (parts.size > 1) {
                                treatment = "کیمیائی تجاویز:" + parts[1]
                            }
                        }
                    }

                    val currentState = uiState
                    (currentState as? UiState.Success)?.let {
                        uiState = it.copy(treatment = treatment, translatedPrediction = translatedName)
                    }
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching treatment from Groq", e)
            }
        }
    }

    private fun uriToMultipartBodyPart(context: Context, uri: Uri): MultipartBody.Part? {
        return try {
            // Create a temporary file in the cache directory
            val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")

            // Copy the image data from the URI to the temporary file
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            // Create the RequestBody and MultipartBody.Part for Retrofit
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("file", file.name, requestFile)

        } catch (e: Exception) {
            Log.e("ViewModel", "File conversion error", e)
            null
        }
    }
}