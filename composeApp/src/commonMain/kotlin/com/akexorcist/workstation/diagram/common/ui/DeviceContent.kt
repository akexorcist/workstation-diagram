package com.akexorcist.workstation.diagram.common.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akexorcist.workstation.diagram.common.data.Connector
import com.akexorcist.workstation.diagram.common.data.Device
import com.akexorcist.workstation.diagram.common.data.DeviceCoordinate
import com.akexorcist.workstation.diagram.common.data.WorkStation
import com.akexorcist.workstation.diagram.common.ui.component.ComputerDeviceComponent
import com.akexorcist.workstation.diagram.common.ui.component.EndDeviceComponent
import com.akexorcist.workstation.diagram.common.ui.component.HubDeviceComponent
import com.akexorcist.workstation.diagram.common.ui.state.WorkstationCoordinateState

private val FirstRowSpacing = 140.dp
private val SecondRowSpacing = 180.dp
private val ThirdRowSpacing = 180.dp
private val ForthRowSpacing = 160.dp

@Composable
internal fun DeviceContent(
    workStation: WorkStation,
    state: WorkstationCoordinateState,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column {
            Spacer(modifier = Modifier.height(302.5.dp))
            OfficeLaptop(
                device = workStation.officeLaptop,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(32.dp))
            PersonalLaptop(
                device = workStation.personalLaptop,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(80.dp))
            PcDesktop(
                device = workStation.pcDesktop,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
        }
        Spacer(modifier = Modifier.width(FirstRowSpacing))
        Column {
            Spacer(modifier = Modifier.height(257.5.dp))
            UsbDockingStation(
                device = workStation.usbDockingStation,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.height(160.dp))
        }
        Spacer(modifier = Modifier.width(SecondRowSpacing))
        Column {
            Spacer(modifier = Modifier.height(64.dp))
            DigitalCamera(
                device = workStation.digitalCamera,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(32.dp))
            HdmiToWebcam(
                device = workStation.hdmiToWebCam,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(32.dp))
            StreamDeck(
                device = workStation.streamDeck,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(130.dp))
            ExternalSsd(
                device = workStation.externalSsd,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(180.dp))
            UsbCSwitcher(
                device = workStation.usbCSwitcher,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(80.dp))
            UsbHub(
                device = workStation.usbHub,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
        }
        Spacer(modifier = Modifier.width(ThirdRowSpacing))
        Column {
            Spacer(modifier = Modifier.height(65.5.dp))
            UsbPowerAdapter(
                device = workStation.usbPowerAdapter,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(16.dp))
            SecondaryMonitor(
                device = workStation.secondaryMonitor,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryMonitor(
                device = workStation.primaryMonitor,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(120.dp))
            UsbDac(
                device = workStation.usbDac,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(23.5.dp))
            UsbDongle1(
                device = workStation.usbDongle1,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(8.dp))
            UsbDongle2(
                device = workStation.usbDongle2,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
        }
        Spacer(modifier = Modifier.width(ForthRowSpacing))
        Column {
            Spacer(modifier = Modifier.height(64.dp))
            LedLamp(
                device = workStation.ledLamp,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(80.dp))
            Speaker(
                device = workStation.speaker,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Microphone1(
                device = workStation.microphone1,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(16.dp))
            HdmiCapture(
                device = workStation.hdmiCapture,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(16.dp))
            AndroidDevice(
                device = workStation.androidDevice,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(16.dp))
            GameController(
                device = workStation.gameController,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Microphone2(
                device = workStation.microphone2,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(35.dp))
            Headphone(
                device = workStation.headphone,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
        }
    }
}

@Composable
private fun OfficeLaptop(
    device: Device.OfficeLapTop,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    ComputerDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun PersonalLaptop(
    device: Device.PersonalLapTop,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    ComputerDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun PcDesktop(
    device: Device.PcDesktop,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    ComputerDeviceComponent(
        device = device,
        height = 200.dp,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun UsbDockingStation(
    device: Device.UsbDockingStation,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    HubDeviceComponent(
        device = device,
        height = 280.dp,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun DigitalCamera(
    device: Device.DigitalCamera,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    EndDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun HdmiToWebcam(
    device: Device.HdmiToWebcam,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    EndDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun StreamDeck(
    device: Device.StreamDeck,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    EndDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun ExternalSsd(
    device: Device.ExternalSsd,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    EndDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun UsbCSwitcher(
    device: Device.UsbCSwitcher,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    HubDeviceComponent(
        device = device,
        height = 160.dp,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun UsbHub(
    device: Device.UsbHub,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    HubDeviceComponent(
        device = device,
        height = 160.dp,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun UsbPowerAdapter(
    device: Device.UsbPowerAdapter,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    HubDeviceComponent(
        device = device,
        height = 120.dp,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun SecondaryMonitor(
    device: Device.SecondaryMonitor,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    HubDeviceComponent(
        device = device,
        height = 150.dp,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun PrimaryMonitor(
    device: Device.PrimaryMonitor,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    HubDeviceComponent(
        device = device,
        height = 320.dp,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun UsbDac(
    device: Device.UsbDac,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    HubDeviceComponent(
        device = device,
        height = 160.dp,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun UsbDongle1(
    device: Device.UsbDongle1,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    EndDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun UsbDongle2(
    device: Device.UsbDongle2,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    EndDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun LedLamp(
    device: Device.LedLamp,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    EndDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun Speaker(
    device: Device.Speaker,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    EndDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun Microphone1(
    device: Device.Microphone1,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    EndDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun Microphone2(
    device: Device.Microphone2,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    EndDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun HdmiCapture(
    device: Device.HdmiCapture,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    EndDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun AndroidDevice(
    device: Device.AndroidDevice,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    EndDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun GameController(
    device: Device.GameController,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    EndDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

@Composable
private fun Headphone(
    device: Device.Headphone,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    EndDeviceComponent(
        device = device,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}

//@Preview(heightDp = 1000, widthDp = 2000, showBackground = true)
//@Composable
//fun DeviceContentPreview() {
//    val state = rememberWorkstationCoordinateHostState()
//    MaterialTheme {
//        DeviceContent(
//            workStation = MyWorkStation,
//            state = state,
//        )
//    }
//}
