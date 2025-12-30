package dev.akexorcist.workstation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import dev.akexorcist.workstation.data.model.Device
import dev.akexorcist.workstation.data.model.LayoutMetadata
import dev.akexorcist.workstation.data.model.Position
import dev.akexorcist.workstation.utils.CoordinateTransformer

/**
 * Renders a list of device nodes with viewport culling optimization.
 * Only devices visible within the viewport are rendered.
 */
@Composable
fun DeviceList(
    devices: List<Device>,
    metadata: LayoutMetadata,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: dev.akexorcist.workstation.data.model.Offset,
    viewportSize: Size,
    selectedDeviceId: String?,
    hoveredDeviceId: String?,
    filteredDeviceIds: Set<String>,
    onDeviceClick: (String) -> Unit,
    onHoverChange: (String?, Boolean) -> Unit
) {
    devices.forEach { device ->
        // Calculate screen position
        val screenPosition = CoordinateTransformer.transformPosition(
            device.position, metadata, canvasSize, zoom, panOffset
        )
        val screenSize = CoordinateTransformer.transformSize(
            device.size, metadata, canvasSize, zoom
        )

        // Viewport culling - only render if visible
        if (isRectVisible(screenPosition, screenSize, viewportSize)) {
            DeviceNode(
                device = device,
                screenPosition = screenPosition,
                screenSize = screenSize,
                isSelected = device.id == selectedDeviceId,
                isHovered = device.id == hoveredDeviceId,
                isFiltered = device.id in filteredDeviceIds,
                onClick = { onDeviceClick(device.id) },
                onHoverChange = { isHovered ->
                    onHoverChange(device.id, isHovered)
                }
            )
        }
    }
}

/**
 * Viewport culling helper - checks if a rectangle is visible within the viewport.
 */
private fun isRectVisible(
    position: Offset,
    size: Size,
    viewportSize: Size
): Boolean {
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