package com.minichain.minicamera

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.params.OutputConfiguration
import android.os.Build
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


    openCameraAndStartCapturing()
  }

  private fun openCameraAndStartCapturing() {
    val cameraManager: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    cameraManager.getBackCameraId()?.let { backCameraId ->
      val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
          Log.d("MAIN_SERVICE", "Camera opened")
          createCaptureSession(camera)
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

  private fun createCaptureSession(camera: CameraDevice) {
    Log.d("MAIN_SERVICE", "Let's create capture session")
    val cameraPreview = (application as App).cameraPreview
    val surface = Surface(cameraPreview)
    Log.d("MAIN_SERVICE", "Is App.cameraPreview released? ${cameraPreview.isReleased}")
    Log.d("MAIN_SERVICE", "Is surface valid? ${surface.isValid}")
    val surfaceTargets = listOf(surface)

    val cameraCaptureSessionCallback = object : CameraCaptureSession.StateCallback() {
      override fun onConfigured(session: CameraCaptureSession) {
        Log.d("MAIN_SERVICE", "Camera capture session configured!")
        val captureRequest = session.device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequest.addTarget(surface)
        session.setRepeatingRequest(captureRequest.build(), null, null)
      }

      override fun onConfigureFailed(session: CameraCaptureSession) {
        Log.d("MAIN_SERVICE", "Camera capture session configuration failed :(")
      }

      override fun onClosed(session: CameraCaptureSession) {
        super.onClosed(session)
        Log.d("MAIN_SERVICE", "Camera capture session closed")
      }

      override fun onSurfacePrepared(session: CameraCaptureSession, surface: Surface) {
        super.onSurfacePrepared(session, surface)
        Log.d("MAIN_SERVICE", "Camera capture session surface prepared")
      }
    }

    val outputConfigurations = mutableListOf<OutputConfiguration>()
    surfaceTargets.forEach {
      val config = OutputConfiguration(it)
      if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
        config.streamUseCase = CameraMetadata.SCALER_AVAILABLE_STREAM_USE_CASES_DEFAULT.toLong()
      }
      outputConfigurations.add(config)
    }

    Log.d("MAIN_SERVICE", "Creating capture session...")
    camera.createCaptureSession(
      surfaceTargets,
      cameraCaptureSessionCallback,
      null
    )
//    val sessionConfiguration = SessionConfiguration(
//      SessionConfiguration.SESSION_REGULAR,
//      outputConfigurations,
//      { Log.d("MAIN_SERVICE", "Camera session executor") },
//      cameraCaptureSessionCallback
//    )
//    camera.createCaptureSession(sessionConfiguration)
    Log.d("MAIN_SERVICE", "Capture session created!")
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