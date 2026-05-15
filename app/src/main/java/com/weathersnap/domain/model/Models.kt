package com.weathersnap.domain.model

data class CitySuggestion(
    val id: Int,
    val name: String,
    val country: String,
    val admin1: String?,
    val latitude: Double,
    val longitude: Double
)

data class WeatherSnapshot(
    val cityName: String,
    val temperature: Double,
    val weatherCondition: String, // e.g. code mapped to string
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double,
    val latitude: Double,
    val longitude: Double
)
