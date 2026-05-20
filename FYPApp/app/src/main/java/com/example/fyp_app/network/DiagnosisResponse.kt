package com.example.fyp_app.network

import com.google.gson.annotations.SerializedName

data class DiagnosisResponse(

    @SerializedName("class")
    val prediction: String? = "Unknown",  // ✅ default added

    @SerializedName("confidence")
    val confidence: Double? = 0.0        // ✅ default added
)