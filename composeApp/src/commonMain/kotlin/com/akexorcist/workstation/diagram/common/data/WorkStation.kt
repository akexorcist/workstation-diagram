package com.akexorcist.workstation.diagram.common.data

import androidx.compose.ui.unit.dp

data class WorkStation(
    val officeLaptop: Device.OfficeLapTop,
    val personalLaptop: Device.PersonalLapTop,
    val pcDesktop: Device.PcDesktop,
    val usbDockingStation: Device.UsbDockingStation,
    val digitalCamera: Device.DigitalCamera,
    val hdmiToWebCam: Device.HdmiToWebcam,
    val streamDeck: Device.StreamDeck,
    val externalSsd: Device.ExternalSsd,
    val usbCSwitcher: Device.UsbCSwitcher,
    val usbHub: Device.UsbHub,
    val usbPowerAdapter: Device.UsbPowerAdapter,
    val secondaryMonitor: Device.SecondaryMonitor,
    val primaryMonitor: Device.PrimaryMonitor,
    val usbDac: Device.UsbDac,
    val usbDongle1: Device.UsbDongle1,
    val usbDongle2: Device.UsbDongle2,
    val ledLamp: Device.LedLamp,
    val speaker: Device.Speaker,
    val microphone1: Device.Microphone1,
    val microphone2: Device.Microphone2,
    val hdmiCapture: Device.HdmiCapture,
    val androidDevice: Device.AndroidDevice,
    val gameController: Device.GameController,
    val headphone: Device.Headphone,
)

