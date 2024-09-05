package com.happstudio.cropimage.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.happstudio.cropimage.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import java.util.Locale
import java.util.Objects


@Throws(IOException::class)
fun Context.createImageFile(): File {
    val imageFile = "JPEG_IMAGE_TEMP"
    return File.createTempFile(imageFile, ".jpg", externalCacheDir)
}

@Throws(IOException::class)
fun Context.createImageFromOldUri(uri: Uri): File? {
    val input = contentResolver.openInputStream(uri) ?: return null
    val documentFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
        Date()
    )
    val imageFile = "JPEG_$timeStamp"
    val file = File(documentFile, "$imageFile.jpg")
    input.copyTo(file.outputStream())
    MediaScannerConnection.scanFile(
        this,
        arrayOf(file.absolutePath),
        null
    ) { path, uri ->
        // File scanned successfully, you can use the URI if needed
        println("File scanned: $path, URI: $uri")
    }
    return file
}

@RequiresApi(Build.VERSION_CODES.R)
fun Context.deleteInternalFile(uri: Uri): Int = try {
    contentResolver.delete(uri, null)
} catch (_: Exception) {
    0
}

fun bitmapToFileProvider(context: Context, bitmap: Bitmap, file: File): Uri? {
    // Get a FileOutputStream to write the bitmap to the file
    val outputStream = FileOutputStream(file)
    // Compress the bitmap to PNG format
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    // Close the FileOutputStream
    outputStream.close()
    // Return a FileProvider URI for the file
    return FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        BuildConfig.APPLICATION_ID + ".provider",
        file,
    )
}