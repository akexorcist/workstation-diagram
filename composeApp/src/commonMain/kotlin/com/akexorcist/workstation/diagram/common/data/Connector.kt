package com.akexorcist.workstation.diagram.common.data

enum class ConnectorSide {
    Left, Right,
}

enum class ConnectorType(val value: String) {
    None(""),
    Spacing(""),
    Thunderbolt3("Thunderbolt 3"),
    DisplayPort("Display Port"),
    UsbA2("USB-A 2.0"),
    UsbA3("USB-A 3.0"),
    UsbA31("USB-A 3.1"),
    UsbA("USB-A"),
    UsbC("USB-C"),
    UsbC32Gen2("USB-C 3.2 Gen 2"),
    MicroUsb("Micro USB"),
    MiniHdmi("Mini HDMI"),
    MicroHdmi("Micro HDMI"),
    Hdmi("HDMI"),
    Aux("AUX"),
    Battery("Battery"),
}

enum class ConnectorDirection {
    Input, Output, None,
}
