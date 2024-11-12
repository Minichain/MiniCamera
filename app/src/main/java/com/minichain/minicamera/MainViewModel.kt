package com.minichain.minicamera

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel(applicationContext: Context) : ViewModel() {

  val cameraPreviewStateFlow = MutableStateFlow<SurfaceTexture?>(null)

  init {
    viewModelScope.launch {
      (applicationContext as App).cameraPreview.let { cameraPreview ->
        cameraPreviewStateFlow.emit(cameraPreview)
        cameraPreview.setOnFrameAvailableListener {
          Log.d("CAMERA_PREVIEW", "Camera preview on frame available")
        }
      }
    }
  }
}