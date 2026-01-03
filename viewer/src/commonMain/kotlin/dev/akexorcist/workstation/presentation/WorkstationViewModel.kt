package dev.akexorcist.workstation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.akexorcist.workstation.data.model.Connection
import dev.akexorcist.workstation.data.model.Device
import dev.akexorcist.workstation.data.model.DeviceSide
import dev.akexorcist.workstation.data.model.Offset
import dev.akexorcist.workstation.data.model.Point
import dev.akexorcist.workstation.data.model.Port
import dev.akexorcist.workstation.data.model.Position
import dev.akexorcist.workstation.data.model.Size
import dev.akexorcist.workstation.data.model.WorkstationLayout
import dev.akexorcist.workstation.data.repository.LoadResult
import dev.akexorcist.workstation.data.repository.WorkstationRepository
import dev.akexorcist.workstation.data.repository.WorkstationRepositoryImpl
import dev.akexorcist.workstation.presentation.config.StateManagementConfig
import dev.akexorcist.workstation.presentation.config.ViewportConfig
import dev.akexorcist.workstation.routing.ConnectionPathConverter
import dev.akexorcist.workstation.routing.RoutedConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkstationViewModel(
    private val repository: WorkstationRepository = WorkstationRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkstationUiState(isLoading = true))
    val uiState: StateFlow<WorkstationUiState> = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WorkstationUiState(isLoading = true)
    )

    private val _diagramState = MutableStateFlow(DiagramState())
    val diagramState: StateFlow<DiagramState> = _diagramState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DiagramState()
    )

    fun loadLayout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.loadLayout()) {
                is LoadResult.Success -> {
                    processLayoutWithConnections(
                        result.layout, 
                        null
                    )
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
            val routedConnections = ConnectionPathConverter.convertConnections(
                devices = layout.devices,
                connections = layout.connections
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
                updateDiagramState()
            }
        }
    }

    /**
     * Zoom towards a specific point on screen (typically viewport center)
     * This keeps the point under the cursor/center fixed during zoom
     * while respecting zoom constraints from workstation.json configuration.
     *
     * @param newZoom The desired new zoom level
     * @param screenPoint The screen point to maintain position during zoom
     */
    fun handleZoomChangeAtPoint(newZoom: Float, screenPoint: Offset) {
        val oldZoom = _uiState.value.zoom
        val oldPan = _uiState.value.panOffset
        val viewportConfig = _uiState.value.layout?.metadata?.viewport

        val validatedZoom = StateManagementConfig.validateZoom(newZoom, viewportConfig)

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

        val validatedPan = StateManagementConfig.validatePan(Offset(newPanX, newPanY))

        _uiState.value = _uiState.value.copy(
            zoom = validatedZoom,
            panOffset = validatedPan
        )
        updateDiagramState()
    }

    fun handlePanChange(offset: Offset) {
        val validatedPan = StateManagementConfig.validatePan(offset)
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

    /**
     * Resets the zoom level to the default zoom value from the workstation.json configuration
     * or to the application default if no configuration is available.
     */
    fun resetZoom() {
        val defaultZoom = _uiState.value.layout?.metadata?.viewport?.defaultZoom ?: ViewportConfig.defaultZoom
        _uiState.value = _uiState.value.copy(zoom = defaultZoom)
        updateDiagramState()
    }

    fun centerViewportOnDevices(viewportWidth: Float = _uiState.value.viewportSize.width, viewportHeight: Float = _uiState.value.viewportSize.height) {
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

    fun handlePortHover(portInfo: String?, isHovered: Boolean) {
        // When a port is hovered, we clear any device hover state to avoid conflicts
        _uiState.value = _uiState.value.copy(
            hoveredPortInfo = if (isHovered) portInfo else null,
            hoveredDeviceId = if (isHovered) null else _uiState.value.hoveredDeviceId
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
                calculateStraightPath(connection, sourceDevice, targetDevice)
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
                val halfWidth = device.size.width / 2f
                val portOffset = port.position.position.coerceIn(-halfWidth, halfWidth)
                Point(
                    deviceRect.left + halfWidth + portOffset,
                    deviceRect.top
                )
            }

            DeviceSide.BOTTOM -> {
                val halfWidth = device.size.width / 2f
                val portOffset = port.position.position.coerceIn(-halfWidth, halfWidth)
                Point(
                    deviceRect.left + halfWidth + portOffset,
                    deviceRect.bottom
                )
            }

            DeviceSide.LEFT -> {
                val halfHeight = device.size.height / 2f
                val portOffset = port.position.position.coerceIn(-halfHeight, halfHeight)
                Point(
                    deviceRect.left,
                    deviceRect.top + halfHeight + portOffset
                )
            }

            DeviceSide.RIGHT -> {
                val halfHeight = device.size.height / 2f
                val portOffset = port.position.position.coerceIn(-halfHeight, halfHeight)
                Point(
                    deviceRect.right,
                    deviceRect.top + halfHeight + portOffset
                )
            }
        }
    }
}