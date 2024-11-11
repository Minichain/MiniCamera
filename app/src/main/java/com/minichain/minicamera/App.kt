package com.minichain.minicamera

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log

class App : Application() {
  override fun onCreate() {
    Log.d("APP", "Application running...")
    super.onCreate()
    val channel = NotificationChannel(
      "foreground_service_notification_channel",
      "Running Notification",
      NotificationManager.IMPORTANCE_HIGH
    )
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
  }
}
