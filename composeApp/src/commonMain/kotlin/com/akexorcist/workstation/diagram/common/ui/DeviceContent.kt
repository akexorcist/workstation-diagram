@file:Suppress("FunctionName")

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
import com.akexorcist.workstation.diagram.common.data.Workstation
import com.akexorcist.workstation.diagram.common.ui.component.ComputerDeviceComponent
import com.akexorcist.workstation.diagram.common.ui.component.EndDeviceComponent
import com.akexorcist.workstation.diagram.common.ui.component.HubDeviceComponent
import com.akexorcist.workstation.diagram.common.ui.state.WorkstationCoordinateState

private val FirstRowSpacing = 140.dp
private val SecondRowSpacing = 120.dp
private val ThirdRowSpacing = 160.dp
private val ForthRowSpacing = 70.dp

@Composable
internal fun DeviceContent(
    workStation: Workstation,
    state: WorkstationCoordinateState,
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column {
            Spacer(modifier = Modifier.height(397.dp))
            OfficeLaptop(
                device = workStation.officeLaptop,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(21.dp))
            PersonalLaptop(
                device = workStation.personalLaptop,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(173.dp))
            GamingConsole1(
                device = workStation.gamingConsole1,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(21.dp))
            GamingConsole2(
                device = workStation.gamingConsole2,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(71.dp))
            PcDesktop(
                device = workStation.pcDesktop,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
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
            Spacer(modifier = Modifier.height(303.dp))
            UsbDockingStation(
                device = workStation.usbDockingStation,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(139.dp))
            HdmiSwitcher(
                device = workStation.hdmiSwitcher,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
        }
        Spacer(modifier = Modifier.width(SecondRowSpacing))
        Column {
            Spacer(modifier = Modifier.height(57.dp))
            DigitalCamera(
                device = workStation.digitalCamera,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(21.dp))
            HdmiToWebcam(
                device = workStation.hdmiToWebcam,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(159.dp))
            StreamDeck(
                device = workStation.streamDeck,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(21.dp))
            ExternalSsd(
                device = workStation.externalSsd,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(99.dp))
            UsbCSwitcher(
                device = workStation.usbCSwitcher,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(182.dp))
            UsbHub(
                device = workStation.usbHub,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
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
            Spacer(modifier = Modifier.height(48.dp))
            UsbPowerAdapter(
                device = workStation.usbPowerAdapter,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(57.dp))
            SecondaryMonitor(
                device = workStation.secondaryMonitor,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
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
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
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
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(63.dp))
            UsbDongle1(
                device = workStation.usbDongle1,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(21.dp))
            UsbDongle2(
                device = workStation.usbDongle2,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
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
            Spacer(modifier = Modifier.height(47.dp))
            LedLamp(
                device = workStation.ledLamp,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(16.dp))
            WirelessCharger(
                device = workStation.wirelessCharger,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Speaker(
                device = workStation.speaker,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(76.dp))
            Microphone1(
                device = workStation.microphone1,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
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
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
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
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(91.dp))
            Microphone2(
                device = workStation.microphone2,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
                onDeviceCoordinated = { state.update(it) },
                onConnectorCoordinated = { state.update(it) },
                onDeviceClick = onDeviceClick,
                onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
                onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
                onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
                onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
            )
            Spacer(modifier = Modifier.height(83.dp))
            Headphone(
                device = workStation.headphone,
                currentHoveredDevice = currentHoveredDevice,
                currentHoveredConnector = currentHoveredConnector,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
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
private fun GamingConsole1(
    device: Device.GamingConsole1,
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
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
private fun GamingConsole2(
    device: Device.GamingConsole2,
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
        height = 370.dp,
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
private fun HdmiSwitcher(
    device: Device.HdmiSwitcher,
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
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
private fun DigitalCamera(
    device: Device.DigitalCamera,
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
        height = 100.dp,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
        height = 250.dp,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
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
private fun SecondaryMonitor(
    device: Device.SecondaryMonitor,
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
        height = 180.dp,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
        height = 290.dp,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
        height = 180.dp,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
        width = 220.dp,
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
private fun WirelessCharger(
    device: Device.WirelessCharger,
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
        width = 220.dp,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
        width = 220.dp,
        height = 100.dp,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
        width = 220.dp,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
        width = 220.dp,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
        width = 220.dp,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
        width = 220.dp,
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
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
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
        currentHoveredDevice = currentHoveredDevice,
        currentHoveredConnector = currentHoveredConnector,
        width = 220.dp,
        onDeviceCoordinated = onDeviceCoordinated,
        onConnectorCoordinated = onConnectorCoordinated,
        onDeviceClick = { onDeviceClick(device) },
        onEnterHoveDeviceInteraction = { onEnterHoveDeviceInteraction(device) },
        onExitHoverDeviceInteraction = { onExitHoverDeviceInteraction(device) },
        onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
        onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
    )
}
