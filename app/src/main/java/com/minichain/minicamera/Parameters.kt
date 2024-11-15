package com.minichain.minicamera

import android.media.MediaFormat

object Parameters {

  /** Video **/
  const val VIDEO_FRAME_RATE = 24
  const val RETENTION_PERIOD = 48
  const val KEY_FRAME_INTERVAL = 2
  const val VIDEO_CODEC = MediaFormat.MIMETYPE_VIDEO_AVC
  val VIDEO_RESOLUTION = VideoResolution.RESOLUTION_640_480
  var VIDEO_BIT_RATE = VIDEO_RESOLUTION.width * VIDEO_RESOLUTION.height * VIDEO_FRAME_RATE * 0.15

  /** Audio **/
  const val AUDIO_SAMPLE_RATE = 44100
  const val AUDIO_CHANNELS = 2
  const val AUDIO_BIT_RATE = 64000
  const val AUDIO_CODEC = MediaFormat.MIMETYPE_AUDIO_AAC

}