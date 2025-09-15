package com.example.snappydrawingapp.draw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class SetSquareVariant {
    DEG_45,
    DEG_30_60
}

class SetSquareTool(
    var isVisible: Boolean = false,
    var variant: SetSquareVariant = SetSquareVariant.DEG_45,
    var center: Offset = Offset.Zero,
    var size: Float = 200f, // Default size in px
    var angle: Float = 0f // Rotation angle in radians
) {
    fun draw(scope: DrawScope, color: Color) {
        if (!isVisible) return
        val triangle = getTrianglePoints(center, size, angle, variant)
        scope.drawLine(color, triangle[0], triangle[1], strokeWidth = 6f)
        scope.drawLine(color, triangle[1], triangle[2], strokeWidth = 6f)
        scope.drawLine(color, triangle[2], triangle[0], strokeWidth = 6f)
    }

    fun isPointOnSetSquare(point: Offset, tolerance: Float = 30f): Boolean {
        val triangle = getTrianglePoints(center, size, angle, variant)
        // Check if point is near any edge of the triangle
        for (i in triangle.indices) {
            val a = triangle[i]
            val b = triangle[(i + 1) % triangle.size]
            val ab = b - a
            val abLength = ab.getDistance()
            if (abLength == 0f) continue
            val t = ((point - a) dot ab) / (abLength * abLength)
            if (t < 0f || t > 1f) continue
            val closest = a + ab * t
            if ((point - closest).getDistance() <= tolerance) return true
        }
        // Also check if point is inside the triangle (optional, for easier dragging)
        if (isPointInTriangle(point, triangle[0], triangle[1], triangle[2])) return true
        return false
    }

    private fun isPointInTriangle(p: Offset, a: Offset, b: Offset, c: Offset): Boolean {
        // Barycentric technique
        val v0 = c - a
        val v1 = b - a
        val v2 = p - a
        val dot00 = v0 dot v0
        val dot01 = v0 dot v1
        val dot02 = v0 dot v2
        val dot11 = v1 dot v1
        val dot12 = v1 dot v2
        val invDenom = 1f / (dot00 * dot11 - dot01 * dot01)
        val u = (dot11 * dot02 - dot01 * dot12) * invDenom
        val v = (dot00 * dot12 - dot01 * dot02) * invDenom
        return (u >= 0f) && (v >= 0f) && (u + v < 1f)
    }

    companion object {
        fun getTrianglePoints(center: Offset, size: Float, angle: Float, variant: SetSquareVariant): List<Offset> {
            return when (variant) {
                SetSquareVariant.DEG_45 -> {
                    // Isosceles right triangle (45°)
                    val half = size / 2f
                    val p1 = Offset(center.x - half, center.y + half)
                    val p2 = Offset(center.x + half, center.y + half)
                    val p3 = Offset(center.x - half, center.y - half)
                    listOf(p1, p2, p3).map { rotate(it, center, angle) }
                }
                SetSquareVariant.DEG_30_60 -> {
                    // 30°–60° triangle
                    val h = size * sin(PI / 3).toFloat()
                    val p1 = Offset(center.x - size / 2f, center.y + h / 2f)
                    val p2 = Offset(center.x + size / 2f, center.y + h / 2f)
                    val p3 = Offset(center.x, center.y - h / 2f)
                    listOf(p1, p2, p3).map { rotate(it, center, angle) }
                }
            }
        }
        private fun rotate(point: Offset, center: Offset, angle: Float): Offset {
            val dx = point.x - center.x
            val dy = point.y - center.y
            val cosA = cos(angle)
            val sinA = sin(angle)
            return Offset(
                center.x + dx * cosA - dy * sinA,
                center.y + dx * sinA + dy * cosA
            )
        }
    }
}
