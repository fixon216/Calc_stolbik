package com.example.stolbik

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View

class PaperView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var layout: ColumnEngine.Layout? = null
        set(value) { field = value; invalidate() }

    private val density = resources.displayMetrics.density
    private val maxCell = 50f * density

    private val bgColor       = 0xFF0E1116.toInt()
    private val gridColor     = 0xFF1E3A5F.toInt()
    private val gridBoldColor = 0xFF2C5282.toInt()

    private val colorA      = 0xFF64B5F6.toInt()
    private val colorB      = 0xFFFFB74D.toInt()
    private val colorResult = 0xFF81C784.toInt()
    private val colorCarry  = 0xFFE57373.toInt()
    private val colorOp     = 0xFFFFEB3B.toInt()
    private val colorDecor  = 0xFFB0BEC5.toInt()

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = gridColor; strokeWidth = 1f; style = Paint.Style.STROKE
    }
    private val gridBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = gridBoldColor; strokeWidth = 1.4f; style = Paint.Style.STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFECEFF1.toInt(); strokeWidth = 2f * density
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND
    }
    private val strikePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorCarry; strokeWidth = 2f * density
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND
    }
    private val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFD54F.toInt(); textSize = 14f * density
        textAlign = Paint.Align.LEFT
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(bgColor)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val lay = layout
        if (lay == null) {
            drawGrid(canvas, w, h, 44f * density, 0f, 0f)
            return
        }

        val cols = maxOf(lay.cols, 1)
        val rows = maxOf(lay.rows, 1)
        val cell = minOf(w / cols, h / rows, maxCell)
        val contentW = cols * cell
        val contentH = rows * cell
        val offsetX = (w - contentW) / 2f
        val offsetY = (h - contentH) / 2f

        drawGrid(canvas, w, h, cell, offsetX, offsetY)

        for (l in lay.hLines) {
            val x1 = offsetX + l.colStart * cell
            val x2 = offsetX + (l.colEnd + 1) * cell
            val y = offsetY + l.row * cell
            canvas.drawLine(x1, y, x2, y, linePaint)
        }
        for (l in lay.vLines) {
            val x = offsetX + l.col * cell
            val y1 = offsetY + l.rowStart * cell
            val y2 = offsetY + (l.rowEnd + 1) * cell
            canvas.drawLine(x, y1, x, y2, linePaint)
        }

        for (c in lay.cells) {
            drawCell(canvas, c, offsetX + c.col * cell, offsetY + c.row * cell, cell)
        }

        lay.note?.let {
            canvas.drawText(it, 8f * density, 20f * density, notePaint)
        }
    }

    private fun drawGrid(canvas: Canvas, w: Float, h: Float,
                         cell: Float, offsetX: Float, offsetY: Float) {
        if (cell < 4f) return
        var idx = 0
        var x = offsetX
        while (x > 0f) { x -= cell; idx++ }
        var i = idx
        while (x < w) {
            canvas.drawLine(x, 0f, x, h, if (i % 5 == 0) gridBoldPaint else gridPaint)
            x += cell; i++
        }
        var y = offsetY
        var j = 0
        while (y > 0f) { y -= cell; j++ }
        var k = j
        while (y < h) {
            canvas.drawLine(0f, y, w, y, if (k % 5 == 0) gridBoldPaint else gridPaint)
            y += cell; k++
        }
    }

    private fun colorFor(role: ColumnEngine.Role): Int = when (role) {
        ColumnEngine.Role.A -> colorA
        ColumnEngine.Role.B -> colorB
        ColumnEngine.Role.RESULT -> colorResult
        ColumnEngine.Role.CARRY -> colorCarry
        ColumnEngine.Role.OP -> colorOp
        ColumnEngine.Role.DECOR -> colorDecor
    }

    private fun drawCell(canvas: Canvas, c: ColumnEngine.Cell,
                         x: Float, y: Float, cell: Float) {
        val cx = x + cell / 2f
        val hasTop = c.top != null
        val color = colorFor(c.role)

        if (hasTop) {
            textPaint.color = colorCarry
            textPaint.textSize = cell * 0.36f
            val fm = textPaint.fontMetrics
            val baseline = y + cell * 0.06f - fm.ascent
            canvas.drawText(c.top!!, cx, baseline, textPaint)
        }

        if (c.main != null) {
            textPaint.color = color
            val isOp = c.role == ColumnEngine.Role.OP
            textPaint.textSize = cell * (if (isOp) 0.72f else 0.58f)
            val fm = textPaint.fontMetrics
            val cy = y + cell * (if (hasTop) 0.72f else 0.52f)
            val baseline = cy - (fm.ascent + fm.descent) / 2f
            canvas.drawText(c.main.toString(), cx, baseline, textPaint)

            if (c.strike) {
                val half = cell * 0.30f
                canvas.drawLine(cx - half, cy, cx + half, cy, strikePaint)
            }
        }
    }
}