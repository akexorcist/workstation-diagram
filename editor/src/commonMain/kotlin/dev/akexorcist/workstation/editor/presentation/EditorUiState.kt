package dev.akexorcist.workstation.editor.presentation

import dev.akexorcist.workstation.data.model.*
import dev.akexorcist.workstation.routing.RoutedConnection

data class EditorUiState(
    val layout: WorkstationLayout? = null,
    val selectedConnectionId: String? = null,
    val hoveredConnectionId: String? = null,
    val hoveredDeviceId: String? = null,
    val hoveredPortInfo: String? = null,
    val zoom: Float = 1.0f,
    val panOffset: Offset = Offset.Zero,
    val viewportSize: Size = Size(1920f, 1080f),
    val isDarkTheme: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val connectionAnimationEnabled: Boolean = true,
    val routedConnections: List<RoutedConnection> = emptyList(),
    val routedConnectionMap: Map<String, RoutedConnection> = emptyMap(),
    val selectedRoutingPoint: Pair<String, Int>? = null,
    val draggingRoutingPoint: Pair<String, Int>? = null,
    val hoveredRoutingPoint: Pair<String, Int>? = null,
    val hoveredLineSegment: Pair<String, Int>? = null,
    val draggingLineSegment: Pair<String, Int>? = null
)

