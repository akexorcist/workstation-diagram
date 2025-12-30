package dev.akexorcist.workstation.presentation

import dev.akexorcist.workstation.data.model.*

data class WorkstationUiState(
    val layout: WorkstationLayout? = null,
    val selectedDeviceId: String? = null,
    val selectedConnectionId: String? = null,
    val zoom: Float = 1.0f,
    val panOffset: Offset = Offset.Zero,
    val isDarkTheme: Boolean = true,
    val searchQuery: String = "",
    val filteredDeviceIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class DiagramState(
    val viewportBounds: Rect = Rect(0f, 0f, 1920f, 1080f),
    val scale: Float = 1.0f,
    val deviceRenderData: List<DeviceRenderData> = emptyList(),
    val connectionRenderData: List<ConnectionRenderData> = emptyList()
)

data class DeviceRenderData(
    val device: Device,
    val screenPosition: Offset,
    val screenSize: Size,
    val isVisible: Boolean,
    val isSelected: Boolean,
    val isHovered: Boolean
)

data class ConnectionRenderData(
    val connection: Connection,
    val path: List<Point>,
    val isVisible: Boolean,
    val isSelected: Boolean,
    val isHovered: Boolean
)

data class Rect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    fun intersects(other: Rect): Boolean {
        return left < other.right && right > other.left && top < other.bottom && bottom > other.top
    }
}