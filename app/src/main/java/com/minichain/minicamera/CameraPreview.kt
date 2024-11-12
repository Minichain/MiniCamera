package com.minichain.minicamera

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CameraPreview(
  modifier: Modifier,
  viewModel: MainViewModel
) {
  Log.d("CAMERA_PREVIEW", "Composable created")
  Box(
    modifier = Modifier.fillMaxSize()
  ) {
    val context = LocalContext.current
    val cameraPreview by viewModel.cameraPreview.collectAsStateWithLifecycle()
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
}

class CustomTextureView(context: Context) : TextureView(context) {
  init {
    surfaceTextureListener = object : SurfaceTextureListener {
      override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {

      }

      override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

      }

      override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
      }

      override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

      }
    }
  }
}