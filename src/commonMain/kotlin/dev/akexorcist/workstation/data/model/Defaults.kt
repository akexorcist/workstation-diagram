package dev.akexorcist.workstation.data.model

import dev.akexorcist.workstation.utils.getCurrentDate

object DataDefaults {
    val defaultDeviceSize = Size(200f, 150f)
    val defaultDevicePosition = Position(0f, 0f)
    val defaultDeviceCategory = DeviceCategory.PERIPHERAL

    val defaultPortPosition = PortPosition(DeviceSide.LEFT, 0.5f)
    val defaultPortDirection = PortDirection.BIDIRECTIONAL
    val defaultPortType = PortType.USB_C

    val defaultConnectionCategory = ConnectionCategory.DATA
    fun defaultConnectionType() = ConnectionType("Unknown", defaultConnectionCategory)

    val defaultCanvasSize = Size(1920f, 1080f)
    val defaultTheme = ThemeConfig(isDark = true)
    const val defaultVersion = "1.0"
    const val defaultTitle = "Workstation Diagram"

    fun defaultDate(): String {
        return getCurrentDate()
    }
}