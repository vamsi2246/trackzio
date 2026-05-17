package com.weathersnap.ui.weather

import com.weathersnap.data.repository.FakeWeatherRepository
import com.weathersnap.domain.model.CitySuggestion
import com.weathersnap.domain.model.WeatherSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeWeatherRepository
    private lateinit var viewModel: WeatherViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo  = FakeWeatherRepository()
        viewModel = WeatherViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() {
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertTrue(state.suggestions.isEmpty())
        assertNull(state.weatherSnapshot)
        assertNull(state.error)
    }

    @Test
    fun `query shorter than 3 chars does not trigger search`() = runTest {
        viewModel.onSearchQueryChanged("Lo")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.suggestions.isEmpty())
    }

    @Test
    fun `query of 3+ chars loads suggestions`() = runTest {
        fakeRepo.cities = listOf(
            CitySuggestion(1, "London", "UK", "England", 51.5, -0.12)
        )
        viewModel.onSearchQueryChanged("Lon")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.suggestions.size)
        assertEquals("London", state.suggestions[0].name)
    }

    @Test
    fun `selecting city loads weather and clears suggestions`() = runTest {
        val city = CitySuggestion(1, "Paris", "France", "Île-de-France", 48.85, 2.35)
        fakeRepo.snapshot = WeatherSnapshot("Paris", 18.0, "Clear sky", 55, 12.0, 1015.0, 48.85, 2.35)

        viewModel.onCitySelected(city)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.weatherSnapshot)
        assertEquals("Paris", state.weatherSnapshot?.cityName)
        assertTrue(state.suggestions.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `error from repository surfaces in uiState`() = runTest {
        fakeRepo.shouldFail = true
        viewModel.onSearchQueryChanged("xyz")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
    }
}
