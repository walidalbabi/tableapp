package com.example.repository

import android.content.Context
import android.net.Uri
import com.example.model.TableFile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TableRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun saveTableToFile(uri: Uri, tableFile: TableFile): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val jsonString = json.encodeToString(tableFile)
                    outputStream.write(jsonString.toByteArray())
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun loadTableFromFile(uri: Uri): TableFile? {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val jsonString = inputStream.bufferedReader().readText()
                    json.decodeFromString<TableFile>(jsonString)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
