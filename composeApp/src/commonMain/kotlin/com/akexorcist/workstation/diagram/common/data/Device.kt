package com.akexorcist.workstation.diagram.common.data

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
    ) {
        companion object {
            fun builder(block: Type.() -> OfficeLapTop) = block(Type.OfficeLaptop)
        }
    }

    data class PersonalLapTop(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.PersonalLaptop,
        title = "Personal Laptop",
        subtitle = "MacBook Pro",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> PersonalLapTop) = block(Type.PersonalLaptop)
        }
    }

    data class PcDesktop(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.PcDesktop,
        title = "PC Desktop",
        subtitle = null,
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> PcDesktop) = block(Type.PcDesktop)
        }
    }

    data class UsbDockingStation(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.UsbDockingStation,
        title = "USB Docking Station",
        subtitle = "Dell WD19TBS",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> UsbDockingStation) = block(Type.UsbDockingStation)
        }
    }

    data class DigitalCamera(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.DigitalCamera,
        title = "Digital Camera",
        subtitle = "Sony α6000",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> DigitalCamera) = block(Type.DigitalCamera)
        }
    }

    data class HdmiToWebcam(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.HdmiToWebcam,
        title = "HDMI To Webcam",
        subtitle = "Elgato Cam Link 4K",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> HdmiToWebcam) = block(Type.HdmiToWebcam)
        }
    }

    data class StreamDeck(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.StreamDeck,
        title = "Stream Deck",
        subtitle = "Elgato Stream Deck MK.2",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> StreamDeck) = block(Type.StreamDeck)
        }
    }

    data class ExternalSsd(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.ExternalSsd,
        title = "External SSD",
        subtitle = "Seagate Fast SSD 512GB",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> ExternalSsd) = block(Type.ExternalSsd)
        }
    }

    data class UsbCSwitcher(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.UsbCSwitcher,
        title = "USB-C Switcher",
        subtitle = "ATEN US3342",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> UsbCSwitcher) = block(Type.UsbCSwitcher)
        }
    }

    data class UsbHub(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.UsbHub,
        title = "USB Hub",
        subtitle = "ORICO MINI-U32",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> UsbHub) = block(Type.UsbHub)
        }
    }

    data class UsbPowerAdapter(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.UsbPowerAdapter,
        title = "USB Power Adapter",
        subtitle = "Aukey PA-T11",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> UsbPowerAdapter) = block(Type.UsbPowerAdapter)
        }
    }

    data class SecondaryMonitor(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.SecondaryMonitor,
        title = "Secondary Monitor",
        subtitle = "Arzopa A1",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> SecondaryMonitor) = block(Type.SecondaryMonitor)
        }
    }

    data class PrimaryMonitor(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.PrimaryMonitor,
        title = "Primary Monitor",
        subtitle = "Dell U2722D",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> PrimaryMonitor) = block(Type.PrimaryMonitor)
        }
    }

    data class UsbDac(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.UsbDac,
        title = "USB DAC",
        subtitle = "GameDAC Gen2",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> UsbDac) = block(Type.UsbDac)
        }
    }

    data class UsbDongle1(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.UsbDongle1,
        title = "Usb Dongle",
        subtitle = "Logitech G Pro X Superlight",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> UsbDongle1) = block(Type.UsbDongle1)
        }
    }

    data class UsbDongle2(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.UsbDongle2,
        title = "Usb Dongle",
        subtitle = "Logi Bolt",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> UsbDongle2) = block(Type.UsbDongle2)
        }
    }

    data class LedLamp(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.LedLamp,
        title = "LED Lamp",
        subtitle = "Baseus Monitor Handing Lamp",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> LedLamp) = block(Type.LedLamp)
        }
    }

    data class Speaker(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.Speaker,
        title = "Speaker",
        subtitle = "Bose SoundLink Mini II",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> Speaker) = block(Type.Speaker)
        }
    }

    data class Microphone1(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.Microphone1,
        title = "Microphone",
        subtitle = "Audio-Technica ATR2500x-USB",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> Microphone1) = block(Type.Microphone1)
        }
    }

    data class HdmiCapture(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.HdmiCapture,
        title = "HD Capture",
        subtitle = "Elgato HD60 X",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> HdmiCapture) = block(Type.HdmiCapture)
        }
    }

    data class AndroidDevice(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.AndroidDevice,
        title = "Android Device",
        subtitle = "Google Pixel 4",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> AndroidDevice) = block(Type.AndroidDevice)
        }
    }

    data class GameController(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.GameController,
        title = "Game Controller",
        subtitle = "Nintendo Switch Pro Controller",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> GameController) = block(Type.GameController)
        }
    }

    data class Microphone2(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.Microphone2,
        title = "Microphone",
        subtitle = "Shure MV7",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> Microphone2) = block(Type.Microphone2)
        }
    }

    data class Headphone(
        override val leftConnections: List<Connector>,
        override val rightConnections: List<Connector>,
    ) : Device(
        type = Type.Headphone,
        title = "Headphone",
        subtitle = "SteelSeries Arctis Nova Pro",
        leftConnections = leftConnections,
        rightConnections = rightConnections,
    ) {
        companion object {
            fun builder(block: Type.() -> Headphone) = block(Type.Headphone)
        }
    }

    fun hasConnection(device: Type) =
        leftConnections.any { it.target == device } || rightConnections.any { it.target == device }

    fun hasConnection(connector: Connector) =
        leftConnections.any { it.target == connector.owner && it.owner == connector.target } ||
                rightConnections.any { it.target == connector.owner && it.owner == connector.target }

    fun isComputer(): Boolean = when (this.type) {
        Type.OfficeLaptop,
        Type.PersonalLaptop,
        Type.PcDesktop -> true

        else -> false
    }

    fun isHub(): Boolean = when (this.type) {
        Type.UsbDockingStation,
        Type.UsbCSwitcher,
        Type.UsbHub,
        Type.UsbPowerAdapter,
        Type.SecondaryMonitor,
        Type.PrimaryMonitor,
        Type.UsbDac -> true

        else -> false
    }

    fun isAccessory(): Boolean = when (this.type) {
        Type.DigitalCamera,
        Type.HdmiToWebcam,
        Type.StreamDeck,
        Type.ExternalSsd,
        Type.UsbDongle1,
        Type.UsbDongle2,
        Type.LedLamp,
        Type.Speaker,
        Type.Microphone1,
        Type.Microphone2,
        Type.HdmiCapture,
        Type.AndroidDevice,
        Type.GameController,
        Type.Headphone -> true

        else -> false
    }

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
    val owner: Device.Type,
    val target: Device.Type,
) {
    companion object {
        fun spacing(device: Device.Type) = Connector(
            type = ConnectorType.Spacing,
            direction = ConnectorDirection.None,
            owner = device,
            target = device,
        )
    }
}
