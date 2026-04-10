package com.example.myapplication.strategy.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream

class FileDraftStrategy(
    private val context: Context
) : BitmapLoaderStrategy, BitmapSaveStrategy {
    override fun loadDraft(): Pair<Bitmap, FloatArray>? {
        val file = File(context.filesDir, "test.png")
        if (!file.exists()) {
            Log.i("testtest", "first open app")
            return null
        }

        val json = File(context.filesDir, "coordinates.json")
        if (!json.exists()) {
            return null
        }
        var values = FloatArray(9) {0f}
        values[0] = 1f
        values[4] = 1f
        values[8] = 1f
        val jsonString = json.readText(Charsets.UTF_8)
        val prase = Gson().fromJson(jsonString, FloatArray::class.java)
        if (prase != null && prase.size == 0) {
            values = prase
        }
        return Pair(BitmapFactory.decodeFile(file.absolutePath), values)

    }

    override suspend fun saveDraft(
        bitmap: Bitmap,
        matrix: FloatArray
    ): Result<Unit> {
        return try{
            val file = File(context.filesDir, "test.png")
            val gson = Gson()
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            val json = gson.toJson(matrix)
            val jsonFile = File(context.filesDir, "coordinates.json")
            FileOutputStream(jsonFile).use { fos ->
                fos.write(json.toByteArray(Charsets.UTF_8))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}