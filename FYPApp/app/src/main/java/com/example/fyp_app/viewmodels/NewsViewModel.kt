package com.example.fyp_app.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp_app.BuildConfig
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    @SerializedName("articles")
    val results: List<NewsArticle>,
)

data class NewsArticle(
    val title: String,
    val description: String?,
    @SerializedName("url")
    val link: String,
    @SerializedName("urlToImage")
    val imageUrl: String?,
    @SerializedName("publishedAt")
    val pubDate: String,
    @SerializedName("source")
    val source: NewsSource,
) {
    val sourceId: String get() = source.name
}

data class NewsSource(
    val name: String
)

interface NewsApi {
    @GET("v2/everything")
    suspend fun getAgroNews(
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY,
        @Query("q") query: String = "agriculture OR farming OR crops",
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20
    ): NewsResponse
}

class NewsViewModel : ViewModel() {
    var articles by mutableStateOf<List<NewsArticle>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val api = Retrofit.Builder()
        .baseUrl("https://newsapi.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NewsApi::class.java)

    init {
        fetchNews()
    }

    fun fetchNews() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = api.getAgroNews()
                if (response.status == "ok") {
                    articles = response.results
                } else {
                    errorMessage = "Failed to load news"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred"
            } finally {
                isLoading = false
            }
        }
    }
}
