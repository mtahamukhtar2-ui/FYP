package com.example.fyp_app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.fyp_app.leaf.LeafBackgroundRemover
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageProcessor {

    /**
     * Professional Plant Leaf Segmentation AI.
     * Task: Isolate the main leaf, removing hands, soil, and background objects.
     * Preserves internal disease symptoms (white powder, brown spots, yellowing).
     */
    suspend fun processLeafImage(context: Context, imageUri: Uri): Uri? = withContext(Dispatchers.IO) {
        return@withContext try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return@withContext null

            // Call the new ML Kit based background remover
            val processedResult = LeafBackgroundRemover.removeBackground(originalBitmap)

            val processedFile = File(context.cacheDir, "processed_${UUID.randomUUID()}.jpg")
            val out = FileOutputStream(processedFile)
            processedResult.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            Uri.fromFile(processedFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveProfileImage(context: Context, imageUri: Uri): Uri? = withContext(Dispatchers.IO) {
        return@withContext try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream) ?: return@withContext null

            val profileDir = File(context.filesDir, "profile")
            if (!profileDir.exists()) profileDir.mkdirs()

            // Unique filename to avoid caching issues
            val profileFile = File(profileDir, "profile_${System.currentTimeMillis()}.jpg")
            
            // Delete old profile pictures
            profileDir.listFiles()?.forEach { it.delete() }

            val out = FileOutputStream(profileFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            Uri.fromFile(profileFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
