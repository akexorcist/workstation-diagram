package com.akexorcist.workstation.diagram.common.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.akexorcist.workstation.diagram.common.data.Device
import com.akexorcist.workstation.diagram.common.data.DeviceCoordinate
import com.akexorcist.workstation.diagram.common.data.WorkspaceCoordinate
import com.akexorcist.workstation.diagram.common.data.WorkstationCoordinates

@Stable
class WorkstationCoordinateState {
    var currentWorkstationCoordinates by mutableStateOf(WorkstationCoordinates())
        private set

    fun update(coordinate: WorkspaceCoordinate) {
        currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
            workspace = coordinate
        )
    }

    fun update(coordinate: DeviceCoordinate.Device) {
        val updatedCoordinate = coordinate.copy(
            offset = Offset(
                x = coordinate.offset.x + (currentWorkstationCoordinates.workspace?.adjustment?.x ?: 0f),
                y = coordinate.offset.y + (currentWorkstationCoordinates.workspace?.adjustment?.y ?: 0f),
            )
        )
        when (updatedCoordinate.device) {
            Device.Type.OfficeLaptop -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    officeLaptop = currentWorkstationCoordinates.officeLaptop.copy(
                        device = updatedCoordinate,
                    )
                )
            }

            Device.Type.PersonalLaptop -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    personalLaptop = currentWorkstationCoordinates.personalLaptop.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.PcDesktop -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    pcDesktop = currentWorkstationCoordinates.pcDesktop.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.UsbDockingStation -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    usbDockingStation = currentWorkstationCoordinates.usbDockingStation.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.DigitalCamera -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    digitalCamera = currentWorkstationCoordinates.digitalCamera.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.HdmiToWebcam -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    hdmiToWebcam = currentWorkstationCoordinates.hdmiToWebcam.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.StreamDeck -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    streamDeck = currentWorkstationCoordinates.streamDeck.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.ExternalSsd -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    externalSsd = currentWorkstationCoordinates.externalSsd.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.UsbCSwitcher -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    usbCSwitcher = currentWorkstationCoordinates.usbCSwitcher.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.UsbHub -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    usbHub = currentWorkstationCoordinates.usbHub.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.UsbPowerAdapter -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    usbPowerAdapter = currentWorkstationCoordinates.usbPowerAdapter.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.SecondaryMonitor -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    secondaryMonitor = currentWorkstationCoordinates.secondaryMonitor.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.PrimaryMonitor -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    primaryMonitor = currentWorkstationCoordinates.primaryMonitor.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.UsbDac -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    usbDac = currentWorkstationCoordinates.usbDac.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.UsbDongle1 -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    usbDongle1 = currentWorkstationCoordinates.usbDongle1.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.UsbDongle2 -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    usbDongle2 = currentWorkstationCoordinates.usbDongle2.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.LedLamp -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    ledLamp = currentWorkstationCoordinates.ledLamp.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.Speaker -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    speaker = currentWorkstationCoordinates.speaker.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.Microphone1 -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    microphone1 = currentWorkstationCoordinates.microphone1.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.Microphone2 -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    microphone2 = currentWorkstationCoordinates.microphone2.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.HdmiCapture -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    hdmiCapture = currentWorkstationCoordinates.hdmiCapture.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.AndroidDevice -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    androidDevice = currentWorkstationCoordinates.androidDevice.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.GameController -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    gameController = currentWorkstationCoordinates.gameController.copy(
                        device = updatedCoordinate
                    )
                )
            }

            Device.Type.Headphone -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    headphone = currentWorkstationCoordinates.headphone.copy(
                        device = updatedCoordinate
                    )
                )
            }
        }
    }

    fun update(coordinate: DeviceCoordinate.Connector) {
        val updatedCoordinate = coordinate.copy(
            offset = Offset(
                x = coordinate.offset.x + (currentWorkstationCoordinates.workspace?.adjustment?.x ?: 0f),
                y = coordinate.offset.y + (currentWorkstationCoordinates.workspace?.adjustment?.y ?: 0f),
            )
        )
        when (updatedCoordinate.device) {
            Device.Type.OfficeLaptop -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    officeLaptop = currentWorkstationCoordinates.officeLaptop.copy(
                        connectors = currentWorkstationCoordinates.officeLaptop.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.PersonalLaptop -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    personalLaptop = currentWorkstationCoordinates.personalLaptop.copy(
                        connectors = currentWorkstationCoordinates.personalLaptop.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.PcDesktop -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    pcDesktop = currentWorkstationCoordinates.pcDesktop.copy(
                        connectors = currentWorkstationCoordinates.pcDesktop.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.UsbDockingStation -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    usbDockingStation = currentWorkstationCoordinates.usbDockingStation.copy(
                        connectors = currentWorkstationCoordinates.usbDockingStation.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.DigitalCamera -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    digitalCamera = currentWorkstationCoordinates.digitalCamera.copy(
                        connectors = currentWorkstationCoordinates.digitalCamera.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.HdmiToWebcam -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    hdmiToWebcam = currentWorkstationCoordinates.hdmiToWebcam.copy(
                        connectors = currentWorkstationCoordinates.hdmiToWebcam.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.StreamDeck -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    streamDeck = currentWorkstationCoordinates.streamDeck.copy(
                        connectors = currentWorkstationCoordinates.streamDeck.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.ExternalSsd -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    externalSsd = currentWorkstationCoordinates.externalSsd.copy(
                        connectors = currentWorkstationCoordinates.externalSsd.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.UsbCSwitcher -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    usbCSwitcher = currentWorkstationCoordinates.usbCSwitcher.copy(
                        connectors = currentWorkstationCoordinates.usbCSwitcher.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.UsbHub -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    usbHub = currentWorkstationCoordinates.usbHub.copy(
                        connectors = currentWorkstationCoordinates.usbHub.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.UsbPowerAdapter -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    usbPowerAdapter = currentWorkstationCoordinates.usbPowerAdapter.copy(
                        connectors = currentWorkstationCoordinates.usbPowerAdapter.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.SecondaryMonitor -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    secondaryMonitor = currentWorkstationCoordinates.secondaryMonitor.copy(
                        connectors = currentWorkstationCoordinates.secondaryMonitor.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.PrimaryMonitor -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    primaryMonitor = currentWorkstationCoordinates.primaryMonitor.copy(
                        connectors = currentWorkstationCoordinates.primaryMonitor.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.UsbDac -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    usbDac = currentWorkstationCoordinates.usbDac.copy(
                        connectors = currentWorkstationCoordinates.usbDac.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.UsbDongle1 -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    usbDongle1 = currentWorkstationCoordinates.usbDongle1.copy(
                        connectors = currentWorkstationCoordinates.usbDongle1.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.UsbDongle2 -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    usbDongle2 = currentWorkstationCoordinates.usbDongle2.copy(
                        connectors = currentWorkstationCoordinates.usbDongle2.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.LedLamp -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    ledLamp = currentWorkstationCoordinates.ledLamp.copy(
                        connectors = currentWorkstationCoordinates.ledLamp.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.Speaker -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    speaker = currentWorkstationCoordinates.speaker.copy(
                        connectors = currentWorkstationCoordinates.speaker.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.Microphone1 -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    microphone1 = currentWorkstationCoordinates.microphone1.copy(
                        connectors = currentWorkstationCoordinates.microphone1.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.Microphone2 -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    microphone2 = currentWorkstationCoordinates.microphone2.copy(
                        connectors = currentWorkstationCoordinates.microphone2.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.HdmiCapture -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    hdmiCapture = currentWorkstationCoordinates.hdmiCapture.copy(
                        connectors = currentWorkstationCoordinates.hdmiCapture.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.AndroidDevice -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    androidDevice = currentWorkstationCoordinates.androidDevice.copy(
                        connectors = currentWorkstationCoordinates.androidDevice.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.GameController -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    gameController = currentWorkstationCoordinates.gameController.copy(
                        connectors = currentWorkstationCoordinates.gameController.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }

            Device.Type.Headphone -> {
                currentWorkstationCoordinates = currentWorkstationCoordinates.copy(
                    headphone = currentWorkstationCoordinates.headphone.copy(
                        connectors = currentWorkstationCoordinates.headphone.connectors
                            ?.toMutableList()
                            ?.apply {
                                if (!contains(updatedCoordinate)) {
                                    add(updatedCoordinate)
                                }
                            }
                            ?: listOf(updatedCoordinate)
                    )
                )
            }
        }
    }
}

@Composable
fun rememberWorkstationCoordinateState() = remember { WorkstationCoordinateState() }
