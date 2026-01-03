package dev.akexorcist.workstation.editor.presentation

import androidx.compose.ui.geometry.Offset as ComposeOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.akexorcist.workstation.data.model.Connection
import dev.akexorcist.workstation.data.model.Offset
import dev.akexorcist.workstation.data.model.Point
import dev.akexorcist.workstation.data.model.Position
import dev.akexorcist.workstation.data.model.WorkstationLayout
import dev.akexorcist.workstation.data.repository.LoadResult
import dev.akexorcist.workstation.data.repository.WorkstationRepository
import dev.akexorcist.workstation.data.repository.WorkstationRepositoryImpl
import dev.akexorcist.workstation.data.serialization.WorkstationLayoutSerializer
import dev.akexorcist.workstation.routing.RoutedConnection
import dev.akexorcist.workstation.presentation.config.StateManagementConfig
import dev.akexorcist.workstation.presentation.config.ViewportConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditorViewModel(
    private val repository: WorkstationRepository = WorkstationRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditorUiState(isLoading = true))
    val uiState: StateFlow<EditorUiState> = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        EditorUiState(isLoading = true)
    )

    fun loadLayout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.loadLayout()) {
                is LoadResult.Success -> {
                    processLayoutWithConnections(result.layout, null)
                }
                is LoadResult.PartialSuccess -> {
                    processLayoutWithConnections(
                        result.layout,
                        "Loaded with warnings: ${result.errors.joinToString(", ")}"
                    )
                }
                is LoadResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private fun processLayoutWithConnections(
        layout: WorkstationLayout,
        errorMessage: String?
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val virtualCanvas = layout.metadata.virtualCanvas ?: layout.metadata.canvasSize

            val routedConnections = processConnections(
                devices = layout.devices,
                connections = layout.connections,
                virtualCanvasSize = virtualCanvas
            )

            val routedConnectionMap = routedConnections.associateBy { it.connectionId }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    layout = layout,
                    isLoading = false,
                    errorMessage = errorMessage,
                    routedConnections = routedConnections,
                    routedConnectionMap = routedConnectionMap
                )
            }
        }
    }
    
    private fun processConnections(
        devices: List<dev.akexorcist.workstation.data.model.Device>,
        connections: List<Connection>,
        virtualCanvasSize: dev.akexorcist.workstation.data.model.Size
    ): List<RoutedConnection> {
        // Only handle connections with manual routing points
        // Automatic routing is not available in editor
        return connections
            .filter { connection ->
                val routingPoints = connection.routingPoints
                routingPoints != null && routingPoints.isNotEmpty()
            }
            .mapNotNull { connection ->
                createRoutedConnectionFromManualPoints(connection, devices, virtualCanvasSize)
            }
    }
    
    private fun createRoutedConnectionFromManualPoints(
        connection: Connection,
        devices: List<dev.akexorcist.workstation.data.model.Device>,
        virtualCanvasSize: dev.akexorcist.workstation.data.model.Size
    ): RoutedConnection? {
        val routingPoints = connection.routingPoints ?: return null
        if (routingPoints.isEmpty()) return null
        
        val sourceDevice = devices.find { it.id == connection.sourceDeviceId } ?: return null
        val targetDevice = devices.find { it.id == connection.targetDeviceId } ?: return null
        val sourcePort = sourceDevice.ports.find { it.id == connection.sourcePortId } ?: return null
        val targetPort = targetDevice.ports.find { it.id == connection.targetPortId } ?: return null
        
        val sourcePortVirtualPos = calculatePortPosition(sourceDevice, sourcePort)
        val targetPortVirtualPos = calculatePortPosition(targetDevice, targetPort)
        
        val virtualWaypoints = buildList {
            add(sourcePortVirtualPos)
            routingPoints.forEach { point ->
                add(point.x to point.y)
            }
            add(targetPortVirtualPos)
        }
        
        return RoutedConnection(
            connectionId = connection.id,
            waypoints = emptyList(),
            virtualWaypoints = virtualWaypoints,
            success = true,
            crossings = 0
        )
    }
    
    private fun calculatePortPosition(
        device: dev.akexorcist.workstation.data.model.Device,
        port: dev.akexorcist.workstation.data.model.Port
    ): Pair<Float, Float> {
        return when (port.position.side) {
            dev.akexorcist.workstation.data.model.DeviceSide.TOP -> {
                val positionX = port.position.position.coerceIn(0f, device.size.width)
                device.position.x + positionX to device.position.y
            }
            dev.akexorcist.workstation.data.model.DeviceSide.BOTTOM -> {
                val positionX = port.position.position.coerceIn(0f, device.size.width)
                device.position.x + positionX to device.position.y + device.size.height
            }
            dev.akexorcist.workstation.data.model.DeviceSide.LEFT -> {
                val positionY = port.position.position.coerceIn(0f, device.size.height)
                device.position.x to device.position.y + positionY
            }
            dev.akexorcist.workstation.data.model.DeviceSide.RIGHT -> {
                val positionY = port.position.position.coerceIn(0f, device.size.height)
                device.position.x + device.size.width to device.position.y + positionY
            }
        }
    }

    fun handleZoomChangeAtPoint(newZoom: Float, screenPoint: Offset) {
        val oldZoom = _uiState.value.zoom
        val oldPan = _uiState.value.panOffset
        val viewportConfig = _uiState.value.layout?.metadata?.viewport

        val validatedZoom = StateManagementConfig.validateZoom(newZoom, viewportConfig)

        val worldX = (screenPoint.x - oldPan.x) / oldZoom
        val worldY = (screenPoint.y - oldPan.y) / oldZoom

        val newPanX = screenPoint.x - (worldX * validatedZoom)
        val newPanY = screenPoint.y - (worldY * validatedZoom)

        val validatedPan = StateManagementConfig.validatePan(Offset(newPanX, newPanY))

        _uiState.value = _uiState.value.copy(
            zoom = validatedZoom,
            panOffset = validatedPan
        )
    }

    fun handlePanChange(offset: Offset) {
        val validatedPan = StateManagementConfig.validatePan(offset)
        _uiState.value = _uiState.value.copy(panOffset = validatedPan)
    }

    fun updateLineSegmentEndpoints(
        connectionId: String,
        segmentIndex: Int,
        screenPosition: ComposeOffset,
        screenDragDelta: ComposeOffset,
        canvasSize: dev.akexorcist.workstation.data.model.Size,
        isHorizontal: Boolean = false
    ) {
        val layout = _uiState.value.layout ?: return
        val connection = layout.connections.find { it.id == connectionId } ?: return
        val routedConnection = _uiState.value.routedConnectionMap[connectionId] ?: return
        val virtualWaypoints = routedConnection.virtualWaypoints
        
        // Validate segment index (must be between routing points, not edge segments)
        if (segmentIndex <= 0 || segmentIndex >= virtualWaypoints.size - 1) return
        
        // Convert screen delta to virtual delta
        val metadata = layout.metadata
        val zoom = _uiState.value.zoom
        val virtualDelta = screenDeltaToVirtualDelta(screenDragDelta, metadata, canvasSize, zoom)
        
        // Constrain to cross-axis based on segment orientation
        val constrainedDelta = if (isHorizontal) {
            // Horizontal segment: only move vertically
            Offset(0f, virtualDelta.y)
        } else {
            // Vertical segment: only move horizontally
            Offset(virtualDelta.x, 0f)
        }
        
        // virtualWaypoints structure: [sourcePort, routingPoint1, routingPoint2, ..., targetPort]
        // Segment index i connects virtualWaypoints[i] to virtualWaypoints[i+1]
        // For segment i, we need to move:
        // - virtualWaypoints[i] (routing point at index i-1 in Connection.routingPoints)
        // - virtualWaypoints[i+1] (routing point at index i in Connection.routingPoints)
        
        // Store original routing points at drag start
        if (dragStartConnectionId != connectionId || dragStartSegmentIndex != segmentIndex) {
            originalRoutingPoints = connection.routingPoints?.toList()
            dragStartConnectionId = connectionId
            dragStartSegmentIndex = segmentIndex
        }
        
        val originalPoints = originalRoutingPoints ?: connection.routingPoints?.toList() ?: return
        val routingPoints = originalPoints.toMutableList()
        
        // Update the start routing point using original position + accumulated delta
        // (virtualWaypoints[i] corresponds to routingPoints[i-1])
        if (segmentIndex > 0 && segmentIndex - 1 < routingPoints.size) {
            val startPointIndex = segmentIndex - 1
            routingPoints[startPointIndex] = Point(
                x = originalPoints[startPointIndex].x + constrainedDelta.x,
                y = originalPoints[startPointIndex].y + constrainedDelta.y
            )
        }
        
        // Update the end routing point using original position + accumulated delta
        // (virtualWaypoints[i+1] corresponds to routingPoints[i])
        if (segmentIndex < routingPoints.size) {
            routingPoints[segmentIndex] = Point(
                x = originalPoints[segmentIndex].x + constrainedDelta.x,
                y = originalPoints[segmentIndex].y + constrainedDelta.y
            )
        }
        
        // Update the connection with new routing points
        val updatedConnection = connection.copy(routingPoints = routingPoints)
        val updatedConnections = layout.connections.map { if (it.id == connectionId) updatedConnection else it }
        val updatedLayout = layout.copy(connections = updatedConnections)
        
        // Update layout and recalculate routed connections
        // Use a coroutine to update asynchronously to avoid blocking the gesture
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main.immediate) {
            updateLayoutWithConnections(updatedLayout)
        }
    }
    
    // Store original routing points at drag start
    private var originalRoutingPoints: List<Point>? = null
    private var dragStartConnectionId: String? = null
    private var dragStartSegmentIndex: Int = -1
    
    fun clearSegmentDragState() {
        originalRoutingPoints = null
        dragStartConnectionId = null
        dragStartSegmentIndex = -1
    }
    
    private fun updateLayoutWithConnections(updatedLayout: dev.akexorcist.workstation.data.model.WorkstationLayout) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val virtualCanvas = updatedLayout.metadata.virtualCanvas ?: updatedLayout.metadata.canvasSize

            val routedConnections = processConnections(
                devices = updatedLayout.devices,
                connections = updatedLayout.connections,
                virtualCanvasSize = virtualCanvas
            )

            val routedConnectionMap = routedConnections.associateBy { it.connectionId }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    layout = updatedLayout,
                    routedConnections = routedConnections,
                    routedConnectionMap = routedConnectionMap
                )
            }
        }
    }

    fun updateRoutingPoint(
        connectionId: String,
        pointIndex: Int,
        screenDelta: Offset,
        canvasSize: dev.akexorcist.workstation.data.model.Size
    ) {
    }

    private fun screenToVirtual(
        screenPosition: ComposeOffset,
        metadata: dev.akexorcist.workstation.data.model.LayoutMetadata,
        canvasSize: dev.akexorcist.workstation.data.model.Size,
        zoom: Float,
        panOffset: Offset
    ): Position {
        val worldX = (screenPosition.x - panOffset.x) / zoom
        val worldY = (screenPosition.y - panOffset.y) / zoom

        val isVirtual = metadata.coordinateSystem == "virtual" && metadata.virtualCanvas != null

        return if (isVirtual) {
            val virtualCanvas = metadata.virtualCanvas!!
            val scaleX = virtualCanvas.width / canvasSize.width
            val scaleY = virtualCanvas.height / canvasSize.height
            Position(
                x = worldX * scaleX,
                y = worldY * scaleY
            )
        } else {
            Position(worldX, worldY)
        }
    }

    private fun screenDeltaToVirtualDelta(
        screenDelta: ComposeOffset,
        metadata: dev.akexorcist.workstation.data.model.LayoutMetadata,
        canvasSize: dev.akexorcist.workstation.data.model.Size,
        zoom: Float
    ): Offset {
        val isVirtual = metadata.coordinateSystem == "virtual" && metadata.virtualCanvas != null

        return if (isVirtual) {
            val virtualCanvas = metadata.virtualCanvas!!
            val scaleX = virtualCanvas.width / canvasSize.width
            val scaleY = virtualCanvas.height / canvasSize.height
            Offset(
                x = (screenDelta.x / zoom) * scaleX,
                y = (screenDelta.y / zoom) * scaleY
            )
        } else {
            Offset(
                x = screenDelta.x / zoom,
                y = screenDelta.y / zoom
            )
        }
    }

    fun setDraggingRoutingPoint(connectionId: String?, segmentIndex: Int?) {
        _uiState.value = _uiState.value.copy(
            draggingRoutingPoint = if (connectionId != null && segmentIndex != null) {
                Pair(connectionId, segmentIndex)
            } else null
        )
    }

    fun setHoveredRoutingPoint(connectionId: String?, segmentIndex: Int?) {
        _uiState.value = _uiState.value.copy(
            hoveredRoutingPoint = if (connectionId != null && segmentIndex != null) {
                Pair(connectionId, segmentIndex)
            } else null
        )
    }

    fun setSelectedLineSegment(connectionId: String?, segmentIndex: Int?) {
        _uiState.value = _uiState.value.copy(
            selectedLineSegment = if (connectionId != null && segmentIndex != null) {
                Pair(connectionId, segmentIndex)
            } else null
        )
    }

    fun resetZoom() {
        val defaultZoom = _uiState.value.layout?.metadata?.viewport?.defaultZoom ?: ViewportConfig.defaultZoom
        _uiState.value = _uiState.value.copy(zoom = defaultZoom)
    }

    fun centerViewportOnDevices(
        viewportWidth: Float = _uiState.value.viewportSize.width,
        viewportHeight: Float = _uiState.value.viewportSize.height
    ) {
        val layout = _uiState.value.layout ?: return
        if (layout.devices.isEmpty()) {
            _uiState.value = _uiState.value.copy(panOffset = StateManagementConfig.initialPan)
            return
        }

        val minX = layout.devices.minOf { it.position.x }
        val maxX = layout.devices.maxOf { it.position.x + it.size.width }
        val minY = layout.devices.minOf { it.position.y }
        val maxY = layout.devices.maxOf { it.position.y + it.size.height }

        val devicesCenterX = (minX + maxX) / 2f
        val devicesCenterY = (minY + maxY) / 2f

        val viewportCenterX = viewportWidth / 2f
        val viewportCenterY = viewportHeight / 2f

        val zoom = _uiState.value.zoom
        val panX = viewportCenterX - (devicesCenterX * zoom)
        val panY = viewportCenterY - (devicesCenterY * zoom)

        _uiState.value = _uiState.value.copy(panOffset = Offset(panX, panY))
    }

    fun toggleTheme() {
        _uiState.value = _uiState.value.copy(isDarkTheme = !_uiState.value.isDarkTheme)
    }

    fun toggleConnectionAnimation() {
        _uiState.value = _uiState.value.copy(
            connectionAnimationEnabled = !_uiState.value.connectionAnimationEnabled
        )
    }

    fun exportToJson(prettyPrint: Boolean = true): String {
        val layout = _uiState.value.layout ?: return ""
        val json = WorkstationLayoutSerializer.toJson(layout)
        return if (prettyPrint) {
            formatJson(json)
        } else {
            json
        }
    }

    private fun formatJson(json: String): String {
        var indent = 0
        val indentSize = 2
        val result = StringBuilder()
        var inString = false
        var escapeNext = false

        for (char in json) {
            when {
                escapeNext -> {
                    result.append(char)
                    escapeNext = false
                }
                char == '\\' -> {
                    result.append(char)
                    escapeNext = true
                }
                char == '"' -> {
                    result.append(char)
                    inString = !inString
                }
                inString -> {
                    result.append(char)
                }
                char == '{' || char == '[' -> {
                    result.append(char)
                    indent++
                    result.append('\n')
                    result.append(" ".repeat(indent * indentSize))
                }
                char == '}' || char == ']' -> {
                    indent--
                    result.append('\n')
                    result.append(" ".repeat(indent * indentSize))
                    result.append(char)
                }
                char == ',' -> {
                    result.append(char)
                    result.append('\n')
                    result.append(" ".repeat(indent * indentSize))
                }
                char == ':' -> {
                    result.append(char)
                    result.append(' ')
                }
                char.isWhitespace() -> {
                }
                else -> {
                    result.append(char)
                }
            }
        }
        return result.toString()
    }
}

