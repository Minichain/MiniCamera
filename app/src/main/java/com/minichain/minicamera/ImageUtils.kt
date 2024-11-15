package com.minichain.minicamera

import android.media.Image
import java.nio.ByteBuffer
import java.nio.ReadOnlyBufferException
import kotlin.experimental.inv

class ImageUtils {
  companion object {

    /**
     * Converts a YUV image with 3 planes (Y, U, V) into a byte array in NV21 format.
     * Width and Height must be multiple of 16. Resolutions such as 800x600 won't work.
     *
     * Based on a code published by Alex Cohn on Stackoverflow.
     **/
    fun yuv420888toNV21(image: Image): ByteArray {
      val width: Int = image.width
      val height: Int = image.height
      val ySize = width * height
      val uvSize = width * height / 4
      val nv21 = ByteArray(ySize + uvSize * 2)

      val yBuffer: ByteBuffer = image.planes[0].buffer // Y
      val uBuffer: ByteBuffer = image.planes[1].buffer // U
      val vBuffer: ByteBuffer = image.planes[2].buffer // V

      var rowStride: Int = image.planes[0].rowStride

      assert(image.planes[0].pixelStride == 1)

      var pos = 0
      if (rowStride == width) {
        yBuffer.get(nv21, 0, ySize)
        pos += ySize
      } else {
        var yBufferPos = -rowStride
        while (pos < ySize) {
          yBufferPos += rowStride
          yBuffer.position(yBufferPos)
          yBuffer.get(nv21, pos, width)
          pos += width
        }
      }

      rowStride = image.planes[2].rowStride
      val pixelStride: Int = image.planes[2].pixelStride

      assert(rowStride == image.planes[1].rowStride)
      assert(pixelStride == image.planes[1].pixelStride)

      if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
        // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
        val savePixel: Byte = vBuffer.get(1)
        try {
          vBuffer.put(1, savePixel.inv())
          if (uBuffer.get(0) == savePixel.inv()) {
            uBuffer.put(1, savePixel)
            uBuffer.position(0)
            vBuffer.position(0)
            uBuffer.get(nv21, ySize, 1)
            vBuffer.get(nv21, ySize + 1, vBuffer.remaining())
            return nv21 // shortcut
          }
        } catch (e: ReadOnlyBufferException) {
          // unfortunately, we cannot check if vBuffer and uBuffer overlap
        }

        // unfortunately, the check failed. We must save U and V pixel by pixel
        vBuffer.put(1, savePixel)
      }

      // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
      // but performance gain would be less significant
      for (row in 0 until height / 2) {
        for (col in 0 until width / 2) {
          val vuPos = col * pixelStride + row * rowStride
          nv21[pos++] = vBuffer.get(vuPos)
          nv21[pos++] = uBuffer.get(vuPos)
        }
      }

      return nv21
    }
  }
}
