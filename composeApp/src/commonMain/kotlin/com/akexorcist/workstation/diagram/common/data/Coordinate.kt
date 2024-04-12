package com.akexorcist.workstation.diagram.common.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize

data class WorkstationCoordinates(
    val workspace: WorkspaceCoordinate? = null,
    val officeLaptop: DeviceCoordinate = DeviceCoordinate(),
    val personalLaptop: DeviceCoordinate = DeviceCoordinate(),
    val pcDesktop: DeviceCoordinate = DeviceCoordinate(),
    val nintendoSwitch: DeviceCoordinate = DeviceCoordinate(),
    val playStation5: DeviceCoordinate = DeviceCoordinate(),
    val usbDockingStation: DeviceCoordinate = DeviceCoordinate(),
    val hdmiSwitcher: DeviceCoordinate = DeviceCoordinate(),
    val digitalCamera: DeviceCoordinate = DeviceCoordinate(),
    val hdmiToWebcam: DeviceCoordinate = DeviceCoordinate(),
    val streamDeck: DeviceCoordinate = DeviceCoordinate(),
    val externalSsd: DeviceCoordinate = DeviceCoordinate(),
    val usbCSwitcher: DeviceCoordinate = DeviceCoordinate(),
    val usbHub: DeviceCoordinate = DeviceCoordinate(),
    val usbPowerAdapter: DeviceCoordinate = DeviceCoordinate(),
    val secondaryMonitor: DeviceCoordinate = DeviceCoordinate(),
    val primaryMonitor: DeviceCoordinate = DeviceCoordinate(),
    val usbDac: DeviceCoordinate = DeviceCoordinate(),
    val usbDongle1: DeviceCoordinate = DeviceCoordinate(),
    val usbDongle2: DeviceCoordinate = DeviceCoordinate(),
    val ledLamp: DeviceCoordinate = DeviceCoordinate(),
    val speaker: DeviceCoordinate = DeviceCoordinate(),
    val microphone1: DeviceCoordinate = DeviceCoordinate(),
    val microphone2: DeviceCoordinate = DeviceCoordinate(),
    val hdmiCapture: DeviceCoordinate = DeviceCoordinate(),
    val androidDevice: DeviceCoordinate = DeviceCoordinate(),
    val gameController: DeviceCoordinate = DeviceCoordinate(),
    val headphone: DeviceCoordinate = DeviceCoordinate(),
) {
    fun areAvailable() = workspace != null &&
            officeLaptop.isAvailable() &&
            personalLaptop.isAvailable() &&
            pcDesktop.isAvailable() &&
            nintendoSwitch.isAvailable() &&
            playStation5.isAvailable() &&
            usbDockingStation.isAvailable() &&
            hdmiSwitcher.isAvailable() &&
            digitalCamera.isAvailable() &&
            hdmiToWebcam.isAvailable() &&
            streamDeck.isAvailable() &&
            externalSsd.isAvailable() &&
            usbCSwitcher.isAvailable() &&
            usbHub.isAvailable() &&
            usbPowerAdapter.isAvailable() &&
            secondaryMonitor.isAvailable() &&
            primaryMonitor.isAvailable() &&
            usbDac.isAvailable() &&
            usbDongle1.isAvailable() &&
            usbDongle2.isAvailable() &&
            ledLamp.isAvailable() &&
            speaker.isAvailable() &&
            microphone1.isAvailable() &&
            microphone2.isAvailable() &&
            hdmiCapture.isAvailable() &&
            androidDevice.isAvailable() &&
            gameController.isAvailable() &&
            headphone.isAvailable()
}


data class WorkspaceCoordinate(
    val offset: Offset,
    val size: IntSize,
    val adjustment: Offset,
)

data class DeviceCoordinate(
    val device: Device? = null,
    val connectors: List<Connector>? = null,
) {

    data class Device(
        val device: com.akexorcist.workstation.diagram.common.data.Device.Type,
        val offset: Offset,
        val size: IntSize,
    )

    data class Connector(
        val device: com.akexorcist.workstation.diagram.common.data.Device.Type,
        val connector: com.akexorcist.workstation.diagram.common.data.Connector,
        val offset: Offset,
        val size: IntSize,
        val side: ConnectorSide,
    ) {
        val rect = Rect(
            offset = offset,
            size = size.toSize(),
        )
    }
}

data class ConnectionLine(
    val source: Connection,
    val target: Connection?,
    val offset: Offset,
    val size: IntSize,
    val side: ConnectorSide,
) {
    val rect = Rect(
        offset = offset,
        size = size.toSize(),
    )

    data class Connection(
        val type: ConnectorType,
        val direction: ConnectorDirection,
        val owner: Device.Type,
    )
}

fun Connector.toConnection() = ConnectionLine.Connection(
    type = type,
    direction = direction,
    owner = owner,
)

fun DeviceCoordinate?.isAvailable() = this?.device != null && this.connectors != null

fun DeviceCoordinate.Connector.getJoint(): Offset {
    return when (this.side) {
        ConnectorSide.Left ->
            Offset(this.offset.x, this.offset.y + this.size.height / 2)

        ConnectorSide.Right ->
            Offset(this.offset.x + this.size.width, this.offset.y + this.size.height / 2)
    }
}

fun ConnectionLine.getJoint(): Offset {
    return when (this.side) {
        ConnectorSide.Left ->
            Offset(this.offset.x, this.offset.y + this.size.height / 2)

        ConnectorSide.Right ->
            Offset(this.offset.x + this.size.width, this.offset.y + this.size.height / 2)
    }
}
