@file:Suppress("FunctionName")

package com.akexorcist.workstation.diagram.common.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.akexorcist.workstation.diagram.common.data.*

@Composable
fun DebugPanel(
    debugConfig: DebugConfig,
    onNextIndex: (Int) -> Unit,
    onPreviousIndex: (Int) -> Unit,
    onToggleShowWorkspaceArea: (Boolean) -> Unit,
    onToggleShowDeviceArea: (Boolean) -> Unit,
    onToggleShowOverlapBoundArea: (Boolean) -> Unit,
    onToggleShowConnectorArea: (Boolean) -> Unit,
    onToggleShowAllConnectionLines: (Boolean) -> Unit,
    onToggleLineConnectionPoint: (Boolean) -> Unit,
    onToggleLineOptimization: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(end = 32.dp)
            .informationBackground()
            .padding(
                horizontal = 24.dp,
                vertical = 16.dp,
            ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onPreviousIndex(debugConfig.lineIndex) }) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous line index",
                    tint = WorkstationDiagramTheme.themeColor.text,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Line ${debugConfig.lineIndex}",
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                color = WorkstationDiagramTheme.themeColor.text,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { onNextIndex(debugConfig.lineIndex) }) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next line index",
                    tint = WorkstationDiagramTheme.themeColor.text,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ToggleDebugMenu(
            label = "Show workspace area",
            isChecked = debugConfig.showWorkspaceArea,
            onCheckedChange = onToggleShowWorkspaceArea,
        )
        ToggleDebugMenu(
            label = "Show device area",
            isChecked = debugConfig.showDeviceArea,
            onCheckedChange = onToggleShowDeviceArea,
        )
        ToggleDebugMenu(
            label = "Show overlap bound area",
            isChecked = debugConfig.showOverlapBoundArea,
            onCheckedChange = onToggleShowOverlapBoundArea,
        )
        ToggleDebugMenu(
            label = "Show connector area",
            isChecked = debugConfig.showConnectorArea,
            onCheckedChange = onToggleShowConnectorArea,
        )
        ToggleDebugMenu(
            label = "Show all connection lines",
            isChecked = debugConfig.showAllConnectionLines,
            onCheckedChange = onToggleShowAllConnectionLines,
        )
        ToggleDebugMenu(
            label = "Show line connection point",
            isChecked = debugConfig.showLineConnectionPoint,
            onCheckedChange = onToggleLineConnectionPoint,
        )
        ToggleDebugMenu(
            label = "Disable line optimization",
            isChecked = debugConfig.disableLineOptimization,
            onCheckedChange = onToggleLineOptimization,
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
        Text(
            text = label,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            color = WorkstationDiagramTheme.themeColor.text,
        )
    }
}

@Composable
fun DebugContent(
    coordinates: WorkstationCoordinates,
    config: Config,
    debugConfig: DebugConfig,
    connections: List<Connection>,
) {
    if (!coordinates.areAvailable()) return
    val workspace = coordinates.workspace ?: return
    val textMeasure = rememberTextMeasurer()
    val debugPoint: List<Offset> = connections.flatMap { connection ->
        connection.path.lines.map { line -> line.end }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (debugConfig.showWorkspaceArea) {
                drawRect(
                    color = Color.Blue,
                    topLeft = workspace.offset,
                    size = workspace.size.toSize(),
                    style = Stroke(
                        width = 2.dp.toPx(),
                    ),
                )
            }

            if (debugConfig.showDeviceArea) {
                getAllDevices(coordinates)
                    .forEach { device ->
                        drawRect(
                            color = Color.Blue,
                            topLeft = if (debugConfig.showOverlapBoundArea) {
                                Offset(
                                    x = device.offset.x - config.minimumHorizontalDistanceToDevice.toPx(),
                                    y = device.offset.y - config.minimumVerticalDistanceToDevice.toPx(),
                                )
                            } else {
                                device.offset
                            },
                            size = if (debugConfig.showOverlapBoundArea) {
                                device.size.toSize().let { size ->
                                    size.copy(
                                        width = size.width + (config.minimumHorizontalDistanceToDevice.toPx() * 2),
                                        height = size.height + (config.minimumVerticalDistanceToDevice.toPx() * 2),
                                    )
                                }
                            } else {
                                device.size.toSize()
                            },
                            style = Stroke(2.dp.toPx()),
                        )
                    }
            }
            if (debugConfig.showConnectorArea) {
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
            if (debugConfig.showLineConnectionPoint) {
                debugPoint.forEachIndexed { index, point ->
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