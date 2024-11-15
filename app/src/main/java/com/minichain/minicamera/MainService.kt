package com.minichain.minicamera

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.util.Range
import android.view.Surface
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.internal.StaticCredentialsProvider
import com.amazonaws.kinesisvideo.client.KinesisVideoClient
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.kinesisvideo.client.CustomKinesisVideoAndroidClientFactory
import com.amazonaws.regions.Regions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainService : Service() {

  private lateinit var backgroundHandlerThread: HandlerThread
  private lateinit var backgroundHandler: Handler
  private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  private var camera: CameraDevice? = null
  private var session: CameraCaptureSession? = null
  private var imageReader: ImageReader = ImageReader.newInstance(
    App.VIDEO_RESOLUTION.width, App.VIDEO_RESOLUTION.height, ImageFormat.YUV_420_888, 1
  )

  private var kinesisVideoClient: KinesisVideoClient? = null

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

    coroutineScope.startListeningToStartStopVideoRequests()

    coroutineScope.launch {

      val credentialsProvider = StaticCredentialsProvider(
        BasicSessionCredentials(
          "",
          "",
          ""
        )
      )

      kinesisVideoClient = CustomKinesisVideoAndroidClientFactory.createKinesisVideoClient(
        application,
        Regions.EU_WEST_1,
        credentialsProvider
      )
    }
  }

  private fun CoroutineScope.startListeningToStartStopVideoRequests() {
    (application as App).videoStatus
      .onEach { status ->
        when (status) {
          VideoStatus.Starting -> openCameraAndStartSession()
          VideoStatus.Stopping -> stopSessionAndCloseCamera()
          else -> { /* Nothing */ }
        }
      }
      .launchIn(this)
  }

  private fun stopSessionAndCloseCamera() {
    session?.close()
    camera?.close()
    (application as App).videoStatus.tryEmit(VideoStatus.Stopped)
  }

  private fun openCameraAndStartSession() {
    val cameraManager: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    cameraManager.addAvailabilityCallback()
    imageReader.apply {
      setOnImageAvailableListener(
        {
          val frame = it.acquireLatestImage()
          Log.d("MAIN_SERVICE", "Frame available!")
          frame.close()
        },
        backgroundHandler
      )
    }
    cameraManager.getBackCameraId()?.let { backCameraId ->
      val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
          Log.d("MAIN_SERVICE", "Camera opened")
          this@MainService.camera = camera
          createCaptureSession()
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

  private fun createCaptureSession() {
    Log.d("MAIN_SERVICE", "Let's create capture session")
    val surface1 = Surface((application as App).cameraPreview)
    val surface2 = imageReader.surface
    val surfaceTargets = listOf(surface1, surface2)

    val cameraCaptureSessionCallback = object : CameraCaptureSession.StateCallback() {
      override fun onConfigured(session: CameraCaptureSession) {
        Log.d("MAIN_SERVICE", "Camera capture session configured!")
        this@MainService.session = session
        val captureRequest = session.device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        captureRequest.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(24, 24))
        captureRequest.addTarget(surface1)
        captureRequest.addTarget(surface2)
        session.setRepeatingRequest(captureRequest.build(), null, null)
        (application as App).videoStatus.tryEmit(VideoStatus.Started)
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

    Log.d("MAIN_SERVICE", "Creating capture session...")
    camera?.createCaptureSession(surfaceTargets, cameraCaptureSessionCallback)
  }

  private fun CameraDevice.createCaptureSession(
    surfaceTargets: List<Surface>,
    cameraCaptureSessionCallback: CameraCaptureSession.StateCallback
  ) {
    val useOldApproach = true
    if (useOldApproach) {
      /** Old approach **/
      createCaptureSession(
        surfaceTargets,
        cameraCaptureSessionCallback,
        null
      )
    } else {
      /** New approach **/
      val outputConfigurations = mutableListOf<OutputConfiguration>()
      surfaceTargets.forEach {
        val config = OutputConfiguration(it)
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
          config.streamUseCase = CameraMetadata.SCALER_AVAILABLE_STREAM_USE_CASES_DEFAULT.toLong()
        }
        outputConfigurations.add(config)
      }
      val sessionConfiguration = SessionConfiguration(
        SessionConfiguration.SESSION_REGULAR,
        outputConfigurations,
        { Log.d("MAIN_SERVICE", "Camera session executor") },
        cameraCaptureSessionCallback
      )
      createCaptureSession(sessionConfiguration)
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

  private fun CameraManager.addAvailabilityCallback() {
    val featureCameraExternal = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_EXTERNAL)
    Log.d("MAIN_SERVICE", "featureCameraExternal: $featureCameraExternal")

    Log.d("MAIN_SERVICE", "Available cameras:")
    cameraIdList.forEachIndexed { index, cameraId ->
      val cameraCharacteristics = getCameraCharacteristics(cameraId)
      Log.d("MAIN_SERVICE", "Available camera[${index}]: Lens facing: ${cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)}")
    }

    val availabilityCallback = object : CameraManager.AvailabilityCallback() {
      override fun onCameraAvailable(cameraId: String) {
        super.onCameraAvailable(cameraId)
        Log.d("MAIN_SERVICE", "Camera available :) cameraId: $cameraId")
      }

      override fun onCameraUnavailable(cameraId: String) {
        super.onCameraUnavailable(cameraId)
        Log.d("MAIN_SERVICE", "Camera unavailable :( cameraId: $cameraId")
      }
    }
    registerAvailabilityCallback(availabilityCallback, backgroundHandler)
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