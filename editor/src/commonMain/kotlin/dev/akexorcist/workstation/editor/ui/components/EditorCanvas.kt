package dev.akexorcist.workstation.editor.ui.components

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import dev.akexorcist.workstation.data.model.Offset as DataOffset
import dev.akexorcist.workstation.editor.presentation.EditorUiState
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import dev.akexorcist.workstation.utils.CoordinateTransformer
import dev.akexorcist.workstation.ui.components.ConnectionCanvas
import dev.akexorcist.workstation.ui.components.DeviceList
import dev.akexorcist.workstation.ui.components.PortsOverlay
import dev.akexorcist.workstation.editor.ui.components.RoutingPointNodes
import dev.akexorcist.workstation.editor.ui.components.LineSegmentOverlay

@Composable
fun EditorCanvas(
    uiState: EditorUiState,
    onPanChange: (DataOffset) -> Unit,
    onHoverSegment: (String?, Int?) -> Unit,
    onDragStartSegment: (String, Int) -> Unit,
    onDragSegment: (String, Int, DataOffset, Boolean) -> Unit,
    onDragEndSegment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accumulatedDrag = remember { mutableStateOf(Offset.Zero) }
    val panOffsetRef = remember { mutableStateOf(uiState.panOffset) }
    panOffsetRef.value = uiState.panOffset

    var actualSize by remember { mutableStateOf(Size(1280f, 720f)) }
    val density = LocalDensity.current.density

    val layout = uiState.layout ?: return

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
            .pointerInput(uiState.draggingLineSegment) {
                var dragStartPan = Offset.Zero
                
                detectDragGestures(
                    onDragStart = {
                        if (uiState.draggingLineSegment == null) {
                            dragStartPan = Offset(panOffsetRef.value.x, panOffsetRef.value.y)
                            accumulatedDrag.value = Offset.Zero
                        }
                    },
                    onDrag = { change, dragAmount ->
                        if (uiState.draggingLineSegment == null) {
                            change.consume()
                            accumulatedDrag.value = Offset(
                                x = accumulatedDrag.value.x + dragAmount.x,
                                y = accumulatedDrag.value.y + dragAmount.y
                            )
                            onPanChange(
                                DataOffset(
                                    x = dragStartPan.x + accumulatedDrag.value.x,
                                    y = dragStartPan.y + accumulatedDrag.value.y
                                )
                            )
                        }
                    },
                    onDragEnd = {
                        if (uiState.draggingLineSegment == null) {
                            accumulatedDrag.value = Offset.Zero
                        }
                    },
                    onDragCancel = {
                        if (uiState.draggingLineSegment == null) {
                            accumulatedDrag.value = Offset.Zero
                        }
                    }
                )
            }
    ) {
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
            routedConnectionMap = uiState.routedConnectionMap,
            selectedConnectionId = uiState.selectedConnectionId,
            isAnimationEnabled = false,
            hoveredDeviceId = uiState.hoveredDeviceId,
            hoveredPortInfo = uiState.hoveredPortInfo,
            relatedConnectionsMap = emptyMap()
        )

        LineSegmentOverlay(
            layout = layout,
            routedConnectionMap = uiState.routedConnectionMap,
            canvasSize = canvasSize,
            zoom = zoom,
            panOffset = uiState.panOffset,
            uiState = uiState,
            onHoverSegment = onHoverSegment,
            onDragStart = onDragStartSegment,
            onDrag = onDragSegment,
            onDragEnd = onDragEndSegment
        )

        RoutingPointNodes(
            layout = layout,
            routedConnectionMap = uiState.routedConnectionMap,
            canvasSize = canvasSize,
            zoom = zoom,
            panOffset = uiState.panOffset
        )

        PortsOverlay(
            layout = layout,
            canvasSize = canvasSize,
            zoom = zoom,
            panOffset = uiState.panOffset,
            viewportSize = viewportSize,
            hoveredDeviceId = uiState.hoveredDeviceId,
            hoveredPortInfo = uiState.hoveredPortInfo,
            relatedPortsMap = emptyMap(),
            density = density,
            onHoverPort = { _, _ -> }
        )

        DeviceList(
            devices = layout.devices,
            metadata = layout.metadata,
            canvasSize = canvasSize,
            zoom = zoom,
            panOffset = uiState.panOffset,
            viewportSize = viewportSize,
            hoveredDeviceId = uiState.hoveredDeviceId,
            hoveredPortInfo = uiState.hoveredPortInfo,
            density = density,
            onDeviceClick = { },
            onHoverChange = { _, _ -> },
            relatedDevicesMap = emptyMap()
        )
    }
}

