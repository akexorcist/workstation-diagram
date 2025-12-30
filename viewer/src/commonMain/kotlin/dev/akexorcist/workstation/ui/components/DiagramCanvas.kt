package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import dev.akexorcist.workstation.data.model.ConnectionCategory
import dev.akexorcist.workstation.presentation.WorkstationUiState
import dev.akexorcist.workstation.presentation.config.RenderingConfig
import dev.akexorcist.workstation.routing.ConnectionRouter
import dev.akexorcist.workstation.routing.RoutedConnection
import dev.akexorcist.workstation.routing.RoutingConfig
import dev.akexorcist.workstation.utils.CoordinateTransformer

// Feature flag to toggle between old and new routing
private const val USE_INTELLIGENT_ROUTING = true

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
    // Track accumulated drag for the current session
    val accumulatedDrag = remember { mutableStateOf(Offset.Zero) }

    // Use a ref to always get the latest panOffset value
    val panOffsetRef = remember { mutableStateOf(uiState.panOffset) }
    panOffsetRef.value = uiState.panOffset

    // Cache for intelligent routing results
    val routedConnections = remember(uiState.layout) {
        if (USE_INTELLIGENT_ROUTING && uiState.layout != null) {
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (uiState.isDarkTheme) Color(0xFF3C3C3C) else Color(0xFFE0E0E0))
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
                width = 1280f, // Will be updated by onGloballyPositioned in parent
                height = 720f
            )
            val viewportSize = androidx.compose.ui.geometry.Size(1280f, 720f)
            val zoom = uiState.zoom

            // Layer 1: Connections (Canvas - bottom layer)
            ConnectionCanvas(
                layout = layout,
                zoom = zoom,
                panOffset = uiState.panOffset,
                canvasSize = canvasSize,
                viewportSize = viewportSize,
                routedConnectionMap = routedConnectionMap,
                selectedConnectionId = uiState.selectedConnectionId
            )

            // Layer 2: Devices (Compose - top layer, handles own clicks)
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
    val deviceMap = layout.devices.associateBy { it.id }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Filter visible connections
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
                    isLineVisible(sourcePosition, targetPosition, viewportSize)
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

                    val baseLineColor = when (connection.connectionType.category) {
                        ConnectionCategory.DATA -> Color.Blue
                        ConnectionCategory.VIDEO -> Color(0xFFBA68C8)
                        ConnectionCategory.AUDIO -> Color(0xFF81C784)
                        ConnectionCategory.POWER -> Color(0xFFFFD54F)
                        ConnectionCategory.NETWORK -> Color(0xFF4DB6AC)
                    }

                    val baseStrokeWidth = RenderingConfig.connectionLineThicknessByCategory[connection.connectionType.category]
                        ?: RenderingConfig.defaultConnectionLineThickness

                    // Use intelligent routing if enabled and available
                    val routedConnection = routedConnectionMap[connection.id]
                    if (USE_INTELLIGENT_ROUTING && routedConnection != null) {
                        val lineColor = if (routedConnection.success) baseLineColor
                        else RoutingConfig.failedRouteColor.copy(alpha = RoutingConfig.failedRouteAlpha)
                        val strokeWidth = if (routedConnection.success) baseStrokeWidth * zoom
                        else baseStrokeWidth * zoom * RoutingConfig.failedRouteWidthMultiplier

                        val path = routedConnection.virtualWaypoints.map { (vx, vy) ->
                            virtualToScreen(vx, vy, layout.metadata, canvasSize, zoom, panOffset)
                        }

                        for (i in 0 until path.size - 1) {
                            drawLine(color = lineColor, start = path[i], end = path[i + 1], strokeWidth = strokeWidth)
                        }
                    } else {
                        // Fallback to old orthogonal path
                        val path = calculateOrthogonalPath(
                            sourcePosition, targetPosition,
                            sourcePort.position.side, targetPort.position.side, zoom
                        )
                        for (i in 0 until path.size - 1) {
                            drawLine(color = baseLineColor, start = path[i], end = path[i + 1], strokeWidth = baseStrokeWidth * zoom)
                        }
                    }
                }
            }
        }

        // Draw ports for all visible devices
        layout.devices.forEach { device ->
            val screenPosition = dev.akexorcist.workstation.utils.CoordinateTransformer.transformPosition(
                device.position,
                layout.metadata,
                canvasSize,
                zoom,
                panOffset
            )
            val screenSize = dev.akexorcist.workstation.utils.CoordinateTransformer.transformSize(
                device.size,
                layout.metadata,
                canvasSize,
                zoom
            )

            // Only draw ports for visible devices
            if (isRectVisibleInViewport(screenPosition, screenSize, viewportSize)) {
                device.ports.forEach { port ->
                    val portPosition = calculatePortScreenPosition(
                        device, port, layout.metadata, canvasSize, zoom, panOffset
                    )
                    val portSize = 8f * zoom

                    val portColor = when (port.type) {
                        dev.akexorcist.workstation.data.model.PortType.USB_C -> Color(0xFF2196F3)
                        dev.akexorcist.workstation.data.model.PortType.USB_A_2_0,
                        dev.akexorcist.workstation.data.model.PortType.USB_A_3_0,
                        dev.akexorcist.workstation.data.model.PortType.USB_A_3_1,
                        dev.akexorcist.workstation.data.model.PortType.USB_A_3_2 -> Color(0xFF4CAF50)
                        dev.akexorcist.workstation.data.model.PortType.HDMI,
                        dev.akexorcist.workstation.data.model.PortType.HDMI_2_1,
                        dev.akexorcist.workstation.data.model.PortType.DISPLAY_PORT,
                        dev.akexorcist.workstation.data.model.PortType.MINI_HDMI,
                        dev.akexorcist.workstation.data.model.PortType.MICRO_HDMI -> Color(0xFFFF9800)
                        dev.akexorcist.workstation.data.model.PortType.ETHERNET -> Color(0xFF9C27B0)
                        dev.akexorcist.workstation.data.model.PortType.AUX -> Color(0xFFE91E63)
                        dev.akexorcist.workstation.data.model.PortType.POWER -> Color(0xFFFFD54F)
                    }

                    // Draw port background
                    drawCircle(
                        color = portColor.copy(alpha = 0.3f),
                        radius = portSize / 2 + 2,
                        center = portPosition
                    )

                    // Draw port
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

// Helper function to check if rect is visible (used in Canvas)
private fun isRectVisibleInViewport(
    position: Offset,
    size: androidx.compose.ui.geometry.Size,
    viewportSize: androidx.compose.ui.geometry.Size
): Boolean {
    val rectLeft = position.x
    val rectRight = position.x + size.width
    val rectTop = position.y
    val rectBottom = position.y + size.height

    val viewportLeft = 0f
    val viewportRight = viewportSize.width
    val viewportTop = 0f
    val viewportBottom = viewportSize.height

    return rectLeft < viewportRight &&
            rectRight > viewportLeft &&
            rectTop < viewportBottom &&
            rectBottom > viewportTop
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
    val offset = when {
        port.position.offset == 0f -> 0.01f
        port.position.offset == 1f -> 0.99f
        else -> port.position.offset
    }

    // Calculate virtual port position (unsnapped)
    val virtualPortX: Float
    val virtualPortY: Float
    when (port.position.side) {
        dev.akexorcist.workstation.data.model.DeviceSide.TOP -> {
            virtualPortX = device.position.x + (device.size.width * offset)
            virtualPortY = device.position.y
        }
        dev.akexorcist.workstation.data.model.DeviceSide.BOTTOM -> {
            virtualPortX = device.position.x + (device.size.width * offset)
            virtualPortY = device.position.y + device.size.height
        }
        dev.akexorcist.workstation.data.model.DeviceSide.LEFT -> {
            virtualPortX = device.position.x
            virtualPortY = device.position.y + (device.size.height * offset)
        }
        dev.akexorcist.workstation.data.model.DeviceSide.RIGHT -> {
            virtualPortX = device.position.x + device.size.width
            virtualPortY = device.position.y + (device.size.height * offset)
        }
    }

    // Snap to grid (10 unit grid cells) - use same algorithm as RoutingGrid
    val gridCellSize = 10f
    var gridX = (virtualPortX / gridCellSize).toInt()
    var gridY = (virtualPortY / gridCellSize).toInt()

    // Adjust grid position to ensure port stays outside device bounds
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
        dev.akexorcist.workstation.data.model.DeviceSide.RIGHT -> {
            // No adjustment needed - these naturally snap outside
        }
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

// Orthogonal path routing
private fun calculateOrthogonalPath(
    start: Offset,
    end: Offset,
    startSide: dev.akexorcist.workstation.data.model.DeviceSide,
    endSide: dev.akexorcist.workstation.data.model.DeviceSide,
    zoom: Float
): List<Offset> {
    val path = mutableListOf<Offset>()
    path.add(start)

    // Distance to extend from port before turning
    val extensionDistance = 30f * zoom

    // Calculate exit and entry points based on port sides
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

    // Determine if ports are on opposite or perpendicular sides
    val isHorizontalStart = startSide == dev.akexorcist.workstation.data.model.DeviceSide.LEFT ||
            startSide == dev.akexorcist.workstation.data.model.DeviceSide.RIGHT
    val isHorizontalEnd = endSide == dev.akexorcist.workstation.data.model.DeviceSide.LEFT ||
            endSide == dev.akexorcist.workstation.data.model.DeviceSide.RIGHT

    // Create path based on port orientations
    when {
        // Both horizontal or both vertical - use midpoint
        isHorizontalStart == isHorizontalEnd -> {
            if (isHorizontalStart) {
                // Both are horizontal (left/right)
                val midX = (startExit.x + endEntry.x) / 2
                path.add(Offset(midX, startExit.y))
                path.add(Offset(midX, endEntry.y))
            } else {
                // Both are vertical (top/bottom)
                val midY = (startExit.y + endEntry.y) / 2
                path.add(Offset(startExit.x, midY))
                path.add(Offset(endEntry.x, midY))
            }
        }
        // Perpendicular sides - single corner
        else -> {
            if (isHorizontalStart) {
                // Start is horizontal, end is vertical
                path.add(Offset(startExit.x, endEntry.y))
            } else {
                // Start is vertical, end is horizontal
                path.add(Offset(endEntry.x, startExit.y))
            }
        }
    }

    path.add(endEntry)
    path.add(end)

    return path
}

// Viewport culling helper functions
private fun isLineVisible(
    start: Offset,
    end: Offset,
    viewportSize: androidx.compose.ui.geometry.Size
): Boolean {
    val minX = minOf(start.x, end.x)
    val maxX = maxOf(start.x, end.x)
    val minY = minOf(start.y, end.y)
    val maxY = maxOf(start.y, end.y)

    val viewportLeft = 0f
    val viewportRight = viewportSize.width
    val viewportTop = 0f
    val viewportBottom = viewportSize.height

    // Only hide when completely off-screen
    return minX < viewportRight &&
            maxX > viewportLeft &&
            minY < viewportBottom &&
            maxY > viewportTop
}

private fun dev.akexorcist.workstation.data.model.Offset.toComposeOffset(): Offset {
    return Offset(x = this.x, y = this.y)
}

// Convert virtual coordinates to screen coordinates
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