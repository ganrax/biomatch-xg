package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

object ImageUtils {

    suspend fun uriToBase64Jpeg(context: Context, uri: Uri, maxDimension: Int = 1024): Pair<String, String>? =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                if (originalBitmap == null) return@withContext null

                val width = originalBitmap.width
                val height = originalBitmap.height
                val longest = max(width, height)

                val scaledBitmap = if (longest > maxDimension) {
                    val scale = maxDimension.toFloat() / longest
                    Bitmap.createScaledBitmap(
                        originalBitmap,
                        (width * scale).toInt(),
                        (height * scale).toInt(),
                        true
                    )
                } else {
                    originalBitmap
                }

                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val byteArray = outputStream.toByteArray()
                val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                Pair(base64, "image/jpeg")
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }
        }

    suspend fun uriToBitmap(context: Context, uri: Uri, maxDimension: Int = 1400): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                if (originalBitmap == null) return@withContext null

                val width = originalBitmap.width
                val height = originalBitmap.height
                val longest = max(width, height)

                if (longest > maxDimension) {
                    val scale = maxDimension.toFloat() / longest
                    Bitmap.createScaledBitmap(
                        originalBitmap,
                        (width * scale).toInt(),
                        (height * scale).toInt(),
                        true
                    )
                } else {
                    originalBitmap
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }
        }
}
