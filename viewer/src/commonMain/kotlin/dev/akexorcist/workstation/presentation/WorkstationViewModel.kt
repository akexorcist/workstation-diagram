package dev.akexorcist.workstation.presentation

import dev.akexorcist.workstation.data.model.*
import dev.akexorcist.workstation.data.repository.LoadResult
import dev.akexorcist.workstation.data.repository.WorkstationRepository
import dev.akexorcist.workstation.data.repository.WorkstationRepositoryImpl
import dev.akexorcist.workstation.presentation.config.InteractionConfig
import dev.akexorcist.workstation.presentation.config.StateManagementConfig
import dev.akexorcist.workstation.presentation.config.ViewportConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WorkstationViewModel(
    private val repository: WorkstationRepository = WorkstationRepositoryImpl()
) {
    private val _uiState = MutableStateFlow(WorkstationUiState(isLoading = true))
    val uiState: StateFlow<WorkstationUiState> = _uiState.asStateFlow()

    private val _diagramState = MutableStateFlow(DiagramState())
    val diagramState: StateFlow<DiagramState> = _diagramState.asStateFlow()

    suspend fun loadLayout() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        when (val result = repository.loadLayout()) {
            is LoadResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    layout = result.layout,
                    isLoading = false
                )
                updateDiagramState()
            }
            is LoadResult.PartialSuccess -> {
                _uiState.value = _uiState.value.copy(
                    layout = result.layout,
                    isLoading = false,
                    errorMessage = "Loaded with warnings: ${result.errors.joinToString(", ")}"
                )
                updateDiagramState()
            }
            is LoadResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun handleZoomChange(zoom: Float) {
        val validatedZoom = StateManagementConfig.validateZoom(zoom)
        _uiState.value = _uiState.value.copy(zoom = validatedZoom)
        updateDiagramState()
    }
    
    /**
     * Zoom towards a specific point on screen (typically viewport center)
     * This keeps the point under the cursor/center fixed during zoom
     */
    fun handleZoomChangeAtPoint(newZoom: Float, screenPoint: Offset, canvasSize: Size) {
        val oldZoom = _uiState.value.zoom
        val oldPan = _uiState.value.panOffset
        
        val validatedZoom = StateManagementConfig.validateZoom(newZoom)
        
        // Calculate the world position at the screen point before zoom
        // screenPoint = worldPos * oldZoom + oldPan
        // worldPos = (screenPoint - oldPan) / oldZoom
        val worldX = (screenPoint.x - oldPan.x) / oldZoom
        val worldY = (screenPoint.y - oldPan.y) / oldZoom
        
        // After zoom, we want the same world position to be at the same screen point
        // screenPoint = worldPos * newZoom + newPan
        // newPan = screenPoint - worldPos * newZoom
        val newPanX = screenPoint.x - (worldX * validatedZoom)
        val newPanY = screenPoint.y - (worldY * validatedZoom)
        
        val validatedPan = StateManagementConfig.validatePan(
            Offset(newPanX, newPanY),
            canvasSize
        )
        
        _uiState.value = _uiState.value.copy(
            zoom = validatedZoom,
            panOffset = validatedPan
        )
        updateDiagramState()
    }

    fun handlePanChange(offset: Offset) {
        val canvasSize = _uiState.value.layout?.metadata?.canvasSize ?: ViewportConfig.defaultCanvasSize
        val validatedPan = StateManagementConfig.validatePan(offset, canvasSize)
        _uiState.value = _uiState.value.copy(panOffset = validatedPan)
        updateDiagramState()
    }

    fun handleDeviceClick(deviceId: String) {
        _uiState.value = _uiState.value.copy(
            selectedDeviceId = deviceId,
            selectedConnectionId = null
        )
        updateDiagramState()
    }

    fun handleConnectionClick(connectionId: String) {
        _uiState.value = _uiState.value.copy(
            selectedConnectionId = connectionId,
            selectedDeviceId = null
        )
        updateDiagramState()
    }

    fun deselectAll() {
        _uiState.value = _uiState.value.copy(
            selectedDeviceId = null,
            selectedConnectionId = null
        )
        updateDiagramState()
    }

    fun toggleTheme() {
        _uiState.value = _uiState.value.copy(isDarkTheme = !_uiState.value.isDarkTheme)
    }

    fun searchDevices(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        val layout = _uiState.value.layout ?: return
        val filteredIds = if (query.isBlank()) {
            emptySet()
        } else {
            layout.devices
                .filter { device ->
                    device.name.contains(query, ignoreCase = true) ||
                    device.model.contains(query, ignoreCase = true)
                }
                .map { it.id }
                .toSet()
        }

        _uiState.value = _uiState.value.copy(filteredDeviceIds = filteredIds)
        updateDiagramState()
    }

    fun resetZoom() {
        _uiState.value = _uiState.value.copy(zoom = ViewportConfig.defaultZoom)
        updateDiagramState()
    }

    fun resetPan() {
        centerViewportOnDevices()
    }

    fun centerViewportOnDevices(viewportWidth: Float = 1920f, viewportHeight: Float = 1080f) {
        val layout = _uiState.value.layout ?: return
        if (layout.devices.isEmpty()) {
            _uiState.value = _uiState.value.copy(panOffset = StateManagementConfig.initialPan)
            updateDiagramState()
            return
        }

        // Calculate bounding box of all devices
        val minX = layout.devices.minOf { it.position.x }
        val maxX = layout.devices.maxOf { it.position.x + it.size.width }
        val minY = layout.devices.minOf { it.position.y }
        val maxY = layout.devices.maxOf { it.position.y + it.size.height }

        // Calculate center of all devices
        val devicesCenterX = (minX + maxX) / 2f
        val devicesCenterY = (minY + maxY) / 2f

        // Center of full viewport (diagram is full window size)
        val viewportCenterX = viewportWidth / 2f
        val viewportCenterY = viewportHeight / 2f

        // Calculate pan offset to center devices in the viewport
        val zoom = _uiState.value.zoom
        val panX = viewportCenterX - (devicesCenterX * zoom)
        val panY = viewportCenterY - (devicesCenterY * zoom)

        _uiState.value = _uiState.value.copy(
            panOffset = Offset(panX, panY)
        )
        updateDiagramState()
    }

    fun handleDeviceHover(deviceId: String?, isHovered: Boolean) {
        _uiState.value = _uiState.value.copy(
            hoveredDeviceId = if (isHovered) deviceId else null
        )
    }

    fun handleConnectionHover(connectionId: String?, isHovered: Boolean) {
        _uiState.value = _uiState.value.copy(
            hoveredConnectionId = if (isHovered) connectionId else null
        )
    }

    fun toggleInstructionExpanded() {
        _uiState.value = _uiState.value.copy(
            isInstructionExpanded = !_uiState.value.isInstructionExpanded
        )
    }

    fun toggleDeviceListExpanded() {
        _uiState.value = _uiState.value.copy(
            isDeviceListExpanded = !_uiState.value.isDeviceListExpanded
        )
    }

    fun toggleConnectionAnimation() {
        _uiState.value = _uiState.value.copy(
            connectionAnimationEnabled = !_uiState.value.connectionAnimationEnabled
        )
    }

    private fun updateDiagramState() {
        val layout = _uiState.value.layout ?: return
        val zoom = _uiState.value.zoom
        val pan = _uiState.value.panOffset

        val deviceRenderData = layout.devices.map { device ->
            val screenPosition = worldToScreen(device.position, zoom, pan)
            val screenSize = Size(device.size.width * zoom, device.size.height * zoom)
            val isVisible = isDeviceVisible(device, zoom, pan)
            val isSelected = device.id == _uiState.value.selectedDeviceId

            DeviceRenderData(
                device = device,
                screenPosition = screenPosition,
                screenSize = screenSize,
                isVisible = isVisible,
                isSelected = isSelected,
                isHovered = false
            )
        }

        val connectionRenderData = layout.connections.map { connection ->
            val sourceDevice = layout.devices.find { it.id == connection.sourceDeviceId }
            val targetDevice = layout.devices.find { it.id == connection.targetDeviceId }

            val path = if (sourceDevice != null && targetDevice != null) {
                calculateStraightPath(connection, sourceDevice, targetDevice, zoom, pan)
            } else {
                emptyList()
            }

            val isVisible = isConnectionVisible(path, zoom, pan)
            val isSelected = connection.id == _uiState.value.selectedConnectionId

            ConnectionRenderData(
                connection = connection,
                path = path,
                isVisible = isVisible,
                isSelected = isSelected,
                isHovered = false
            )
        }

        _diagramState.value = _diagramState.value.copy(
            scale = zoom,
            deviceRenderData = deviceRenderData,
            connectionRenderData = connectionRenderData
        )
    }

    private fun worldToScreen(position: Position, zoom: Float, pan: Offset): Offset {
        return Offset(
            x = position.x * zoom + pan.x,
            y = position.y * zoom + pan.y
        )
    }

    private fun isDeviceVisible(device: Device, zoom: Float, pan: Offset): Boolean {
        val screenPosition = worldToScreen(device.position, zoom, pan)
        val screenSize = Size(device.size.width * zoom, device.size.height * zoom)

        val deviceRect = Rect(
            left = screenPosition.x,
            top = screenPosition.y,
            right = screenPosition.x + screenSize.width,
            bottom = screenPosition.y + screenSize.height
        )

        val viewportRect = Rect(
            left = -ViewportConfig.viewportCullingMargin,
            top = -ViewportConfig.viewportCullingMargin,
            right = 1920f + ViewportConfig.viewportCullingMargin,
            bottom = 1080f + ViewportConfig.viewportCullingMargin
        )

        return deviceRect.intersects(viewportRect)
    }

    private fun isConnectionVisible(path: List<Point>, zoom: Float, pan: Offset): Boolean {
        if (path.isEmpty()) return false

        val screenPoints = path.map { point ->
            worldToScreen(Position(point.x, point.y), zoom, pan)
        }

        val minX = screenPoints.minOf { it.x } - ViewportConfig.viewportCullingMargin
        val maxX = screenPoints.maxOf { it.x } + ViewportConfig.viewportCullingMargin
        val minY = screenPoints.minOf { it.y } - ViewportConfig.viewportCullingMargin
        val maxY = screenPoints.maxOf { it.y } + ViewportConfig.viewportCullingMargin

        val pathRect = Rect(minX, minY, maxX, maxY)
        val viewportRect = Rect(0f, 0f, 1920f, 1080f)

        return pathRect.intersects(viewportRect)
    }

    private fun calculateStraightPath(
        connection: Connection,
        sourceDevice: Device,
        targetDevice: Device,
        zoom: Float,
        pan: Offset
    ): List<Point> {
        val sourcePort = sourceDevice.ports.find { it.id == connection.sourcePortId }
        val targetPort = targetDevice.ports.find { it.id == connection.targetPortId }

        if (sourcePort == null || targetPort == null) return emptyList()

        val sourcePosition = calculatePortWorldPosition(sourceDevice, sourcePort)
        val targetPosition = calculatePortWorldPosition(targetDevice, targetPort)

        return listOf(sourcePosition, targetPosition)
    }

    private fun calculatePortWorldPosition(device: Device, port: Port): Point {
        val deviceRect = Rect(
            device.position.x,
            device.position.y,
            device.position.x + device.size.width,
            device.position.y + device.size.height
        )

        return when (port.position.side) {
            DeviceSide.TOP -> {
                val positionX = when {
                    port.position.position < 0 -> 0f
                    port.position.position > device.size.width -> device.size.width
                    else -> port.position.position
                }
                Point(
                    deviceRect.left + positionX,
                    deviceRect.top
                )
            }
            DeviceSide.BOTTOM -> {
                val positionX = when {
                    port.position.position < 0 -> 0f
                    port.position.position > device.size.width -> device.size.width
                    else -> port.position.position
                }
                Point(
                    deviceRect.left + positionX,
                    deviceRect.bottom
                )
            }
            DeviceSide.LEFT -> {
                val positionY = when {
                    port.position.position < 0 -> 0f
                    port.position.position > device.size.height -> device.size.height
                    else -> port.position.position
                }
                Point(
                    deviceRect.left,
                    deviceRect.top + positionY
                )
            }
            DeviceSide.RIGHT -> {
                val positionY = when {
                    port.position.position < 0 -> 0f
                    port.position.position > device.size.height -> device.size.height
                    else -> port.position.position
                }
                Point(
                    deviceRect.right,
                    deviceRect.top + positionY
                )
            }
        }
    }
}