package com.example.myapplication.manager

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.os.Build
import android.provider.MediaStore
import android.util.JsonReader
import android.util.Log
import com.example.myapplication.`interface`.Coordinates
import com.example.myapplication.model.DrawingModel
import com.example.myapplication.strategy.BitmapExportStrategy
import com.example.myapplication.strategy.BitmapSaveStrategy
import com.example.myapplication.strategy.FileDraftStrategy
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class BitmapFileManager(
    private val saveStrategy: BitmapSaveStrategy,
    private val loadStrategy: FileDraftStrategy,
    private val exportStrategy: BitmapExportStrategy
    ) {
    suspend fun saveDraft(bitmap: Bitmap, matrix: FloatArray): Result<Unit> = withContext(Dispatchers.IO) {
        saveStrategy.saveDraft(bitmap, matrix)
    }

    fun loadDraft(): Pair<Bitmap, FloatArray>? {
        return loadStrategy.loadDraft()
    }

    suspend fun exportDraft(bitmap: Bitmap) : Result<Unit> = withContext(Dispatchers.IO) {
        exportStrategy.export(bitmap)
    }

}