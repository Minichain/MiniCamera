package com.minichain.minicamera

import android.content.Context
import android.media.MediaCodec
import android.util.Log
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.internal.StaticCredentialsProvider
import com.amazonaws.kinesisvideo.client.KinesisVideoClient
import com.amazonaws.kinesisvideo.internal.client.mediasource.CustomMediaSourceConfiguration
import com.amazonaws.kinesisvideo.producer.StreamInfo
import com.amazonaws.mobileconnectors.kinesisvideo.client.CustomKinesisVideoAndroidClientFactory
import com.amazonaws.mobileconnectors.kinesisvideo.mediasource.android.CustomMediaSource
import com.amazonaws.mobileconnectors.kinesisvideo.util.CustomFrameUtility
import com.amazonaws.regions.Regions
import java.nio.ByteBuffer

class VideoStreamingManager(
  private val context: Context
) {

  private var kinesisVideoClient: KinesisVideoClient? = null
  private var customMediaSource: CustomMediaSource? = null
  private var customMediaSourceFrameIndex = 0

  fun startVideoStream() {
    val credentialsProvider = StaticCredentialsProvider(
      BasicSessionCredentials(
        BuildConfig.awsAccessKey,
        BuildConfig.awsSecretKey,
        BuildConfig.sessionToken
      )
    )

    kinesisVideoClient = CustomKinesisVideoAndroidClientFactory.createKinesisVideoClient(
      context,
      Regions.fromName(BuildConfig.regionName),
      credentialsProvider
    )?.also { videoClient ->
      customMediaSource = createCustomMediaSource(videoClient, BuildConfig.streamName)
    }
  }

  private fun createCustomMediaSource(
    kinesisVideoClient: KinesisVideoClient,
    streamName: String
  ): CustomMediaSource {
    val mediaSourceConfiguration = CustomMediaSourceConfiguration(
      CustomMediaSourceConfiguration.builder()
        .withFrameRate(Parameters.VIDEO_FRAME_RATE)
        .withRetentionPeriodInHours(Parameters.RETENTION_PERIOD)
        .withNalAdaptationFlags(StreamInfo.NalAdaptationFlags.NAL_ADAPTATION_ANNEXB_CPD_AND_FRAME_NALS)
        .withEncodingMimeType(Parameters.VIDEO_CODEC)
        .withIsAbsoluteTimecode(false)
    )
    val customMediaSource: CustomMediaSource = kinesisVideoClient.createMediaSource(
      streamName,
      mediaSourceConfiguration
    ) as CustomMediaSource
    customMediaSource.start()
    return customMediaSource
  }

  private var lastFrameTimeStamp: Long = 0
  private val frameDuration = 1000 / Parameters.VIDEO_FRAME_RATE.toLong()

  fun putFrame(bufferInfo: MediaCodec.BufferInfo, data: ByteArray) {
    val byteBuffer = ByteBuffer.wrap(data)
    when {
      CustomMediaCodec.isCodecPrivateData(bufferInfo) -> {
        Log.d("VIDEO_STREAMING_MANAGER", "Data from encoder: Codec Private Data")
        customMediaSource?.updateSinkWithPrivateData(data)
      }
      CustomMediaCodec.isEndOfStream(bufferInfo) -> {
        Log.d("VIDEO_STREAMING_MANAGER", "Data from encoder: End of Stream")
        stopVideoStream()
      }
      else -> {
        var currentTime = System.currentTimeMillis()
        // If frame comes too early, we wait until the precise moment to push it.
        while ((currentTime - lastFrameTimeStamp) < frameDuration) {
          currentTime = System.currentTimeMillis()
        }

        customMediaSource?.pushFrameToSink(
          CustomFrameUtility.createFrame(
            bufferInfo,
            currentTime,
            frameDuration,
            customMediaSourceFrameIndex++,
            byteBuffer
          )
        )

        lastFrameTimeStamp = currentTime
      }
    }
  }

  fun stopVideoStream() {
    customMediaSource?.stop()
    customMediaSource = null
    kinesisVideoClient?.stopAllMediaSources()
    kinesisVideoClient?.free()
    kinesisVideoClient = null
    CustomKinesisVideoAndroidClientFactory.freeKinesisVideoClient()
  }
}