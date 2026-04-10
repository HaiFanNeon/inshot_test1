package com.example.myapplication.model

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.myapplication.enum.*

data class DrawingUiState(
    val drawingTool: DrawingTool = DrawingTool.NONE,
    val color: Int = Color.BLACK,
    val brushSize: Float = 8f,
    val eraseSize: Float = 30f
)

class DrawingModel {

    /**
     * color：线的颜色
     * stroke：线的宽度
     * drawPath：保存这条线
     */
    data class Stroke(
        val drawPath: Path = Path(),
        val paint: Paint = Paint()
    )

    /**
     * curPath：当前path
     * undoStk：回退栈
     * redoStk：撤销回退栈
     * drawingTool：画笔还是橡皮擦
     */
    data class PathData(
        var curPath: Path = Path(),
        val undoStk: ArrayDeque<Stroke> = ArrayDeque(),
        val redoStk: ArrayDeque<Stroke> = ArrayDeque(),
        var drawingTool: DrawingTool = DrawingTool.NONE
    )
}

