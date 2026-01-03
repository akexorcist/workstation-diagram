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
import dev.akexorcist.workstation.editor.ui.components.findSegmentAtPoint
import dev.akexorcist.workstation.editor.ui.components.constrainDragToCrossAxis
import dev.akexorcist.workstation.editor.ui.components.SegmentOrientation

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
            .pointerInput(
                // Only include keys that should trigger gesture detector recreation
                // Don't include panOffset - it changes during drag and would interrupt the gesture
                uiState.selectedLineSegment,
                uiState.zoom,
                actualSize.width,
                actualSize.height
            ) {
                val canvasSize = CoordinateTransformer.canvasSize(
                    width = actualSize.width,
                    height = actualSize.height
                )
                
                // Capture current state values - these are captured once when pointerInput is created
                // We'll read fresh values from uiState inside the gesture handlers
                var dragStartPan = Offset.Zero
                var isDraggingSegment = false
                var accumulatedSegmentDrag = Offset.Zero
                var segmentDragInfo: Pair<Pair<String, Int>, SegmentOrientation>? = null
                
                detectDragGestures(
                    onDragStart = { offset ->
                        // Read fresh state values at drag start
                        val currentSelectedSegment = uiState.selectedLineSegment
                        val currentRoutedConnectionMap = uiState.routedConnectionMap
                        val currentZoom = uiState.zoom
                        val currentPanOffset = uiState.panOffset
                        
                        // Check if we're starting a drag on a line segment
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
                                // Start dragging line segment
                                isDraggingSegment = true
                                segmentDragInfo = segmentInfo
                                accumulatedSegmentDrag = Offset.Zero
                                onDragStartSegment(segmentInfo.first.first, segmentInfo.first.second)
                            } else {
                                // Start viewport pan
                                isDraggingSegment = false
                                segmentDragInfo = null
                                dragStartPan = Offset(panOffsetRef.value.x, panOffsetRef.value.y)
                                accumulatedDrag.value = Offset.Zero
                            }
                        } else {
                            // Start viewport pan
                            isDraggingSegment = false
                            segmentDragInfo = null
                            dragStartPan = Offset(panOffsetRef.value.x, panOffsetRef.value.y)
                            accumulatedDrag.value = Offset.Zero
                        }
                    },
                    onDrag = { change, dragAmount ->
                        val currentSegmentDragInfo = segmentDragInfo
                        if (isDraggingSegment && currentSegmentDragInfo != null) {
                            // Handle line segment drag - accumulate delta
                            change.consume()
                            
                            // Constrain to cross-axis and accumulate
                            val constrainedDelta = constrainDragToCrossAxis(
                                dragAmount,
                                currentSegmentDragInfo.second
                            )
                            
                            accumulatedSegmentDrag = Offset(
                                x = accumulatedSegmentDrag.x + constrainedDelta.x,
                                y = accumulatedSegmentDrag.y + constrainedDelta.y
                            )
                            
                            // Apply accumulated drag to update the segment
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
                            // Handle viewport pan
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
                        if (isDraggingSegment) {
                            // End line segment drag - final update
                            isDraggingSegment = false
                            accumulatedSegmentDrag = Offset.Zero
                            segmentDragInfo = null
                            onDragEndSegment()
                        } else {
                            // End viewport pan
                            accumulatedDrag.value = Offset.Zero
                        }
                    },
                    onDragCancel = {
                        if (isDraggingSegment) {
                            // Cancel line segment drag
                            isDraggingSegment = false
                            accumulatedSegmentDrag = Offset.Zero
                            segmentDragInfo = null
                            onDragEndSegment()
                        } else {
                            // Cancel viewport pan
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
            onHoverSegment = onHoverSegment
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

