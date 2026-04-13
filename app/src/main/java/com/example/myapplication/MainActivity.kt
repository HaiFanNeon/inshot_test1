package com.example.myapplication
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View

import android.widget.SeekBar

import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.config.DrawingViewModelFactory
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.enum.DrawingTool
import com.example.myapplication.manager.BitmapFileManager
import com.example.myapplication.`interface`.impl.bitmap.BitmapRoomImpl
import com.example.myapplication.`interface`.impl.bitmap.FileDraftImpl
import com.example.myapplication.`interface`.impl.bitmap.MediaBitmapExportImpl

import com.example.myapplication.viewModel.DrawingViewModel
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity(), View.OnClickListener{
    private var bitmapFileManager: BitmapFileManager = BitmapFileManager(
        saveStrategy = BitmapRoomImpl(this),
        loadStrategy = FileDraftImpl(this),
        exportStrategy = MediaBitmapExportImpl(this),
    )
    private val viewModel: DrawingViewModel by viewModels {
        DrawingViewModelFactory(bitmapFileManager)
    }


    private lateinit var viewBinding: ActivityMainBinding

    @SuppressLint("WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(viewBinding.root)

        loadDraw()
        viewBinding.drawingView.onSaveBitmapListener = {
            save()
        }
        with(viewBinding) {
            btnBrush.setOnClickListener(this@MainActivity)
            btnEarse.setOnClickListener(this@MainActivity)
            btnPreview.setOnClickListener(this@MainActivity)
            btnClear.setOnClickListener(this@MainActivity)
            btnExport.setOnClickListener(this@MainActivity)
            btnUndo.setOnClickListener(this@MainActivity)
            btnRedo.setOnClickListener(this@MainActivity)
            btnColorBlack.setOnClickListener(this@MainActivity)
            btnColorBlue.setOnClickListener(this@MainActivity)
        }
        viewBinding.sbStrokeWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                viewModel.setBrushSize(progress.toFloat())
                viewBinding.tvStrokeWidth.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        viewBinding.sbEraseWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                viewModel.setEraseSize(progress.toFloat())
                viewBinding.tvEraseWidth.text = progress.toString()
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
                    viewBinding.drawingView.updateConfig(
                        state.drawingTool,
                        state.color,
                        state.brushSize,
                        state.eraseSize
                    )

                    val isReview = state.drawingTool == DrawingTool.REVIEW

                    viewBinding.btnPreview.isSelected = isReview
                    viewBinding.btnBrush.isSelected =
                        !isReview && state.drawingTool == DrawingTool.BRUSH
                    viewBinding.btnEarse.isSelected =
                        !isReview && state.drawingTool == DrawingTool.ERASE

                }
            }
        }


    }

    private fun save() {
        val bp = viewBinding.drawingView.getBitmap()
        viewModel.save(bp, viewBinding.drawingView.transformMatrix)
    }

    private fun loadDraw() {
        val pair = viewModel.loadDraw() ?: return
        viewBinding.drawingView.setBitmap(pair.first)
        viewBinding.drawingView.transformMatrix.set(pair.second)
    }

    override fun onClick(v: View?) {
        with(viewBinding) {
            btnEarse.setOnClickListener {
                viewModel.setDrawingTool(DrawingTool.ERASE)
            }
            btnBrush.setOnClickListener {
                viewModel.setDrawingTool(DrawingTool.BRUSH)
            }
            btnClear.setOnClickListener {
                lifecycleScope.launch { viewBinding.drawingView.clear() }
            }
            btnExport.setOnClickListener {
                viewModel.exportBitmap(viewBinding.drawingView.getBitmap())
            }
            btnPreview.setOnClickListener{
                viewModel.setDrawingTool(DrawingTool.REVIEW)
            }
            btnColorBlack.setOnClickListener {
                viewModel.setBrushColor(Color.BLACK)
            }
            btnColorBlue.setOnClickListener {
                viewModel.setBrushColor(Color.BLUE)
            }
            btnUndo.setOnClickListener {
                viewBinding.drawingView.undo()
            }
            btnRedo.setOnClickListener {
                viewBinding.drawingView.redo()
            }
        }
    }
}