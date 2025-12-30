package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.akexorcist.workstation.data.model.DeviceCategory
import dev.akexorcist.workstation.data.model.ConnectionCategory
import dev.akexorcist.workstation.presentation.WorkstationUiState
import dev.akexorcist.workstation.presentation.config.RenderingConfig
import dev.akexorcist.workstation.utils.CoordinateTransformer

@Composable
fun DiagramCanvas(
    uiState: WorkstationUiState,
    onDeviceClick: (String) -> Unit,
    onConnectionClick: (String) -> Unit,
    onPanChange: (dev.akexorcist.workstation.data.model.Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    // UI state for hover effects
    var hoveredDeviceId by remember { mutableStateOf<String?>(null) }
    var mousePosition by remember { mutableStateOf<Offset?>(null) }
    
    // Track accumulated drag for the current session
    val accumulatedDrag = remember { mutableStateOf(Offset.Zero) }
    
    // Use a ref to always get the latest panOffset value
    val panOffsetRef = remember { mutableStateOf(uiState.panOffset) }
    panOffsetRef.value = uiState.panOffset

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(if (uiState.isDarkTheme) Color(0xFF3C3C3C) else Color(0xFFE0E0E0))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Move -> {
                                val position = event.changes.first().position
                                mousePosition = position
                                
                                // Check which device is under the cursor
                                hoveredDeviceId = uiState.layout?.let { layout ->
                                    val canvasSize = CoordinateTransformer.canvasSize(size.width.toFloat(), size.height.toFloat())
                                    layout.devices.find { device ->
                                        val screenPos = CoordinateTransformer.transformPosition(
                                            device.position,
                                            layout.metadata,
                                            canvasSize,
                                            uiState.zoom,
                                            uiState.panOffset
                                        )
                                        val screenSize = CoordinateTransformer.transformSize(
                                            device.size,
                                            layout.metadata,
                                            canvasSize,
                                            uiState.zoom
                                        )
                                        val rect = Rect(
                                            left = screenPos.x,
                                            top = screenPos.y,
                                            right = screenPos.x + screenSize.width,
                                            bottom = screenPos.y + screenSize.height
                                        )
                                        rect.contains(position)
                                    }?.id
                                }
                            }
                            PointerEventType.Exit -> {
                                hoveredDeviceId = null
                                mousePosition = null
                            }
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { position ->
                        // Find clicked device
                        val clickedDevice = uiState.layout?.let { layout ->
                            val canvasSize = CoordinateTransformer.canvasSize(size.width.toFloat(), size.height.toFloat())
                            layout.devices.find { device ->
                                val screenPos = CoordinateTransformer.transformPosition(
                                    device.position,
                                    layout.metadata,
                                    canvasSize,
                                    uiState.zoom,
                                    uiState.panOffset
                                )
                                val screenSize = CoordinateTransformer.transformSize(
                                    device.size,
                                    layout.metadata,
                                    canvasSize,
                                    uiState.zoom
                                )
                                val rect = Rect(
                                    left = screenPos.x,
                                    top = screenPos.y,
                                    right = screenPos.x + screenSize.width,
                                    bottom = screenPos.y + screenSize.height
                                )
                                rect.contains(position)
                            }
                        }
                        
                        if (clickedDevice != null) {
                            onDeviceClick(clickedDevice.id)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                var dragStartPan = Offset.Zero
                detectDragGestures(
                    onDragStart = {
                        // Capture the current ViewModel pan as the starting point for this drag
                        dragStartPan = panOffsetRef.value.toComposeOffset()
                        accumulatedDrag.value = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Accumulate the drag delta
                        accumulatedDrag.value = Offset(
                            x = accumulatedDrag.value.x + dragAmount.x,
                            y = accumulatedDrag.value.y + dragAmount.y
                        )
                        // Send the new absolute position (drag start + accumulated) to ViewModel
                        onPanChange(
                            dev.akexorcist.workstation.data.model.Offset(
                                x = dragStartPan.x + accumulatedDrag.value.x,
                                y = dragStartPan.y + accumulatedDrag.value.y
                            )
                        )
                    },
                    onDragEnd = {
                        // Clear accumulated drag for next session
                        accumulatedDrag.value = Offset.Zero
                    },
                    onDragCancel = {
                        // Clear accumulated drag for next session
                        accumulatedDrag.value = Offset.Zero
                    }
                )
            }
    ) {
        val zoom = uiState.zoom
        val viewportSize = androidx.compose.ui.geometry.Size(size.width, size.height)

        uiState.layout?.let { layout ->
            val canvasSize = CoordinateTransformer.canvasSize(size.width.toFloat(), size.height.toFloat())
            val deviceMap = layout.devices.associateBy { it.id }

            // Filter visible connections
            val visibleConnections = layout.connections.filter { connection ->
                val sourceDevice = deviceMap[connection.sourceDeviceId]
                val targetDevice = deviceMap[connection.targetDeviceId]

                if (sourceDevice != null && targetDevice != null) {
                    val sourcePort = sourceDevice.ports.find { it.id == connection.sourcePortId }
                    val targetPort = targetDevice.ports.find { it.id == connection.targetPortId }

                    if (sourcePort != null && targetPort != null) {
                        val sourcePosition = calculatePortScreenPosition(sourceDevice, sourcePort, layout.metadata, canvasSize, zoom, uiState.panOffset)
                        val targetPosition = calculatePortScreenPosition(targetDevice, targetPort, layout.metadata, canvasSize, zoom, uiState.panOffset)
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
                        val sourcePosition = calculatePortScreenPosition(sourceDevice, sourcePort, layout.metadata, canvasSize, zoom, uiState.panOffset)
                        val targetPosition = calculatePortScreenPosition(targetDevice, targetPort, layout.metadata, canvasSize, zoom, uiState.panOffset)

                        val lineColor = when (connection.connectionType.category) {
                            ConnectionCategory.DATA -> Color.Blue
                            ConnectionCategory.VIDEO -> Color(0xFFBA68C8)
                            ConnectionCategory.AUDIO -> Color(0xFF81C784)
                            ConnectionCategory.POWER -> Color(0xFFFFD54F)
                            ConnectionCategory.NETWORK -> Color(0xFF4DB6AC)
                        }

                        val strokeWidth = RenderingConfig.connectionLineThicknessByCategory[connection.connectionType.category]
                            ?: RenderingConfig.defaultConnectionLineThickness

                        // Calculate orthogonal path
                        val path = calculateOrthogonalPath(
                            sourcePosition, 
                            targetPosition, 
                            sourcePort.position.side,
                            targetPort.position.side,
                            zoom
                        )

                        // Draw the path with multiple line segments
                        for (i in 0 until path.size - 1) {
                            drawLine(
                                color = lineColor,
                                start = path[i],
                                end = path[i + 1],
                                strokeWidth = strokeWidth * zoom
                            )
                        }
                    }
                }
            }

            // Filter visible devices
            val visibleDevices = layout.devices.filter { device ->
                val screenPosition = CoordinateTransformer.transformPosition(
                    device.position,
                    layout.metadata,
                    canvasSize,
                    zoom,
                    uiState.panOffset
                )

                val screenSize = CoordinateTransformer.transformSize(
                    device.size,
                    layout.metadata,
                    canvasSize,
                    zoom
                )

                isRectVisible(screenPosition, screenSize, viewportSize)
            }

            visibleDevices.forEach { device ->
                val screenPosition = CoordinateTransformer.transformPosition(
                    device.position,
                    layout.metadata,
                    canvasSize,
                    zoom,
                    uiState.panOffset
                )

                val screenSize = CoordinateTransformer.transformSize(
                    device.size,
                    layout.metadata,
                    canvasSize,
                    zoom
                )

                val deviceColor = when (device.category) {
                    DeviceCategory.HUB -> Color(0xFF4CAF50)
                    DeviceCategory.PERIPHERAL -> Color(0xFFFF9800)
                    DeviceCategory.CENTRAL_DEVICE -> Color(0xFF2196F3)
                }

                val isHovered = device.id == hoveredDeviceId
                val isSelected = device.id == uiState.selectedDeviceId

                val borderColor = when {
                    isSelected -> Color.White
                    isHovered -> deviceColor.copy(alpha = 1f)
                    else -> deviceColor
                }

                val borderWidth = when {
                    isSelected -> RenderingConfig.defaultDeviceBorderThickness * 2 * zoom
                    isHovered -> RenderingConfig.defaultDeviceBorderThickness * 1.5f * zoom
                    else -> RenderingConfig.defaultDeviceBorderThickness * zoom
                }

                val backgroundAlpha = when {
                    isSelected -> 0.4f
                    isHovered -> 0.3f
                    else -> 0.2f
                }

                // Draw device background with semi-transparent fill
                drawRoundRect(
                    color = deviceColor.copy(alpha = backgroundAlpha),
                    topLeft = screenPosition,
                    size = screenSize,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(RenderingConfig.defaultDeviceBorderRadius * zoom)
                )

                // Draw device border
                drawRoundRect(
                    color = borderColor,
                    topLeft = screenPosition,
                    size = screenSize,
                    style = Stroke(width = borderWidth),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(RenderingConfig.defaultDeviceBorderRadius * zoom)
                )

                device.ports.forEach { port ->
                    val portPosition = calculatePortScreenPosition(device, port, layout.metadata, canvasSize, zoom, uiState.panOffset)
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

private fun calculatePortScreenPosition(
    device: dev.akexorcist.workstation.data.model.Device,
    port: dev.akexorcist.workstation.data.model.Port,
    metadata: dev.akexorcist.workstation.data.model.LayoutMetadata,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: dev.akexorcist.workstation.data.model.Offset
): Offset {
    // Get device position and size in screen space
    val screenPos = CoordinateTransformer.transformPosition(
        device.position,
        metadata,
        canvasSize,
        zoom,
        panOffset
    )
    val screenSize = CoordinateTransformer.transformSize(
        device.size,
        metadata,
        canvasSize,
        zoom
    )
    
    // Create device rectangle in screen space
    val deviceRect = androidx.compose.ui.geometry.Rect(
        left = screenPos.x,
        top = screenPos.y,
        right = screenPos.x + screenSize.width,
        bottom = screenPos.y + screenSize.height
    )

    val offset = when {
        port.position.offset == 0f -> 0.01f
        port.position.offset == 1f -> 0.99f
        else -> port.position.offset
    }

    // Calculate port position on the device rectangle (already in screen space)
    return when (port.position.side) {
        dev.akexorcist.workstation.data.model.DeviceSide.TOP -> Offset(
            x = deviceRect.left + (deviceRect.width * offset),
            y = deviceRect.top
        )
        dev.akexorcist.workstation.data.model.DeviceSide.BOTTOM -> Offset(
            x = deviceRect.left + (deviceRect.width * offset),
            y = deviceRect.bottom
        )
        dev.akexorcist.workstation.data.model.DeviceSide.LEFT -> Offset(
            x = deviceRect.left,
            y = deviceRect.top + (deviceRect.height * offset)
        )
        dev.akexorcist.workstation.data.model.DeviceSide.RIGHT -> Offset(
            x = deviceRect.right,
            y = deviceRect.top + (deviceRect.height * offset)
        )
    }
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
private fun isRectVisible(
    position: Offset,
    size: androidx.compose.ui.geometry.Size,
    viewportSize: androidx.compose.ui.geometry.Size
): Boolean {
    // Note: Canvas is full-screen but sidebar (300dp) and control panel (60dp) overlay on top
    // We render everything to allow devices to be visible under overlays
    // but we could add margins here if we want to hide things under the UI
    val rectLeft = position.x
    val rectRight = position.x + size.width
    val rectTop = position.y
    val rectBottom = position.y + size.height

    val viewportLeft = 0f
    val viewportRight = viewportSize.width
    val viewportTop = 0f
    val viewportBottom = viewportSize.height

    // Only hide when completely off-screen
    return rectLeft < viewportRight &&
            rectRight > viewportLeft &&
            rectTop < viewportBottom &&
            rectBottom > viewportTop
}

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

// Helper extension to convert model Offset to Compose Offset
private fun dev.akexorcist.workstation.data.model.Offset.toComposeOffset(): Offset {
    return Offset(x = this.x, y = this.y)
}