package com.minichain.minicamera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
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
import android.util.Log
import android.util.Range
import android.view.Surface
import androidx.core.app.ActivityCompat

class CameraManager(
  private val context: Context,
  private val preview: SurfaceTexture,
  val onFrameAvailable: (frame: ByteArray) -> Unit
) {

  private lateinit var backgroundHandlerThread: HandlerThread
  private lateinit var backgroundHandler: Handler

  private var camera: CameraDevice? = null
  private var session: CameraCaptureSession? = null
  private var imageReader: ImageReader = ImageReader.newInstance(
    Parameters.VIDEO_RESOLUTION.width,
    Parameters.VIDEO_RESOLUTION.height,
    ImageFormat.YUV_420_888,
    1
  )

  fun openCameraAndStartSession() {

    backgroundHandlerThread = HandlerThread("CameraVideoThread")
    backgroundHandlerThread.start()
    backgroundHandler = Handler(backgroundHandlerThread.looper)

    val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    cameraManager.addAvailabilityCallback()

    ImageReader.newInstance(
      Parameters.VIDEO_RESOLUTION.width,
      Parameters.VIDEO_RESOLUTION.height,
      ImageFormat.YUV_420_888,
      1
    ).apply {
      imageReader = this
      setOnImageAvailableListener(
        {
          val frame = it.acquireLatestImage()
          Log.d("MAIN_SERVICE", "Frame available!")
          onFrameAvailable(ImageUtils.yuv420888toNV21(frame))
          frame.close()
        },
        backgroundHandler
      )
    }

    cameraManager.getBackCameraId()?.let { backCameraId ->
      val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
          Log.d("MAIN_SERVICE", "Camera opened")
          this@CameraManager.camera = camera
          createCaptureSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
          Log.d("MAIN_SERVICE", "Camera disconnected")
        }

        override fun onError(camera: CameraDevice, error: Int) {
          Log.d("MAIN_SERVICE", "Camera error. Error: $error")
        }
      }

      if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
        cameraManager.openCamera(backCameraId, cameraStateCallback, backgroundHandler)
      }
    }
  }

  private fun createCaptureSession() {
    Log.d("MAIN_SERVICE", "Let's create capture session")
    val surface1 = Surface(preview)
    val surface2 = imageReader.surface
    val surfaceTargets = listOf(surface1, surface2)

    val cameraCaptureSessionCallback = object : CameraCaptureSession.StateCallback() {
      override fun onConfigured(session: CameraCaptureSession) {
        Log.d("MAIN_SERVICE", "Camera capture session configured!")
        this@CameraManager.session = session
        val captureRequest = session.device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        captureRequest.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(24, 24))
        captureRequest.addTarget(surface1)
        captureRequest.addTarget(surface2)
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
    val featureCameraExternal = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_EXTERNAL)
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

  fun stopSessionAndCloseCamera() {
    session?.close()
    camera?.close()
    backgroundHandlerThread.quitSafely()
    backgroundHandlerThread.join()
  }
}