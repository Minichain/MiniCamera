package com.amazonaws.kinesisvideo.internal.client.mediasource

import com.amazonaws.kinesisvideo.producer.StreamInfo.NalAdaptationFlags

open class CustomMediaSourceConfiguration(private val mBuilder: Builder) : MediaSourceConfiguration {

    class Builder : MediaSourceConfiguration.Builder<CustomMediaSourceConfiguration> {
        var mimeType: String? = null
            private set
        var frameRate = 0
            private set
        var encodingBitrate = 0
            private set
        var isEncoderHardwareAccelerated = false
            private set
        var gopDurationMillis = 0
            private set
        var codecPrivateData: ByteArray? = null
            private set
        var frameTimescale: Long = 0
            private set
        var nalAdaptationFlags: NalAdaptationFlags? = null
            private set

        var mIsAbsoluteTimeCode = false
        var mRetentionPeriodInHours = 0

        fun withEncodingMimeType(mimeType: String?): Builder {
            this.mimeType = mimeType
            return this
        }

        fun withRetentionPeriodInHours(retentionPeriodInHours: Int): Builder {
            mRetentionPeriodInHours = retentionPeriodInHours
            return this
        }

        fun withFrameRate(frameRate: Int): Builder {
            this.frameRate = frameRate
            return this
        }

        fun withCodecPrivateData(privateData: ByteArray): Builder {
            codecPrivateData = privateData
            return this
        }

        fun withFrameTimeScale(timescale: Long): Builder {
            frameTimescale = timescale
            return this
        }

        fun withGopDurationMillis(gopDuration: Int): Builder {
            gopDurationMillis = gopDuration
            return this
        }

        fun withNalAdaptationFlags(nalAdaptationFlags: NalAdaptationFlags?): Builder {
            this.nalAdaptationFlags = nalAdaptationFlags
            return this
        }

        fun withIsAbsoluteTimecode(isAbsoluteTimecode: Boolean): Builder {
            mIsAbsoluteTimeCode = isAbsoluteTimecode
            return this
        }

        fun getmRetentionPeriodInHours(): Int {
            return mRetentionPeriodInHours
        }

        override fun build(): CustomMediaSourceConfiguration {
            return CustomMediaSourceConfiguration(this)
        }
    }

    override fun getMediaSourceType(): String {
        return MEDIA_SOURCE_TYPE
    }

    override fun getMediaSourceDescription(): String {
        return MEDIA_SOURCE_DESCRIPTION
    }

    /**
     * Gets the frame rate of the camera.
     */
    val frameRate: Int
        get() = mBuilder.frameRate

    /**
     * Gets the retention period in hours
     */
    val retentionPeriodInHours: Int
        get() = mBuilder.mRetentionPeriodInHours

    /**
     * Gets the encoding bitrate.
     */
    val bitRate: Int
        get() = mBuilder.encodingBitrate

    /**
     * Gets the encoder mime type.
     */
    val encoderMimeType: String
        get() = mBuilder.mimeType!!

    /**
     * Gets the GOP (group-of-pictures) duration in milliseconds.
     */
    val gopDurationMillis: Int
        get() = mBuilder.gopDurationMillis

    /**
     * Gets the codec private data.
     */
    val codecPrivateData: ByteArray?
        get() = mBuilder.codecPrivateData

    /**
     * Gets the timescale
     */
    val timeScale: Long
        get() = mBuilder.frameTimescale

    /**
     * Get the Nal Adaption Flag
     */
    val nalAdaptationFlags: NalAdaptationFlags?
        get() = mBuilder.nalAdaptationFlags

    /**
     * Get if timecode is absolute or not
     * @return
     */
    val isAbsoluteTimecode: Boolean
        get() = mBuilder.mIsAbsoluteTimeCode

    companion object {
        private const val MEDIA_SOURCE_DESCRIPTION = "Configuration for a camera media source"
        const val MEDIA_SOURCE_TYPE = "AbstractCameraMediaSource"
        fun builder(): Builder {
            return Builder()
        }
    }

}