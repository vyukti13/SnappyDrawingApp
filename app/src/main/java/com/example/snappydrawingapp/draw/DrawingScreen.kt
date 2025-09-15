package com.example.snappydrawingapp.draw

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import com.example.snappydrawingapp.draw.Stroke as CustomStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(modifier: Modifier = Modifier) {
    val history = remember { History() }
    val strokes = remember { mutableStateListOf<CustomStroke>() }
    var activeStroke by remember { mutableStateOf<CustomStroke?>(null) }
    val ruler = remember { RulerTool() }
    var draggingRuler by remember { mutableStateOf(false) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isRulerMode by remember { mutableStateOf(false) }
    var rulerStart by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(Unit) {
        // initialize from history (empty by default)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color.White)) {
        val canvasWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val canvasHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
        val rulerWidthPx = with(LocalDensity.current) { 20.dp.toPx() }
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Snappy Drawing") }, actions = {
                    IconButton(onClick = {
                        isRulerMode = !isRulerMode
                        ruler.isVisible = isRulerMode
                        if (isRulerMode) {
                            // Set default pose: vertical ruler in the center
                            val centerX = canvasWidthPx / 2f
                            val start = Offset(centerX, 0f)
                            val end = Offset(centerX, canvasHeightPx)
                            ruler.pose = RulerPose(start, end, PI.toFloat() / 2f)
                        } else {
                            ruler.pose = null
                            rulerStart = null
                        }
                    }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Ruler",
                            tint = if (isRulerMode) Color.Blue else Color.Gray
                        )
                    }
                    IconButton(onClick = {
                        if (strokes.isNotEmpty()) {
                            strokes.removeAt(strokes.lastIndex)
                            history.undo()
                        }
                    }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Undo") }
                    IconButton(onClick = {
                        val redone = history.redo()
                        if (redone != null) strokes.add(redone)
                    }) { Icon(Icons.Filled.ArrowForward, contentDescription = "Redo") }
                })
            },
        ) { padding ->
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(top = 56.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures(
                            panZoomLock = false,
                            onGesture = { centroid, pan, zoom, rotation ->
                                if (isRulerMode && ruler.pose != null && rotation != 0f) {
                                    // Two-finger rotate ruler
                                    draggingRuler = true
                                    val currentPose = ruler.pose!!
                                    val center = currentPose.center
                                    val currentAngle = atan2(
                                        currentPose.end.y - currentPose.start.y,
                                        currentPose.end.x - currentPose.start.x
                                    )
                                    val newAngle = currentAngle + rotation
                                    val length = currentPose.length
                                    val halfLength = length / 2f
                                    val newStart = center + Offset(
                                        cos(newAngle - PI.toFloat()),
                                        sin(newAngle - PI.toFloat())
                                    ) * halfLength
                                    val newEnd = center + Offset(cos(newAngle), sin(newAngle)) * halfLength
                                    ruler.pose = RulerPose(newStart, newEnd, newAngle)
                                } else {
                                    scale = (scale * zoom).coerceIn(0.25f, 4f)
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            var dragOffset: Offset? = null
                            while (true) {
                                val event = awaitPointerEvent()
                                val pressedPointers = event.changes.count { it.pressed }
                                val change = event.changes.firstOrNull { it.pressed }
                                val point = change?.position?.let { (it - offset) / scale }

                                // --- Ruler Dragging Logic ---
                                if (isRulerMode && ruler.isVisible && ruler.pose != null) {
                                    if (pressedPointers == 1 && change != null && point != null) {
                                        if (!draggingRuler && ruler.isPointOnRuler(point, 30f)) {
                                            // Start dragging ruler
                                            draggingRuler = true
                                            dragOffset = point - ruler.pose!!.center
                                            change.consume()
                                            continue
                                        }
                                        if (draggingRuler && dragOffset != null) {
                                            // Move ruler by dragging
                                            val newCenter = point - dragOffset!!
                                            val length = ruler.pose!!.length
                                            val angle = ruler.pose!!.angle
                                            val halfLength = length / 2f
                                            val newStart = newCenter + Offset(cos(angle - PI.toFloat()), sin(angle - PI.toFloat())) * halfLength
                                            val newEnd = newCenter + Offset(cos(angle), sin(angle)) * halfLength
                                            ruler.pose = RulerPose(newStart, newEnd, angle)
                                            change.consume()
                                            continue
                                        }
                                    } else if (pressedPointers == 0 && draggingRuler) {
                                        // Stop dragging ruler
                                        draggingRuler = false
                                        dragOffset = null
                                        continue
                                    }
                                }

                                // --- Drawing Logic ---
                                if ((isRulerMode && ruler.isVisible && ruler.pose != null && !draggingRuler && !(pressedPointers == 1 && change != null && point != null && ruler.isPointOnRuler(point, 30f))) || (!isRulerMode || !ruler.isVisible || ruler.pose == null)) {
                                    // Normal drawing mode
                                    if (pressedPointers == 1 && change != null && point != null) {
                                        if (activeStroke == null) {
                                            activeStroke = CustomStroke(mutableListOf(point))
                                        } else {
                                            activeStroke?.points?.add(point)
                                        }
                                        change.consume()
                                    } else if (pressedPointers == 0 && activeStroke != null) {
                                        val stroke = activeStroke!!
                                        strokes.add(stroke)
                                        activeStroke = null
                                    }
                                }
                            }
                        }
                    }
            ) {
                withTransform({
                    translate(offset.x, offset.y)
                    scale(scale)
                }) {
                    strokes.forEach { s: CustomStroke ->
                        if (s.points.size > 1) {
                            val path = Path().apply {
                                moveTo(s.points.first().x, s.points.first().y)
                                for (i in 1 until s.points.size) {
                                    val p = s.points[i]
                                    lineTo(p.x, p.y)
                                }
                            }
                            drawPath(path = path, color = s.color, style = Stroke(width = s.widthPx))
                        }
                    }
                    if (ruler.isVisible && ruler.pose != null) {
                        ruler.draw(this, Color.Blue, rulerWidthPx)
                    }
                }
            }
        }
    }
}
