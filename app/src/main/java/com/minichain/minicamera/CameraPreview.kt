package com.minichain.minicamera

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CameraPageContent(
  modifier: Modifier,
  applicationContext: Context
) {
  Log.d("CAMERA_PREVIEW", "Composable created")
  val viewModel = MainViewModel(applicationContext as App)
  Box(
    modifier = modifier.fillMaxSize()
  ) {
    CameraPreview(
      viewModel = viewModel,
      modifier = Modifier.align(Alignment.Center)
    )
    StartStopButton(
      viewModel = viewModel,
      modifier = Modifier.align(Alignment.BottomCenter)
    )
  }
}

@Composable
fun CameraPreview(
  viewModel: MainViewModel,
  modifier: Modifier
) {
  val context = LocalContext.current
  val cameraPreview by viewModel.cameraPreviewStateFlow.collectAsStateWithLifecycle()
  val videoStatus by viewModel.videoStatusStateFlow.collectAsStateWithLifecycle()
  when (videoStatus) {
    VideoStatus.Stopped -> {
      Text(
        modifier = modifier,
        text = "Video Preview"
      )
    }
    VideoStatus.Started -> {
      cameraPreview?.let { cameraPreviewNotNull ->
        AndroidView(
          factory = {
            CustomTextureView(context).apply {
              setSurfaceTexture(cameraPreviewNotNull)
            }
          }
        )
      }
    }
    else -> {
      CircularProgressIndicator(
        modifier = modifier
      )
    }
  }
}

@Composable
fun StartStopButton(
  viewModel: MainViewModel,
  modifier: Modifier
) {
  val videoStatus by viewModel.videoStatusStateFlow.collectAsStateWithLifecycle()
  FloatingActionButton(
    modifier = modifier.padding(12.dp),
    onClick = {
      Log.d("MAIN_ACTIVITY", "Start/Stop button clicked")
      when (videoStatus) {
        VideoStatus.Stopped -> viewModel.updateVideoStatus(VideoStatus.Starting)
        VideoStatus.Started -> viewModel.updateVideoStatus(VideoStatus.Stopping)
        else -> { /* Nothing */ }
      }
    }
  ) {
    Icon(
      imageVector = when (videoStatus) {
        VideoStatus.Stopped -> Icons.Default.PlayArrow
        VideoStatus.Started,
        VideoStatus.Starting,
        VideoStatus.Stopping -> Icons.Default.Close
      },
      contentDescription = ""
    )
  }
}
