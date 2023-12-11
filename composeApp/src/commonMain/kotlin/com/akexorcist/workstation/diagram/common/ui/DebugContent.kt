package com.akexorcist.workstation.diagram.common.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.akexorcist.workstation.diagram.common.data.DeviceCoordinate
import com.akexorcist.workstation.diagram.common.data.WorkstationCoordinates
import com.akexorcist.workstation.diagram.common.ui.state.Config
import com.akexorcist.workstation.diagram.common.utility.px
import kotlin.math.round


@Composable
fun DebugPanel(
    config: Config,
    onNextIndex: (Int) -> Unit,
    onPreviousIndex: (Int) -> Unit,
    onToggleShowWorkspaceArea: (Boolean) -> Unit,
    onToggleShowDeviceArea: (Boolean) -> Unit,
    onToggleShowOverlapBoundArea: (Boolean) -> Unit,
    onToggleShowConnectorArea: (Boolean) -> Unit,
    onToggleShowConnectionLine: (Boolean) -> Unit,
    onToggleLineConnectionPoint: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(32.dp)
            .background(
                color = Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
            )
            .border(
                width = 1.dp,
                color = Color.Black.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { onPreviousIndex(config.lineIndex) }) {
                Text(text = "<")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Line ${config.lineIndex}")
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onNextIndex(config.lineIndex) }) {
                Text(text = ">")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ToggleDebugMenu(
            label = "Show workspace area",
            isChecked = config.showWorkspaceArea,
            onCheckedChange = onToggleShowWorkspaceArea,
        )
        ToggleDebugMenu(
            label = "Show device area",
            isChecked = config.showDeviceArea,
            onCheckedChange = onToggleShowDeviceArea,
        )
        ToggleDebugMenu(
            label = "Show overlap bound area",
            isChecked = config.showOverlapBoundArea,
            onCheckedChange = onToggleShowOverlapBoundArea,
        )
        ToggleDebugMenu(
            label = "Show connector area",
            isChecked = config.showConnectorArea,
            onCheckedChange = onToggleShowConnectorArea,
        )
        ToggleDebugMenu(
            label = "Show connection line",
            isChecked = config.showAllConnectionLines,
            onCheckedChange = onToggleShowConnectionLine,
        )
        ToggleDebugMenu(
            label = "Show line connection point",
            isChecked = config.showLineConnectionPoint,
            onCheckedChange = onToggleLineConnectionPoint,
        )
    }
}

@Composable
private fun ToggleDebugMenu(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
        )
        Text(label)
    }
}

@Composable
fun DebugContent(
    coordinates: WorkstationCoordinates,
    config: Config,
    lineConnectionPoints: List<Offset>,
) {
    if (!coordinates.areAvailable()) return
    val workspace = coordinates.workspace ?: return
    val textMeasure = rememberTextMeasurer()
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (config.showWorkspaceArea) {
                drawRect(
                    color = Color.Blue,
                    topLeft = workspace.offset,
                    size = workspace.size.toSize(),
                    style = Stroke(
                        width = 2.dp.toPx(),
                    ),
                )
            }

            if (config.showDeviceArea) {
                // Draw all device area with blue line
                getAllDevices(coordinates)
                    .forEach { device ->
                        drawRect(
                            color = Color.Blue,
                            topLeft = if (config.showOverlapBoundArea) {
                                Offset(
                                    x = device.offset.x - MinimumHorizontalDistanceToDevice.toPx(),
                                    y = device.offset.y - MinimumVerticalDistanceToDevice.toPx(),
                                )
                            } else {
                                device.offset
                            },
                            size = if (config.showOverlapBoundArea) {
                                device.size.toSize().let { size ->
                                    size.copy(
                                        width = size.width + (MinimumHorizontalDistanceToDevice.toPx() * 2),
                                        height = size.height + (MinimumVerticalDistanceToDevice.toPx() * 2),
                                    )
                                }
                            } else {
                                device.size.toSize()
                            },
                            style = Stroke(2.dp.toPx()),
                        )
                    }
            }
            if (config.showConnectorArea) {
                // Draw all connector area with blue line
                getAllConnectorsByBottom(coordinates).forEach {
                    drawRect(
                        color = Color.Blue,
                        topLeft = it.offset,
                        size = it.size.toSize(),
                        style = Stroke(2.dp.toPx()),
                    )
                }
            }
            if (config.showLineConnectionPoint) {
                lineConnectionPoints.forEachIndexed { index, point ->
                    drawCircle(
                        color = Color.Blue,
                        radius = 10.dp.toPx(),
                        center = point,
                        style = Stroke(2.dp.toPx()),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 10.dp.toPx(),
                        center = point,
                        style = Fill,
                    )
                    val size = textMeasure.measure(
                        text = "$index",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color.Blue,
                        ),
                    ).size
                    drawText(
                        textMeasurer = textMeasure,
                        text = "$index",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color.Blue,
                        ),
                        topLeft = Offset(
                            x = point.x - (size.width / 2f),
                            y = point.y - (size.height / 2f),
                        )
                    )
                }
            }
        }
    }
}

private fun getAllConnectorsByBottom(
    coordinates: WorkstationCoordinates,
): List<DeviceCoordinate.Connector> {
    return listOf(
        coordinates.officeLaptop.connectors,
        coordinates.personalLaptop.connectors,
        coordinates.pcDesktop.connectors,
        coordinates.usbDockingStation.connectors,
        coordinates.digitalCamera.connectors,
        coordinates.hdmiToWebcam.connectors,
        coordinates.streamDeck.connectors,
        coordinates.externalSsd.connectors,
        coordinates.usbCSwitcher.connectors,
        coordinates.usbHub.connectors,
        coordinates.usbPowerAdapter.connectors,
        coordinates.secondaryMonitor.connectors,
        coordinates.primaryMonitor.connectors,
        coordinates.usbDac.connectors,
        coordinates.usbDongle1.connectors,
        coordinates.usbDongle2.connectors,
        coordinates.ledLamp.connectors,
        coordinates.speaker.connectors,
        coordinates.microphone1.connectors,
        coordinates.microphone2.connectors,
        coordinates.hdmiCapture.connectors,
        coordinates.androidDevice.connectors,
        coordinates.gameController.connectors,
        coordinates.headphone.connectors,
    )
        .flatMap { it ?: listOf() }
}

private fun getAllDevices(
    coordinates: WorkstationCoordinates,
): List<DeviceCoordinate.Device> = listOfNotNull(
    coordinates.officeLaptop.device,
    coordinates.personalLaptop.device,
    coordinates.pcDesktop.device,
    coordinates.usbDockingStation.device,
    coordinates.digitalCamera.device,
    coordinates.hdmiToWebcam.device,
    coordinates.streamDeck.device,
    coordinates.externalSsd.device,
    coordinates.usbCSwitcher.device,
    coordinates.usbHub.device,
    coordinates.usbPowerAdapter.device,
    coordinates.secondaryMonitor.device,
    coordinates.primaryMonitor.device,
    coordinates.usbDac.device,
    coordinates.usbDongle1.device,
    coordinates.usbDongle2.device,
    coordinates.ledLamp.device,
    coordinates.speaker.device,
    coordinates.microphone1.device,
    coordinates.microphone2.device,
    coordinates.hdmiCapture.device,
    coordinates.androidDevice.device,
    coordinates.gameController.device,
    coordinates.headphone.device,
)