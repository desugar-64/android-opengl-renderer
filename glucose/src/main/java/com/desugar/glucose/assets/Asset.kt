package com.desugar.glucose.assets

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.nio.ByteBuffer

class PixelMap(
    val width: Int,
    val height: Int,
    val channels: Int,
    val pixelData: ByteBuffer
)

interface AssetManager {
    fun loadPictureAsset(path: String): PixelMap
    fun loadTextAsset(path: String): String
}

class AndroidAssetManager(
    private val assetManager: android.content.res.AssetManager
) : AssetManager {
    override fun loadPictureAsset(path: String): PixelMap {
        val stream = assetManager.open(path)
        val bmp = BitmapFactory.decodeStream(stream)
        stream.close()
        val channels: Int = getChannels(bmp.config)
        val pixelsData = ByteBuffer.allocate(bmp.allocationByteCount)

        bmp.copyPixelsToBuffer(pixelsData)

//        val data = pixelsData.array()
//        val uBytes = IntArray(bmp.width * bmp.height)
//        var i = 0
        val hasAlpha = channels > 3
//        for (byteIndex in data.indices step channels) {
//            val a: Int = if (hasAlpha) {
//                data[byteIndex + 0].toInt() and 0xFF
//            } else {
//                255
//            }
//            val r: Int
//            val g: Int
//            val b: Int
//
//            if (hasAlpha) {
//                r = data[byteIndex + 1].toInt() and 0xFF
//                g = data[byteIndex + 2].toInt() and 0xFF
//                b = data[byteIndex + 3].toInt() and 0xFF
//            } else {
//                r = data[byteIndex + 0].toInt() and 0xFF
//                g = data[byteIndex + 1].toInt() and 0xFF
//                b = data[byteIndex + 2].toInt() and 0xFF
//            }
//
//            uBytes[i++] = Color.argb(b, g, r, a)
//        }
//        bmp.getPixels(data, 0, bmp.width, 0, 0, bmp.width, bmp.height)
//        for (idx in data.indices) {
//
//        }
        val pixelMap = PixelMap(bmp.width, bmp.height, channels, pixelsData.rewind() as ByteBuffer)
        bmp.recycle()
        return pixelMap
    }

    override fun loadTextAsset(path: String): String {
        val stream = assetManager.open(path)
        val string = stream.bufferedReader().readText()
        stream.close()
        return string
    }

    private fun getChannels(config: Bitmap.Config?): Int {
        config ?: return 0
        return when (config) {
            Bitmap.Config.ALPHA_8 -> 1
            Bitmap.Config.RGB_565 -> 3
            Bitmap.Config.ARGB_8888 -> 4
            Bitmap.Config.RGBA_F16 -> 4
            Bitmap.Config.HARDWARE -> error("HW bitmaps unsupported")
            else -> 3
        }
    }
}