package dev.akexorcist.workstation.editor.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import dev.akexorcist.workstation.data.model.DeviceSide
import dev.akexorcist.workstation.data.model.Position
import dev.akexorcist.workstation.editor.presentation.EditorUiState
import dev.akexorcist.workstation.presentation.config.RenderingConfig
import dev.akexorcist.workstation.utils.CoordinateTransformer
import dev.akexorcist.workstation.data.model.Offset as DataOffset

@Composable
fun PortOverlay(
    layout: dev.akexorcist.workstation.data.model.WorkstationLayout,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: DataOffset,
    uiState: EditorUiState,
    onHoverPort: (String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedPort = uiState.selectedPort
    
    val cursorIcon = remember(selectedPort) {
        if (selectedPort != null) PointerIcon.Crosshair else PointerIcon.Default
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerHoverIcon(cursorIcon)
    )
}

internal fun findPortAtPoint(
    point: Offset,
    layout: dev.akexorcist.workstation.data.model.WorkstationLayout,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: DataOffset
): Pair<String, String>? {
    val baseHitThreshold = 10f
    val hitThreshold = baseHitThreshold / zoom.coerceAtLeast(0.1f)
    
    var closestPort: Pair<String, String>? = null
    var closestDistance = Float.MAX_VALUE
    
    layout.devices.forEach { device ->
        device.ports.forEach { port ->
            val portPosition = calculatePortScreenPositionWithGrid(
                device,
                port,
                layout.metadata,
                canvasSize,
                zoom,
                panOffset
            )
            
            val capsuleHeight = RenderingConfig.portCapsuleHeight * zoom
            val capsuleWidth = getEstimatedPortWidthInternal(port, zoom)
            val overlap = RenderingConfig.portDeviceOverlap * zoom
            
            val adjustedPosition = when (port.position.side) {
                DeviceSide.LEFT -> {
                    val deviceEdgeX = portPosition.x
                    Offset(deviceEdgeX - capsuleWidth + overlap, portPosition.y - capsuleHeight / 2)
                }
                DeviceSide.RIGHT -> {
                    Offset(portPosition.x - overlap, portPosition.y - capsuleHeight / 2)
                }
                DeviceSide.TOP -> {
                    Offset(portPosition.x - capsuleWidth / 2, portPosition.y - capsuleHeight + overlap)
                }
                DeviceSide.BOTTOM -> {
                    Offset(portPosition.x - capsuleWidth / 2, portPosition.y - overlap)
                }
            }
            
            val rect = androidx.compose.ui.geometry.Rect(
                left = adjustedPosition.x,
                top = adjustedPosition.y,
                right = adjustedPosition.x + capsuleWidth,
                bottom = adjustedPosition.y + capsuleHeight
            )
            
            val distance = calculateDistanceToRect(point, rect)
            
            if (distance < hitThreshold && distance < closestDistance) {
                closestDistance = distance
                closestPort = Pair(device.id, port.id)
            }
        }
    }
    
    return closestPort
}

private fun calculateDistanceToRect(point: Offset, rect: androidx.compose.ui.geometry.Rect): Float {
    val closestX = point.x.coerceIn(rect.left, rect.right)
    val closestY = point.y.coerceIn(rect.top, rect.bottom)
    
    val dx = point.x - closestX
    val dy = point.y - closestY
    
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

internal fun constrainPortDragToEdge(
    dragDelta: Offset,
    deviceSide: DeviceSide
): Offset {
    return when (deviceSide) {
        DeviceSide.TOP, DeviceSide.BOTTOM -> Offset(dragDelta.x, 0f)
        DeviceSide.LEFT, DeviceSide.RIGHT -> Offset(0f, dragDelta.y)
    }
}

private fun calculatePortScreenPositionWithGrid(
    device: dev.akexorcist.workstation.data.model.Device,
    port: dev.akexorcist.workstation.data.model.Port,
    metadata: dev.akexorcist.workstation.data.model.LayoutMetadata,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: DataOffset
): Offset {
    val virtualPortX: Float
    val virtualPortY: Float

    when (port.position.side) {
        DeviceSide.TOP -> {
            val positionX = port.position.position.coerceIn(0f, device.size.width)
            virtualPortX = device.position.x + positionX
            virtualPortY = device.position.y
        }
        DeviceSide.BOTTOM -> {
            val positionX = port.position.position.coerceIn(0f, device.size.width)
            virtualPortX = device.position.x + positionX
            virtualPortY = device.position.y + device.size.height
        }
        DeviceSide.LEFT -> {
            val positionY = port.position.position.coerceIn(0f, device.size.height)
            virtualPortX = device.position.x
            virtualPortY = device.position.y + positionY
        }
        DeviceSide.RIGHT -> {
            val positionY = port.position.position.coerceIn(0f, device.size.height)
            virtualPortX = device.position.x + device.size.width
            virtualPortY = device.position.y + positionY
        }
    }

    val virtualPosition = Position(virtualPortX, virtualPortY)
    val screenPos = CoordinateTransformer.transformPosition(
        virtualPosition,
        metadata,
        canvasSize,
        zoom,
        panOffset
    )

    return Offset(screenPos.x, screenPos.y)
}

private fun getEstimatedPortWidthInternal(
    port: dev.akexorcist.workstation.data.model.Port,
    zoom: Float
): Float {
    val baseWidth = RenderingConfig.portCapsuleBaseWidth * zoom
    val charWidth = port.name.length * RenderingConfig.portCapsuleWidthPerChar * zoom
    val innerPaddingWidth = RenderingConfig.portCapsuleHorizontalPadding * 2 * zoom
    val standardSidePadding = RenderingConfig.portCapsuleSidePadding * zoom
    val deviceSidePadding = RenderingConfig.portCapsuleDeviceSidePadding * zoom
    return baseWidth + charWidth + innerPaddingWidth + standardSidePadding + deviceSidePadding
}
