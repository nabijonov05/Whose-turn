package com.whoseturn.app.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Slice(
        val label: String,
        val value: Double,
        val color: Int
    )

    private val slices = mutableListOf<Slice>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 36f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = Color.DKGRAY
    }
    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 24f
        color = Color.GRAY
    }

    private val rectF = RectF()
    private var centerText: String = ""
    private var centerSubText: String = ""

    fun setData(newSlices: List<Slice>, centerTitle: String, centerSubtitle: String) {
        slices.clear()
        slices.addAll(newSlices)
        centerText = centerTitle
        centerSubText = centerSubtitle
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (slices.isEmpty()) return

        val total = slices.sumOf { it.value }
        if (total <= 0) return

        val width = width.toFloat()
        val height = height.toFloat()
        val size = width.coerceAtMost(height)
        val stroke = size * 0.14f

        paint.strokeWidth = stroke

        val padding = stroke + 16f
        rectF.set(padding, padding, width - padding, height - padding)

        var startAngle = -90f
        for (slice in slices) {
            val sweep = (slice.value / total * 360f).toFloat()
            paint.color = slice.color
            // small gap between slices
            val drawSweep = if (slices.size > 1) (sweep - 4f).coerceAtLeast(1f) else sweep
            canvas.drawArc(rectF, startAngle + 2f, drawSweep, false, paint)
            startAngle += sweep
        }

        // Center text
        val centerX = width / 2f
        val centerY = height / 2f
        canvas.drawText(centerText, centerX, centerY - 6f, textPaint)
        canvas.drawText(centerSubText, centerX, centerY + 30f, subTextPaint)
    }
}
