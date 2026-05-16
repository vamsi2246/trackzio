package com.weathersnap.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {
    
    fun compressImage(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return null

            val outputDir = context.filesDir
            val outputFile = File(outputDir, "compressed_\${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(outputFile)

            // Compress to 80% quality
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun getFileSizeInKB(file: File): Long {
        return file.length() / 1024
    }
}
