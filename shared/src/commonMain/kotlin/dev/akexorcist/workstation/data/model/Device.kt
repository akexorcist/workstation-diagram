package dev.akexorcist.workstation.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: String,
    val name: String,
    val model: String,
    val type: String,
    val category: DeviceCategory,
    val position: Position,
    val size: Size,
    val ports: List<Port>,
    val specifications: DeviceSpecifications
)

@Serializable
enum class DeviceType {
    LAPTOP,
    MONITOR,
    DOCKING_STATION,
    USB_HUB,
    GAMING_CONSOLE,
    CAMERA,
    AUDIO_DEVICE,
    STORAGE
}

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
    val type: PortType,
    val direction: PortDirection,
    val position: PortPosition
)

@Serializable
enum class PortType {
    USB_C,
    USB_A_2_0,
    USB_A_3_0,
    USB_A_3_1,
    USB_A_3_2,
    HDMI,
    HDMI_2_1,
    DISPLAY_PORT,
    MINI_HDMI,
    MICRO_HDMI,
    ETHERNET,
    AUX,
    POWER
}

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