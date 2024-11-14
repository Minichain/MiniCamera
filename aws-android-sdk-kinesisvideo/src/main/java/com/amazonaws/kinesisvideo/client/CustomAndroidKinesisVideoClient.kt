package com.amazonaws.kinesisvideo.client

import android.content.Context
import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException
import com.amazonaws.kinesisvideo.common.logging.Log
import com.amazonaws.kinesisvideo.internal.client.NativeKinesisVideoClient
import com.amazonaws.kinesisvideo.internal.client.mediasource.MediaSource
import com.amazonaws.kinesisvideo.internal.client.mediasource.MediaSourceConfiguration
import com.amazonaws.kinesisvideo.internal.producer.client.KinesisVideoServiceClient
import com.amazonaws.mobileconnectors.kinesisvideo.util.CameraUtils
import com.amazonaws.mobileconnectors.kinesisvideo.mediasource.android.CustomAndroidMediaSourceFactory
import java.util.concurrent.ScheduledExecutorService
import kotlin.jvm.Throws

class CustomAndroidKinesisVideoClient(log: Log, private val mContext: Context, configuration: KinesisVideoClientConfiguration,
                                serviceClient: KinesisVideoServiceClient, executor: ScheduledExecutorService)
    : NativeKinesisVideoClient(log, configuration, serviceClient, executor) {

    @Throws(KinesisVideoException::class)
    override fun createMediaSource(streamName: String, mediaSourceConfiguration: MediaSourceConfiguration): MediaSource {
        val mediaSource = CustomAndroidMediaSourceFactory.createMediaSource(streamName, mContext, mediaSourceConfiguration)
        registerMediaSource(mediaSource)
        return mediaSource
    }

    override fun listSupportedConfigurations(): List<MediaSourceConfiguration.Builder<out MediaSourceConfiguration>> {
        return CameraUtils.getSupportedCameraConfigrations(mContext)
    }
}