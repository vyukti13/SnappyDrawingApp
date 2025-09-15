package com.example.snappydrawingapp.draw

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
    var menuExpanded by remember { mutableStateOf(false) }

    // SetSquare tool state
    val setSquare = remember { SetSquareTool() }
    var isSetSquareVisible by remember { mutableStateOf(false) }
    var setSquareVariant by remember { mutableStateOf(SetSquareVariant.DEG_45) }
    var draggingSetSquare by remember { mutableStateOf(false) }
    var setSquareDragOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(Unit) {
        // initialize from history (empty by default)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val canvasWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val canvasHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
        val rulerWidthPx = 8f // Set a default stroke width for the ruler
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Snappy Drawing") }, actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Tools Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isSetSquareVisible) "Remove Set Square" else "Add Set Square") },
                            onClick = {
                                isSetSquareVisible = !isSetSquareVisible
                                setSquare.isVisible = isSetSquareVisible
                                menuExpanded = false
                                if (isSetSquareVisible) {
                                    setSquare.center =
                                        Offset(canvasWidthPx / 2f, canvasHeightPx / 2f)
                                    setSquare.size = 200f
                                    setSquare.angle = 0f
                                    setSquare.variant = setSquareVariant
                                }
                            }
                        )
                        if (isSetSquareVisible) {
                            DropdownMenuItem(
                                text = { Text("Set Square Variant: " + if (setSquareVariant == SetSquareVariant.DEG_45) "45°" else "30°–60°") },
                                onClick = {
                                    setSquareVariant =
                                        if (setSquareVariant == SetSquareVariant.DEG_45) SetSquareVariant.DEG_30_60 else SetSquareVariant.DEG_45
                                    setSquare.variant = setSquareVariant
                                    menuExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(if (isRulerMode) "Remove Ruler" else "Add Ruler") },
                            onClick = {
                                isRulerMode = !isRulerMode
                                ruler.isVisible = isRulerMode
                                menuExpanded = false
                                if (isRulerMode) {
                                    // Optionally set initial ruler position/size here
                                    ruler.pose = RulerPose(
                                        Offset(canvasWidthPx / 4f, canvasHeightPx / 2f),
                                        Offset(canvasWidthPx * 3f / 4f, canvasHeightPx / 2f),
                                        0f
                                    )
                                }
                            }
                        )
                    }
                    IconButton(onClick = {
                        history.undo()
                        strokes.clear()
                        strokes.addAll(history.getStrokes())
                    }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Undo") }
                    IconButton(onClick = {
                        history.redo()
                        strokes.clear()
                        strokes.addAll(history.getStrokes())
                    }) { Icon(Icons.Filled.ArrowForward, contentDescription = "Redo") }
                })
            })
        { padding ->
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(top = 56.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures(
                            panZoomLock = false,
                            onGesture = { centroid, pan, zoom, rotation ->
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
                                val newEnd =
                                    center + Offset(cos(newAngle), sin(newAngle)) * halfLength
                                ruler.pose = RulerPose(newStart, newEnd, newAngle)

                                scale = (scale * zoom).coerceIn(0.25f, 4f)
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

                                // --- Set Square Dragging Logic ---
                                if (isSetSquareVisible && setSquare.isVisible && point != null) {
                                    if (pressedPointers == 1 && change != null) {
                                        if (!draggingSetSquare && setSquare.isPointOnSetSquare(
                                                point,
                                                80f
                                            )
                                        ) { // Increased tolerance
                                            // Start dragging set square
                                            draggingSetSquare = true
                                            setSquareDragOffset = point - setSquare.center
                                            println("SetSquare drag started at: $point, offset: $setSquareDragOffset")
                                            change.consume()
                                        }
                                        if (draggingSetSquare) {
                                            setSquare.center = point - setSquareDragOffset
                                            println("SetSquare center updated to: ${setSquare.center}")
                                            change.consume()
                                            continue
                                        }
                                    } else if (pressedPointers == 0 && draggingSetSquare) {
                                        draggingSetSquare = false
                                        setSquareDragOffset = Offset.Zero
                                        println("SetSquare drag ended")
                                        continue
                                    }
                                }

                                // --- Ruler Dragging Logic ---
                                if (isRulerMode && ruler.isVisible && ruler.pose != null) {
                                    if (pressedPointers == 1 && change != null && point != null) {
                                        if (!draggingRuler && ruler.isPointOnRuler(
                                                point,
                                                30f
                                            )
                                        ) {
                                            // Start dragging ruler
                                            draggingRuler = true
                                            dragOffset = point - ruler.pose!!.center
                                            change.consume()
                                        }
                                        if (draggingRuler && dragOffset != null) {
                                            // Move ruler by dragging
                                            val newCenter = point - dragOffset
                                            val length = ruler.pose!!.length
                                            val angle = ruler.pose!!.angle
                                            val halfLength = length / 2f
                                            val newStart = newCenter + Offset(
                                                cos(angle - PI.toFloat()),
                                                sin(angle - PI.toFloat())
                                            ) * halfLength
                                            val newEnd =
                                                newCenter + Offset(
                                                    cos(angle),
                                                    sin(angle)
                                                ) * halfLength
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
                                if ((isRulerMode && ruler.isVisible && ruler.pose != null && !draggingRuler && !(pressedPointers == 1 && change != null && point != null && ruler.isPointOnRuler(
                                        point,
                                        30f
                                    ))) || (!isRulerMode || !ruler.isVisible || ruler.pose == null)
                                ) {
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
                                        history.add(stroke)
                                        strokes.clear()
                                        strokes.addAll(history.getStrokes())
                                        activeStroke = null
                                    }
                                }
                            }
                        }
                    }
            ) {
                withTransform({
                    translate(offset.x, offset.y)
                    scale(scale, scale) // Pass both scaleX and scaleY
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
                            drawPath(
                                path = path,
                                color = s.color,
                                style = Stroke(width = s.widthPx)
                            )
                        }
                    }
                    if (ruler.isVisible && ruler.pose != null) {
                        ruler.draw(this, Color.Blue, rulerWidthPx)
                    }
                    // Draw set square if visible
                    if (isSetSquareVisible && setSquare.isVisible) {
                        setSquare.draw(this, Color.Red)
                    }
                }
            }
        }
    }
}
