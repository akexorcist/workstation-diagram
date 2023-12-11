package com.akexorcist.workstation.diagram.common.utility

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.toSize
import com.akexorcist.workstation.diagram.common.data.ConnectorSide
import com.akexorcist.workstation.diagram.common.data.Device
import com.akexorcist.workstation.diagram.common.data.DeviceCoordinate
import com.akexorcist.workstation.diagram.common.data.WorkstationCoordinates

fun WorkstationCoordinates.getSortedDeviceConnectorsByLeft(): List<Pair<Rect, Device.Type>> = listOfNotNull(
    this.officeLaptop.device,
    this.personalLaptop.device,
    this.pcDesktop.device,
    this.usbDockingStation.device,
    this.digitalCamera.device,
    this.hdmiToWebcam.device,
    this.streamDeck.device,
    this.externalSsd.device,
    this.usbCSwitcher.device,
    this.usbHub.device,
    this.usbPowerAdapter.device,
    this.secondaryMonitor.device,
    this.primaryMonitor.device,
    this.usbDac.device,
    this.usbDongle1.device,
    this.usbDongle2.device,
    this.ledLamp.device,
    this.speaker.device,
    this.microphone1.device,
    this.microphone2.device,
    this.hdmiCapture.device,
    this.androidDevice.device,
    this.gameController.device,
    this.headphone.device,
)
    .sortedBy { it.offset.x }
    .map {
        Rect(
            offset = it.offset,
            size = it.size.toSize(),
        ) to it.device
    }

fun WorkstationCoordinates.getSortedConnectorByBottom(): List<DeviceCoordinate.Connector> {
    return this.getAllConnectorsByBottom()
        .filter { connector -> connector.side == ConnectorSide.Left }
        .sortedByDescending { it.offset.y }
}

private fun WorkstationCoordinates.getAllConnectorsByBottom(): List<DeviceCoordinate.Connector> {
    return listOf(
        this.officeLaptop.connectors,
        this.personalLaptop.connectors,
        this.pcDesktop.connectors,
        this.usbDockingStation.connectors,
        this.digitalCamera.connectors,
        this.hdmiToWebcam.connectors,
        this.streamDeck.connectors,
        this.externalSsd.connectors,
        this.usbCSwitcher.connectors,
        this.usbHub.connectors,
        this.usbPowerAdapter.connectors,
        this.secondaryMonitor.connectors,
        this.primaryMonitor.connectors,
        this.usbDac.connectors,
        this.usbDongle1.connectors,
        this.usbDongle2.connectors,
        this.ledLamp.connectors,
        this.speaker.connectors,
        this.microphone1.connectors,
        this.microphone2.connectors,
        this.hdmiCapture.connectors,
        this.androidDevice.connectors,
        this.gameController.connectors,
        this.headphone.connectors,
    )
        .flatMap { it ?: listOf() }
}

fun List<Pair<Rect, Device.Type>>.mapToMinimumBound(
    horizontalBoundDistance: Float,
    verticalBoundDistance: Float,
) = this.map {
    it.first.copy(
        left = it.first.left - horizontalBoundDistance,
        top = it.first.top - verticalBoundDistance,
        right = it.first.right + horizontalBoundDistance,
        bottom = it.first.bottom + verticalBoundDistance,
    ) to it.second
}

fun getTargetConnector(
    coordinates: WorkstationCoordinates,
    connector: DeviceCoordinate.Connector,
): DeviceCoordinate.Connector? {
    return when (connector.sourceConnector.target) {
        Device.Type.OfficeLaptop -> coordinates.officeLaptop.connectors
        Device.Type.PersonalLaptop -> coordinates.personalLaptop.connectors
        Device.Type.PcDesktop -> coordinates.pcDesktop.connectors
        Device.Type.UsbDockingStation -> coordinates.usbDockingStation.connectors
        Device.Type.DigitalCamera -> coordinates.digitalCamera.connectors
        Device.Type.HdmiToWebcam -> coordinates.hdmiToWebcam.connectors
        Device.Type.StreamDeck -> coordinates.streamDeck.connectors
        Device.Type.ExternalSsd -> coordinates.externalSsd.connectors
        Device.Type.UsbCSwitcher -> coordinates.usbCSwitcher.connectors
        Device.Type.UsbHub -> coordinates.usbHub.connectors
        Device.Type.UsbPowerAdapter -> coordinates.usbPowerAdapter.connectors
        Device.Type.SecondaryMonitor -> coordinates.secondaryMonitor.connectors
        Device.Type.PrimaryMonitor -> coordinates.primaryMonitor.connectors
        Device.Type.UsbDac -> coordinates.usbDac.connectors
        Device.Type.UsbDongle1 -> coordinates.usbDongle1.connectors
        Device.Type.UsbDongle2 -> coordinates.usbDongle2.connectors
        Device.Type.LedLamp -> coordinates.ledLamp.connectors
        Device.Type.Speaker -> coordinates.speaker.connectors
        Device.Type.Microphone1 -> coordinates.microphone1.connectors
        Device.Type.Microphone2 -> coordinates.microphone2.connectors
        Device.Type.HdmiCapture -> coordinates.hdmiCapture.connectors
        Device.Type.AndroidDevice -> coordinates.androidDevice.connectors
        Device.Type.GameController -> coordinates.gameController.connectors
        Device.Type.Headphone -> coordinates.headphone.connectors
    }?.find { it.sourceConnector.target == connector.device }
}
