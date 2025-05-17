package br.com.victall.listenfree.upload

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class UploadManager(private val context: Context) {

    suspend fun copyFileToInternalStorage(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = getFileName(uri)
            val outputFile = File(context.filesDir, fileName)
            
            inputStream?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        cursor?.moveToFirst()
        val fileName = cursor?.getString(nameIndex ?: 0) ?: "audio_${System.currentTimeMillis()}.mp3"
        cursor?.close()
        return fileName
    }

    suspend fun validateMp3File(file: File): Boolean = withContext(Dispatchers.IO) {
        // TODO: Implementar validação do arquivo MP3
        // Por enquanto, apenas verifica a extensão
        file.name.lowercase().endsWith(".mp3")
    }
} 