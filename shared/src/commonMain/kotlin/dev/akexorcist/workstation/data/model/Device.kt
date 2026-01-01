package dev.akexorcist.workstation.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: String,
    val description: String,
    val title: String,
    val subtitle: String? = null,
    val type: String,
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
    PERIPHERAL,
    CENTRAL_DEVICE
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
data class DeviceSpecifications(
    val manufacturer: String? = null,
    val modelNumber: String? = null,
    val technicalSpecs: Map<String, String> = emptyMap(),
    val url: String? = null
)

@Serializable
data class InformationItem(
    val key: String,
    val value: String
)

@Serializable
data class DeviceSpecification(
    val type: String,
    val title: String,
    val subtitle: String? = null,
    val website: String? = null,
    val image: String,
    val description: String,
    val information: List<InformationItem> = emptyList()
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