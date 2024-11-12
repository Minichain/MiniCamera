package com.minichain.minicamera

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.TextureView

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
