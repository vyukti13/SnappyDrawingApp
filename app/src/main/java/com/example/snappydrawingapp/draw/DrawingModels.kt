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

