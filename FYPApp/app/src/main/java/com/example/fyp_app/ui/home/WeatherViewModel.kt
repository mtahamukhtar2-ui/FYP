package com.example.fyp_app.ui.home

import com.example.fyp_app.BuildConfig
import android.util.Log // Added this import to fix the error
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Data models for the API response
data class WeatherResponse(val main: MainData, val weather: List<WeatherDesc>, val wind: WindData, val name: String)
data class MainData(val temp: Double, val humidity: Int)
data class WeatherDesc(val main: String)
data class WindData(val speed: Double)

interface WeatherApi {
    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

class WeatherViewModel : ViewModel() {
    var temp by mutableStateOf("--°C")
    var condition by mutableStateOf("Locating...")
    var city by mutableStateOf("")
    var humidity by mutableStateOf("--%")
    var windSpeed by mutableStateOf("-- km/h")

    private val api = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApi::class.java)

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            // Update status so user knows GPS worked and we are now calling the API
            condition = "Updating..."
            try {
                // Ensure the API key is active (usually takes 1-2 hours for new OpenWeather keys)
                val response = api.getWeather(lat, lon, BuildConfig.WEATHER_API_KEY)
                temp = "${response.main.temp.toInt()}°C"
                condition = response.weather.firstOrNull()?.main ?: "Clear"
                city = response.name
                humidity = "${response.main.humidity}%"
                windSpeed = "${(response.wind.speed * 3.6).toInt()} km/h" // Convert m/s to km/h
            } catch (e: Exception) {
                // This happens if the key isn't active yet or there is no internet
                condition = "Error"
                Log.e("WeatherViewModel", "API Error: ${e.message}")
            }
        }
    }
}