package com.weathersnap.domain.model

data class Report(
    val id: Long = 0,
    val cityName: String,
    val temperature: Double,
    val weatherCondition: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double,
    val notes: String,
    val imagePath: String,
    val originalImageSizeBytes: Long,
    val compressedImageSizeBytes: Long,
    val timestamp: Long
)
