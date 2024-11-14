package com.amazonaws.mobileconnectors.kinesisvideo.client

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.kinesisvideo.auth.KinesisVideoCredentialsProvider
import com.amazonaws.kinesisvideo.client.CustomAndroidKinesisVideoClient
import com.amazonaws.kinesisvideo.client.KinesisVideoClient
import com.amazonaws.kinesisvideo.client.KinesisVideoClientConfiguration
import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException
import com.amazonaws.kinesisvideo.common.logging.Log
import com.amazonaws.kinesisvideo.common.logging.LogLevel
import com.amazonaws.kinesisvideo.common.logging.OutputChannel
import com.amazonaws.kinesisvideo.producer.DeviceInfo
import com.amazonaws.kinesisvideo.producer.StorageInfo
import com.amazonaws.kinesisvideo.producer.Tag
import com.amazonaws.kinesisvideo.storage.DefaultStorageCallbacks
import com.amazonaws.mobileconnectors.kinesisvideo.auth.KinesisVideoCredentialsProviderImpl
import com.amazonaws.mobileconnectors.kinesisvideo.service.KinesisVideoAndroidServiceClient
import com.amazonaws.mobileconnectors.kinesisvideo.util.AndroidLogOutputChannel
import com.amazonaws.regions.Regions
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import kotlin.jvm.Throws

class CustomKinesisVideoAndroidClientFactory {
    companion object {
        private const val DEVICE_VERSION = 0
        private const val TEN_STREAMS = 10
        private const val SPILL_RATIO_90_PERCENT = 90
        private const val MIN_STORAGE_SIZE_64_MEGS = 64 * 1024 * 1024.toLong()
        private const val MAX_STORAGE_SIZE_384_MEGS = 384 * 1024 * 1024.toLong()
        private const val TOTAL_MEMORY_RATIO = 0.9
        private const val DEVICE_NAME = "android-client-library"
        private val STORAGE_PATH = Environment.getExternalStorageDirectory().path
        private const val NUMBER_OF_THREADS_IN_POOL = 2
        private const val LOG_TAG = "KinesisVideoAndroidClient"

        /**
         * NOTE: This is just a sample code to produce a singleton-like instance. Any call to create a client with
         * a different configuration will result in an instance with another configuration returned.
         *
         * TODO: Fix the views properly so we don't need a factory here.
         */
        private var KINESIS_VIDEO_CLIENT_INSTANCE: KinesisVideoClient? = null

        /**
         * Create KinesisVideo client.
         *
         * @param context Android context to use
         * @param credentialsProvider Credentials provider
         * @return
         * @throws KinesisVideoException
         */
        @Throws(KinesisVideoException::class)
        fun createKinesisVideoClient(context: Context, credentialsProvider: AWSCredentialsProvider): KinesisVideoClient? {
            return createKinesisVideoClient(context, Regions.DEFAULT_REGION, credentialsProvider)
        }

        /**
         * Create KinesisVideo client.
         *
         * @param context Android context to use
         * @param regions Regions object
         * @param awsCredentialsProvider Credentials provider
         * @return
         * @throws KinesisVideoException
         */
        @Throws(KinesisVideoException::class)
        fun createKinesisVideoClient(context: Context, regions: Regions, awsCredentialsProvider: AWSCredentialsProvider): KinesisVideoClient? {
            val outputChannel: OutputChannel = AndroidLogOutputChannel()
            val log = Log(outputChannel, LogLevel.VERBOSE, LOG_TAG)
            val kinesisVideoCredentialsProvider: KinesisVideoCredentialsProvider = KinesisVideoCredentialsProviderImpl(awsCredentialsProvider, log)
            val configuration = KinesisVideoClientConfiguration.builder()
                    .withRegion(regions.getName())
                    .withCredentialsProvider(kinesisVideoCredentialsProvider)
                    .withLogChannel(outputChannel)
                    .withStorageCallbacks(DefaultStorageCallbacks())
                    .build()
            val executor = Executors.newScheduledThreadPool(NUMBER_OF_THREADS_IN_POOL)
            return createKinesisVideoClient(context, configuration, defaultDeviceInfo(context), log, executor)
        }

        /**
         * Create KinesisVideo client.
         */
        @Throws(KinesisVideoException::class)
        fun createKinesisVideoClient(context: Context, configuration: KinesisVideoClientConfiguration, deviceInfo: DeviceInfo,
                                     log: Log, executor: ScheduledExecutorService): KinesisVideoClient? {
            if (KINESIS_VIDEO_CLIENT_INSTANCE == null) {
                val serviceClient = KinesisVideoAndroidServiceClient(log)
                val kinesisVideoClient: KinesisVideoClient = CustomAndroidKinesisVideoClient(log, context, configuration, serviceClient, executor)
                kinesisVideoClient.initialize(deviceInfo)
                KINESIS_VIDEO_CLIENT_INSTANCE = kinesisVideoClient
            }
            return KINESIS_VIDEO_CLIENT_INSTANCE
        }

        @Throws(KinesisVideoException::class)
        fun freeKinesisVideoClient() {
            KINESIS_VIDEO_CLIENT_INSTANCE!!.free()
            KINESIS_VIDEO_CLIENT_INSTANCE = null
        }

        private fun defaultDeviceInfo(context: Context): DeviceInfo {
            return DeviceInfo(DEVICE_VERSION, DEVICE_NAME, defaultStorageInfo(context), TEN_STREAMS, defaultDeviceTags())
        }

        private fun defaultStorageInfo(context: Context): StorageInfo {
            return StorageInfo(0, StorageInfo.DeviceStorageType.DEVICE_STORAGE_TYPE_IN_MEM, defaultMemorySize(context), SPILL_RATIO_90_PERCENT, STORAGE_PATH)
        }

        private fun defaultMemorySize(context: Context): Long {
            val memoryInfo = ActivityManager.MemoryInfo()
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    ?: return MIN_STORAGE_SIZE_64_MEGS
            activityManager.getMemoryInfo(memoryInfo)
            val available = (memoryInfo.availMem * TOTAL_MEMORY_RATIO).toLong()
            return Math.min(MAX_STORAGE_SIZE_384_MEGS, available)
        }

        private fun defaultDeviceTags(): Array<Tag>? {
            // Tags for devices are not supported yet
            return null
        }
    }
}