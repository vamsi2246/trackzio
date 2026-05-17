package com.weathersnap.data.repository

import com.weathersnap.data.api.GeocodingApi
import com.weathersnap.data.api.WeatherApi
import com.weathersnap.data.model.CurrentWeather
import com.weathersnap.data.model.GeocodingResponse
import com.weathersnap.data.model.GeocodingResult
import com.weathersnap.data.model.WeatherResponse
import com.weathersnap.domain.model.CitySuggestion
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WeatherRepositoryTest {

    private val geocodingApi: GeocodingApi = mockk()
    private val weatherApi: WeatherApi     = mockk()
    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setup() {
        repository = WeatherRepositoryImpl(geocodingApi, weatherApi)
    }

    @Test
    fun `searchCities returns mapped suggestions on success`() = runTest {
        val fakeResult = GeocodingResult(1, "London", 51.5, -0.12, "United Kingdom", "England")
        coEvery { geocodingApi.searchCity(any(), any(), any(), any()) } returns
                GeocodingResponse(listOf(fakeResult))

        val result = repository.searchCities("London")

        assertTrue(result.isSuccess)
        val suggestions = result.getOrNull()!!
        assertEquals(1, suggestions.size)
        assertEquals("London", suggestions[0].name)
        assertEquals("United Kingdom", suggestions[0].country)
    }

    @Test
    fun `searchCities returns cached result on second call`() = runTest {
        val fakeResult = GeocodingResult(1, "Paris", 48.85, 2.35, "France", "Île-de-France")
        coEvery { geocodingApi.searchCity(any(), any(), any(), any()) } returns
                GeocodingResponse(listOf(fakeResult))

        // First call — hits the network
        repository.searchCities("paris")
        // Second call — should come from cache
        repository.searchCities("paris")

        // API should only have been called once
        coVerify(exactly = 1) { geocodingApi.searchCity(any(), any(), any(), any()) }
    }

    @Test
    fun `searchCities returns failure on network error`() = runTest {
        coEvery { geocodingApi.searchCity(any(), any(), any(), any()) } throws
                RuntimeException("Network error")

        val result = repository.searchCities("Berlin")

        assertTrue(result.isFailure)
    }

    @Test
    fun `getWeather returns correct snapshot`() = runTest {
        val city = CitySuggestion(1, "Tokyo", "Japan", "Tokyo", 35.68, 139.69)
        val fakeWeather = CurrentWeather(22.5, 65, 0, 1013.0, 10.5)
        coEvery { weatherApi.getCurrentWeather(any(), any(), any()) } returns
                WeatherResponse(fakeWeather)

        val result = repository.getWeather(city)

        assertTrue(result.isSuccess)
        val snapshot = result.getOrNull()!!
        assertEquals("Tokyo", snapshot.cityName)
        assertEquals(22.5, snapshot.temperature, 0.01)
        assertEquals("Clear sky", snapshot.weatherCondition)
        assertEquals(65, snapshot.humidity)
    }

    @Test
    fun `getWeather returns failure when current weather is null`() = runTest {
        val city = CitySuggestion(1, "Unknown", "Unknown", null, 0.0, 0.0)
        coEvery { weatherApi.getCurrentWeather(any(), any(), any()) } returns WeatherResponse(null)

        val result = repository.getWeather(city)

        assertTrue(result.isFailure)
    }
}
