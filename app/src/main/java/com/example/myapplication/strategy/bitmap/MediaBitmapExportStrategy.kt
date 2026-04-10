package com.example.myapplication.strategy.bitmap

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaBitmapExportStrategy(
    private val context: Context
) : BitmapExportStrategy{
    override suspend fun export(bitmap: Bitmap): Result<Unit> = withContext(Dispatchers.IO){
        val exportBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(exportBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        val filename = "${System.currentTimeMillis()}.png"
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/draw")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            } else {
                val path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ).absolutePath + "draw"
                File(path).mkdirs()
                put(MediaStore.Images.Media.DATA, "${path}/${filename}")
            }
        }
        try {
            // 3. 插入数据库并获取输出流
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw Exception("Failed to create new MediaStore record.")

            resolver.openOutputStream(uri)?.use { fos ->
                exportBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}