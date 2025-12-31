package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import dev.akexorcist.workstation.data.model.ConnectionCategory
import dev.akexorcist.workstation.presentation.WorkstationUiState
import dev.akexorcist.workstation.presentation.config.RenderingConfig
import dev.akexorcist.workstation.routing.ConnectionRouter
import dev.akexorcist.workstation.routing.RoutedConnection
import dev.akexorcist.workstation.routing.RoutingConfig
import dev.akexorcist.workstation.ui.theme.ThemeColor
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import dev.akexorcist.workstation.utils.CoordinateTransformer

/**
 * Hybrid Compose-First Diagram Canvas
 * - Devices: Rendered with Compose components (DeviceNode, DeviceList)
 * - Connections: Rendered with Canvas (efficient for lines)
 * - Interactions: Handled by Compose gestures + ViewModel state
 */
@Composable
fun DiagramCanvas(
    uiState: WorkstationUiState,
    onDeviceClick: (String) -> Unit,
    onConnectionClick: (String) -> Unit,
    onPanChange: (dev.akexorcist.workstation.data.model.Offset) -> Unit,
    onHoverDevice: (String?, Boolean) -> Unit = { _, _ -> },
    onHoverConnection: (String?, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val accumulatedDrag = remember { mutableStateOf(Offset.Zero) }

    val panOffsetRef = remember { mutableStateOf(uiState.panOffset) }
    panOffsetRef.value = uiState.panOffset

    val routedConnections = remember(uiState.layout) {
        if (uiState.layout != null) {
            val layout = uiState.layout
            val virtualCanvas = layout.metadata.virtualCanvas ?: layout.metadata.canvasSize
            ConnectionRouter().routeConnections(layout.devices, layout.connections, virtualCanvas)
        } else {
            emptyList()
        }
    }
    val routedConnectionMap = remember(routedConnections) {
        routedConnections.associateBy { it.connectionId }
    }

    var actualSize by remember { mutableStateOf(Size(1280f, 720f)) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WorkstationTheme.themeColor.outerBackground)
            .onGloballyPositioned { coordinates ->
                actualSize = Size(
                    coordinates.size.width.toFloat(),
                    coordinates.size.height.toFloat()
                )
            }
            .pointerInput(Unit) {
                var dragStartPan = Offset.Zero
                detectDragGestures(
                    onDragStart = {
                        dragStartPan = panOffsetRef.value.toComposeOffset()
                        accumulatedDrag.value = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedDrag.value = Offset(
                            x = accumulatedDrag.value.x + dragAmount.x,
                            y = accumulatedDrag.value.y + dragAmount.y
                        )
                        onPanChange(
                            dev.akexorcist.workstation.data.model.Offset(
                                x = dragStartPan.x + accumulatedDrag.value.x,
                                y = dragStartPan.y + accumulatedDrag.value.y
                            )
                        )
                    },
                    onDragEnd = {
                        accumulatedDrag.value = Offset.Zero
                    },
                    onDragCancel = {
                        accumulatedDrag.value = Offset.Zero
                    }
                )
            }
    ) {
        if (uiState.layout != null) {
            val layout = uiState.layout
            val canvasSize = CoordinateTransformer.canvasSize(
                width = actualSize.width,
                height = actualSize.height
            )
            val viewportSize = actualSize
            val zoom = uiState.zoom

            ConnectionCanvas(
                layout = layout,
                zoom = zoom,
                panOffset = uiState.panOffset,
                canvasSize = canvasSize,
                viewportSize = viewportSize,
                routedConnectionMap = routedConnectionMap,
                selectedConnectionId = uiState.selectedConnectionId
            )

            DeviceList(
                devices = layout.devices,
                metadata = layout.metadata,
                canvasSize = canvasSize,
                zoom = zoom,
                panOffset = uiState.panOffset,
                viewportSize = viewportSize,
                selectedDeviceId = uiState.selectedDeviceId,
                hoveredDeviceId = uiState.hoveredDeviceId,
                filteredDeviceIds = uiState.filteredDeviceIds,
                onDeviceClick = onDeviceClick,
                onHoverChange = onHoverDevice
            )
        }
    }
}

/**
 * Renders connections and ports using Canvas for efficient line drawing.
 */
