package com.minichain.minicamera

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.SessionConfiguration
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.view.Surface
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

class MainService : Service() {

  private lateinit var backgroundHandlerThread: HandlerThread
  private lateinit var backgroundHandler: Handler


  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      MainServiceAction.Start.toString() -> start()
      MainServiceAction.Stop.toString() -> stop()
    }
    return START_STICKY
  }

  enum class MainServiceAction {
    Start,
    Stop
  }

  private fun start() {
    Log.d("MAIN_SERVICE", "Main service starting...")

    backgroundHandlerThread = HandlerThread("CameraVideoThread")
    backgroundHandlerThread.start()
    backgroundHandler = Handler(backgroundHandlerThread.looper)

    startForeground()

    val cameraManager: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    cameraManager.getBackCameraId()?.let { backCameraId ->
      val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
          Log.d("MAIN_SERVICE", "Camera opened")

          val surfaceTexture = SurfaceTexture(false)
          val surface = Surface(surfaceTexture)

//          camera.createCaptureSession(
//            SessionConfiguration(
//              listOf(surface),
//              object : CameraCaptureSession.StateCallback() {
//                override fun onConfigured(session: CameraCaptureSession) {
//
//                }
//
//                override fun onConfigureFailed(session: CameraCaptureSession) {
//
//                }
//              },
//              backgroundHandler
//            )
//          )
        }

        override fun onDisconnected(camera: CameraDevice) {
          Log.d("MAIN_SERVICE", "Camera disconnected")
        }

        override fun onError(camera: CameraDevice, error: Int) {
          Log.d("MAIN_SERVICE", "Camera error. Error: $error")
        }
      }

      if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
        cameraManager.openCamera(backCameraId, cameraStateCallback, backgroundHandler)
      }
    }
  }

  private fun CameraManager.getBackCameraId(): String? {
    cameraIdList.forEach { cameraId ->
      val cameraCharacteristics = getCameraCharacteristics(cameraId)
      if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
        return cameraId
      }
    }
    return null
  }

  private fun stop() {
    Log.d("MAIN_SERVICE", "Main service stopping...")
    backgroundHandlerThread.quitSafely()
    backgroundHandlerThread.join()
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
}