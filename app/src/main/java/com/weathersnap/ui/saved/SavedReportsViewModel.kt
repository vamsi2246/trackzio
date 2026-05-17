package com.weathersnap.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weathersnap.domain.model.Report
import com.weathersnap.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class SavedReportsUiState {
    object Loading : SavedReportsUiState()
    object Empty : SavedReportsUiState()
    data class Success(val reports: List<Report>) : SavedReportsUiState()
}

@HiltViewModel
class SavedReportsViewModel @Inject constructor(
    reportRepository: ReportRepository
) : ViewModel() {

    val uiState: StateFlow<SavedReportsUiState> = reportRepository.getAllReports()
        .map { reports ->
            if (reports.isEmpty()) SavedReportsUiState.Empty
            else SavedReportsUiState.Success(reports)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SavedReportsUiState.Loading
        )
}
