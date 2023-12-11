package com.akexorcist.workstation.diagram.common.data

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class Device(
    open val type: Type,
    open val title: String,
    open val subtitle: String?,
    open val leftConnections: List<Connector>,
    open val rightConnections: List<Connector>,
) {
    data class OfficeLapTop(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.OfficeLaptop,
        title = "Office Laptop",
        subtitle = "MacBook Pro",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class PersonalLapTop(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.PersonalLaptop,
        title = "Personal Laptop",
        subtitle = "MacBook Pro",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class PcDesktop(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.PcDesktop,
        title = "PC Desktop",
        subtitle = null,
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class UsbDockingStation(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.UsbDockingStation,
        title = "USB Docking Station",
        subtitle = "Dell WD19TBS",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class DigitalCamera(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.DigitalCamera,
        title = "Digital Camera",
        subtitle = "Sony α6000",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class HdmiToWebcam(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.HdmiToWebcam,
        title = "HDMI To Webcam",
        subtitle = "Elgato Cam Link 4K",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class StreamDeck(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.StreamDeck,
        title = "Stream Deck",
        subtitle = "Elgato Stream Deck MK.2",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class ExternalSsd(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.ExternalSsd,
        title = "External SSD",
        subtitle = "Seagate Fast SSD 512GB",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class UsbCSwitcher(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.UsbCSwitcher,
        title = "USB-C Switcher",
        subtitle = "ATEN US3342",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class UsbHub(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.UsbHub,
        title = "USB Hub",
        subtitle = "ORICO MINI-U32",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class UsbPowerAdapter(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.UsbPowerAdapter,
        title = "USB Power Adapter",
        subtitle = "Aukey PA-T11",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class SecondaryMonitor(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.SecondaryMonitor,
        title = "Secondary Monitor",
        subtitle = "Arzopa A1",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class PrimaryMonitor(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.PrimaryMonitor,
        title = "Primary Monitor",
        subtitle = "Dell U2722D",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class UsbDac(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.UsbDac,
        title = "USB DAC",
        subtitle = "GameDAC Gen2",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class UsbDongle1(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.UsbDongle1,
        title = "Usb Dongle",
        subtitle = "Logitech G Pro X Superlight",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class UsbDongle2(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.UsbDongle2,
        title = "Usb Dongle",
        subtitle = "Logi Bolt",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class LedLamp(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.LedLamp,
        title = "LED Lamp",
        subtitle = "Baseus Monitor Handing Lamp",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class Speaker(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.Speaker,
        title = "Speaker",
        subtitle = "Bose SoundLink Mini II",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class Microphone1(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.Microphone1,
        title = "Microphone",
        subtitle = "Audio-Technica ATR2500x-USB",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class HdmiCapture(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.HdmiCapture,
        title = "HD Capture",
        subtitle = "Elgato HD60 X",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class AndroidDevice(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.AndroidDevice,
        title = "Android Device",
        subtitle = "Google Pixel 4",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class GameController(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.GameController,
        title = "Game Controller",
        subtitle = "Nintendo Switch Pro Controller",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class Microphone2(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.Microphone2,
        title = "Microphone",
        subtitle = "Shure MV7",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    data class Headphone(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.Headphone,
        title = "Headphone",
        subtitle = "SteelSeries Arctis Nova Pro",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    )

    enum class Type {
        OfficeLaptop,
        PersonalLaptop,
        PcDesktop,
        UsbDockingStation,
        DigitalCamera,
        HdmiToWebcam,
        StreamDeck,
        ExternalSsd,
        UsbCSwitcher,
        UsbHub,
        UsbPowerAdapter,
        SecondaryMonitor,
        PrimaryMonitor,
        UsbDac,
        UsbDongle1,
        UsbDongle2,
        LedLamp,
        Speaker,
        Microphone1,
        Microphone2,
        HdmiCapture,
        AndroidDevice,
        GameController,
        Headphone,
    }
}

data class Connector(
    val type: ConnectorType,
    val direction: ConnectorDirection,
    val target: Device.Type,
)
