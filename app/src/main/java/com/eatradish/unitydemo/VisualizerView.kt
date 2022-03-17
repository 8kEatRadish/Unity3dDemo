package com.eatradish.unitydemo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class VisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var mBytes: ByteArray? = null
    private val mRect: Rect = Rect()
    private val paint: Paint = Paint()

    init {
        mBytes = null
        paint.apply {
            strokeWidth = 1f
            isAntiAlias = true
            color = Color.rgb(255, 255, 255)
        }
    }

    fun updateVisualizer(byteArray: ByteArray?) {
        mBytes = byteArray
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (mBytes == null) {
            return
        }
        mRect.set(0, 0, width, height)
        mBytes!!.forEachIndexed { index, byte ->
            val left = width * index / (mBytes!!.size - 1)
            val top =
                mRect.height() / 2 - (mBytes!![index] + 128).toByte() * (mRect.height() / 2) / 128
            val right = left + 1
            val bottom = mRect.height() / 2
            canvas?.drawRect(
                left.toFloat(),
                top.toFloat(),
                right.toFloat(),
                bottom.toFloat(),
                paint
            )
        }
    }
}