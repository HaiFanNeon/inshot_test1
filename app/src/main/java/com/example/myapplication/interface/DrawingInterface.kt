//package com.example.myapplication.`interface`
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.PorterDuff
//import android.util.Log
//import android.view.MotionEvent
//import com.example.myapplication.enum.ReviewMode
//import com.example.myapplication.model.DrawingModel
//import java.io.File
//import java.io.FileOutputStream
//
//interface DrawingInterface {
//
//    // 计算xy相关
//    fun getDistance(event: MotionEvent) : Float
//    fun getMidPoint(event: MotionEvent) : Pair<Float, Float>
//    fun getAngle(event: MotionEvent): Float
//    // 获取bitmap
//    fun getBitmap(): Bitmap
//    fun setBitmap(bitmap: Bitmap)
//    // 清空画布
//    fun clear()
//    // 切换橡皮擦/画笔
//    fun setEraseMode()
//    fun setBrushMode()
//    // 设置预览模式
//    fun setReview()
//    // 编辑模式
//    fun setEdit()
//    // 获取当前模式
//    fun getModel(): ReviewMode
//    // 回退
//    fun undo(): Boolean
//    fun redo(): Boolean
//}