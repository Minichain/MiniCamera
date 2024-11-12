package com.minichain.minicamera

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CameraPageContent(
  modifier: Modifier,
  applicationContext: Context
) {
  Log.d("CAMERA_PREVIEW", "Composable created")
  val viewModel = MainViewModel(applicationContext)
  Box(
    modifier = modifier.fillMaxSize()
  ) {
    CameraPreview(
      viewModel = viewModel
    )
    StartStopButton(
      viewModel = viewModel,
      modifier = Modifier.align(Alignment.BottomCenter)
    )
  }
}

@Composable
fun CameraPreview(
  viewModel: MainViewModel
) {
  val context = LocalContext.current
  val cameraPreview by viewModel.cameraPreviewStateFlow.collectAsStateWithLifecycle()
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

@Composable
fun StartStopButton(
  viewModel: MainViewModel,
  modifier: Modifier
) {
  FloatingActionButton(
    modifier = modifier,
    onClick = {
      Log.d("MAIN_ACTIVITY", "Start/Stop button clicked")
    }
  ) {
    Icon(
      imageVector = Icons.Default.PlayArrow,
      contentDescription = ""
    )
  }
}
