package com.weathersnap.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weathersnap.domain.model.CitySuggestion
import com.weathersnap.domain.model.WeatherSnapshot
import com.weathersnap.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherUiState(
    val searchQuery: String = "",
    val suggestions: List<CitySuggestion> = emptyList(),
    val isSearching: Boolean = false,
    val selectedCity: CitySuggestion? = null,
    val weatherSnapshot: WeatherSnapshot? = null,
    val isLoadingWeather: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, error = null) }
        
        searchJob?.cancel()
        if (query.length > 2) {
            searchJob = viewModelScope.launch {
                _uiState.update { it.copy(isSearching = true) }
                delay(500) // debounce
                val result = weatherRepository.searchCities(query)
                result.onSuccess { cities ->
                    _uiState.update { it.copy(suggestions = cities, isSearching = false) }
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isSearching = false, suggestions = emptyList()) }
                }
            }
        } else {
            _uiState.update { it.copy(suggestions = emptyList(), isSearching = false) }
        }
    }

    fun onCitySelected(city: CitySuggestion) {
        _uiState.update { 
            it.copy(
                selectedCity = city, 
                suggestions = emptyList(), 
                searchQuery = city.name,
                isLoadingWeather = true,
                error = null,
                weatherSnapshot = null
            ) 
        }
        
        viewModelScope.launch {
            val result = weatherRepository.getWeather(city)
            result.onSuccess { snapshot ->
                _uiState.update { it.copy(weatherSnapshot = snapshot, isLoadingWeather = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoadingWeather = false) }
            }
        }
    }
}
