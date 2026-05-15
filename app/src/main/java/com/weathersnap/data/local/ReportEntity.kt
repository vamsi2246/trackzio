package com.weathersnap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.weathersnap.domain.model.Report

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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

fun ReportEntity.toDomain(): Report {
    return Report(
        id = id,
        cityName = cityName,
        temperature = temperature,
        weatherCondition = weatherCondition,
        humidity = humidity,
        windSpeed = windSpeed,
        pressure = pressure,
        notes = notes,
        imagePath = imagePath,
        originalImageSizeBytes = originalImageSizeBytes,
        compressedImageSizeBytes = compressedImageSizeBytes,
        timestamp = timestamp
    )
}

fun Report.toEntity(): ReportEntity {
    return ReportEntity(
        id = id,
        cityName = cityName,
        temperature = temperature,
        weatherCondition = weatherCondition,
        humidity = humidity,
        windSpeed = windSpeed,
        pressure = pressure,
        notes = notes,
        imagePath = imagePath,
        originalImageSizeBytes = originalImageSizeBytes,
        compressedImageSizeBytes = compressedImageSizeBytes,
        timestamp = timestamp
    )
}
