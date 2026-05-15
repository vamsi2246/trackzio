package com.weathersnap.data.api

import com.weathersnap.data.model.GeocodingResponse
import com.weathersnap.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {
    @GET("search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}

interface WeatherApi {
    @GET("forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,weather_code,surface_pressure,wind_speed_10m"
    ): WeatherResponse
}
