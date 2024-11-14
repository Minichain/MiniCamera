package com.amazonaws.mobileconnectors.kinesisvideo.mediasource.android

import android.util.Log
import com.amazonaws.kinesisvideo.client.mediasource.MediaSourceState
import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException
import com.amazonaws.kinesisvideo.internal.client.mediasource.MediaSource
import com.amazonaws.kinesisvideo.internal.client.mediasource.MediaSourceConfiguration
import com.amazonaws.kinesisvideo.internal.client.mediasource.MediaSourceSink
import com.amazonaws.kinesisvideo.producer.*
import com.amazonaws.kinesisvideo.util.StreamInfoConstants
import com.amazonaws.kinesisvideo.internal.client.mediasource.CustomMediaSourceConfiguration
import kotlin.jvm.Throws

/**
 * Android camera wrapper
 */
class CustomMediaSource(private val mStreamName: String) : MediaSource {
    private var mMediaSourceState: MediaSourceState? = null
    private var mMediaSourceConfiguration: CustomMediaSourceConfiguration? = null
    private var mMediaSourceSink: MediaSourceSink? = null

    override fun getMediaSourceState(): MediaSourceState {
        return mMediaSourceState!!
    }

    override fun getConfiguration(): MediaSourceConfiguration {
        return mMediaSourceConfiguration!!
    }

    @Throws(KinesisVideoException::class)
    override fun getStreamInfo(): StreamInfo {
        // Need to fix-up the content type as the Console playback only accepts video/h264 and will fail
        // if the mime type is video/avc which is the default in Android.
        var contentType = mMediaSourceConfiguration!!.encoderMimeType
        if (contentType == "video/avc") {
            contentType = "video/h264"
        }
        return StreamInfo(StreamInfoConstants.VERSION_ZERO,
                mStreamName,
                StreamInfo.StreamingType.STREAMING_TYPE_REALTIME,
                contentType,
                StreamInfoConstants.NO_KMS_KEY_ID, mMediaSourceConfiguration!!.retentionPeriodInHours
                * Time.HUNDREDS_OF_NANOS_IN_AN_HOUR,
                StreamInfoConstants.NOT_ADAPTIVE,
                StreamInfoConstants.MAX_LATENCY,
                StreamInfoConstants.DEFAULT_GOP_DURATION,
                StreamInfoConstants.KEYFRAME_FRAGMENTATION,
                StreamInfoConstants.SDK_GENERATES_TIMECODES,
                mMediaSourceConfiguration!!.isAbsoluteTimecode,
                StreamInfoConstants.REQUEST_FRAGMENT_ACKS,
                StreamInfoConstants.RECOVER_ON_FAILURE,
                StreamInfo.codecIdFromContentType(mMediaSourceConfiguration!!.encoderMimeType),
                StreamInfo.createTrackName(mMediaSourceConfiguration!!.encoderMimeType),
                mMediaSourceConfiguration!!.bitRate,
                mMediaSourceConfiguration!!.frameRate,
                StreamInfoConstants.DEFAULT_BUFFER_DURATION,
                StreamInfoConstants.DEFAULT_REPLAY_DURATION,
                StreamInfoConstants.DEFAULT_STALENESS_DURATION,
                mMediaSourceConfiguration!!.timeScale / Time.NANOS_IN_A_TIME_UNIT,
                StreamInfoConstants.RECALCULATE_METRICS,
                mMediaSourceConfiguration!!.codecPrivateData, arrayOf(
                Tag("device", "Test Device"),
                Tag("stream", "Test Stream")),
                mMediaSourceConfiguration!!.nalAdaptationFlags!!)
    }

    @Throws(KinesisVideoException::class)
    override fun initialize(mediaSourceSink: MediaSourceSink) {
        mMediaSourceSink = mediaSourceSink
        mMediaSourceState = MediaSourceState.INITIALIZED
    }

    override fun configure(configuration: MediaSourceConfiguration) {
        require(configuration is CustomMediaSourceConfiguration) {
            ("expected instance of CameraMediaSourceConfiguration"
                    + ", received " + configuration)
        }
        mMediaSourceConfiguration = configuration
    }

    @Throws(KinesisVideoException::class)
    override fun start() {
        mMediaSourceState = MediaSourceState.RUNNING
    }

    @Throws(KinesisVideoException::class)
    override fun stop() {
        mMediaSourceState = MediaSourceState.STOPPED
    }

    override fun isStopped(): Boolean {
        return mMediaSourceState == MediaSourceState.STOPPED
    }

    @Throws(KinesisVideoException::class)
    override fun free() {
    }

    override fun getStreamCallbacks(): StreamCallbacks? {
        return null
    }

    fun updateSinkWithPrivateData(privateData: ByteArray) {
        try {
            Log.d("CustomKinesisLog", "updating sink with codec private data")
            mMediaSourceSink!!.onCodecPrivateData(privateData)
        } catch (e: KinesisVideoException) {
            Log.d("CustomKinesisLog", "error updating sink with codec private data $e")
            throw RuntimeException("error updating sink with codec private data $e")
        }
    }

    fun pushFrameToSink(frame: KinesisVideoFrame) {
        try {
            mMediaSourceSink!!.onFrame(frame)
        } catch (e: KinesisVideoException) {
            Log.d("CustomKinesisLog", "Exception when pushing frame to sink: ${e.stackTrace}")
        }
    }

    override fun getMediaSourceSink(): MediaSourceSink {
        return mMediaSourceSink!!
    }
}