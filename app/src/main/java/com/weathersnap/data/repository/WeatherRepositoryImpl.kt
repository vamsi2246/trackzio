package com.weathersnap.data.repository

import com.weathersnap.data.api.GeocodingApi
import com.weathersnap.data.api.WeatherApi
import com.weathersnap.domain.model.CitySuggestion
import com.weathersnap.domain.model.WeatherSnapshot
import com.weathersnap.domain.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class WeatherRepositoryImpl(
    private val geocodingApi: GeocodingApi,
    private val weatherApi: WeatherApi
) : WeatherRepository {

    // In-memory cache for city suggestions
    private val suggestionCache = ConcurrentHashMap<String, List<CitySuggestion>>()

    override suspend fun searchCities(query: String): Result<List<CitySuggestion>> = withContext(Dispatchers.IO) {
        try {
            val cached = suggestionCache[query.lowercase()]
            if (cached != null) {
                return@withContext Result.success(cached)
            }

            val response = geocodingApi.searchCity(name = query)
            val suggestions = response.results?.map {
                CitySuggestion(
                    id = it.id,
                    name = it.name,
                    country = it.country ?: "",
                    admin1 = it.admin1,
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            } ?: emptyList()

            suggestionCache[query.lowercase()] = suggestions
            Result.success(suggestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWeather(city: CitySuggestion): Result<WeatherSnapshot> = withContext(Dispatchers.IO) {
        try {
            val response = weatherApi.getCurrentWeather(
                latitude = city.latitude,
                longitude = city.longitude
            )
            
            val current = response.current ?: throw Exception("Weather data unavailable")
            
            val snapshot = WeatherSnapshot(
                cityName = city.name,
                temperature = current.temperature,
                weatherCondition = mapWeatherCode(current.weatherCode),
                humidity = current.humidity,
                windSpeed = current.windSpeed,
                pressure = current.pressure,
                latitude = city.latitude,
                longitude = city.longitude
            )
            Result.success(snapshot)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1, 2, 3 -> "Mainly clear, partly cloudy, and overcast"
            45, 48 -> "Fog and depositing rime fog"
            51, 53, 55 -> "Drizzle: Light, moderate, and dense intensity"
            56, 57 -> "Freezing Drizzle: Light and dense intensity"
            61, 63, 65 -> "Rain: Slight, moderate and heavy intensity"
            66, 67 -> "Freezing Rain: Light and heavy intensity"
            71, 73, 75 -> "Snow fall: Slight, moderate, and heavy intensity"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers: Slight, moderate, and violent"
            85, 86 -> "Snow showers slight and heavy"
            95 -> "Thunderstorm: Slight or moderate"
            96, 99 -> "Thunderstorm with slight and heavy hail"
            else -> "Unknown"
        }
    }
}
