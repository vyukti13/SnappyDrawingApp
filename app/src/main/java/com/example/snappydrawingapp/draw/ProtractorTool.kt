package com.example.snappydrawingapp.draw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ProtractorTool {
    var center: Offset = Offset.Zero
    var size: Float = 200f
    var angle: Float = 0f
    var isVisible: Boolean = false

    fun draw(drawScope: DrawScope, color: Color = Color.Magenta) {
        val radius = size / 2f
        val startAngle = 0f
        val sweepAngle = 180f
        // Draw the arc (protractor body)
        drawScope.drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(size, size),
            style = Stroke(width = 4f)
        )
        // Draw angle marks every 10 degrees
        for (deg in 0..180 step 10) {
            val rad = deg * PI.toFloat() / 180f + angle
            val x1 = center.x + cos(rad) * (radius - 10)
            val y1 = center.y + sin(rad) * (radius - 10)
            val x2 = center.x + cos(rad) * radius
            val y2 = center.y + sin(rad) * radius
            drawScope.drawLine(
                color = color,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = if (deg % 30 == 0) 4f else 2f
            )
        }
    }
}

