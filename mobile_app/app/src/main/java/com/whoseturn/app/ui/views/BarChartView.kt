package com.whoseturn.app.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Bar(
        val label: String,
        val value: Double,
        val color: Int,
        val valueDisplay: String
    )

    private val bars = mutableListOf<Bar>()
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 26f
        color = Color.DKGRAY
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 24f
        color = Color.GRAY
    }

    private val barRect = RectF()

    fun setData(newBars: List<Bar>) {
        bars.clear()
        bars.addAll(newBars)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bars.isEmpty()) return

        val maxVal = bars.maxOfOrNull { it.value } ?: 1.0
        val effectiveMax = if (maxVal <= 0.0) 1.0 else maxVal

        val totalWidth = width.toFloat()
        val totalHeight = height.toFloat()
        val bottomMargin = 40f
        val topMargin = 40f
        val chartHeight = totalHeight - bottomMargin - topMargin

        val barCount = bars.size
        val slotWidth = totalWidth / barCount
        val barWidth = slotWidth * 0.55f

        for (i in 0 until barCount) {
            val bar = bars[i]
            val barHeight = ((bar.value / effectiveMax) * chartHeight).toFloat().coerceAtLeast(10f)

            val left = i * slotWidth + (slotWidth - barWidth) / 2f
            val right = left + barWidth
            val bottom = totalHeight - bottomMargin
            val top = bottom - barHeight

            barRect.set(left, top, right, bottom)
            barPaint.color = bar.color
            canvas.drawRoundRect(barRect, 14f, 14f, barPaint)

            // Value text above bar
            canvas.drawText(bar.valueDisplay, left + barWidth / 2f, top - 10f, textPaint)

            // Label text below bar
            val shortLabel = if (bar.label.length > 8) bar.label.take(7) + ".." else bar.label
            canvas.drawText(shortLabel, left + barWidth / 2f, bottom + 30f, labelPaint)
        }
    }
}
