package com.minichain.minicamera

import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import android.media.MediaCodecInfo
import android.media.MediaFormat
import java.io.IOException

class CustomMediaCodec {

  private var videoMediaCodec: MediaCodec? = null
  private var videoMediaFormat: MediaFormat? = null

  init {
    videoMediaFormat = MediaFormat.createVideoFormat(Parameters.VIDEO_CODEC, Parameters.VIDEO_RESOLUTION.width, Parameters.VIDEO_RESOLUTION.height).apply {
      setInteger(MediaFormat.KEY_BIT_RATE, Parameters.VIDEO_BIT_RATE.toInt())
      setInteger(MediaFormat.KEY_FRAME_RATE, Parameters.VIDEO_FRAME_RATE)
      setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
      setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, Parameters.KEY_FRAME_INTERVAL)
    }
  }

  fun start() {
    try {
      if (videoMediaCodec == null) {
        videoMediaCodec = MediaCodec.createEncoderByType(Parameters.VIDEO_CODEC).apply {
          configure(videoMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
          start()
        }
      }
    } catch (e: IOException) {

    }
  }

  fun stop() {
    videoMediaCodec?.stop()
    videoMediaCodec?.release()
    videoMediaCodec = null
  }

  fun encodeVideo(
    rawData: ByteArray,
    onVideoEncoded: (bufferInfo: BufferInfo, outData: ByteArray) -> Unit
  ) {
    videoMediaCodec?.apply {
      try {
        val inputBufferIndex: Int = dequeueInputBuffer(500)
        if (inputBufferIndex >= 0) {
          getInputBuffer(inputBufferIndex)?.clear()
          getInputBuffer(inputBufferIndex)?.put(rawData)
          queueInputBuffer(inputBufferIndex, 0, rawData.size, nanosSinceFirstFrame() / 1000, 0)
        }
        val bufferInfo = BufferInfo()
        var outputBufferIndex: Int = dequeueOutputBuffer(bufferInfo, 0)
        while (outputBufferIndex >= 0) {
          val outData = ByteArray(bufferInfo.size)
          getOutputBuffer(outputBufferIndex)?.get(outData)

          onVideoEncoded(bufferInfo, outData)

          releaseOutputBuffer(outputBufferIndex, false)
          outputBufferIndex = dequeueOutputBuffer(bufferInfo, 0)
        }
      } catch (t: Throwable) {
        t.printStackTrace()
      }
    }
  }

  private var firstFrameTimestamp: Long = -1

  private fun nanosSinceFirstFrame(): Long {
    val currentTime = System.currentTimeMillis()
    if (firstFrameTimestamp < 0) {
      firstFrameTimestamp = currentTime
    }
    return (currentTime - firstFrameTimestamp) * 1000000
  }

  companion object {
    fun isEndOfStream(bufferInfo: BufferInfo): Boolean {
      return (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
    }

    fun isCodecPrivateData(bufferInfo: BufferInfo): Boolean {
      return (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0
    }
  }
}
