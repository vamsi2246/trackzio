package com.weathersnap.data.model

import com.google.gson.annotations.SerializedName

// Geocoding API response
data class GeocodingResponse(
    @SerializedName("results")
    val results: List<GeocodingResult>?
)

data class GeocodingResult(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("country") val country: String?,
    @SerializedName("admin1") val admin1: String?
)

// Weather API response
data class WeatherResponse(
    @SerializedName("current") val current: CurrentWeather?
)

data class CurrentWeather(
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("surface_pressure") val pressure: Double,
    @SerializedName("wind_speed_10m") val windSpeed: Double
)
