package com.amazonaws.mobileconnectors.kinesisvideo.mediasource.android

import android.content.Context
import com.amazonaws.kinesisvideo.internal.client.mediasource.MediaSource
import com.amazonaws.kinesisvideo.internal.client.mediasource.MediaSourceConfiguration

object CustomAndroidMediaSourceFactory {

    fun createMediaSource(streamName: String, context: Context, configuration: MediaSourceConfiguration): MediaSource {
        return createAndroidMediaSource(streamName, context, configuration)
    }

    private fun createAndroidMediaSource(streamName: String, context: Context, configuration: MediaSourceConfiguration): CustomMediaSource {
        val mediaSource = CustomMediaSource(streamName)
        mediaSource.configure(configuration)
        return mediaSource
    }
}