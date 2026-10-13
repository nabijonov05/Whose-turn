package com.whoseturn.app.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress: Int = 0
    private var progressColor: Int = Color.parseColor("#10B981")
    private var trackColor: Int = Color.parseColor("#E2E8F0")

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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
        textSize = 22f
        color = Color.GRAY
    }

    private val rectF = RectF()
    private var label: String = "Bajarildi"

    fun setProgress(value: Int, color: Int = Color.parseColor("#10B981"), subLabel: String = "Bajarildi") {
        progress = value.coerceIn(0, 100)
        progressColor = color
        label = subLabel
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val size = width.coerceAtMost(height)
        val stroke = size * 0.12f

        trackPaint.strokeWidth = stroke
        trackPaint.color = trackColor

        progressPaint.strokeWidth = stroke
        progressPaint.color = progressColor

        val padding = stroke / 2f + 8f
        rectF.set(padding, padding, width - padding, height - padding)

        // Draw background circle
        canvas.drawArc(rectF, 0f, 360f, false, trackPaint)

        // Draw progress arc
        val sweepAngle = (progress / 100f) * 360f
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint)

        // Draw percentage text
        val centerX = width / 2f
        val centerY = height / 2f
        canvas.drawText("$progress%", centerX, centerY - 4f, textPaint)
        canvas.drawText(label, centerX, centerY + 28f, subTextPaint)
    }
}
