package com.example.myapplication.viewModel

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.myapplication.enum.DrawingTool
import com.example.myapplication.manager.BitmapFileManager
import com.example.myapplication.model.DrawingUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DrawingViewModel(private val bitmapFileManager: BitmapFileManager ): ViewModel() {

    private val _uiState = MutableStateFlow(DrawingUiState())
    val uiState: StateFlow<DrawingUiState> = _uiState.asStateFlow()

    fun setDrawingTool(tool: DrawingTool) {
        _uiState.update {
            it.copy(drawingTool = tool)
        }
    }

    fun setBrushColor(colorr: Int) {
        _uiState.update {
            it.copy(color = colorr)
        }

    }

    fun setBrushSize(size: Float) {
        _uiState.update { it.copy(brushSize = size) }
    }

    fun setEraseSize(size: Float) {
        _uiState.update { it.copy(eraseSize = size) }
    }

    fun exportBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            val result = bitmapFileManager.exportDraft(bitmap)
            result.onSuccess {
            }.onFailure { exception ->
                Log.i("testtest", exception.message.toString())
            }
        }
    }

    fun save(bitmap: Bitmap, matrix: Matrix) {
        viewModelScope.launch{
            val values = FloatArray(9)
            matrix.getValues(values)
            bitmapFileManager.saveDraft(bitmap, values)
        }
    }

    fun loadDraw(): Pair<Bitmap, Matrix>? {
        val loadDraft = bitmapFileManager.loadDraft() ?: return null
        val matrix = Matrix().apply {
            setValues(loadDraft.second)
        }
        return Pair(loadDraft.first, matrix)
    }

}