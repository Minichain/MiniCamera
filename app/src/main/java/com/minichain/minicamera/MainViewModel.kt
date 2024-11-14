package com.minichain.minicamera

import android.graphics.SurfaceTexture
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel(private val applicationContext: App) : ViewModel() {

  val cameraPreviewStateFlow = MutableStateFlow<SurfaceTexture?>(null)

  val videoStatusStateFlow = MutableStateFlow(VideoStatus.Stopped)

  init {
    viewModelScope.launch {
      applicationContext.cameraPreview.let { cameraPreview ->
        cameraPreviewStateFlow.emit(cameraPreview)
      }

      applicationContext.videoStatus
        .onEach { status ->
          videoStatusStateFlow.emit(status)
        }
        .launchIn(this)
    }
  }

  fun updateVideoStatus(videoStatus: VideoStatus) {
    Log.d("MAIN_VIEW_MODEL", "Update Video Status to $videoStatus")
    (applicationContext as App).videoStatus.tryEmit(videoStatus)
  }
}