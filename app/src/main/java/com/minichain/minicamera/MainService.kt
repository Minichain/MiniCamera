package com.minichain.minicamera

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainService : Service() {

  private var customMediaCodec: CustomMediaCodec? = null
  private var cameraManager: CameraManager? = null
  private var videoStreamingManager: VideoStreamingManager? = null
  private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      MainServiceAction.Start.toString() -> start()
      MainServiceAction.Stop.toString() -> stop()
    }
    return START_STICKY
  }

  private fun start() {
    Log.d("MAIN_SERVICE", "Main service starting...")

    startForeground()

    coroutineScope.launch {

      customMediaCodec = CustomMediaCodec()

      cameraManager = CameraManager(
        context = applicationContext,
        preview = (application as App).cameraPreview,
        onFrameAvailable = { frame ->
          customMediaCodec?.encodeVideo(
            rawData = frame,
            onVideoEncoded = { bufferInfo, outData ->
              videoStreamingManager?.putFrame(bufferInfo, outData)
            }
          )
        }
      )

      videoStreamingManager = VideoStreamingManager(
        context = applicationContext
      )

      startListeningToStartStopVideoStreamRequests()
    }
  }

  private fun CoroutineScope.startListeningToStartStopVideoStreamRequests() {
    (application as App).videoStatus
      .onEach { status ->
        when (status) {
          VideoStatus.Starting -> {
            customMediaCodec?.start()
            cameraManager?.openCameraAndStartSession()
            videoStreamingManager?.startVideoStream()
            (application as App).videoStatus.tryEmit(VideoStatus.Started)
          }
          VideoStatus.Stopping -> {
            videoStreamingManager?.stopVideoStream()
            cameraManager?.stopSessionAndCloseCamera()
            customMediaCodec?.stop()
            (application as App).videoStatus.tryEmit(VideoStatus.Stopped)
          }
          else -> { /* Nothing */ }
        }
      }
      .launchIn(this)
  }

  private fun stop() {
    Log.d("MAIN_SERVICE", "Main service stopping...")
    stopSelf()
  }

  private fun startForeground() {
    Log.d("MAIN_SERVICE", "Starting foreground...")
    createNotification()
  }

  private fun createNotification() {
    Log.d("MAIN_SERVICE", "Creating notification...")
    val notification = NotificationCompat.Builder(this, "foreground_service_notification_channel")
      .setOngoing(true)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle("Foreground Service")
      .setContentText("Foreground Service running...")
      .build()
    ServiceCompat.startForeground(
      this,
      100,
      notification,
      ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
    )
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  enum class MainServiceAction {
    Start,
    Stop
  }
}