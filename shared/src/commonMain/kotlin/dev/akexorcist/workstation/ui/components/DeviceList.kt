package dev.akexorcist.workstation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import dev.akexorcist.workstation.data.model.Device
import dev.akexorcist.workstation.data.model.LayoutMetadata
import dev.akexorcist.workstation.utils.CoordinateTransformer

/**
 * Renders a list of device nodes with viewport culling optimization.
 * Only devices visible within the viewport are rendered.
 * Uses a custom Layout that ignores parent constraints to prevent height constraints
 * from affecting device node sizes.
 */
@Composable
fun DeviceList(
    devices: List<Device>,
    metadata: LayoutMetadata,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: dev.akexorcist.workstation.data.model.Offset,
    viewportSize: Size,
    hoveredDeviceId: String?,
    hoveredPortInfo: String? = null,
    density: Float = 1f,
    onDeviceClick: (String) -> Unit,
    onHoverChange: (String?, Boolean) -> Unit,
    relatedDevicesMap: Map<String, Boolean> = emptyMap()
) {
    val isHoverHighlightActive = hoveredDeviceId != null || hoveredPortInfo != null
    
    val visibleDevices = devices.filter { device ->
        val screenPosition = CoordinateTransformer.transformPosition(
            device.position, metadata, canvasSize, zoom, panOffset
        )
        val screenSize = CoordinateTransformer.transformSize(
            device.size, metadata, canvasSize, zoom
        )
        isRectVisible(screenPosition, screenSize, viewportSize)
    }
    
    Layout(
        content = {
            visibleDevices.forEach { device ->
                key(device.id) {
                    val screenPosition = CoordinateTransformer.transformPosition(
                        device.position, metadata, canvasSize, zoom, panOffset
                    )
                    val screenSize = CoordinateTransformer.transformSize(
                        device.size, metadata, canvasSize, zoom
                    )
                    
                    val isRelatedToHoveredDevice = !isHoverHighlightActive || relatedDevicesMap[device.id] == true

                    DeviceNode(
                        device = device,
                        screenPosition = screenPosition,
                        screenSize = screenSize,
                        zoom = zoom,
                        isRelatedToHoveredDevice = isRelatedToHoveredDevice,
                        density = density,
                        onClick = { onDeviceClick(device.id) },
                        onHoverChange = { isHovered ->
                            onHoverChange(device.id, isHovered)
                        }
                    )
                }
            }
        },
        modifier = Modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(Constraints())
        }
        
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEach { placeable ->
                placeable.placeRelative(0, 0)
            }
        }
    }
}

/**
 * Check if a rectangle is visible in the viewport with culling margin.
 */
private fun isRectVisible(
    position: Offset,
    size: Size,
    viewportSize: Size
): Boolean {
    val cullingMargin = 50f
    
    val rectLeft = position.x
    val rectRight = position.x + size.width
    val rectTop = position.y
    val rectBottom = position.y + size.height

    val viewportLeft = -cullingMargin
    val viewportRight = viewportSize.width + cullingMargin
    val viewportTop = -cullingMargin
    val viewportBottom = viewportSize.height + cullingMargin

    return rectLeft < viewportRight &&
            rectRight > viewportLeft &&
            rectTop < viewportBottom &&
            rectBottom > viewportTop
}