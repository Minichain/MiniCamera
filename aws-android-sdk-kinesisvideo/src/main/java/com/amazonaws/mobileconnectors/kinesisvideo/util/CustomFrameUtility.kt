package com.amazonaws.mobileconnectors.kinesisvideo.util

import android.media.MediaCodec
import com.amazonaws.kinesisvideo.producer.KinesisVideoFrame
import java.nio.ByteBuffer

object CustomFrameUtility {
    private const val FRAME_FLAG_KEY_FRAME = 1
    private const val FRAME_FLAG_NONE = 0
    private const val HUNDREDS_OF_NANOS_IN_MS: Long = 10 * 1000;

    fun createFrame(bufferInfo: MediaCodec.BufferInfo, timeStamp: Long, frameDuration: Long, frameIndex: Int, encodedFrameData: ByteBuffer?): KinesisVideoFrame {
        val flags = if (isKeyFrame(bufferInfo)) FRAME_FLAG_KEY_FRAME else FRAME_FLAG_NONE
        return KinesisVideoFrame(
            frameIndex,
            flags,
            timeStamp * HUNDREDS_OF_NANOS_IN_MS,
            timeStamp * HUNDREDS_OF_NANOS_IN_MS,
            frameDuration * 1000,
            encodedFrameData!!)
    }

    private fun isKeyFrame(bufferInfo: MediaCodec.BufferInfo): Boolean {
        return bufferInfo.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME != 0
    }
}