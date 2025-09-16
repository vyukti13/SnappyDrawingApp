package com.example.snappydrawingapp.draw

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.xr.runtime.math.toDegrees
import androidx.xr.runtime.math.toRadians
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class RulerPose(
    val start: Offset,
    val end: Offset,
    val angle: Float = 0f
) {
    val length: Float get() = (end - start).getDistance()
    val center: Offset get() = (start + end) / 2f
    val direction: Offset get() = (end - start).normalized()

    fun getEdgePoint(t: Float): Offset = start + (end - start) * t
}

class RulerTool {
    var pose: RulerPose? by mutableStateOf(null)
    var isVisible: Boolean by mutableStateOf(false)

    private val commonAngles = listOf(0f, 30f, 45f, 60f, 90f, 120f, 135f, 150f, 180f)

    fun snapToAngle(angle: Float): Float {
        val degrees = toDegrees(angle)
        val normalized = ((degrees % 360f) + 360f) % 360f

        return commonAngles.minByOrNull { abs(it - normalized) }?.let {
            toRadians(it)
        } ?: angle
    }

    fun snapToPoint(point: Offset, snapRadius: Float, existingPoints: List<Offset>): Offset? {
        return existingPoints.minByOrNull { (it - point).getDistance() }
            ?.takeIf { (it - point).getDistance() <= snapRadius }
    }

    fun updatePose(
        start: Offset,
        end: Offset,
        snapToAngles: Boolean = true,
        snapToPoints: Boolean = true,
        existingPoints: List<Offset> = emptyList(),
        snapRadius: Float = 20f
    ) {
        var snappedStart = start
        var snappedEnd = end

        if (snapToPoints) {
            snapToPoint(start, snapRadius, existingPoints)?.let { snappedStart = it }
            snapToPoint(end, snapRadius, existingPoints)?.let { snappedEnd = it }
        }

        var angle = atan2(snappedEnd.y - snappedStart.y, snappedEnd.x - snappedStart.x)
        if (snapToAngles) {
            angle = snapToAngle(angle)
            // Recalculate end point with snapped angle
            val length = (end - start).getDistance()
            snappedEnd = snappedStart + Offset(cos(angle), sin(angle)) * length
        }

        pose = RulerPose(snappedStart, snappedEnd, angle)
    }

    fun draw(drawScope: DrawScope, color: Color = Color.Blue, strokeWidthPx: Float = 3f) {
        pose?.let { ruler ->
            // Draw ruler line
            drawScope.drawLine(
                color = color,
                start = ruler.start,
                end = ruler.end,
                strokeWidth = strokeWidthPx
            )

            // Draw ruler markings every 20dp
            val segment = ruler.end - ruler.start
            val segmentLength = segment.getDistance()
            val markingCount = (segmentLength / 20f).toInt()

            for (i in 0..markingCount) {
                val t = i.toFloat() / markingCount
                val point = ruler.getEdgePoint(t)
                val perpendicular = Offset(-segment.y, segment.x).normalized() * 10f

                drawScope.drawLine(
                    color = color,
                    start = point - perpendicular,
                    end = point + perpendicular,
                    strokeWidth = 1f
                )
            }

            // Draw center point
            drawScope.drawCircle(
                color = color,
                radius = 8f,
                center = ruler.center
            )
        }
    }

    fun draw(scope: DrawScope, color: Color = Color.Blue) {
        pose?.let {
            // Draw the ruler line
            scope.drawLine(
                color = color,
                start = it.start,
                end = it.end,
                strokeWidth = 8f
            )
            // Draw handles at both ends
            scope.drawCircle(color, radius = 16f, center = it.start)
            scope.drawCircle(color, radius = 16f, center = it.end)
        }
    }

    fun isPointOnRuler(point: Offset, tolerance: Float = 20f): Boolean {
        return pose?.let { ruler ->
            val segment = ruler.end - ruler.start
            val segmentLength = segment.getDistance()
            if (segmentLength == 0f) return false

            val t = ((point - ruler.start) dot segment) / (segmentLength * segmentLength)
            if (t < 0f || t > 1f) return false

            val closestPoint = ruler.getEdgePoint(t)
            (point - closestPoint).getDistance() <= tolerance
        } ?: false
    }

    // Returns true if the point is within thresholdPx of the ruler line segment
    fun isTouchOnRuler(point: Offset, thresholdPx: Float = 40f): Boolean {
        val pose = pose ?: return false
        val start = pose.start
        val end = pose.end
        val lineVec = end - start
        val pointVec = point - start
        val lineLenSq = lineVec.getDistanceSquared()
        if (lineLenSq == 0f) return false
        val t = ((pointVec.x * lineVec.x + pointVec.y * lineVec.y) / lineLenSq).coerceIn(0f, 1f)
        val projection = start + lineVec * t
        val distance = (point - projection).getDistance()
        return distance <= thresholdPx
    }

    fun getEdgeDirection(): Offset? = pose?.direction
}

private fun Offset.normalized(): Offset {
    val length = getDistance()
    return if (length > 0f) this / length else this
}

private fun Offset.getDistance(): Float = sqrt(x * x + y * y)

private fun Offset.getDistanceSquared(): Float = x * x + y * y

infix fun Offset.dot(other: Offset): Float = x * other.x + y * other.y
