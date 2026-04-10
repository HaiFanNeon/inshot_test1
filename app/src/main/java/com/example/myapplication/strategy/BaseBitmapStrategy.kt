package com.example.myapplication.strategy

import android.graphics.Bitmap


interface BitmapSaveStrategy {
    suspend fun saveDraft(bitmap: Bitmap, matrix: FloatArray): Result<Unit>
}

interface BitmapLoaderStrategy {
    fun loadDraft(): Pair<Bitmap, FloatArray>?
}

interface BitmapExportStrategy {
    suspend fun export(bitmap: Bitmap): Result<Unit>
}