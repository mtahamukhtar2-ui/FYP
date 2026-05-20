package com.example.fyp_app.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    // ✅ FIXED: We use @Path to dynamically change the URL based on the selected crop
    // Example: if endpoint is "predict_corn", it calls -> /predict_corn
    @Multipart
    @POST("{endpoint}")
    suspend fun uploadImage(
        @Path("endpoint", encoded = true) endpoint: String,
        @Part file: MultipartBody.Part // ✅ Changed parameter name to 'file' for consistency
    ): Response<DiagnosisResponse>
}