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

            val layoutWithSyncedPorts = syncPortPositionsWithConnections(layout)

            val routedConnections = processConnections(
                devices = layoutWithSyncedPorts.devices,
                connections = layoutWithSyncedPorts.connections,
                virtualCanvasSize = virtualCanvas,
                metadata = layout.metadata
            )

            val routedConnectionMap = routedConnections.associateBy { it.connectionId }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    layout = layoutWithSyncedPorts,
                    isLoading = false,
                    errorMessage = errorMessage,
                    routedConnections = routedConnections,
                    routedConnectionMap = routedConnectionMap
                )
            }
        }
    }
    
    private fun syncPortPositionsWithConnections(
        layout: WorkstationLayout
    ): WorkstationLayout {
        val deviceMap = layout.devices.associateBy { it.id }.toMutableMap()
        var hasChanges = false
        
        layout.connections.forEach { connection ->
            val routingPoints = connection.routingPoints ?: return@forEach
            if (routingPoints.isEmpty()) return@forEach
            
            val sourceDevice = deviceMap[connection.sourceDeviceId] ?: return@forEach
            val sourcePort = sourceDevice.ports.find { it.id == connection.sourcePortId } ?: return@forEach
            
            val firstRoutingPoint = routingPoints.first()
            val newSourcePortPosition = calculatePortPositionFromRoutingPoint(
                device = sourceDevice,
                port = sourcePort,
                routingPoint = firstRoutingPoint,
                metadata = layout.metadata
            )
            
            if (newSourcePortPosition != null && newSourcePortPosition != sourcePort.position.position) {
                val updatedPort = sourcePort.copy(
                    position = sourcePort.position.copy(position = newSourcePortPosition)
                )
                val updatedPorts = sourceDevice.ports.map { if (it.id == sourcePort.id) updatedPort else it }
                deviceMap[sourceDevice.id] = sourceDevice.copy(ports = updatedPorts)
                hasChanges = true
            }
            
            val targetDevice = deviceMap[connection.targetDeviceId] ?: return@forEach
            val targetPort = targetDevice.ports.find { it.id == connection.targetPortId } ?: return@forEach
            
            val lastRoutingPoint = routingPoints.last()
            val newTargetPortPosition = calculatePortPositionFromRoutingPoint(
                device = targetDevice,
                port = targetPort,
                routingPoint = lastRoutingPoint,
                metadata = layout.metadata
            )
            
            if (newTargetPortPosition != null && newTargetPortPosition != targetPort.position.position) {
                val updatedPort = targetPort.copy(
                    position = targetPort.position.copy(position = newTargetPortPosition)
                )
                val updatedPorts = targetDevice.ports.map { if (it.id == targetPort.id) updatedPort else it }
                deviceMap[targetDevice.id] = targetDevice.copy(ports = updatedPorts)
                hasChanges = true
            }
        }
        
        return if (hasChanges) {
            layout.copy(devices = deviceMap.values.toList())
        } else {
            layout
        }
    }
    
    private fun calculatePortPositionFromRoutingPoint(
        device: dev.akexorcist.workstation.data.model.Device,
        port: dev.akexorcist.workstation.data.model.Port,
        routingPoint: Point,
        metadata: dev.akexorcist.workstation.data.model.LayoutMetadata
    ): Float? {
        val gridConfig = metadata.grid
        val gridSize = gridConfig?.size ?: 20f
        
        return when (port.position.side) {
            dev.akexorcist.workstation.data.model.DeviceSide.TOP,
            dev.akexorcist.workstation.data.model.DeviceSide.BOTTOM -> {
                val newPosition = routingPoint.x - device.position.x
                newPosition.coerceIn(gridSize, device.size.width - gridSize)
            }
            dev.akexorcist.workstation.data.model.DeviceSide.LEFT,
            dev.akexorcist.workstation.data.model.DeviceSide.RIGHT -> {
                val newPosition = routingPoint.y - device.position.y
                newPosition.coerceIn(gridSize, device.size.height - gridSize)
            }
        }
    }
    
    private fun processConnections(
        devices: List<dev.akexorcist.workstation.data.model.Device>,
        connections: List<Connection>,
        virtualCanvasSize: dev.akexorcist.workstation.data.model.Size,
        metadata: dev.akexorcist.workstation.data.model.LayoutMetadata
    ): List<RoutedConnection> {
        // Only handle connections with manual routing points
        // Automatic routing is not available in editor
        return connections
            .filter { connection ->
                val routingPoints = connection.routingPoints
                routingPoints != null && routingPoints.isNotEmpty()
            }
            .mapNotNull { connection ->
                createRoutedConnectionFromManualPoints(connection, devices, virtualCanvasSize, metadata)
            }
    }
    
    private fun createRoutedConnectionFromManualPoints(
        connection: Connection,
        devices: List<dev.akexorcist.workstation.data.model.Device>,
        virtualCanvasSize: dev.akexorcist.workstation.data.model.Size,
        metadata: dev.akexorcist.workstation.data.model.LayoutMetadata
    ): RoutedConnection? {
        val routingPoints = connection.routingPoints ?: return null
        if (routingPoints.isEmpty()) return null
        
        val sourceDevice = devices.find { it.id == connection.sourceDeviceId } ?: return null
        val targetDevice = devices.find { it.id == connection.targetDeviceId } ?: return null
        val sourcePort = sourceDevice.ports.find { it.id == connection.sourcePortId } ?: return null
        val targetPort = targetDevice.ports.find { it.id == connection.targetPortId } ?: return null
        
        val sourcePortVirtualPos = calculatePortPosition(sourceDevice, sourcePort, metadata)
        val targetPortVirtualPos = calculatePortPosition(targetDevice, targetPort, metadata)
        
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
        port: dev.akexorcist.workstation.data.model.Port,
        metadata: dev.akexorcist.workstation.data.model.LayoutMetadata
    ): Pair<Float, Float> {
        val gridConfig = metadata.grid
        val gridSize = gridConfig?.size ?: 20f
        
        return when (port.position.side) {
            dev.akexorcist.workstation.data.model.DeviceSide.TOP -> {
                val positionX = port.position.position.coerceIn(gridSize, device.size.width - gridSize)
                device.position.x + positionX to device.position.y
            }
            dev.akexorcist.workstation.data.model.DeviceSide.BOTTOM -> {
                val positionX = port.position.position.coerceIn(gridSize, device.size.width - gridSize)
                device.position.x + positionX to device.position.y + device.size.height
            }
            dev.akexorcist.workstation.data.model.DeviceSide.LEFT -> {
                val positionY = port.position.position.coerceIn(gridSize, device.size.height - gridSize)
                device.position.x to device.position.y + positionY
            }
            dev.akexorcist.workstation.data.model.DeviceSide.RIGHT -> {
                val positionY = port.position.position.coerceIn(gridSize, device.size.height - gridSize)
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
        
        if (segmentIndex <= 0 || segmentIndex >= virtualWaypoints.size - 1) return
        
        val metadata = layout.metadata
        val zoom = _uiState.value.zoom
        val virtualDelta = screenDeltaToVirtualDelta(screenDragDelta, metadata, canvasSize, zoom)
        
        val constrainedDelta = if (isHorizontal) {
            Offset(0f, virtualDelta.y)
        } else {
            Offset(virtualDelta.x, 0f)
        }
        
        if (dragStartConnectionId != connectionId || dragStartSegmentIndex != segmentIndex) {
            originalRoutingPoints = connection.routingPoints?.toList()
            dragStartConnectionId = connectionId
            dragStartSegmentIndex = segmentIndex
        }
        
        val originalPoints = originalRoutingPoints ?: connection.routingPoints?.toList() ?: return
        val routingPoints = originalPoints.toMutableList()
        
        val gridConfig = metadata.grid
        val gridSize = gridConfig?.size ?: 20f
        val gridEnabled = gridConfig?.enabled ?: true
        
        if (segmentIndex > 0 && segmentIndex - 1 < routingPoints.size) {
            val startPointIndex = segmentIndex - 1
            val newX = originalPoints[startPointIndex].x + constrainedDelta.x
            val newY = originalPoints[startPointIndex].y + constrainedDelta.y
            
            routingPoints[startPointIndex] = if (gridEnabled) {
                Point(
                    x = snapToGrid(newX, gridSize),
                    y = snapToGrid(newY, gridSize)
                )
            } else {
                Point(x = newX, y = newY)
            }
        }
        
        if (segmentIndex < routingPoints.size) {
            val newX = originalPoints[segmentIndex].x + constrainedDelta.x
            val newY = originalPoints[segmentIndex].y + constrainedDelta.y
            
            routingPoints[segmentIndex] = if (gridEnabled) {
                Point(
                    x = snapToGrid(newX, gridSize),
                    y = snapToGrid(newY, gridSize)
                )
            } else {
                Point(x = newX, y = newY)
            }
        }
        
        val updatedConnection = connection.copy(routingPoints = routingPoints)
        val updatedConnections = layout.connections.map { if (it.id == connectionId) updatedConnection else it }
        val updatedLayout = layout.copy(connections = updatedConnections)
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main.immediate) {
            updateLayoutWithConnections(updatedLayout)
        }
    }
    
    private var originalRoutingPoints: List<Point>? = null
    private var dragStartConnectionId: String? = null
    private var dragStartSegmentIndex: Int = -1
    
    fun clearSegmentDragState() {
        originalRoutingPoints = null
        dragStartConnectionId = null
        dragStartSegmentIndex = -1
    }
    
    private var originalPortPosition: Float? = null
    private var dragStartDeviceId: String? = null
    private var dragStartPortId: String? = null
    
    fun clearPortDragState() {
        originalPortPosition = null
        dragStartDeviceId = null
        dragStartPortId = null
    }
    
    fun updatePortPosition(
        deviceId: String,
        portId: String,
        screenDragDelta: ComposeOffset,
        canvasSize: dev.akexorcist.workstation.data.model.Size,
        isHorizontal: Boolean
    ) {
        val layout = _uiState.value.layout ?: return
        val device = layout.devices.find { it.id == deviceId } ?: return
        val port = device.ports.find { it.id == portId } ?: return
        
        val metadata = layout.metadata
        val zoom = _uiState.value.zoom
        val virtualDelta = screenDeltaToVirtualDelta(screenDragDelta, metadata, canvasSize, zoom)
        
        val constrainedDelta = if (isHorizontal) {
            Offset(virtualDelta.x, 0f)
        } else {
            Offset(0f, virtualDelta.y)
        }
        
        if (dragStartDeviceId != deviceId || dragStartPortId != portId) {
            originalPortPosition = port.position.position
            dragStartDeviceId = deviceId
            dragStartPortId = portId
        }
        
        val originalPosition = originalPortPosition ?: port.position.position
        
        val newPosition = if (isHorizontal) {
            originalPosition + constrainedDelta.x
        } else {
            originalPosition + constrainedDelta.y
        }
        
        val gridConfig = metadata.grid
        val gridSize = gridConfig?.size ?: 20f
        val gridEnabled = gridConfig?.enabled ?: true
        
        val clampedPosition = when (port.position.side) {
            dev.akexorcist.workstation.data.model.DeviceSide.TOP,
            dev.akexorcist.workstation.data.model.DeviceSide.BOTTOM -> {
                newPosition.coerceIn(gridSize, device.size.width - gridSize)
            }
            dev.akexorcist.workstation.data.model.DeviceSide.LEFT,
            dev.akexorcist.workstation.data.model.DeviceSide.RIGHT -> {
                newPosition.coerceIn(gridSize, device.size.height - gridSize)
            }
        }
        
        val finalPosition = if (gridEnabled) {
            snapToGrid(clampedPosition, gridSize)
        } else {
            clampedPosition
        }
        
        val updatedPort = port.copy(
            position = port.position.copy(position = finalPosition)
        )
        
        val updatedPorts = device.ports.map { if (it.id == portId) updatedPort else it }
        val updatedDevice = device.copy(ports = updatedPorts)
        val updatedDevices = layout.devices.map { if (it.id == deviceId) updatedDevice else it }
        var updatedLayout = layout.copy(devices = updatedDevices)
        
        val oldPortVirtualPos = calculatePortPosition(device, port, metadata)
        val newPortVirtualPos = calculatePortPosition(updatedDevice, updatedPort, metadata)
        
        val portDeltaX = newPortVirtualPos.first - oldPortVirtualPos.first
        val portDeltaY = newPortVirtualPos.second - oldPortVirtualPos.second
        
        val isPortHorizontal = port.position.side == dev.akexorcist.workstation.data.model.DeviceSide.TOP || 
                               port.position.side == dev.akexorcist.workstation.data.model.DeviceSide.BOTTOM
        
        val updatedConnections = updatedLayout.connections.map { connection ->
            val routingPoints = connection.routingPoints ?: return@map connection
            if (routingPoints.isEmpty()) return@map connection
            
            val updatedRoutingPoints = routingPoints.toMutableList()
            var needsUpdate = false
            
            if (connection.sourceDeviceId == deviceId && connection.sourcePortId == portId) {
                val firstPoint = routingPoints[0]
                val newPoint = if (isPortHorizontal) {
                    Point(x = firstPoint.x + portDeltaX, y = firstPoint.y)
                } else {
                    Point(x = firstPoint.x, y = firstPoint.y + portDeltaY)
                }
                updatedRoutingPoints[0] = newPoint
                needsUpdate = true
            }
            
            if (connection.targetDeviceId == deviceId && connection.targetPortId == portId) {
                val lastIndex = updatedRoutingPoints.size - 1
                val lastPoint = routingPoints[lastIndex]
                val newPoint = if (isPortHorizontal) {
                    Point(x = lastPoint.x + portDeltaX, y = lastPoint.y)
                } else {
                    Point(x = lastPoint.x, y = lastPoint.y + portDeltaY)
                }
                updatedRoutingPoints[lastIndex] = newPoint
                needsUpdate = true
            }
            
            if (needsUpdate) {
                connection.copy(routingPoints = updatedRoutingPoints)
            } else {
                connection
            }
        }
        
        updatedLayout = updatedLayout.copy(connections = updatedConnections)
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main.immediate) {
            updateLayoutWithConnections(updatedLayout)
        }
    }
    
    private fun snapToGrid(value: Float, gridSize: Float): Float {
        return kotlin.math.round(value / gridSize) * gridSize
    }
    
    private fun updateLayoutWithConnections(updatedLayout: dev.akexorcist.workstation.data.model.WorkstationLayout) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val virtualCanvas = updatedLayout.metadata.virtualCanvas ?: updatedLayout.metadata.canvasSize

            val layoutWithSyncedPorts = syncPortPositionsWithConnections(updatedLayout)

            val routedConnections = processConnections(
                devices = layoutWithSyncedPorts.devices,
                connections = layoutWithSyncedPorts.connections,
                virtualCanvasSize = virtualCanvas,
                metadata = updatedLayout.metadata
            )

            val routedConnectionMap = routedConnections.associateBy { it.connectionId }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    layout = layoutWithSyncedPorts,
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
    
    fun setSelectedPort(deviceId: String?, portId: String?) {
        _uiState.value = _uiState.value.copy(
            selectedPort = if (deviceId != null && portId != null) {
                Pair(deviceId, portId)
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

