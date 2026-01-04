package dev.akexorcist.workstation.editor.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import dev.akexorcist.workstation.data.model.Offset as DataOffset
import dev.akexorcist.workstation.utils.CoordinateTransformer

@Composable
fun DeviceOverlay(
    layout: dev.akexorcist.workstation.data.model.WorkstationLayout,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: DataOffset,
    onHoverDevice: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentHoveredDevice by remember { mutableStateOf<String?>(null) }
    
    val cursorIcon = remember(currentHoveredDevice) {
        if (currentHoveredDevice != null) PointerIcon.Crosshair else PointerIcon.Default
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(layout, canvasSize, zoom, panOffset, onHoverDevice) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.pressed }) continue
                        
                        val pointerPosition = event.changes.firstOrNull()?.position ?: continue
                        
                        val deviceId = findDeviceAtPoint(
                            pointerPosition,
                            layout,
                            canvasSize,
                            zoom,
                            panOffset
                        )
                        
                        if (deviceId != null) {
                            if (currentHoveredDevice != deviceId) {
                                currentHoveredDevice = deviceId
                                onHoverDevice(deviceId)
                            }
                        } else {
                            if (currentHoveredDevice != null) {
                                currentHoveredDevice = null
                                onHoverDevice(null)
                            }
                        }
                    }
                }
            }
            .pointerHoverIcon(cursorIcon)
    )
}

internal fun findDeviceAtPoint(
    point: Offset,
    layout: dev.akexorcist.workstation.data.model.WorkstationLayout,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: DataOffset
): String? {
    layout.devices.forEach { device ->
        val screenPosition = CoordinateTransformer.transformPosition(
            device.position,
            layout.metadata,
            canvasSize,
            zoom,
            panOffset
        )
        val screenSize = CoordinateTransformer.transformSize(
            device.size,
            layout.metadata,
            canvasSize,
            zoom
        )
        
        val rect = androidx.compose.ui.geometry.Rect(
            left = screenPosition.x,
            top = screenPosition.y,
            right = screenPosition.x + screenSize.width,
            bottom = screenPosition.y + screenSize.height
        )
        
        if (point.x >= rect.left && point.x <= rect.right &&
            point.y >= rect.top && point.y <= rect.bottom) {
            return device.id
        }
    }
    
    return null
}

