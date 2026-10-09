package com.example.stolbik

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var paper: PaperView
    private val expr = StringBuilder()
    private var resultShown = false

    private val ops = "+−×÷"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        paper = findViewById(R.id.paperView)

        val keypad = findViewById<LinearLayout>(R.id.keypad)
        for (i in 0 until keypad.childCount) {
            val row = keypad.getChildAt(i) as LinearLayout
            for (j in 0 until row.childCount) {
                val b = row.getChildAt(j) as Button
                b.setOnClickListener { onKey(b.text.toString()) }
            }
        }
        refresh()
    }

    private fun onKey(k: String) {
        when (k) {
            "C" -> { expr.clear(); resultShown = false }
            "⌫" -> { resultShown = false; if (expr.isNotEmpty()) expr.deleteCharAt(expr.length - 1) }
            "=" -> doEquals()
            in ops -> {
                if (resultShown) { resultShown = false; expr.append(k[0]) }
                else if (expr.isEmpty()) { expr.append('−'); return }
                else if (expr.last() in ops) expr.setCharAt(expr.length - 1, k[0])
                else {
                    val opIdx = expr.indexOfFirst { it in ops }
                    if (opIdx >= 0 && opIdx < expr.length - 1) {
                        val r = tryEval(expr.toString()) ?: return
                        expr.clear(); expr.append(r)
                    }
                    expr.append(k[0])
                }
            }
            "," -> {
                if (resultShown) { expr.clear(); resultShown = false }
                val opIdx = expr.indexOfFirst { it in ops }
                val numPart = if (opIdx >= 0) expr.substring(opIdx + 1) else expr.toString()
                if (numPart.contains(',')) return
                if (numPart.isEmpty()) expr.append('0')
                expr.append(',')
            }
            else -> {
                if (resultShown) { expr.clear(); resultShown = false }
                if (expr.length >= 20) return
                expr.append(k)
            }
        }
        refresh()
    }

    private fun refresh() {
        if (resultShown) return
        val s = expr.toString()
        paper.layout = if (s.isEmpty()) null else ColumnEngine.inputLayout(s)
    }

    private fun doEquals() {
        val s = expr.toString()
        val opIdx = s.indexOfFirst { it in ops }
        if (opIdx <= 0 || opIdx == s.length - 1) return

        val a = s.substring(0, opIdx)
        val op = s[opIdx]
        val b = s.substring(opIdx + 1)
        if (a.isEmpty() || b.isEmpty()) return

        val layout = ColumnEngine.compute(a, b, op)
        paper.layout = layout
        resultShown = true
        expr.clear(); expr.append(layout.result)
    }

    private fun tryEval(s: String): String? {
        val opIdx = s.indexOfFirst { it in ops }
        if (opIdx <= 0 || opIdx == s.length - 1) return null
        val a = s.substring(0, opIdx)
        val op = s[opIdx]
        val b = s.substring(opIdx + 1)
        if (a.isEmpty() || b.isEmpty()) return null
        return ColumnEngine.compute(a, b, op).result
    }
}