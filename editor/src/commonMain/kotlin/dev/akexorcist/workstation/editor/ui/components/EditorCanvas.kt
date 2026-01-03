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
import dev.akexorcist.workstation.editor.ui.components.PortOverlay
import dev.akexorcist.workstation.editor.ui.components.findSegmentAtPoint
import dev.akexorcist.workstation.editor.ui.components.findPortAtPoint
import dev.akexorcist.workstation.editor.ui.components.constrainDragToCrossAxis
import dev.akexorcist.workstation.editor.ui.components.constrainPortDragToEdge
import dev.akexorcist.workstation.editor.ui.components.SegmentOrientation
import dev.akexorcist.workstation.data.model.DeviceSide

@Composable
fun EditorCanvas(
    uiState: EditorUiState,
    onPanChange: (DataOffset) -> Unit,
    onHoverSegment: (String?, Int?) -> Unit,
    onDragStartSegment: (String, Int) -> Unit,
    onDragSegment: (String, Int, DataOffset, Boolean) -> Unit,
    onDragEndSegment: () -> Unit,
    onHoverPort: (String?, String?) -> Unit,
    onDragStartPort: (String, String) -> Unit,
    onDragPort: (String, String, DataOffset, Boolean) -> Unit,
    onDragEndPort: () -> Unit,
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
            .pointerInput(
                uiState.selectedLineSegment,
                uiState.selectedPort,
                uiState.zoom,
                actualSize.width,
                actualSize.height
            ) {
                val canvasSize = CoordinateTransformer.canvasSize(
                    width = actualSize.width,
                    height = actualSize.height
                )
                
                var dragStartPan = Offset.Zero
                var isDraggingPort = false
                var isDraggingSegment = false
                var accumulatedPortDrag = Offset.Zero
                var accumulatedSegmentDrag = Offset.Zero
                var portDragInfo: Pair<String, String>? = null
                var portDeviceSide: DeviceSide? = null
                var segmentDragInfo: Pair<Pair<String, Int>, SegmentOrientation>? = null
                
                detectDragGestures(
                    onDragStart = { offset ->
                        val currentSelectedPort = uiState.selectedPort
                        val currentSelectedSegment = uiState.selectedLineSegment
                        val currentRoutedConnectionMap = uiState.routedConnectionMap
                        val currentZoom = uiState.zoom
                        val currentPanOffset = uiState.panOffset
                        
                        if (currentSelectedPort != null) {
                            val portInfo = findPortAtPoint(
                                offset,
                                layout,
                                canvasSize,
                                currentZoom,
                                currentPanOffset
                            )
                            
                            if (portInfo != null && portInfo.first == currentSelectedPort.first && portInfo.second == currentSelectedPort.second) {
                                val device = layout.devices.find { it.id == portInfo.first } ?: return@detectDragGestures
                                val port = device.ports.find { it.id == portInfo.second } ?: return@detectDragGestures
                                
                                isDraggingPort = true
                                portDragInfo = portInfo
                                portDeviceSide = port.position.side
                                accumulatedPortDrag = Offset.Zero
                                isDraggingSegment = false
                                segmentDragInfo = null
                                onDragStartPort(portInfo.first, portInfo.second)
                                return@detectDragGestures
                            }
                        }
                        
                        if (currentSelectedSegment != null) {
                            val segmentInfo = findSegmentAtPoint(
                                offset,
                                layout,
                                currentRoutedConnectionMap,
                                canvasSize,
                                currentZoom,
                                currentPanOffset
                            )
                            
                            if (segmentInfo != null && segmentInfo.first == currentSelectedSegment) {
                                isDraggingSegment = true
                                segmentDragInfo = segmentInfo
                                accumulatedSegmentDrag = Offset.Zero
                                isDraggingPort = false
                                portDragInfo = null
                                portDeviceSide = null
                                onDragStartSegment(segmentInfo.first.first, segmentInfo.first.second)
                                return@detectDragGestures
                            }
                        }
                        
                        isDraggingPort = false
                        isDraggingSegment = false
                        portDragInfo = null
                        portDeviceSide = null
                        segmentDragInfo = null
                        dragStartPan = Offset(panOffsetRef.value.x, panOffsetRef.value.y)
                        accumulatedDrag.value = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        if (isDraggingPort && portDragInfo != null && portDeviceSide != null) {
                            change.consume()
                            
                            val constrainedDelta = constrainPortDragToEdge(
                                dragAmount,
                                portDeviceSide!!
                            )
                            
                            accumulatedPortDrag = Offset(
                                x = accumulatedPortDrag.x + constrainedDelta.x,
                                y = accumulatedPortDrag.y + constrainedDelta.y
                            )
                            
                            val isHorizontal = portDeviceSide == DeviceSide.TOP || portDeviceSide == DeviceSide.BOTTOM
                            
                            onDragPort(
                                portDragInfo!!.first,
                                portDragInfo!!.second,
                                DataOffset(
                                    x = accumulatedPortDrag.x,
                                    y = accumulatedPortDrag.y
                                ),
                                isHorizontal
                            )
                        } else {
                            val currentSegmentDragInfo = segmentDragInfo
                            if (isDraggingSegment && currentSegmentDragInfo != null) {
                                change.consume()
                                
                                val constrainedDelta = constrainDragToCrossAxis(
                                    dragAmount,
                                    currentSegmentDragInfo.second
                                )
                                
                                accumulatedSegmentDrag = Offset(
                                    x = accumulatedSegmentDrag.x + constrainedDelta.x,
                                    y = accumulatedSegmentDrag.y + constrainedDelta.y
                                )
                                
                                onDragSegment(
                                    currentSegmentDragInfo.first.first,
                                    currentSegmentDragInfo.first.second,
                                    DataOffset(
                                        x = accumulatedSegmentDrag.x,
                                        y = accumulatedSegmentDrag.y
                                    ),
                                    currentSegmentDragInfo.second == SegmentOrientation.HORIZONTAL
                                )
                            } else {
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
                        }
                    },
                    onDragEnd = {
                        if (isDraggingPort) {
                            isDraggingPort = false
                            accumulatedPortDrag = Offset.Zero
                            portDragInfo = null
                            portDeviceSide = null
                            onDragEndPort()
                        } else if (isDraggingSegment) {
                            isDraggingSegment = false
                            accumulatedSegmentDrag = Offset.Zero
                            segmentDragInfo = null
                            onDragEndSegment()
                        } else {
                            accumulatedDrag.value = Offset.Zero
                        }
                    },
                    onDragCancel = {
                        if (isDraggingPort) {
                            isDraggingPort = false
                            accumulatedPortDrag = Offset.Zero
                            portDragInfo = null
                            portDeviceSide = null
                            onDragEndPort()
                        } else if (isDraggingSegment) {
                            isDraggingSegment = false
                            accumulatedSegmentDrag = Offset.Zero
                            segmentDragInfo = null
                            onDragEndSegment()
                        } else {
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

        PortOverlay(
            layout = layout,
            canvasSize = canvasSize,
            zoom = zoom,
            panOffset = uiState.panOffset,
            uiState = uiState,
            onHoverPort = onHoverPort
        )

        LineSegmentOverlay(
            layout = layout,
            routedConnectionMap = uiState.routedConnectionMap,
            canvasSize = canvasSize,
            zoom = zoom,
            panOffset = uiState.panOffset,
            uiState = uiState,
            onHoverSegment = onHoverSegment,
            onHoverPort = onHoverPort
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
            onHoverPort = { _, _ -> },
            selectedPort = uiState.selectedPort
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

