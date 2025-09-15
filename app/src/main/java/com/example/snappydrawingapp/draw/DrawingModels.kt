package com.example.snappydrawingapp.draw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

// Simple polyline stroke captured from touch input
data class Stroke(
    val points: MutableList<Offset> = mutableListOf(),
    val color: Color = Color.Black,
    val widthPx: Float = 4f,
)

// History stack for undo/redo of strokes
class History {
    private val done: MutableList<Stroke> = mutableListOf()
    private val undone: MutableList<Stroke> = mutableListOf()

    fun push(stroke: Stroke) {
        done.add(stroke)
        undone.clear()
    }

    fun undo(): Stroke? {
        if (done.isEmpty()) return null
        val s = done.removeLast()
        undone.add(s)
        return s
    }

    fun redo(): Stroke? {
        if (undone.isEmpty()) return null
        val s = undone.removeLast()
        done.add(s)
        return s
    }

    fun clear() {
        done.clear()
        undone.clear()
    }

    fun current(): List<Stroke> = done
}