@Composable
private fun ConnectionCanvas(
    layout: dev.akexorcist.workstation.data.model.WorkstationLayout,
    zoom: Float,
    panOffset: dev.akexorcist.workstation.data.model.Offset,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    viewportSize: androidx.compose.ui.geometry.Size,
    routedConnectionMap: Map<String, RoutedConnection>,
    selectedConnectionId: String?
) {
    val connectionTheme = WorkstationTheme.themeColor.connection
    val hubColor = WorkstationTheme.themeColor.hub
    val peripheralColor = WorkstationTheme.themeColor.peripheral
    val selectedId = selectedConnectionId
    val hoveredConnectionId: String? = null
    
    val deviceMap = layout.devices.associateBy { it.id }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val visibleConnections = layout.connections.filter { connection ->
            val sourceDevice = deviceMap[connection.sourceDeviceId]
            val targetDevice = deviceMap[connection.targetDeviceId]

            if (sourceDevice != null && targetDevice != null) {
                val sourcePort = sourceDevice.ports.find { it.id == connection.sourcePortId }
                val targetPort = targetDevice.ports.find { it.id == connection.targetPortId }

                if (sourcePort != null && targetPort != null) {
                    val sourcePosition = calculatePortScreenPosition(
                        sourceDevice, sourcePort, layout.metadata, canvasSize, zoom, panOffset
                    )
                    val targetPosition = calculatePortScreenPosition(
                        targetDevice, targetPort, layout.metadata, canvasSize, zoom, panOffset
                    )
                    
                    val routedConnection = routedConnectionMap[connection.id]
                    if (routedConnection != null) {
                        val path = routedConnection.virtualWaypoints.map { (vx, vy) ->
                            virtualToScreen(vx, vy, layout.metadata, canvasSize, zoom, panOffset)
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
                    val sourcePosition = calculatePortScreenPosition(
                        sourceDevice, sourcePort, layout.metadata, canvasSize, zoom, panOffset
                    )
                    val targetPosition = calculatePortScreenPosition(
                        targetDevice, targetPort, layout.metadata, canvasSize, zoom, panOffset
                    )

                    val routedConnection = routedConnectionMap[connection.id]
                    
                    if (routedConnection != null && !routedConnection.success) {
                        val failedLineColor = ThemeColor.Pink500.copy(alpha = RoutingConfig.failedRouteAlpha)
                        val failedStrokeWidth = RenderingConfig.connectionWidth * zoom * RoutingConfig.failedRouteWidthMultiplier
                        
                        val path = routedConnection.virtualWaypoints.map { (vx, vy) ->
                            virtualToScreen(vx, vy, layout.metadata, canvasSize, zoom, panOffset)
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
                    } 
                    else {
                        val path = if (routedConnection != null) {
                            routedConnection.virtualWaypoints.map { (vx, vy) ->
                                virtualToScreen(vx, vy, layout.metadata, canvasSize, zoom, panOffset)
                            }
                        } else {
                            calculateOrthogonalPath(
                                sourcePosition, targetPosition,
                                sourcePort.position.side, targetPort.position.side, zoom
                            )
                        }
                        
                        val isSelected = connection.id == selectedId
                        val isHovered = connection.id == hoveredConnectionId
                        val inputBackgroundColor = when {
                            isSelected -> connectionTheme.inputBackgroundActiveColor
                            isHovered -> connectionTheme.inputBackgroundActiveColor.copy(alpha = 0.7f)
                            else -> connectionTheme.inputBackgroundInactiveColor
                        }
                        
                        val outputBackgroundColor = when {
                            isSelected -> connectionTheme.outputBackgroundActiveColor
                            isHovered -> connectionTheme.outputBackgroundActiveColor.copy(alpha = 0.7f)
                            else -> connectionTheme.outputBackgroundInactiveColor
                        }
                        
                        val inputForegroundColor = when {
                            isSelected -> connectionTheme.inputActiveColor
                            isHovered -> connectionTheme.inputActiveColor.copy(alpha = 0.7f)
                            else -> connectionTheme.inputInactiveColor
                        }
                        
                        val outputForegroundColor = when {
                            isSelected -> connectionTheme.outputActiveColor
                            isHovered -> connectionTheme.outputActiveColor.copy(alpha = 0.7f)
                            else -> connectionTheme.outputInactiveColor
                        }
                        
                        // Draw the gradient connection with background and dashed foreground
                        drawGradientConnectionPath(
                            path = path,
                            inputBackgroundColor = inputBackgroundColor,
                            outputBackgroundColor = outputBackgroundColor,
                            inputForegroundColor = inputForegroundColor,
                            outputForegroundColor = outputForegroundColor,
                            zoom = zoom
                        )
                    }
                }
            }
        }

        layout.devices.forEach { device ->
            device.ports.forEach { port ->
                val portPosition = calculatePortScreenPosition(
                    device, port, layout.metadata, canvasSize, zoom, panOffset
                )
                val portSize = 8f * zoom
                val portRadius = portSize / 2 + 2 // Include background radius for visibility check

                // Only draw ports that are visible in the viewport
                if (isPortVisibleInViewport(portPosition, portRadius, viewportSize)) {
                    val portColor = when (port.type) {
                        dev.akexorcist.workstation.data.model.PortType.USB_C -> ThemeColor.DimBlue500
                        dev.akexorcist.workstation.data.model.PortType.USB_A_2_0,
                        dev.akexorcist.workstation.data.model.PortType.USB_A_3_0,
                        dev.akexorcist.workstation.data.model.PortType.USB_A_3_1,
                        dev.akexorcist.workstation.data.model.PortType.USB_A_3_2 -> hubColor
                        dev.akexorcist.workstation.data.model.PortType.HDMI,
                        dev.akexorcist.workstation.data.model.PortType.HDMI_2_1,
                        dev.akexorcist.workstation.data.model.PortType.DISPLAY_PORT,
                        dev.akexorcist.workstation.data.model.PortType.MINI_HDMI,
                        dev.akexorcist.workstation.data.model.PortType.MICRO_HDMI -> peripheralColor
                        dev.akexorcist.workstation.data.model.PortType.ETHERNET -> ThemeColor.Purple500
                        dev.akexorcist.workstation.data.model.PortType.AUX -> ThemeColor.Pink500
                        dev.akexorcist.workstation.data.model.PortType.POWER -> ThemeColor.DimAmber500
                    }

                    drawCircle(
                        color = portColor.copy(alpha = 0.3f),
                        radius = portSize / 2 + 2,
                        center = portPosition
                    )

                    drawCircle(
                        color = portColor,
                        radius = portSize / 2,
                        center = portPosition
                    )
                }
            }
        }
    }
}

private fun isRectVisibleInViewport(
    position: Offset,
    size: androidx.compose.ui.geometry.Size,
    viewportSize: androidx.compose.ui.geometry.Size
): Boolean {
    val rectLeft = position.x
    val rectRight = position.x + size.width
    val rectTop = position.y
    val rectBottom = position.y + size.height

    val cullingMargin = 50f
    
    val viewportLeft = -cullingMargin
    val viewportRight = viewportSize.width + cullingMargin
    val viewportTop = -cullingMargin
    val viewportBottom = viewportSize.height + cullingMargin

    return rectLeft < viewportRight &&
            rectRight > viewportLeft &&
            rectTop < viewportBottom &&
            rectBottom > viewportTop
}

/**
 * Check if a port is visible in the viewport with culling margin.
 */
private fun isPortVisibleInViewport(
    center: Offset,
    radius: Float,
    viewportSize: androidx.compose.ui.geometry.Size
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



// Helper functions for port position calculation
private fun calculatePortScreenPosition(
    device: dev.akexorcist.workstation.data.model.Device,
    port: dev.akexorcist.workstation.data.model.Port,
    metadata: dev.akexorcist.workstation.data.model.LayoutMetadata,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: dev.akexorcist.workstation.data.model.Offset
): Offset {
    val virtualPortX: Float
    val virtualPortY: Float
    
    when (port.position.side) {
        dev.akexorcist.workstation.data.model.DeviceSide.TOP -> {
            val portPosition = when {
                port.position.position < 0 -> 0f
                port.position.position > device.size.width -> device.size.width
                else -> port.position.position
            }
            
            virtualPortX = device.position.x + portPosition
            virtualPortY = device.position.y
        }
        dev.akexorcist.workstation.data.model.DeviceSide.BOTTOM -> {
            val portPosition = when {
                port.position.position < 0 -> 0f
                port.position.position > device.size.width -> device.size.width
                else -> port.position.position
            }
            
            virtualPortX = device.position.x + portPosition
            virtualPortY = device.position.y + device.size.height
        }
        dev.akexorcist.workstation.data.model.DeviceSide.LEFT -> {
            val portPosition = when {
                port.position.position < 0 -> 0f
                port.position.position > device.size.height -> device.size.height
                else -> port.position.position
            }
            
            virtualPortX = device.position.x
            virtualPortY = device.position.y + portPosition
        }
        dev.akexorcist.workstation.data.model.DeviceSide.RIGHT -> {
            val portPosition = when {
                port.position.position < 0 -> 0f
                port.position.position > device.size.height -> device.size.height
                else -> port.position.position
            }
            
            virtualPortX = device.position.x + device.size.width
            virtualPortY = device.position.y + portPosition
        }
    }

    val gridCellSize = 10f
    var gridX = (virtualPortX / gridCellSize).toInt()
    var gridY = (virtualPortY / gridCellSize).toInt()

    val deviceGridLeft = (device.position.x / gridCellSize).toInt()
    val deviceGridRight = ((device.position.x + device.size.width) / gridCellSize).toInt()
    val deviceGridTop = (device.position.y / gridCellSize).toInt()
    val deviceGridBottom = ((device.position.y + device.size.height) / gridCellSize).toInt()

    when (port.position.side) {
        dev.akexorcist.workstation.data.model.DeviceSide.TOP -> {
            if (gridY >= deviceGridTop) gridY = deviceGridTop - 1
        }
        dev.akexorcist.workstation.data.model.DeviceSide.LEFT -> {
            if (gridX >= deviceGridLeft) gridX = deviceGridLeft - 1
        }
        dev.akexorcist.workstation.data.model.DeviceSide.BOTTOM,
        dev.akexorcist.workstation.data.model.DeviceSide.RIGHT -> {}
    }

    val snappedVirtualX = gridX * gridCellSize + (gridCellSize / 2f)
    val snappedVirtualY = gridY * gridCellSize + (gridCellSize / 2f)

    // Transform snapped virtual position to screen space
    val snappedVirtual = dev.akexorcist.workstation.data.model.Position(snappedVirtualX, snappedVirtualY)
    val screenPos = CoordinateTransformer.transformPosition(
        snappedVirtual,
        metadata,
        canvasSize,
        zoom,
        panOffset
    )

    return Offset(screenPos.x, screenPos.y)
}

private fun calculateOrthogonalPath(
    start: Offset,
    end: Offset,
    startSide: dev.akexorcist.workstation.data.model.DeviceSide,
    endSide: dev.akexorcist.workstation.data.model.DeviceSide,
    zoom: Float
): List<Offset> {
    val path = mutableListOf<Offset>()
    path.add(start)

    val extensionDistance = 30f * zoom

    val startExit = when (startSide) {
        dev.akexorcist.workstation.data.model.DeviceSide.TOP ->
            Offset(start.x, start.y - extensionDistance)
        dev.akexorcist.workstation.data.model.DeviceSide.BOTTOM ->
            Offset(start.x, start.y + extensionDistance)
        dev.akexorcist.workstation.data.model.DeviceSide.LEFT ->
            Offset(start.x - extensionDistance, start.y)
        dev.akexorcist.workstation.data.model.DeviceSide.RIGHT ->
            Offset(start.x + extensionDistance, start.y)
    }

    val endEntry = when (endSide) {
        dev.akexorcist.workstation.data.model.DeviceSide.TOP ->
            Offset(end.x, end.y - extensionDistance)
        dev.akexorcist.workstation.data.model.DeviceSide.BOTTOM ->
            Offset(end.x, end.y + extensionDistance)
        dev.akexorcist.workstation.data.model.DeviceSide.LEFT ->
            Offset(end.x - extensionDistance, end.y)
        dev.akexorcist.workstation.data.model.DeviceSide.RIGHT ->
            Offset(end.x + extensionDistance, end.y)
    }

    path.add(startExit)

    val isHorizontalStart = startSide == dev.akexorcist.workstation.data.model.DeviceSide.LEFT ||
            startSide == dev.akexorcist.workstation.data.model.DeviceSide.RIGHT
    val isHorizontalEnd = endSide == dev.akexorcist.workstation.data.model.DeviceSide.LEFT ||
            endSide == dev.akexorcist.workstation.data.model.DeviceSide.RIGHT

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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGradientConnectionPath(
    path: List<Offset>,
    inputBackgroundColor: Color,
    outputBackgroundColor: Color, 
    inputForegroundColor: Color,
    outputForegroundColor: Color,
    zoom: Float
) {
    if (path.size < 2) return
    
    val backgroundWidth = RenderingConfig.connectionBackgroundWidth * zoom
    val foregroundWidth = RenderingConfig.connectionForegroundWidth * zoom
    
    val startPoint = path.first()
    val endPoint = path.last()
    
    // Create gradient brushes for the entire path
    val backgroundBrush = Brush.linearGradient(
        colors = listOf(inputBackgroundColor, outputBackgroundColor),
        start = startPoint,
        end = endPoint
    )
    
    val foregroundBrush = Brush.linearGradient(
        colors = listOf(inputForegroundColor, outputForegroundColor),
        start = startPoint,
        end = endPoint
    )
    
    // Step 1: Draw the background line
    val backgroundPath = androidx.compose.ui.graphics.Path()
    backgroundPath.moveTo(path.first().x, path.first().y)
    
    for (i in 1 until path.size) {
        backgroundPath.lineTo(path[i].x, path[i].y)
    }
    
    drawPath(
        path = backgroundPath,
        brush = backgroundBrush,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = backgroundWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
    
    // Step 2: Draw evenly spaced dots along the path
    val dotRadius = RenderingConfig.connectionDotLength * zoom
    val dotGap = RenderingConfig.connectionDotGap * zoom
    val dotStep = dotRadius * 2 + dotGap
    
    val totalLength = calculatePathLength(path)
    
    // Calculate exactly how many dots we need to distribute evenly
    val dotsCount = (totalLength / dotStep).toInt() + 1
    
    // If we have at least 2 dots, distribute them evenly
    if (dotsCount >= 2) {
        // Adjust spacing to distribute dots evenly
        val adjustedStep = totalLength / (dotsCount - 1)
        
        for (i in 0 until dotsCount) {
            val distance = i * adjustedStep
            if (distance > totalLength) break
            
            val dotPosition = getPointAtDistance(path, distance)
            
            // Draw the dot as a filled circle
            drawCircle(
                brush = foregroundBrush,
                radius = dotRadius,
                center = dotPosition
            )
        }
    }
}

private fun calculatePathLength(path: List<Offset>): Float {
    var length = 0f
    for (i in 0 until path.size - 1) {
        val start = path[i]
        val end = path[i + 1]
        length += kotlin.math.sqrt(
            (end.x - start.x) * (end.x - start.x) + 
            (end.y - start.y) * (end.y - start.y)
        )
    }
    return length
}

private fun getPointAtDistance(path: List<Offset>, distance: Float): Offset {
    if (distance <= 0f) return path.first()
    
    var distanceSoFar = 0f
    
    for (i in 0 until path.size - 1) {
        val start = path[i]
        val end = path[i + 1]
        
        val segmentLength = kotlin.math.sqrt(
            (end.x - start.x) * (end.x - start.x) + 
            (end.y - start.y) * (end.y - start.y)
        )
        
        if (distanceSoFar + segmentLength >= distance) {
            // This segment contains our point
            val remainingDistance = distance - distanceSoFar
            val ratio = remainingDistance / segmentLength
            
            return Offset(
                x = start.x + (end.x - start.x) * ratio,
                y = start.y + (end.y - start.y) * ratio
            )
        }
        
        distanceSoFar += segmentLength
    }
    
    return path.last() // If we somehow exceeded the path length
}

private fun calculatePathLengthToPoint(path: List<Offset>, pointIndex: Int): Float {
    var length = 0f
    for (i in 0 until pointIndex) {
        if (i + 1 < path.size) {
            val start = path[i]
            val end = path[i + 1]
            length += kotlin.math.sqrt(
                (end.x - start.x) * (end.x - start.x) + 
                (end.y - start.y) * (end.y - start.y)
            )
        }
    }
    return length
}

/**
 * Check if any segment of a path is visible in the viewport with culling margin.
 */
private fun isPathVisible(
    path: List<Offset>,
    viewportSize: androidx.compose.ui.geometry.Size
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
        isPointInViewport(end, viewportLeft, viewportRight, viewportTop, viewportBottom)) {
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
    return point.x >= viewportLeft && point.x <= viewportRight &&
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
        return y >= yMin && y <= yMax
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
        return x >= xMin && x <= xMax
    }
    return false
}

private fun dev.akexorcist.workstation.data.model.Offset.toComposeOffset(): Offset {
    return Offset(x = this.x, y = this.y)
}

private fun virtualToScreen(
    virtualX: Float,
    virtualY: Float,
    metadata: dev.akexorcist.workstation.data.model.LayoutMetadata,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: dev.akexorcist.workstation.data.model.Offset
): Offset {
    val position = dev.akexorcist.workstation.data.model.Position(virtualX, virtualY)
    return CoordinateTransformer.transformPosition(position, metadata, canvasSize, zoom, panOffset)
}