val MyWorkStation = WorkStation(
    officeLaptop = Device.OfficeLapTop(
        leftConnections = listOf(),
        rightConnections = listOf(
            Connector(
                type = ConnectorType.Thunderbolt3,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbDockingStation,
            ),
        ),
    ),
    personalLaptop = Device.PersonalLapTop(
        leftConnections = listOf(),
        rightConnections = listOf(
            Connector(
                type = ConnectorType.Thunderbolt3,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbDockingStation,
            ),
        ),
    ),
    pcDesktop = Device.PcDesktop(
        leftConnections = listOf(),
        rightConnections = listOf(
            Connector(
                type = ConnectorType.DisplayPort,
                direction = ConnectorDirection.Output,
                target = Device.Type.SecondaryMonitor,
            ),
            Connector(
                type = ConnectorType.DisplayPort,
                direction = ConnectorDirection.Output,
                target = Device.Type.PrimaryMonitor,
            ),
            Connector(
                type = ConnectorType.UsbA3,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbCSwitcher,
            ),
            Connector(
                type = ConnectorType.UsbA3,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbHub,
            ),
        ),
    ),
    usbDockingStation = Device.UsbDockingStation(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.None,
                direction = ConnectorDirection.Output,
                target = Device.Type.OfficeLaptop,
            ),
            Connector(
                type = ConnectorType.None,
                direction = ConnectorDirection.Output,
                target = Device.Type.PersonalLaptop,
            ),
        ),
        rightConnections = listOf(
            Connector(
                type = ConnectorType.UsbA2,
                direction = ConnectorDirection.Input,
                target = Device.Type.StreamDeck,
            ),
            Connector(
                type = ConnectorType.DisplayPort,
                direction = ConnectorDirection.Output,
                target = Device.Type.SecondaryMonitor,
            ),
            Connector(
                type = ConnectorType.DisplayPort,
                direction = ConnectorDirection.Output,
                target = Device.Type.PrimaryMonitor,
            ),
            Connector(
                type = ConnectorType.UsbC32Gen2,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbCSwitcher,
            ),
            Connector(
                type = ConnectorType.UsbA3,
                direction = ConnectorDirection.Input,
                target = Device.Type.ExternalSsd,
            ),
            Connector(
                type = ConnectorType.UsbA3,
                direction = ConnectorDirection.Output,
                target = Device.Type.UsbDac,
            ),
        ),
    ),
    digitalCamera = Device.DigitalCamera(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.MicroHdmi,
                direction = ConnectorDirection.Output,
                target = Device.Type.HdmiToWebcam,
            ),
        ),
        rightConnections = listOf(
            Connector(
                type = ConnectorType.Battery,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbPowerAdapter,
            ),
        ),
    ),
    hdmiToWebCam = Device.HdmiToWebcam(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.Hdmi,
                direction = ConnectorDirection.Input,
                target = Device.Type.DigitalCamera,
            ),
            Connector(
                type = ConnectorType.UsbA3,
                direction = ConnectorDirection.Output,
                target = Device.Type.UsbCSwitcher,
            ),
        ),
        rightConnections = listOf(),
    ),
    streamDeck = Device.StreamDeck(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.UsbC,
                direction = ConnectorDirection.Output,
                target = Device.Type.UsbDockingStation,
            ),
        ),
        rightConnections = listOf(),
    ),
    externalSsd = Device.ExternalSsd(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.UsbC,
                direction = ConnectorDirection.Output,
                target = Device.Type.UsbDockingStation,
            ),
        ),
        rightConnections = listOf(),
    ),
    usbCSwitcher = Device.UsbCSwitcher(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.UsbC32Gen2,
                direction = ConnectorDirection.Output,
                target = Device.Type.UsbDockingStation,
            ),
            Connector(
                type = ConnectorType.UsbC,
                direction = ConnectorDirection.Output,
                target = Device.Type.PcDesktop,
            ),
        ),
        rightConnections = listOf(
            Connector(
                type = ConnectorType.UsbC32Gen2,
                direction = ConnectorDirection.Input,
                target = Device.Type.PrimaryMonitor,
            ),
            Connector(
                type = ConnectorType.UsbA3,
                direction = ConnectorDirection.Input,
                target = Device.Type.HdmiToWebcam,
            ),
            Connector(
                type = ConnectorType.UsbA2,
                direction = ConnectorDirection.Input,
                target = Device.Type.Microphone2,
            ),
        ),
    ),
    usbHub = Device.UsbHub(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.UsbA3,
                direction = ConnectorDirection.Output,
                target = Device.Type.PcDesktop,
            ),
        ),
        rightConnections = listOf(
            Connector(
                type = ConnectorType.UsbA2,
                direction = ConnectorDirection.Output,
                target = Device.Type.UsbDac,
            ),
            Connector(
                type = ConnectorType.UsbA2,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbDongle1,
            ),
            Connector(
                type = ConnectorType.UsbA2,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbDongle2,
            ),
        ),
    ),
    usbPowerAdapter = Device.UsbPowerAdapter(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.UsbA2,
                direction = ConnectorDirection.Output,
                target = Device.Type.DigitalCamera,
            ),
            Connector(
                type = ConnectorType.UsbC,
                direction = ConnectorDirection.Output,
                target = Device.Type.SecondaryMonitor,
            ),
        ),
        rightConnections = listOf(
            Connector(
                type = ConnectorType.UsbA2,
                direction = ConnectorDirection.Output,
                target = Device.Type.LedLamp,
            ),
            Connector(
                type = ConnectorType.UsbA2,
                direction = ConnectorDirection.Output,
                target = Device.Type.Speaker,
            ),
        ),
    ),
    secondaryMonitor = Device.SecondaryMonitor(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.UsbA,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbPowerAdapter,
            ),
            Connector(
                type = ConnectorType.UsbC,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbDockingStation,
            ),
            Connector(
                type = ConnectorType.MiniHdmi,
                direction = ConnectorDirection.Input,
                target = Device.Type.PcDesktop,
            ),
        ),
        rightConnections = listOf(),
    ),
    primaryMonitor = Device.PrimaryMonitor(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.DisplayPort,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbDockingStation,
            ),
            Connector(
                type = ConnectorType.Hdmi,
                direction = ConnectorDirection.Input,
                target = Device.Type.PcDesktop,
            ),
            Connector(
                type = ConnectorType.UsbC32Gen2,
                direction = ConnectorDirection.Output,
                target = Device.Type.UsbCSwitcher,
            ),
            Connector(
                type = ConnectorType.Aux,
                direction = ConnectorDirection.Output,
                target = Device.Type.UsbDac,
            ),
        ),
        rightConnections = listOf(
            Connector(
                type = ConnectorType.UsbA2,
                direction = ConnectorDirection.Input,
                target = Device.Type.Microphone1,
            ),
            Connector(
                type = ConnectorType.UsbA31,
                direction = ConnectorDirection.Input,
                target = Device.Type.HdmiCapture,
            ),
            Connector(
                type = ConnectorType.UsbA31,
                direction = ConnectorDirection.Input,
                target = Device.Type.AndroidDevice,
            ),
            Connector(
                type = ConnectorType.UsbA3,
                direction = ConnectorDirection.Input,
                target = Device.Type.GameController,
            ),
        ),
    ),
    usbDac = Device.UsbDac(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.Aux,
                direction = ConnectorDirection.Input,
                target = Device.Type.PrimaryMonitor,
            ),
            Connector(
                type = ConnectorType.UsbC,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbDockingStation,
            ),
            Connector(
                type = ConnectorType.UsbC,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbHub,
            ),
        ),
        rightConnections = listOf(
            Connector(
                type = ConnectorType.Aux,
                direction = ConnectorDirection.Output,
                target = Device.Type.Speaker,
            ),
            Connector(
                type = ConnectorType.Aux,
                direction = ConnectorDirection.Output,
                target = Device.Type.Headphone,
            ),
        ),
    ),
    usbDongle1 = Device.UsbDongle1(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.None,
                direction = ConnectorDirection.Output,
                target = Device.Type.UsbHub,
            ),
        ),
        rightConnections = listOf(),
    ),
    usbDongle2 = Device.UsbDongle2(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.None,
                direction = ConnectorDirection.Output,
                target = Device.Type.UsbHub,
            ),
        ),
        rightConnections = listOf(),
    ),
    ledLamp = Device.LedLamp(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.MicroUsb,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbPowerAdapter,
            ),
        ),
        rightConnections = listOf(),
    ),
    speaker = Device.Speaker(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.MicroUsb,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbPowerAdapter,
            ),
            Connector(
                type = ConnectorType.Aux,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbDac,
            ),
        ),
        rightConnections = listOf(),
    ),
    microphone1 = Device.Microphone1(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.UsbC,
                direction = ConnectorDirection.Output,
                target = Device.Type.PrimaryMonitor,
            ),
        ),
        rightConnections = listOf(),
    ),
    microphone2 = Device.Microphone2(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.MicroUsb,
                direction = ConnectorDirection.Output,
                target = Device.Type.UsbCSwitcher,
            ),
        ),
        rightConnections = listOf(),
    ),
    hdmiCapture = Device.HdmiCapture(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.UsbC,
                direction = ConnectorDirection.Output,
                target = Device.Type.PrimaryMonitor,
            ),
        ),
        rightConnections = listOf(),
    ),
    androidDevice = Device.AndroidDevice(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.UsbC,
                direction = ConnectorDirection.Output,
                target = Device.Type.PrimaryMonitor,
            ),
        ),
        rightConnections = listOf(),
    ),
    gameController = Device.GameController(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.UsbA3,
                direction = ConnectorDirection.Output,
                target = Device.Type.PrimaryMonitor,
            ),
        ),
        rightConnections = listOf(),
    ),
    headphone = Device.Headphone(
        leftConnections = listOf(
            Connector(
                type = ConnectorType.Aux,
                direction = ConnectorDirection.Input,
                target = Device.Type.UsbDac,
            ),
        ),
        rightConnections = listOf(),
    ),
)
