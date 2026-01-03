package dev.akexorcist.workstation.editor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.data.model.Offset as DataOffset
import dev.akexorcist.workstation.data.model.Position
import dev.akexorcist.workstation.editor.presentation.EditorUiState
import dev.akexorcist.workstation.routing.RoutedConnection
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import dev.akexorcist.workstation.utils.CoordinateTransformer

@Composable
fun LineSegmentOverlay(
    layout: dev.akexorcist.workstation.data.model.WorkstationLayout,
    routedConnectionMap: Map<String, RoutedConnection>,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: DataOffset,
    uiState: EditorUiState,
    onHoverSegment: (String?, Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val borderWidthPx = remember(zoom) { with(density) { 2.dp.toPx() * zoom } }
    val borderColor = WorkstationTheme.themeColor.border
    
    var currentHoveredSegment by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var currentSegmentOrientation by remember { mutableStateOf<SegmentOrientation?>(null) }
    
    val selectedSegment = uiState.selectedLineSegment
    
    val cursorIcon = remember(currentSegmentOrientation) {
        when (currentSegmentOrientation) {
            SegmentOrientation.HORIZONTAL -> PointerIcon.Crosshair
            SegmentOrientation.VERTICAL -> PointerIcon.Crosshair
            else -> PointerIcon.Default
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(layout, routedConnectionMap, canvasSize, zoom, panOffset) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        // Skip hover detection during active pointer press (dragging)
                        if (event.changes.any { it.pressed }) continue
                        
                        val pointerPosition = event.changes.firstOrNull()?.position ?: continue
                        
                        val segmentInfo = findSegmentAtPoint(
                            pointerPosition,
                            layout,
                            routedConnectionMap,
                            canvasSize,
                            zoom,
                            panOffset
                        )
                        
                        if (segmentInfo != null) {
                            if (currentHoveredSegment != segmentInfo.first || currentSegmentOrientation != segmentInfo.second) {
                                currentHoveredSegment = segmentInfo.first
                                currentSegmentOrientation = segmentInfo.second
                                onHoverSegment(segmentInfo.first.first, segmentInfo.first.second)
                            }
                        } else {
                            if (currentHoveredSegment != null) {
                                currentHoveredSegment = null
                                currentSegmentOrientation = null
                                onHoverSegment(null, null)
                            }
                        }
                    }
                }
            }
            .pointerHoverIcon(cursorIcon)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (selectedSegment != null) {
                val routedConnection = routedConnectionMap[selectedSegment.first] ?: return@Canvas
                val virtualWaypoints = routedConnection.virtualWaypoints
                
                if (virtualWaypoints.size >= 2 && selectedSegment.second < virtualWaypoints.size - 1) {
                    val startIndex = selectedSegment.second
                    val endIndex = selectedSegment.second + 1
                    
                    val startWaypoint = virtualWaypoints[startIndex]
                    val endWaypoint = virtualWaypoints[endIndex]
                    
                    val startScreen = CoordinateTransformer.transformPosition(
                        Position(startWaypoint.first, startWaypoint.second),
                        layout.metadata,
                        canvasSize,
                        zoom,
                        panOffset
                    )
                    val endScreen = CoordinateTransformer.transformPosition(
                        Position(endWaypoint.first, endWaypoint.second),
                        layout.metadata,
                        canvasSize,
                        zoom,
                        panOffset
                    )
                    
                    drawLine(
                        color = borderColor,
                        start = Offset(startScreen.x, startScreen.y),
                        end = Offset(endScreen.x, endScreen.y),
                        strokeWidth = borderWidthPx,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
    }
}

internal enum class SegmentOrientation {
    HORIZONTAL,
    VERTICAL
}

internal fun findSegmentAtPoint(
    point: Offset,
    layout: dev.akexorcist.workstation.data.model.WorkstationLayout,
    routedConnectionMap: Map<String, RoutedConnection>,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: DataOffset
): Pair<Pair<String, Int>, SegmentOrientation>? {
    val hitThreshold = 10f
    
    var closestSegment: Pair<Pair<String, Int>, SegmentOrientation>? = null
    var closestDistance = Float.MAX_VALUE
    
    layout.connections.forEach { connection ->
        val routedConnection = routedConnectionMap[connection.id] ?: return@forEach
        val virtualWaypoints = routedConnection.virtualWaypoints
        
        if (virtualWaypoints.size < 2) return@forEach
        
        for (i in 0 until virtualWaypoints.size - 1) {
            val startWaypoint = virtualWaypoints[i]
            val endWaypoint = virtualWaypoints[i + 1]
            
            val startScreen = CoordinateTransformer.transformPosition(
                Position(startWaypoint.first, startWaypoint.second),
                layout.metadata,
                canvasSize,
                zoom,
                panOffset
            )
            val endScreen = CoordinateTransformer.transformPosition(
                Position(endWaypoint.first, endWaypoint.second),
                layout.metadata,
                canvasSize,
                zoom,
                panOffset
            )
            
            val start = Offset(startScreen.x, startScreen.y)
            val end = Offset(endScreen.x, endScreen.y)
            
            if (isOrthogonalSegment(start, end)) {
                val distance = calculateDistanceToSegment(point, start, end)
                if (distance < hitThreshold && distance < closestDistance) {
                    closestDistance = distance
                    closestSegment = Pair(
                        Pair(connection.id, i),
                        if (start.y == end.y) SegmentOrientation.HORIZONTAL else SegmentOrientation.VERTICAL
                    )
                }
            }
        }
    }
    
    return closestSegment
}

private fun isOrthogonalSegment(start: Offset, end: Offset): Boolean {
    return start.x == end.x || start.y == end.y
}

private fun calculateDistanceToSegment(point: Offset, segmentStart: Offset, segmentEnd: Offset): Float {
    val dx = segmentEnd.x - segmentStart.x
    val dy = segmentEnd.y - segmentStart.y
    
    if (dx == 0f && dy == 0f) {
        return kotlin.math.sqrt(
            (point.x - segmentStart.x) * (point.x - segmentStart.x) +
            (point.y - segmentStart.y) * (point.y - segmentStart.y)
        )
    }
    
    val t = ((point.x - segmentStart.x) * dx + (point.y - segmentStart.y) * dy) / (dx * dx + dy * dy)
    val clampedT = t.coerceIn(0f, 1f)
    
    val closestPoint = Offset(
        segmentStart.x + clampedT * dx,
        segmentStart.y + clampedT * dy
    )
    
    return kotlin.math.sqrt(
        (point.x - closestPoint.x) * (point.x - closestPoint.x) +
        (point.y - closestPoint.y) * (point.y - closestPoint.y)
    )
}

internal fun constrainDragToCrossAxis(
    dragDelta: Offset,
    orientation: SegmentOrientation
): Offset {
    return when (orientation) {
        SegmentOrientation.HORIZONTAL -> Offset(0f, dragDelta.y)
        SegmentOrientation.VERTICAL -> Offset(dragDelta.x, 0f)
    }
}

