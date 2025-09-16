package com.example.snappydrawingapp.draw

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
    var initialRulerTouch by remember { mutableStateOf(false) }
    var rulerDragOffset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isRulerMode by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    // SetSquare tool state
    val setSquare45 = remember { SetSquareTool() }
    var isSetSquare45Visible by remember { mutableStateOf(false) }
    var setSquare45Variant by remember { mutableStateOf(SetSquareVariant.DEG_45) }
    var draggingSetSquare45 by remember { mutableStateOf(false) }
    var setSquare45DragOffset by remember { mutableStateOf(Offset.Zero) }
    // Make setSquareCenter a mutable state
    var setSquare45Center by remember { mutableStateOf(Offset.Zero) }

    // Sync setSquare.center with setSquareCenter
    LaunchedEffect(isSetSquare45Visible, setSquare45Variant) {
        if (isSetSquare45Visible) {
            setSquare45.center = setSquare45Center
            setSquare45.variant = setSquare45Variant
        }
    }

    // SetSquare tool state
    val setSquare3060 = remember { SetSquareTool() }
    var isSetSquare3060Visible by remember { mutableStateOf(false) }
    var setSquare3060Variant by remember { mutableStateOf(SetSquareVariant.DEG_30_60) }
    var draggingSetSquare3060 by remember { mutableStateOf(false) }
    var setSquare3060DragOffset by remember { mutableStateOf(Offset.Zero) }
    // Make setSquareCenter a mutable state
    var setSquare3060Center by remember { mutableStateOf(Offset.Zero) }

    // Sync setSquare.center with setSquareCenter
    LaunchedEffect(isSetSquare3060Visible, setSquare3060Variant) {
        if (isSetSquare3060Visible) {
            setSquare3060.center = setSquare3060Center
            setSquare3060.variant = setSquare3060Variant
        }
    }

    // Protractor tool state
    val protractor = remember { ProtractorTool() }
    var isProtractorVisible by remember { mutableStateOf(false) }
    var protractorCenter by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(isProtractorVisible) {
        if (isProtractorVisible) {
            protractor.center = protractorCenter
            protractor.size = 200f
            protractor.angle = 0f
            protractor.isVisible = true
        } else {
            protractor.isVisible = false
        }
    }

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
                            text = { Text(if (isSetSquare45Visible) "Remove 45° Set Square" else "Add 45° Set Square") },
                            onClick = {
                                isSetSquare45Visible = !isSetSquare45Visible
                                setSquare45.isVisible = isSetSquare45Visible
                                menuExpanded = false
                                if (isSetSquare45Visible) {
                                    setSquare45Center =
                                        Offset(canvasWidthPx / 3f, canvasHeightPx / 2f)
                                    setSquare45.size = 200f
                                    setSquare45.angle = 0f
                                    setSquare45.variant = SetSquareVariant.DEG_45
                                    setSquare45.center = setSquare45Center
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isSetSquare3060Visible) "Remove 30-60° Set Square" else "Add 30-60° Set Square") },
                            onClick = {
                                isSetSquare3060Visible = !isSetSquare3060Visible
                                setSquare3060.isVisible = isSetSquare3060Visible
                                menuExpanded = false
                                if (isSetSquare3060Visible) {
                                    setSquare3060Center =
                                        Offset(canvasWidthPx * 2f / 3f, canvasHeightPx / 2f)
                                    setSquare3060.size = 200f
                                    setSquare3060.angle = 0f
                                    setSquare3060.variant = SetSquareVariant.DEG_30_60
                                    setSquare3060.center = setSquare3060Center
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isRulerMode) "Remove Ruler" else "Add Ruler") },
                            onClick = {
                                isRulerMode = !isRulerMode
                                ruler.isVisible = isRulerMode
                                menuExpanded = false
                                if (isRulerMode) {
                                    ruler.pose = RulerPose(
                                        Offset(canvasWidthPx / 4f, canvasHeightPx / 2f),
                                        Offset(canvasWidthPx * 3f / 4f, canvasHeightPx / 2f),
                                        0f
                                    )
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isProtractorVisible) "Remove Protractor" else "Add Protractor") },
                            onClick = {
                                isProtractorVisible = !isProtractorVisible
                                protractor.isVisible = isProtractorVisible
                                menuExpanded = false
                                if (isProtractorVisible) {
                                    protractorCenter = Offset(canvasWidthPx / 2f, canvasHeightPx / 2f)
                                    protractor.center = protractorCenter
                                    protractor.size = 200f
                                    protractor.angle = 0f
                                }
                            }
                        )
                    }
                    IconButton(onClick = {
                        history.undo()
                        strokes.clear()
                        strokes.addAll(history.getStrokes())
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Undo") }
                    IconButton(onClick = {
                        history.redo()
                        strokes.clear()
                        strokes.addAll(history.getStrokes())
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Redo") }
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
                                if (!draggingRuler) return@detectTransformGestures
                                if (ruler.pose == null) return@detectTransformGestures // Prevent crash
                                val currentPose = ruler.pose!!
                                // Update center by pan
                                val newCenter = currentPose.center + pan
                                val currentAngle = atan2(
                                    currentPose.end.y - currentPose.start.y,
                                    currentPose.end.x - currentPose.start.x
                                )
                                val newAngle = currentAngle + rotation
                                val length = currentPose.length
                                val halfLength = length / 2f
                                val newStart = newCenter + Offset(
                                    cos(newAngle - PI.toFloat()),
                                    sin(newAngle - PI.toFloat())
                                ) * halfLength
                                val newEnd = newCenter + Offset(cos(newAngle), sin(newAngle)) * halfLength
                                ruler.pose = RulerPose(newStart, newEnd, newAngle)
                                scale = (scale * zoom).coerceIn(0.25f, 4f)
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val pressedPointers = event.changes.count { it.pressed }
                                val change = event.changes.firstOrNull { it.pressed }
                                val point = change?.position?.let { (it - offset) / scale }

                                // --- Ruler Dragging Logic ---
                                if (isRulerMode && ruler.isVisible && ruler.pose != null && point != null) {
                                    if (event.type == androidx.compose.ui.input.pointer.PointerEventType.Press) {
                                        initialRulerTouch = ruler.isTouchOnRuler(point)
                                        draggingRuler = initialRulerTouch
                                        if (draggingRuler) {
                                            // Store offset between touch and center
                                            rulerDragOffset = point - ruler.pose!!.center
                                        }
                                    } else if (event.type == androidx.compose.ui.input.pointer.PointerEventType.Release) {
                                        draggingRuler = false
                                        initialRulerTouch = false
                                        rulerDragOffset = Offset.Zero
                                    }
                                }

                                // --- Set Square Dragging Logic (for both set squares) ---
                                if (isSetSquare45Visible && setSquare45.isVisible && point != null) {
                                    if (pressedPointers == 1 && change != null) {
                                        if (!draggingSetSquare45 && setSquare45.isPointOnSetSquare(point, 120f)) {
                                            draggingSetSquare45 = true
                                            setSquare45DragOffset = point - setSquare45Center
                                            change.consume()
                                        }
                                        if (draggingSetSquare45) {
                                            setSquare45Center = point - setSquare45DragOffset
                                            setSquare45.center = setSquare45Center
                                            change.consume()
                                            continue
                                        }
                                    } else if (pressedPointers == 0 && draggingSetSquare45) {
                                        draggingSetSquare45 = false
                                        setSquare45DragOffset = Offset.Zero
                                        continue
                                    }
                                }
                                if (isSetSquare3060Visible && setSquare3060.isVisible && point != null) {
                                    if (pressedPointers == 1 && change != null) {
                                        if (!draggingSetSquare3060 && setSquare3060.isPointOnSetSquare(point, 120f)) {
                                            draggingSetSquare3060 = true
                                            setSquare3060DragOffset = point - setSquare3060Center
                                            change.consume()
                                            continue
                                        }
                                        if (draggingSetSquare3060) {
                                            setSquare3060.center = setSquare3060Center
                                            change.consume()
                                            continue
                                        }
                                    } else if (pressedPointers == 0 && draggingSetSquare3060) {
                                        draggingSetSquare3060 = false
                                        continue
                                    }
                                }

                                // --- Drawing Logic (always check, unless dragging a tool) ---
                                val canDraw = !draggingSetSquare45 && !draggingSetSquare3060 && !draggingRuler
                                if (canDraw && pressedPointers == 1 && point != null) {
                                    if (activeStroke == null) {
                                        activeStroke = CustomStroke(mutableListOf(point))
                                    } else {
                                        activeStroke?.points?.add(point)
                                    }
                                    change.consume()
                                } else if (canDraw && pressedPointers == 0 && activeStroke != null) {
                                    val stroke = activeStroke!!
                                    history.add(stroke)
                                    strokes.clear()
                                    strokes.addAll(history.getStrokes())
                                    activeStroke = null
                                }
                            }
                        }
                    }
            ) {
                // The receiver here is DrawScope
                withTransform({
                    translate(offset.x, offset.y)
                    scale(scale, scale)
                }) {
                    strokes.forEach { s: CustomStroke ->
                        if (s.points.size > 1) {
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(s.points.first().x, s.points.first().y)
                                for (i in 1 until s.points.size) {
                                    val p = s.points[i]
                                    lineTo(p.x, p.y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = s.color,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = s.widthPx
                                )
                            )
                        }
                    }
                    if (ruler.isVisible && ruler.pose != null) {
                        ruler.draw(this, Color.Blue, 8f)
                    }
                    if (isSetSquare45Visible && setSquare45.isVisible) {
                        setSquare45.draw(this, Color.Red)
                    }
                    if (isSetSquare3060Visible && setSquare3060.isVisible) {
                        setSquare3060.draw(this, Color.Green)
                    }
                    if (protractor.isVisible) {
                        protractor.draw(this, Color.Magenta)
                    }
                }
            }
        }
    }
}
