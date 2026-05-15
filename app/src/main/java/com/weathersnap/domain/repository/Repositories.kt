package com.weathersnap.domain.repository

import com.weathersnap.domain.model.CitySuggestion
import com.weathersnap.domain.model.Report
import com.weathersnap.domain.model.WeatherSnapshot
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun searchCities(query: String): Result<List<CitySuggestion>>
    suspend fun getWeather(city: CitySuggestion): Result<WeatherSnapshot>
}

interface ReportRepository {
    fun getAllReports(): Flow<List<Report>>
    suspend fun saveReport(report: Report): Result<Long>
}
