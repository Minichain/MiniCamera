package com.minichain.minicamera

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow

class App : Application() {

  companion object {
    val VIDEO_RESOLUTION = VideoResolution.RESOLUTION_640_480
  }

  val cameraPreview: SurfaceTexture = SurfaceTexture(false).apply {
    setDefaultBufferSize(VIDEO_RESOLUTION.width, VIDEO_RESOLUTION.height)
  }

  val videoStatus = MutableStateFlow(VideoStatus.Stopped)

  override fun onCreate() {
    Log.d("APP", "Application running...")
    super.onCreate()
    val channel = NotificationChannel(
      "foreground_service_notification_channel",
      "Running Notification",
      NotificationManager.IMPORTANCE_HIGH
    )
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
  }
}
