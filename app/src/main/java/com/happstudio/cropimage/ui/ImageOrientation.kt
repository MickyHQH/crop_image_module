package com.happstudio.cropimage.ui

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.IOException

object ImageOrientation {
    private fun uriToBitmap(uri: Uri, contentResolver: ContentResolver): Bitmap? {
        // Get the dimensions of the image
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options)

        // Calculate the scale factor
        val scaleFactor = (options.outWidth / 1024).coerceAtMost(options.outHeight / 1024)

        // Decode the image with the scale factor
        options.inJustDecodeBounds = false
        options.inSampleSize = scaleFactor
        return BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options)
    }

    fun modifyOrientation(context: Context, uri: Uri): Bitmap? {
        val bitmap = uriToBitmap(uri, context.contentResolver) ?: return null
        val orientation = try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exifInterface = ExifInterface(inputStream)
                exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED,
                )
            }
        } catch (_: IOException) {
            ExifInterface.ORIENTATION_UNDEFINED
        }
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(
                bitmap,
                horizontal = true,
                vertical = false,
            )

            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(
                bitmap,
                horizontal = false,
                vertical = true,
            )

            else -> bitmap
        }
    }

    fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale((if (horizontal) -1 else 1).toFloat(), (if (vertical) -1 else 1).toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
