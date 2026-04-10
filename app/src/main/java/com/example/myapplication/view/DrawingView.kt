package com.example.myapplication.view

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import android.os.Build

import android.util.AttributeSet
import android.util.Log

import android.view.MotionEvent
import android.view.View

import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import com.example.myapplication.enum.*
import com.example.myapplication.ext.clear
import com.example.myapplication.ext.copyFrom
import com.example.myapplication.`interface`.Coordinates
import com.example.myapplication.model.DrawingModel.*

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val pathData = PathData()
    private lateinit var bitmap: Bitmap
    private lateinit var bitmapCanvas: Canvas
    val transformMatrix = Matrix()
    private val inverseMatrix = Matrix()
    var onSaveBitmapListener : (() -> Unit)? = null
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var initialDistance = 0f
    private var initialRotation = 0f
    private var drawPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }
    private var erasePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 30f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && !::bitmap.isInitialized) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bitmapCanvas = Canvas(bitmap)
            bitmapCanvas.drawColor(Color.WHITE)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.concat(transformMatrix)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.drawPath(pathData.curPath, getPaint())
        canvas.restore()
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        if (!::bitmap.isInitialized) return false
        val x: Float = event.x
        val y: Float = event.y

        val isReviewMode = getModel() == DrawingTool.REVIEW

        return if (isReviewMode) {
            reviewTouchEvent(event, x, y)
        } else {
            editTouchEvent(event, x, y)
        }
    }

    private fun getDistance(event: MotionEvent) : Float {
        if (event.pointerCount < 2) return 0f
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)

        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    private fun getMidPoint(event: MotionEvent) : Pair<Float, Float> {
        val dx = (event.getX(0) + event.getX(1)) / 2f
        val dy = (event.getY(0) + event.getY(1)) / 2f

        return Pair(dx, dy)
    }

    private fun getAngle(event: MotionEvent): Float {
        val dx = event.getX(1) - event.getX(0)
        val dy = event.getY(1) - event.getY(0)
        return Math.toDegrees(kotlin.math.atan2(dy, dx).toDouble()).toFloat()
    }


    fun setBitmap(bitmap: Bitmap) {
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        this.bitmap = newBitmap
        bitmapCanvas = Canvas(this.bitmap)
        invalidate()
    }

    fun getBitmap(): Bitmap {
        return bitmap
    }
    fun clear() {
        if (width > 0 && height > 0) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmapCanvas = Canvas(bitmap)
        }
        bitmapCanvas.drawColor(Color.WHITE)
        transformMatrix.reset()
        inverseMatrix.reset()
        pathData.curPath.reset()
        onSaveBitmapListener?.invoke()
        invalidate()
    }

    private fun getModel(): DrawingTool {
        return pathData.drawingTool
    }

    fun updateConfig(tool: DrawingTool, color: Int, stroke: Float, eraseSize: Float) {
        pathData.drawingTool = tool
        drawPaint.color = color
        drawPaint.strokeWidth = stroke
        erasePaint.strokeWidth = eraseSize
        invalidate()
    }

    private fun undo() : Boolean {
        return true
    }

    private fun redo(): Boolean {
        return true
    }


    private fun drawPathAll() {
        return
    }

    private fun getPaint() : Paint {
        return if (pathData.drawingTool == DrawingTool.ERASE) erasePaint else drawPaint
    }

    private fun extBitmap(x: Float, y:Float) {
        val limit = 100f

        if (x in 0f..(bitmap.width.toFloat() - limit) && y in 0f..(bitmap.height.toFloat() - limit)) return

        val newWeight = Math.max(bitmap.width + 1000, (x + 500).toInt())
        val newHeight = Math.max(bitmap.height + 1000, (y + 500).toInt())

        val offsetX = (newWeight - bitmap.width) / 2f
        val offsetY = (newHeight - bitmap.height) / 2f

        val newBitmap = createBitmap(newWeight, newHeight)
        val newCanvas = Canvas(newBitmap).apply {
            drawColor(Color.WHITE)
            drawBitmap(bitmap, offsetX, offsetY, null)
        }

        transformMatrix.preTranslate(-offsetX, -offsetY)

        pathData.curPath.offset(offsetX, offsetY)

        editLastY += offsetY
        editLastX += offsetX

        bitmap = newBitmap
        bitmapCanvas = newCanvas
    }

    private fun reviewTouchEvent(event: MotionEvent, x: Float, y: Float): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = x
                lastTouchY = y
                return true
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    initialDistance = getDistance(event)
                    initialRotation = getAngle(event)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1) {
                    val dx = x - lastTouchX
                    val dy = y - lastTouchY
                    transformMatrix.postTranslate(dx, dy)
                    lastTouchX = x
                    lastTouchY = y
                    invalidate()
                } else if (event.pointerCount >= 2) {
                    val newDistance = getDistance(event)
                    val newRotation = getAngle(event)

                    val (midX, midY) = getMidPoint(event)

                    if (initialDistance > 0) {
                        val scaleFactor = newDistance / initialDistance
                        transformMatrix.postScale(scaleFactor, scaleFactor, midX, midY)
                    }
                    var angleDiff = newRotation - initialRotation
                    if (angleDiff > 180) angleDiff -= 360
                    if (angleDiff < 180) angleDiff += 360
                    transformMatrix.postRotate(angleDiff, midX, midY)
                    initialRotation = newRotation
                    initialDistance = newDistance
                    lastTouchY = y
                    lastTouchX = x
                }
                invalidate()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (event.actionMasked == MotionEvent.ACTION_POINTER_UP && event.pointerCount == 2) {
                    val remainingPointerIndex = if (event.actionIndex == 0) 1 else 0
                    lastTouchX = event.getX(remainingPointerIndex)
                    lastTouchY = event.getY(remainingPointerIndex)
                }
                onSaveBitmapListener?.invoke()
            }
        }
        return super.onTouchEvent(event)
    }

    private var editLastX = 0f
    private var editLastY = 0f

    private fun editTouchEvent(event: MotionEvent, x: Float, y: Float): Boolean {
        transformMatrix.invert(inverseMatrix)
        val pts = floatArrayOf(x, y)
        inverseMatrix.mapPoints(pts)
        val canvasX = pts[0]
        val canvasY = pts[1]

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {

                extBitmap(canvasX, canvasY)
                pts[0] = x
                pts[1] = y
                transformMatrix.invert(inverseMatrix)
                inverseMatrix.mapPoints(pts)
                pathData.curPath.reset()
                pathData.curPath.moveTo(pts[0], pts[1])
                editLastX = pts[0]
                editLastY = pts[1]
                return true
            }

            MotionEvent.ACTION_MOVE -> {

                extBitmap(canvasX, canvasY)

                pts[0] = x
                pts[1] = y
                transformMatrix.invert(inverseMatrix)
                inverseMatrix.mapPoints(pts)
                pathData.curPath.quadTo(pts[0], pts[1], (editLastX + pts[0]) / 2f, (editLastY + pts[1]) / 2f)
                editLastX = pts[0]
                editLastY = pts[1]

                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                val paint = if (pathData.drawingTool == DrawingTool.ERASE) erasePaint else drawPaint
                bitmapCanvas.drawPath(pathData.curPath, paint)
                pathData.curPath.reset()
                onSaveBitmapListener?.invoke()
                invalidate()
            }
        }
        return super.onTouchEvent(event)
    }
}