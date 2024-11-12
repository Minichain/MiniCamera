package com.minichain.minicamera

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel(applicationContext: Context) : ViewModel() {

  val cameraPreview = MutableStateFlow<SurfaceTexture?>(null)

  init {
    viewModelScope.launch {
      cameraPreview.emit((applicationContext as App).cameraPreview)
//      (applicationContext as App).cameraPreview.setOnFrameAvailableListener {
//        Log.d("CAMERA_PREVIEW", "Camera preview on frame available")
//      }
    }
  }
}