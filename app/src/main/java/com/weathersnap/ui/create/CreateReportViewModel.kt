package com.weathersnap.ui.create

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.weathersnap.domain.model.Report
import com.weathersnap.domain.model.WeatherSnapshot
import com.weathersnap.domain.repository.ReportRepository
import com.weathersnap.utils.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class CreateReportUiState(
    val weatherSnapshot: WeatherSnapshot? = null,
    val capturedImageUri: String? = null,
    val notes: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val originalImageSizeBytes: Long = 0L,
    val compressedImageSizeBytes: Long = 0L
)

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateReportUiState())
    val uiState: StateFlow<CreateReportUiState> = _uiState.asStateFlow()

    init {
        // Navigation Compose automatically populates SavedStateHandle with nav-argument values.
        // This means snapshotJson survives rotation AND process death without extra work.
        val snapshotJson: String? = savedStateHandle["snapshotJson"]
        val snapshot = snapshotJson?.let {
            runCatching { Gson().fromJson(it, WeatherSnapshot::class.java) }.getOrNull()
        }

        val recoveredNotes: String = savedStateHandle["draft_notes"] ?: ""
        val recoveredImage: String? = savedStateHandle["draft_image"]

        _uiState.update {
            it.copy(
                weatherSnapshot  = snapshot,
                notes            = recoveredNotes,
                capturedImageUri = recoveredImage
            )
        }

        // Restore size metadata so the UI doesn't show 0 KB after rotation
        recoveredImage?.let { uri ->
            val file = runCatching { File(Uri.parse(uri).path ?: "") }.getOrNull()
            if (file != null && file.exists()) {
                _uiState.update { state ->
                    state.copy(compressedImageSizeBytes = file.length())
                }
            }
        }
    }

    fun onNotesChanged(newNotes: String) {
        _uiState.update { it.copy(notes = newNotes) }
        savedStateHandle["draft_notes"] = newNotes
    }

    fun onImageCaptured(context: Context, uriString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val sourceFile = runCatching { File(Uri.parse(uriString).path ?: "") }.getOrNull()
            val origSize   = sourceFile?.takeIf { it.exists() }?.length() ?: 0L

            val compressed = ImageCompressor.compressImage(context, Uri.parse(uriString))
            if (compressed != null) {
                val finalUri = Uri.fromFile(compressed).toString()
                _uiState.update {
                    it.copy(
                        capturedImageUri      = finalUri,
                        originalImageSizeBytes  = origSize,
                        compressedImageSizeBytes = compressed.length()
                    )
                }
                savedStateHandle["draft_image"]          = finalUri
                savedStateHandle["draft_orig_size"]      = origSize
                savedStateHandle["draft_comp_size"]      = compressed.length()
            } else {
                _uiState.update { it.copy(error = "Image compression failed") }
            }
        }
    }

    fun saveReport() {
        val state    = _uiState.value
        val snapshot = state.weatherSnapshot ?: run {
            _uiState.update { it.copy(error = "Weather snapshot missing") }; return
        }
        val imagePath = state.capturedImageUri ?: run {
            _uiState.update { it.copy(error = "No photo captured") }; return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val report = Report(
                cityName                = snapshot.cityName,
                temperature             = snapshot.temperature,
                weatherCondition        = snapshot.weatherCondition,
                humidity                = snapshot.humidity,
                windSpeed               = snapshot.windSpeed,
                pressure                = snapshot.pressure,
                notes                   = state.notes,
                imagePath               = imagePath,
                originalImageSizeBytes  = state.originalImageSizeBytes,
                compressedImageSizeBytes = state.compressedImageSizeBytes,
                timestamp               = System.currentTimeMillis()
            )

            reportRepository.saveReport(report)
                .onSuccess {
                    // Clear draft keys so no stale data on next visit
                    savedStateHandle.remove<String>("draft_notes")
                    savedStateHandle.remove<String>("draft_image")
                    savedStateHandle.remove<Long>("draft_orig_size")
                    savedStateHandle.remove<Long>("draft_comp_size")
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Only delete temp files if the report was never saved.
        // On rotation, ViewModel is NOT cleared, so this is safe to do here.
        if (!_uiState.value.saveSuccess) {
            val imageUri = _uiState.value.capturedImageUri ?: return
            // Only delete files stored in the app's internal directories (not user-selected)
            runCatching {
                val file = File(Uri.parse(imageUri).path ?: return)
                if (file.exists()) file.delete()
            }
        }
    }
}
