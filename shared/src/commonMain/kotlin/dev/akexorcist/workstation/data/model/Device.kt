package dev.akexorcist.workstation.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: String,
    val description: String,
    val title: String,
    val label: String,
    val category: DeviceCategory,
    val position: Position,
    val size: Size,
    val ports: List<Port>,
    val specifications: List<InformationItem> = emptyList(),
    val url: String? = null
)

@Serializable
enum class DeviceCategory {
    HUB,
    DEVICE,
    HOST
}

@Serializable
data class Port(
    val id: String,
    val name: String,
    val direction: PortDirection,
    val position: PortPosition
)

@Serializable
enum class PortDirection {
    INPUT,
    OUTPUT,
    BIDIRECTIONAL
}

@Serializable
data class PortPosition(
    val side: DeviceSide,
    val position: Float
)

@Serializable
enum class DeviceSide {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
}

@Serializable
data class InformationItem(
    val key: String,
    val value: String
)

@Serializable
data class Position(
    val x: Float,
    val y: Float
)

@Serializable
data class Size(
    val width: Float,
    val height: Float
)

@Serializable
data class Offset(
    val x: Float,
    val y: Float
) {
    companion object {
        val Zero = Offset(0f, 0f)
    }
}