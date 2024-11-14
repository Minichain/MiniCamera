package com.minichain.minicamera

enum class VideoResolution {
  RESOLUTION_320_240,  //4:3
  RESOLUTION_640_480,  //4:3
  RESOLUTION_960_720;  //4:3

  val width: Int
    get() = when (this) {
      RESOLUTION_320_240 -> 320
      RESOLUTION_640_480 -> 640
      RESOLUTION_960_720 -> 960
    }

  val height: Int
    get() = when (this) {
      RESOLUTION_320_240 -> 240
      RESOLUTION_640_480 -> 480
      RESOLUTION_960_720 -> 720
    }

  override fun toString(): String {
    return when (this) {
      RESOLUTION_320_240 -> "320x240"
      RESOLUTION_640_480 -> "640x480"
      RESOLUTION_960_720 -> "960x720"
    }
  }
}