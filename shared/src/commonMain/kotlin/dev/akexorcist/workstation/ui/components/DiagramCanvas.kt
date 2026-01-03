package dev.akexorcist.workstation.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StampedPathEffectStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.data.model.Device
import dev.akexorcist.workstation.data.model.DeviceSide
import dev.akexorcist.workstation.data.model.LayoutMetadata
import dev.akexorcist.workstation.data.model.Port
import dev.akexorcist.workstation.data.model.PortDirection
import dev.akexorcist.workstation.data.model.Position
import dev.akexorcist.workstation.presentation.config.RenderingConfig
import dev.akexorcist.workstation.routing.RoutedConnection
import dev.akexorcist.workstation.ui.theme.ThemeColor
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import dev.akexorcist.workstation.utils.CoordinateTransformer
import dev.akexorcist.workstation.utils.DeviceConnectionInfo

/**
 * Renders connections and ports using Canvas for efficient line drawing.
 */
@Composable
fun ConnectionCanvas(
    layout: dev.akexorcist.workstation.data.model.WorkstationLayout,
    zoom: Float,
    panOffset: dev.akexorcist.workstation.data.model.Offset,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    viewportSize: Size,
    routedConnectionMap: Map<String, RoutedConnection>,
    selectedConnectionId: String?,
    isAnimationEnabled: Boolean = RenderingConfig.connectionAnimationEnabled,
    hoveredDeviceId: String? = null,
    hoveredPortInfo: String? = null,
    relatedConnectionsMap: Map<String, Boolean> = emptyMap()
) {
    val opacityTargets = remember { mutableMapOf<String, Float>() }
    val currentOpacities = remember { mutableMapOf<String, Float>() }

    fun getAnimatedOpacity(key: String, targetValue: Float): Float {
        opacityTargets[key] = targetValue
        val current = currentOpacities[key] ?: 1f
        val newValue = if (current == targetValue) {
            current
        } else {
            current + (targetValue - current) * 0.1f
        }
        currentOpacities[key] = newValue
        return newValue
    }

    val phase = if (isAnimationEnabled) {
        val infiniteTransition = rememberInfiniteTransition()
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = RenderingConfig.connectionAnimationDuration,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        mutableStateOf(0f)
    }
    val connectionTheme = WorkstationTheme.themeColor.connection
    val hoveredConnectionId: String? = null

    val deviceMap = layout.devices.associateBy { it.id }

    val isHoverHighlightActive = hoveredDeviceId != null || hoveredPortInfo != null

    Canvas(modifier = Modifier.fillMaxSize()) {
        val visibleConnections = layout.connections.filter { connection ->
            val sourceDevice = deviceMap[connection.sourceDeviceId]
            val targetDevice = deviceMap[connection.targetDeviceId]

            if (sourceDevice != null && targetDevice != null) {
                val sourcePort = sourceDevice.ports.find { it.id == connection.sourcePortId }
                val targetPort = targetDevice.ports.find { it.id == connection.targetPortId }

                if (sourcePort != null && targetPort != null) {
                    val sourcePosition = adjustPortPositionToCenter(
                        calculatePortScreenPosition(
                            sourceDevice, sourcePort, layout.metadata, canvasSize, zoom, panOffset
                        ),
                        sourcePort,
                        zoom
                    )
                    val targetPosition = adjustPortPositionToCenter(
                        calculatePortScreenPosition(
                            targetDevice, targetPort, layout.metadata, canvasSize, zoom, panOffset
                        ),
                        targetPort,
                        zoom
                    )

                    val routedConnection = routedConnectionMap[connection.id]
                    if (routedConnection != null) {
                        val path = routedConnection.virtualWaypoints.mapIndexed { index, (vx, vy) ->
                            val screenPoint = virtualToScreen(vx, vy, layout.metadata, canvasSize, zoom, panOffset)
                            // Adjust first and last waypoints to port centers
                            when (index) {
                                0 -> adjustPortPositionToCenter(
                                    Offset(screenPoint.x, screenPoint.y),
                                    sourcePort,
                                    zoom
                                )
                                routedConnection.virtualWaypoints.size - 1 -> adjustPortPositionToCenter(
                                    Offset(screenPoint.x, screenPoint.y),
                                    targetPort,
                                    zoom
                                )
                                else -> Offset(screenPoint.x, screenPoint.y)
                            }
                        }
                        isPathVisible(path, viewportSize)
                    } else {
                        val path = calculateOrthogonalPath(
                            sourcePosition, targetPosition,
                            sourcePort.position.side, targetPort.position.side, zoom
                        )
                        isPathVisible(path, viewportSize)
                    }
                } else {
                    false
                }
            } else {
                false
            }
        }

        visibleConnections.forEach { connection ->
            val sourceDevice = deviceMap[connection.sourceDeviceId]
            val targetDevice = deviceMap[connection.targetDeviceId]

            if (sourceDevice != null && targetDevice != null) {
                val sourcePort = sourceDevice.ports.find { it.id == connection.sourcePortId }
                val targetPort = targetDevice.ports.find { it.id == connection.targetPortId }

                if (sourcePort != null && targetPort != null) {
                    val sourcePosition = adjustPortPositionToCenter(
                        calculatePortScreenPosition(
                            sourceDevice, sourcePort, layout.metadata, canvasSize, zoom, panOffset
                        ),
                        sourcePort,
                        zoom
                    )
                    val targetPosition = adjustPortPositionToCenter(
                        calculatePortScreenPosition(
                            targetDevice, targetPort, layout.metadata, canvasSize, zoom, panOffset
                        ),
                        targetPort,
                        zoom
                    )

                    val routedConnection = routedConnectionMap[connection.id]

                    if (routedConnection != null && !routedConnection.success) {
                        val failedLineColor = ThemeColor.Pink500.copy(alpha = RenderingConfig.failedRouteAlpha)
                        val failedStrokeWidth = RenderingConfig.connectionWidth * zoom * RenderingConfig.failedRouteWidthMultiplier

                        val path = routedConnection.virtualWaypoints.mapIndexed { index, (vx, vy) ->
                            val screenPoint = virtualToScreen(vx, vy, layout.metadata, canvasSize, zoom, panOffset)
                            // Adjust first and last waypoints to port centers
                            when (index) {
                                0 -> adjustPortPositionToCenter(
                                    Offset(screenPoint.x, screenPoint.y),
                                    sourcePort,
                                    zoom
                                )
                                routedConnection.virtualWaypoints.size - 1 -> adjustPortPositionToCenter(
                                    Offset(screenPoint.x, screenPoint.y),
                                    targetPort,
                                    zoom
                                )
                                else -> Offset(screenPoint.x, screenPoint.y)
                            }
                        }

                        for (i in 0 until path.size - 1) {
                            drawLine(
                                color = failedLineColor,
                                start = path[i],
                                end = path[i + 1],
                                strokeWidth = failedStrokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    } else {
                        val path = routedConnection?.virtualWaypoints?.mapIndexed { index, (vx, vy) ->
                            val screenPoint = virtualToScreen(vx, vy, layout.metadata, canvasSize, zoom, panOffset)
                            // Adjust first and last waypoints to port centers
                            when (index) {
                                0 -> adjustPortPositionToCenter(
                                    Offset(screenPoint.x, screenPoint.y),
                                    sourcePort,
                                    zoom
                                )
                                routedConnection.virtualWaypoints.size - 1 -> adjustPortPositionToCenter(
                                    Offset(screenPoint.x, screenPoint.y),
                                    targetPort,
                                    zoom
                                )
                                else -> Offset(screenPoint.x, screenPoint.y)
                            }
                        } ?: calculateOrthogonalPath(
                            sourcePosition, targetPosition,
                            sourcePort.position.side, targetPort.position.side, zoom
                        )

                        val isSelected = connection.id == selectedConnectionId
                        val isHovered = connection.id == hoveredConnectionId
                        val sourceDirection = sourcePort.direction
                        val targetDirection = targetPort.direction

                        val (startBackgroundColor, endBackgroundColor) = when (sourceDirection) {
                            PortDirection.OUTPUT if targetDirection == PortDirection.INPUT -> {
                                val outputBg = when {
                                    isSelected -> connectionTheme.outputBackgroundActiveColor
                                    isHovered -> connectionTheme.outputBackgroundActiveColor.copy(alpha = 0.7f)
                                    else -> connectionTheme.outputBackgroundInactiveColor
                                }
                                val inputBg = when {
                                    isSelected -> connectionTheme.inputBackgroundActiveColor
                                    isHovered -> connectionTheme.inputBackgroundActiveColor.copy(alpha = 0.7f)
                                    else -> connectionTheme.inputBackgroundInactiveColor
                                }
                                Pair(outputBg, inputBg)
                            }
                            PortDirection.INPUT if targetDirection == PortDirection.OUTPUT -> {
                                val inputBg = when {
                                    isSelected -> connectionTheme.inputBackgroundActiveColor
                                    isHovered -> connectionTheme.inputBackgroundActiveColor.copy(alpha = 0.7f)
                                    else -> connectionTheme.inputBackgroundInactiveColor
                                }
                                val outputBg = when {
                                    isSelected -> connectionTheme.outputBackgroundActiveColor
                                    isHovered -> connectionTheme.outputBackgroundActiveColor.copy(alpha = 0.7f)
                                    else -> connectionTheme.outputBackgroundInactiveColor
                                }
                                Pair(inputBg, outputBg)
                            }
                            else -> {
                                val inputBg = when {
                                    isSelected -> connectionTheme.inputBackgroundActiveColor
                                    isHovered -> connectionTheme.inputBackgroundActiveColor.copy(alpha = 0.7f)
                                    else -> connectionTheme.inputBackgroundInactiveColor
                                }
                                val outputBg = when {
                                    isSelected -> connectionTheme.outputBackgroundActiveColor
                                    isHovered -> connectionTheme.outputBackgroundActiveColor.copy(alpha = 0.7f)
                                    else -> connectionTheme.outputBackgroundInactiveColor
                                }
                                Pair(inputBg, outputBg)
                            }
                        }

                        // Always use active colors for the foreground path
                        val (startForegroundColor, endForegroundColor) = when (sourceDirection) {
                            PortDirection.OUTPUT if targetDirection == PortDirection.INPUT -> {
                                // Output to Input
                                Pair(connectionTheme.outputActiveColor, connectionTheme.inputActiveColor)
                            }
                            PortDirection.INPUT if targetDirection == PortDirection.OUTPUT -> {
                                // Input to Output
                                Pair(connectionTheme.inputActiveColor, connectionTheme.outputActiveColor)
                            }
                            else -> {
                                // Default case - either both are the same or unknown
                                Pair(connectionTheme.inputActiveColor, connectionTheme.outputActiveColor)
                            }
                        }

                        // Apply opacity for unrelated connections during hover
                        val isRelated = !isHoverHighlightActive || relatedConnectionsMap[connection.id] == true

                        // Get the current animated opacity value for this connection
                        val targetOpacity = if (isRelated) 1f else RenderingConfig.unrelatedConnectionOpacity
                        val connectionKey = "connection-${connection.id}"
                        val opacityMultiplier = getAnimatedOpacity(connectionKey, targetOpacity)

                        val adjustedStartBg = startBackgroundColor.copy(alpha = startBackgroundColor.alpha * opacityMultiplier)
                        val adjustedEndBg = endBackgroundColor.copy(alpha = endBackgroundColor.alpha * opacityMultiplier)
                        val adjustedStartFg = startForegroundColor.copy(alpha = startForegroundColor.alpha * opacityMultiplier)
                        val adjustedEndFg = endForegroundColor.copy(alpha = endForegroundColor.alpha * opacityMultiplier)

                        drawGradientConnectionPath(
                            path = path,
                            startBackgroundColor = adjustedStartBg,
                            endBackgroundColor = adjustedEndBg,
                            startForegroundColor = adjustedStartFg,
                            endForegroundColor = adjustedEndFg,
                            zoom = zoom,
                            phase = if (isAnimationEnabled) phase.value else -1f,
                        )
                    }
                }
            }
        }
    }
}

// Helper functions for port position calculation
private fun calculatePortScreenPosition(
    device: Device,
    port: Port,
    metadata: LayoutMetadata,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: dev.akexorcist.workstation.data.model.Offset
): Offset {
    val virtualPortX: Float
    val virtualPortY: Float

    when (port.position.side) {
        DeviceSide.TOP -> {
            val halfWidth = device.size.width / 2f
            val portOffset = port.position.position.coerceIn(-halfWidth, halfWidth)
            virtualPortX = device.position.x + halfWidth + portOffset
            virtualPortY = device.position.y
        }

        DeviceSide.BOTTOM -> {
            val halfWidth = device.size.width / 2f
            val portOffset = port.position.position.coerceIn(-halfWidth, halfWidth)
            virtualPortX = device.position.x + halfWidth + portOffset
            virtualPortY = device.position.y + device.size.height
        }

        DeviceSide.LEFT -> {
            val halfHeight = device.size.height / 2f
            val portOffset = port.position.position.coerceIn(-halfHeight, halfHeight)
            virtualPortX = device.position.x
            virtualPortY = device.position.y + halfHeight + portOffset
        }

        DeviceSide.RIGHT -> {
            val halfHeight = device.size.height / 2f
            val portOffset = port.position.position.coerceIn(-halfHeight, halfHeight)
            virtualPortX = device.position.x + device.size.width
            virtualPortY = device.position.y + halfHeight + portOffset
        }
    }

    val gridCellSize = 10f
    var gridX = (virtualPortX / gridCellSize).toInt()
    var gridY = (virtualPortY / gridCellSize).toInt()

    val deviceGridLeft = (device.position.x / gridCellSize).toInt()
    val deviceGridTop = (device.position.y / gridCellSize).toInt()

    when (port.position.side) {
        DeviceSide.TOP -> {
            if (gridY >= deviceGridTop) gridY = deviceGridTop - 1
        }

        DeviceSide.LEFT -> {
            if (gridX >= deviceGridLeft) gridX = deviceGridLeft - 1
        }

        DeviceSide.BOTTOM,
        DeviceSide.RIGHT -> {
        }
    }

    val snappedVirtualX = gridX * gridCellSize + (gridCellSize / 2f)
    val snappedVirtualY = gridY * gridCellSize + (gridCellSize / 2f)

    // Transform snapped virtual position to screen space
    val snappedVirtual = Position(snappedVirtualX, snappedVirtualY)
    val screenPos = CoordinateTransformer.transformPosition(
        snappedVirtual,
        metadata,
        canvasSize,
        zoom,
        panOffset
    )

    return Offset(screenPos.x, screenPos.y)
}

private fun adjustPortPositionToCenter(
    portPosition: Offset,
    port: Port,
    zoom: Float
): Offset {
    val capsuleHeight = RenderingConfig.portCapsuleHeight * zoom
    val overlap = RenderingConfig.portDeviceOverlap * zoom
    val capsuleWidth = getEstimatedPortWidth(port, zoom)

    return when (port.position.side) {
        DeviceSide.TOP -> {
            Offset(
                portPosition.x + (capsuleWidth / 4f),
                portPosition.y - (capsuleHeight / 2f) + overlap
            )
        }
        DeviceSide.BOTTOM -> {
            Offset(
                portPosition.x + (capsuleWidth / 4f),
                portPosition.y + (capsuleHeight / 2f) - overlap
            )
        }
        DeviceSide.LEFT,
        DeviceSide.RIGHT -> {
            Offset(portPosition.x, portPosition.y + (capsuleHeight / 4f))
        }
    }
}

private fun calculateOrthogonalPath(
    start: Offset,
    end: Offset,
    startSide: DeviceSide,
    endSide: DeviceSide,
    zoom: Float
): List<Offset> {
    val path = mutableListOf<Offset>()
    path.add(start)

    val extensionDistance = 30f * zoom

    val startExit = when (startSide) {
        DeviceSide.TOP ->
            Offset(start.x, start.y - extensionDistance)

        DeviceSide.BOTTOM ->
            Offset(start.x, start.y + extensionDistance)

        DeviceSide.LEFT ->
            Offset(start.x - extensionDistance, start.y)

        DeviceSide.RIGHT ->
            Offset(start.x + extensionDistance, start.y)
    }

    val endEntry = when (endSide) {
        DeviceSide.TOP ->
            Offset(end.x, end.y - extensionDistance)

        DeviceSide.BOTTOM ->
            Offset(end.x, end.y + extensionDistance)

        DeviceSide.LEFT ->
            Offset(end.x - extensionDistance, end.y)

        DeviceSide.RIGHT ->
            Offset(end.x + extensionDistance, end.y)
    }

    path.add(startExit)

    val isHorizontalStart = startSide == DeviceSide.LEFT ||
            startSide == DeviceSide.RIGHT
    val isHorizontalEnd = endSide == DeviceSide.LEFT ||
            endSide == DeviceSide.RIGHT

    when {
        isHorizontalStart == isHorizontalEnd -> {
            if (isHorizontalStart) {
                val midX = (startExit.x + endEntry.x) / 2
                path.add(Offset(midX, startExit.y))
                path.add(Offset(midX, endEntry.y))
            } else {
                val midY = (startExit.y + endEntry.y) / 2
                path.add(Offset(startExit.x, midY))
                path.add(Offset(endEntry.x, midY))
            }
        }

        else -> {
            if (isHorizontalStart) {
                path.add(Offset(startExit.x, endEntry.y))
            } else {
                path.add(Offset(endEntry.x, startExit.y))
            }
        }
    }

    path.add(endEntry)
    path.add(end)

    return path
}

private fun DrawScope.drawGradientConnectionPath(
    path: List<Offset>,
    startBackgroundColor: Color,
    endBackgroundColor: Color,
    startForegroundColor: Color,
    endForegroundColor: Color,
    zoom: Float,
    phase: Float = 0f,
) {
    if (path.size < 2) return

    val backgroundWidth = RenderingConfig.connectionBackgroundWidth * zoom
    val foregroundWidth = RenderingConfig.connectionForegroundWidth * zoom

    var totalPathLength = 0f
    val segmentLengths = mutableListOf<Float>()

    for (i in 0 until path.size - 1) {
        val length = Offset(
            path[i + 1].x - path[i].x,
            path[i + 1].y - path[i].y
        ).getDistance()
        segmentLengths.add(length)
        totalPathLength += length
    }

    val useCurvedCorners = phase >= 0f
    
    val backgroundPath = if (useCurvedCorners) {
        createCurvedPath(path, RenderingConfig.connectionCornerRadius * zoom)
    } else {
        Path().apply {
            if (path.isNotEmpty()) {
                moveTo(path[0].x, path[0].y)
                for (i in 1 until path.size) {
                    lineTo(path[i].x, path[i].y)
                }
            }
        }
    }

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(startBackgroundColor, endBackgroundColor),
        start = path.first(),
        end = path.last()
    )
    drawPath(
        path = backgroundPath,
        brush = backgroundBrush,
        style = Stroke(
            width = backgroundWidth,
            cap = StrokeCap.Round,
            join = if (useCurvedCorners) StrokeJoin.Round else StrokeJoin.Miter
        )
    )

    val foregroundPath = if (useCurvedCorners) {
        createCurvedPath(path, RenderingConfig.connectionCornerRadius * zoom)
    } else {
        Path().apply {
            if (path.isNotEmpty()) {
                moveTo(path[0].x, path[0].y)
                for (i in 1 until path.size) {
                    lineTo(path[i].x, path[i].y)
                }
            }
        }
    }

    // Create oval path for stamped effect
    val ovalWidth = foregroundWidth * 0.9f
    val ovalHeight = ovalWidth
    val ovalPath = Path().apply {
        addOval(
            Rect(
                -ovalWidth / 2f,
                -ovalHeight / 2f,
                ovalWidth / 2f,
                ovalHeight / 2f
            )
        )
    }

    val stampSpacing = ovalWidth * RenderingConfig.connectionAnimationStampSpacing
    val lineCornerRadius = ovalWidth / 2f

    val foregroundBrush = Brush.linearGradient(
        colors = listOf(startForegroundColor, endForegroundColor),
        start = path.first(),
        end = path.last()
    )
    
    val pathEffect = if (phase >= 0f) {
        PathEffect.chainPathEffect(
            outer = PathEffect.stampedPathEffect(
                shape = ovalPath,
                style = StampedPathEffectStyle.Translate,
                phase = phase * RenderingConfig.connectionAnimationPhaseScale * zoom,
                advance = stampSpacing,
            ),
            inner = PathEffect.cornerPathEffect(lineCornerRadius),
        )
    } else {
        null
    }
    
    drawPath(
        path = foregroundPath,
        brush = foregroundBrush,
        style = Stroke(
            width = foregroundWidth,
            pathEffect = pathEffect,
            cap = StrokeCap.Round,
            join = if (useCurvedCorners) StrokeJoin.Round else StrokeJoin.Miter
        )
    )
}

/**
 * Check if any segment of a path is visible in the viewport with culling margin.
 */
private fun isPathVisible(
    path: List<Offset>,
    viewportSize: Size
): Boolean {
    if (path.isEmpty()) return false

    val cullingMargin = 50f

    val viewportLeft = -cullingMargin
    val viewportRight = viewportSize.width + cullingMargin
    val viewportTop = -cullingMargin
    val viewportBottom = viewportSize.height + cullingMargin

    for (i in 0 until path.size - 1) {
        val start = path[i]
        val end = path[i + 1]

        if (isLineSegmentVisible(start, end, viewportLeft, viewportRight, viewportTop, viewportBottom)) {
            return true
        }
    }

    return false
}

private fun isLineSegmentVisible(
    start: Offset,
    end: Offset,
    viewportLeft: Float,
    viewportRight: Float,
    viewportTop: Float,
    viewportBottom: Float
): Boolean {
    if (isPointInViewport(start, viewportLeft, viewportRight, viewportTop, viewportBottom) ||
        isPointInViewport(end, viewportLeft, viewportRight, viewportTop, viewportBottom)
    ) {
        return true
    }

    if (lineIntersectsVerticalLine(start, end, viewportLeft, viewportTop, viewportBottom)) {
        return true
    }

    if (lineIntersectsVerticalLine(start, end, viewportRight, viewportTop, viewportBottom)) {
        return true
    }

    if (lineIntersectsHorizontalLine(start, end, viewportTop, viewportLeft, viewportRight)) {
        return true
    }

    if (lineIntersectsHorizontalLine(start, end, viewportBottom, viewportLeft, viewportRight)) {
        return true
    }

    return false
}

private fun isPointInViewport(
    point: Offset,
    viewportLeft: Float,
    viewportRight: Float,
    viewportTop: Float,
    viewportBottom: Float
): Boolean {
    return point.x in viewportLeft..viewportRight &&
            point.y >= viewportTop && point.y <= viewportBottom
}

private fun lineIntersectsVerticalLine(
    start: Offset,
    end: Offset,
    x: Float,
    yMin: Float,
    yMax: Float
): Boolean {
    if ((start.x <= x && end.x >= x) || (start.x >= x && end.x <= x)) {
        val t = if (end.x - start.x != 0f) {
            (x - start.x) / (end.x - start.x)
        } else {
            return false
        }

        val y = start.y + t * (end.y - start.y)
        return y in yMin..yMax
    }
    return false
}

private fun lineIntersectsHorizontalLine(
    start: Offset,
    end: Offset,
    y: Float,
    xMin: Float,
    xMax: Float
): Boolean {
    if ((start.y <= y && end.y >= y) || (start.y >= y && end.y <= y)) {
        val t = if (end.y - start.y != 0f) {
            (y - start.y) / (end.y - start.y)
        } else {
            return false
        }

        val x = start.x + t * (end.x - start.x)
        return x in xMin..xMax
    }
    return false
}

private fun dev.akexorcist.workstation.data.model.Offset.toComposeOffset(): Offset {
    return Offset(x = this.x, y = this.y)
}

private fun virtualToScreen(
    virtualX: Float,
    virtualY: Float,
    metadata: LayoutMetadata,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: dev.akexorcist.workstation.data.model.Offset
): Offset {
    val position = Position(virtualX, virtualY)
    return CoordinateTransformer.transformPosition(position, metadata, canvasSize, zoom, panOffset)
}

/**
 * Creates a path with rounded corners using Bezier curves
 * @param points List of points that form the path
 * @param cornerRadius Radius for the rounded corners
 * @return Path object with Bezier curves for corners
 */
private fun createCurvedPath(points: List<Offset>, cornerRadius: Float): Path {
    if (points.size < 2) return Path()

    val path = Path()
    path.moveTo(points[0].x, points[0].y)

    // If only two points, just draw a straight line
    if (points.size == 2) {
        path.lineTo(points[1].x, points[1].y)
        return path
    }

    // First segment - always a line from start to first corner
    val firstPoint = points[0]
    val firstCorner = points[1]

    // Calculate distance from start to first corner
    val firstSegmentLength = kotlin.math.sqrt(
        (firstCorner.x - firstPoint.x) * (firstCorner.x - firstPoint.x) +
                (firstCorner.y - firstPoint.y) * (firstCorner.y - firstPoint.y)
    )

    // If the first segment is shorter than cornerRadius*2, use half the length as cornerRadius
    val firstCornerRadius = kotlin.math.min(cornerRadius, firstSegmentLength / 2)

    // Calculate the point where the curve should start
    val firstCurveStart = Offset(
        firstPoint.x + (firstCorner.x - firstPoint.x) * (1 - firstCornerRadius / firstSegmentLength),
        firstPoint.y + (firstCorner.y - firstPoint.y) * (1 - firstCornerRadius / firstSegmentLength)
    )

    // Draw line from start to the curve start point
    path.lineTo(firstCurveStart.x, firstCurveStart.y)

    // Process each corner point except the first and last
    for (i in 1 until points.size - 1) {
        val prev = points[i - 1]
        val current = points[i]
        val next = points[i + 1]

        // Calculate incoming and outgoing vectors
        val incomingX = current.x - prev.x
        val incomingY = current.y - prev.y
        val outgoingX = next.x - current.x
        val outgoingY = next.y - current.y

        // Calculate lengths of incoming and outgoing segments
        val incomingLength = kotlin.math.sqrt(incomingX * incomingX + incomingY * incomingY)
        val outgoingLength = kotlin.math.sqrt(outgoingX * outgoingX + outgoingY * outgoingY)

        // Limit corner radius based on segment lengths
        val maxRadius = kotlin.math.min(incomingLength, outgoingLength) / 2
        val actualCornerRadius = kotlin.math.min(cornerRadius, maxRadius)

        // Only proceed if we have a valid radius and non-zero segments
        if (actualCornerRadius > 0 && incomingLength > 0 && outgoingLength > 0) {
            // Normalize incoming and outgoing vectors
            val incomingNormX = incomingX / incomingLength
            val incomingNormY = incomingY / incomingLength
            val outgoingNormX = outgoingX / outgoingLength
            val outgoingNormY = outgoingY / outgoingLength

            // Calculate start and end points of the curve
            val curveStart = Offset(
                current.x - incomingNormX * actualCornerRadius,
                current.y - incomingNormY * actualCornerRadius
            )

            val curveEnd = Offset(
                current.x + outgoingNormX * actualCornerRadius,
                current.y + outgoingNormY * actualCornerRadius
            )

            // Calculate control points for the curve
            val controlPoint = current

            // Draw the curve
            path.lineTo(curveStart.x, curveStart.y)
            path.quadraticTo(
                controlPoint.x, controlPoint.y,
                curveEnd.x, curveEnd.y
            )
        } else {
            // If we can't create a curve, just draw a straight line to the point
            path.lineTo(current.x, current.y)
        }
    }

    // Final segment - from last curve to end point
    path.lineTo(points.last().x, points.last().y)

    return path
}

@Composable
fun PortsOverlay(
    layout: dev.akexorcist.workstation.data.model.WorkstationLayout,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: dev.akexorcist.workstation.data.model.Offset,
    viewportSize: Size,
    hoveredDeviceId: String? = null,
    hoveredPortInfo: String? = null,
    relatedPortsMap: Map<String, Boolean> = emptyMap(),
    density: Float = 1f,
    onHoverPort: (String?, Boolean) -> Unit = { _, _ -> }
) {
    val isHoverHighlightActive = hoveredDeviceId != null || hoveredPortInfo != null

    // Render each port with its own width based on content
    layout.devices.forEach { device ->
        device.ports.forEach { port ->
            val portPosition = calculatePortScreenPosition(
                device, port, layout.metadata, canvasSize, zoom, panOffset
            )

            // Use fixed height that scales with zoom for consistency
            val capsuleHeight = RenderingConfig.portCapsuleHeight * zoom

            // Calculate individual width for this port based on its content
            val capsuleWidth = getEstimatedPortWidth(port, zoom)

            // Determine clip edge and position based on device side
            val clipEdge: String

            val overlap = RenderingConfig.portDeviceOverlap * zoom

            val adjustedPosition = when (port.position.side) {
                DeviceSide.LEFT -> {
                    clipEdge = "right"
                    val deviceEdgeX = portPosition.x
                    Offset(deviceEdgeX - capsuleWidth + overlap, portPosition.y - capsuleHeight / 2)
                }

                DeviceSide.RIGHT -> {
                    clipEdge = "left"
                    Offset(portPosition.x - overlap, portPosition.y - capsuleHeight / 2)
                }

                DeviceSide.TOP -> {
                    clipEdge = "bottom"
                    Offset(portPosition.x - capsuleWidth / 2, portPosition.y - capsuleHeight + overlap)
                }

                DeviceSide.BOTTOM -> {
                    clipEdge = "top"
                    Offset(portPosition.x - capsuleWidth / 2, portPosition.y - overlap)
                }
            }

            // Check if port is related to hovered device
            val portKey = "${device.id}:${port.id}"
            val isRelatedToHoveredDevice = !isHoverHighlightActive || relatedPortsMap[portKey] == true

            // Only render if the port is in the viewport
            val portCheckRadius = kotlin.math.max(capsuleWidth, capsuleHeight)
            if (isPortVisibleInViewport(portPosition, portCheckRadius, viewportSize)) {
                CapsulePortNode(
                    port = port,
                    deviceId = device.id,
                    zoom = zoom,
                    clipEdge = clipEdge,
                    isRelatedToHoveredDevice = isRelatedToHoveredDevice,
                    density = density,
                    onHoverChange = onHoverPort,
                    modifier = Modifier
                        .offset(
                            x = (adjustedPosition.x / density).dp,
                            y = (adjustedPosition.y / density).dp
                        )
                        // We only need to set a minimum width, the height will be determined by the content
                        .width((capsuleWidth / density).dp)
                )
            }
        }
    }
}

private fun getEstimatedPortWidth(port: Port, zoom: Float): Float {
    val baseWidth = RenderingConfig.portCapsuleBaseWidth * zoom
    val charWidth = port.name.length * RenderingConfig.portCapsuleWidthPerChar * zoom
    val innerPaddingWidth = RenderingConfig.portCapsuleHorizontalPadding * 2 * zoom

    val standardSidePadding = RenderingConfig.portCapsuleSidePadding * zoom
    val deviceSidePadding = RenderingConfig.portCapsuleDeviceSidePadding * zoom
    return baseWidth + charWidth + innerPaddingWidth + standardSidePadding + deviceSidePadding
}


/**
 * Check if a port is visible in the viewport with culling margin.
 */
private fun isPortVisibleInViewport(
    center: Offset,
    radius: Float,
    viewportSize: Size
): Boolean {
    val cullingMargin = 50f

    val viewportLeft = -cullingMargin
    val viewportRight = viewportSize.width + cullingMargin
    val viewportTop = -cullingMargin
    val viewportBottom = viewportSize.height + cullingMargin

    val circleLeft = center.x - radius
    val circleRight = center.x + radius
    val circleTop = center.y - radius
    val circleBottom = center.y + radius

    return circleLeft < viewportRight &&
            circleRight > viewportLeft &&
            circleTop < viewportBottom &&
            circleBottom > viewportTop
}