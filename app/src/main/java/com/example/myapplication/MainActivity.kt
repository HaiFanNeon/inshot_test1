package com.example.myapplication
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.config.DrawingViewModelFactory
import com.example.myapplication.enum.DrawingTool
import com.example.myapplication.ext.copyFrom
import com.example.myapplication.manager.BitmapFileManager
import com.example.myapplication.model.DrawingModel
import com.example.myapplication.strategy.BitmapRoomStrategy
import com.example.myapplication.strategy.BitmapSaveStrategy
import com.example.myapplication.strategy.FileDraftStrategy
import com.example.myapplication.strategy.MediaBitmapExportStrategy
import com.example.myapplication.view.DrawingView
import com.example.myapplication.viewModel.DrawingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.text.set


class MainActivity : ComponentActivity(), View.OnClickListener{

    private lateinit var drawingView: DrawingView
    private lateinit var bitmapFileManager: BitmapFileManager
    private val viewModel: DrawingViewModel by viewModels {
        DrawingViewModelFactory(bitmapFileManager)
    }

    private lateinit var btnBrush: Button
    private lateinit var btnErase: Button
    private lateinit var btnPreview: Button
    private lateinit var btnClear: Button
    private lateinit var btnExport: Button
    private lateinit var sbStrokeWidth: SeekBar

    @SuppressLint("WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        drawingView = findViewById<DrawingView>(R.id.drawingView)
        bitmapFileManager = BitmapFileManager(
            saveStrategy = BitmapRoomStrategy(this),
            loadStrategy = FileDraftStrategy(this),
            exportStrategy = MediaBitmapExportStrategy(this),
        )
        loadDraw()
        drawingView.onSaveBitmapListener = {
            save()
        }
        btnBrush = findViewById(R.id.btnBrush)
        btnErase = findViewById(R.id.btnEarse)
        btnPreview = findViewById(R.id.btnPreview)
        btnClear = findViewById(R.id.btnClear)
        btnExport = findViewById(R.id.btnExport)
        sbStrokeWidth = findViewById(R.id.sbStrokeWidth)
        btnBrush.setOnClickListener(this)
        btnErase.setOnClickListener(this)
        btnPreview.setOnClickListener(this)
        btnClear.setOnClickListener(this)
        btnExport.setOnClickListener(this)
        findViewById<Button>(R.id.btnColorBlack).setOnClickListener(this)
        findViewById<Button>(R.id.btnColorBlue).setOnClickListener(this)
        val textView = findViewById<TextView>(R.id.tvStrokeWidth)
        val testErase = findViewById<TextView>(R.id.tvEraseWidth)

        findViewById<SeekBar>(R.id.sbStrokeWidth).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                viewModel.setBrushSize(progress.toFloat())
                textView.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        findViewById<SeekBar>(R.id.sbEraseWidth).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                viewModel.setEraseSize(progress.toFloat())
                testErase.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })
        
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    drawingView.updateConfig(
                        state.drawingTool,
                        state.color,
                        state.brushSize,
                        state.eraseSize
                    )

                    val isReview = state.drawingTool == DrawingTool.REVIEW

                    btnPreview.isSelected = isReview
                    btnBrush.isSelected =
                        !isReview && state.drawingTool == DrawingTool.BRUSH
                    btnErase.isSelected =
                        !isReview && state.drawingTool == DrawingTool.ERASE

                }
            }
        }


    }

    private fun save() {
        val bp = drawingView.getBitmap()
        viewModel.save(bp, drawingView.transformMatrix)
    }

    private fun loadDraw() {
        val pair = viewModel.loadDraw() ?: return
        drawingView.setBitmap(pair.first)
        drawingView.transformMatrix.set(pair.second)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btnEarse -> {
                viewModel.setDrawingTool(DrawingTool.ERASE)
            }
            R.id.btnBrush -> {
                viewModel.setDrawingTool(DrawingTool.BRUSH)
            }
            R.id.btnClear -> {
                lifecycleScope.launch { drawingView.clear() }
            }
            R.id.btnExport -> {
                viewModel.exportBitmap(drawingView.getBitmap())
            }
            R.id.btnPreview -> {
                viewModel.setDrawingTool(DrawingTool.REVIEW)
            }
            R.id.btnColorBlack -> {
                viewModel.setBrushColor(Color.BLACK)
            }
            R.id.btnColorBlue -> {
                viewModel.setBrushColor(Color.BLUE)
            }
        }
    }
}