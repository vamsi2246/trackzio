package com.weathersnap.ui.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.weathersnap.utils.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject

data class CameraUiState(
    val capturedUri: Uri? = null,
    val originalSize: Long = 0L,
    val compressedSize: Long = 0L,
    val isCapturing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var imageCapture: ImageCapture? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    fun startCamera(context: Context, lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val cameraProvider = future.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera binding failed", e)
                _uiState.update { it.copy(error = "Camera failed to start: ${e.message}") }
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun capturePhoto(context: Context) {
        val capture = imageCapture ?: return
        _uiState.update { it.copy(isCapturing = true, error = null) }

        val rawFile = File(context.cacheDir, "ws_raw_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(rawFile).build()

        capture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val originalSize = rawFile.length()
                    val compressed   = ImageCompressor.compressImage(context, Uri.fromFile(rawFile))

                    // Delete raw file — we only keep the compressed version
                    runCatching { rawFile.delete() }

                    if (compressed != null) {
                        _uiState.update {
                            it.copy(
                                capturedUri    = Uri.fromFile(compressed),
                                originalSize   = originalSize,
                                compressedSize = compressed.length(),
                                isCapturing    = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                capturedUri = Uri.fromFile(rawFile),
                                originalSize = originalSize,
                                isCapturing  = false,
                                error        = "Compression failed; using original"
                            )
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Capture failed", exception)
                    runCatching { rawFile.delete() }
                    _uiState.update {
                        it.copy(error = "Capture failed: ${exception.message}", isCapturing = false)
                    }
                }
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraViewModel"
    }
}
