package com.akexorcist.workstation.diagram.common.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.akexorcist.workstation.diagram.common.data.*
import com.akexorcist.workstation.diagram.common.ui.state.Config
import com.akexorcist.workstation.diagram.common.ui.state.ConnectionInfo
import com.akexorcist.workstation.diagram.common.utility.px
import kotlin.math.*

val MinimumHorizontalDistanceToDevice = 120.dp
val MinimumVerticalDistanceToDevice = 30.dp
val MinimumDistanceBetweenLine = 40.dp
val MinimumStartLineDistance = MinimumHorizontalDistanceToDevice

@Composable
internal fun ConnectionContent(
    connectionInfo: ConnectionInfo,
    config: Config,
    onAddDebugPoint: (Offset) -> Unit,
) {
    if (!connectionInfo.areDevicesAndConnectorsAvailable()) return
    println("############ Recomposition ############")
    val paths: List<Path> = connectionInfo.connectors.run {
        if (config.showAllConnectionLines) {
            this
        } else {
            filterIndexed { index, _ -> index == config.lineIndex }
//            filterIndexed { index, _ -> index == 6 || index == 1 }
        }
    }.map { connector ->
        getConnectorPath(
            startConnector = connector,
            devices = connectionInfo.deviceAreas,
            connectors = connectionInfo.connectorAreas,
            coordinates = connectionInfo.coordinates,
            minimumDistanceBetweenLine = MinimumDistanceBetweenLine.px(),
            minimumStartLineDistance = MinimumStartLineDistance.px(),
            onAddDebugPoint = onAddDebugPoint,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        paths.forEach { path ->
            ConnectionLine(path = path)
        }
    }
}

@Composable
private fun ConnectionLine(path: Path) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawPath(
            path = path,
            color = Color.Green,
            style = Stroke(2.dp.toPx()),
        )
    }
}

private fun getConnectorPath(
    startConnector: DeviceCoordinate.Connector,
    coordinates: WorkstationCoordinates,
    devices: List<Pair<Rect, Device.Type>>,
    connectors: List<Rect>,
    minimumDistanceBetweenLine: Float,
    minimumStartLineDistance: Float,
    onAddDebugPoint: (Offset) -> Unit,
): Path {
    val endConnector = getTargetConnector(coordinates, startConnector) ?: return Path()
    val startRect = startConnector.let { Rect(it.offset, it.size.toSize()) }
    val startJoint = startConnector.getJoint()
    val endRect = endConnector.let { Rect(it.offset, it.size.toSize()) }
    val endJoint = endConnector.getJoint()
    val lineRect = Rect(
        left = min(startJoint.x, endJoint.x),
        top = min(startJoint.y, endJoint.y),
        right = max(startJoint.x, endJoint.x),
        bottom = max(startJoint.y, endJoint.y),
    )
    println("${startConnector.device} ${startConnector.connector.type} => ${startConnector.connector.target}")
    return findPath(
        path = Path(),
        devices = devices,
        connectors = connectors,
        startRect = startRect,
        startJoint = startJoint,
        endRect = endRect,
        endJoint = endJoint,
        endDeviceType = startConnector.connector.target,
        lineRect = lineRect,
        minimumDistanceBetweenLine = minimumDistanceBetweenLine,
        minimumStartLineDistance = minimumStartLineDistance,
        onAddDebugPoint = onAddDebugPoint,
    )
}

sealed class LineDirection {
    data class Left(
        val target: Offset,
    ) : LineDirection()

    data class Right(
        val target: Offset,
    ) : LineDirection()

    data class Down(
        val target: Offset,
    ) : LineDirection()

    data class Up(
        val target: Offset,
    ) : LineDirection()
}

private fun Rect.getOverlapDevices(
    startRect: Rect?,
    devices: List<Pair<Rect, Device.Type>>,
    exclude: Device.Type,
): List<Pair<Rect, Device.Type>> = devices.filter {
    if (exclude == it.second) return@filter false
    this.overlaps(
        it.first.copy(
            left = it.first.left + (startRect?.width ?: 0f),
            top = it.first.top,
            right = it.first.right - (startRect?.width ?: 0f),
            bottom = it.first.bottom,
        )
    )
}

private fun Rect.getOverlapConnectors(
    connectors: List<Rect>,
): List<Rect> = connectors.filter {
    this.overlaps(
        it.copy(
            left = it.left,
            top = it.top,
            right = it.right,
            bottom = it.bottom,
        )
    )
}

private fun findPath(
    path: Path,
    devices: List<Pair<Rect, Device.Type>>,
    connectors: List<Rect>,
    startRect: Rect?,
    startJoint: Offset,
    endRect: Rect,
    endJoint: Offset,
    endDeviceType: Device.Type,
    lineRect: Rect,
    minimumDistanceBetweenLine: Float,
    minimumStartLineDistance: Float,
    onAddDebugPoint: (Offset) -> Unit,
): Path {
    println("##########################")
    val overlapDevices = lineRect.getOverlapDevices(
        startRect = startRect,
        devices = devices,
        exclude = endDeviceType,
    )
    val overlapConnectors = lineRect.getOverlapConnectors(
        connectors = connectors,
    )
    overlapDevices.forEach {
        println("Overlap devices: ${it.second}, ${it.first}")
    }
//    overlapConnectors.forEach {
//        println("Overlap connectors: $it")
//    }
    println("lineRect: $lineRect")

    val nextPosition: Offset = when {
        startRect != null -> {
            val closestOverlapDeviceByX = overlapDevices.minByOrNull {
                val x = abs(min(it.first.left, it.first.right) - startJoint.x)
                val y = abs(min(it.first.top, it.first.bottom) - startJoint.y)
                sqrt(x.pow(2) + y.pow(2))
            }
            val isEndConnectorSameX = startRect.left == endRect.left || startRect.right == endRect.right
            val closestByLeftDeviceAtSameY = overlapDevices.filter {
                (it.first.top..it.first.bottom).contains(startJoint.y) && it.first.right < startJoint.x
            }.minByOrNull {
                startJoint.x - it.first.right
            }
            val closestByRightDeviceAtSameY = overlapDevices.filter {
                (it.first.top..it.first.bottom).contains(startJoint.y) && it.first.left > startJoint.x
            }.minByOrNull {
                it.first.left - startJoint.x
            }
            val anyOverlapDeviceAtSameY = overlapDevices.any {
                (it.first.top..it.first.bottom).contains(startJoint.y)
            }
//            println("closestOverlapDeviceByX: $closestOverlapDeviceByX")
//            println("isEndConnectorSameX: $isEndConnectorSameX")
//            println("closestByLeftDeviceAtSameY: $closestByLeftDeviceAtSameY")
//            println("closestByRightDeviceAtSameY: $closestByRightDeviceAtSameY")
//            println("anyOverlapDeviceAtSameY: $anyOverlapDeviceAtSameY")
            when {
                isEndConnectorSameX && startRect.left == startJoint.x -> {
                    println("Start with go left with start line distance")
                    val newX = startJoint.x - abs(minimumStartLineDistance - startRect.width)
                    Offset(
                        x = newX,
                        y = startJoint.y,
                    )
                }

                isEndConnectorSameX && startRect.right == startJoint.x -> {
                    println("Start with go right with start line distance")
                    val newX = startJoint.x + abs(minimumStartLineDistance - startRect.width)
                    Offset(
                        x = newX,
                        y = startJoint.y,
                    )
                }

                startRect.left == startJoint.x && anyOverlapDeviceAtSameY -> {
                    // Go left
                    val newX = when {
                        closestByLeftDeviceAtSameY != null -> {
                            println("Start with go left to right of closest device on the left")
                            closestByLeftDeviceAtSameY.first.right
                        }

                        startRect.right < endRect.left -> {
                            println("Start with go left with start line distance")
                            startJoint.x - abs(minimumStartLineDistance - startRect.width)
                        }

                        else -> {
                            println("Start with go left (1)")
                            startJoint.x - (abs(startJoint.x - endJoint.x) / 2f)
                        }
                    }
                    Offset(
                        x = newX,
                        y = startJoint.y,
                    )
                }

                startRect.right == startJoint.x && anyOverlapDeviceAtSameY -> {
                    // Go right
                    val newX = when {
                        closestByRightDeviceAtSameY != null -> {
                            println("Start with go right to left of closest device on the right")
                            closestByRightDeviceAtSameY.first.left
                        }

                        startRect.left < endRect.right -> {
                            println("Start with go right with start line distance")
                            startJoint.x + minimumStartLineDistance
                        }

                        else -> {
                            println("Start with go right (1)")
                            startJoint.x + (abs(startJoint.x - endJoint.x) / 2f)
                        }
                    }
                    Offset(
                        x = newX,
                        y = startJoint.y,
                    )
                }

                closestOverlapDeviceByX != null && startJoint.x > endJoint.x -> {
                    // Go left
                    println("Start with go left with overlap device on the left")
                    Offset(
                        x = ((startJoint.x - minimumStartLineDistance) + closestOverlapDeviceByX.first.right) / 2f,
                        y = startJoint.y,
                    )
                }

                closestOverlapDeviceByX != null && startJoint.x < endJoint.x -> {
                    // Go right
                    println("Start with go right with overlap device on the right")
                    Offset(
                        x = ((startJoint.x + minimumStartLineDistance) + closestOverlapDeviceByX.first.left) / 2f,
                        y = startJoint.y,
                    )
                }

                overlapDevices.isEmpty() && startJoint.x > endJoint.x -> {
                    // Go left
                    println("Start with go left (2)")
                    Offset(
                        x = startJoint.x - (abs(startJoint.x - endJoint.x) / 2f),
                        y = startJoint.y,
                    )
                }

                overlapDevices.isEmpty() && startJoint.x < endJoint.x -> {
                    // Go right
                    println("Start with go right (2)")
                    Offset(
                        x = startJoint.x + (abs(startJoint.x - endJoint.x) / 2f),
                        y = startJoint.y,
                    )
                }

                else -> {
                    println("Where to go? (1)")
                    return path
                }
            }
        }

        else -> {
            println("startJoint $startJoint")
            println("endJoint $endJoint")
            println("lineRect $lineRect")
            val closestDevice = overlapDevices.minByOrNull {
                min(
                    min(abs(startJoint.x - it.first.left), abs(startJoint.x - it.first.right)),
                    min(abs(startJoint.y - it.first.top), abs(startJoint.y - it.first.bottom)),
                )
            }
            println("closestDevice $closestDevice")
            val canGoToSameYWithEnd = startJoint.y != endJoint.y &&
                    Rect(
                        left = min(startJoint.x, endJoint.x),
                        top = min(startJoint.y, endJoint.y),
                        right = max(startJoint.x, endJoint.x),
                        bottom = max(startJoint.y, endJoint.y),
                    ).getOverlapDevices(
                        startRect = null,
                        devices = devices,
                        exclude = endDeviceType,
                    ).isEmpty()
            val mostClosestDeviceAtSameY = overlapDevices.filter {
                it.first.top < startJoint.y && startJoint.y < it.first.bottom &&
                        ((startJoint.x > endJoint.x && it.first.right <= startJoint.x) ||
                                (startJoint.x < endJoint.x && it.first.left >= startJoint.x))
            }.minByOrNull {
                when {
                    startJoint.x > endJoint.x -> startJoint.x - it.first.right
                    startJoint.x < endJoint.x -> it.first.left - startJoint.x
                    else -> Float.MAX_VALUE
                }
            }
            val anyConnectorAtSameX = overlapConnectors.any { connector ->
                println("Overlap connector $connector")
                (connector.left < startJoint.x && startJoint.x < connector.right)
            }
            println("anyConnectorAtSameX $anyConnectorAtSameX")
            when {
                anyConnectorAtSameX && endRect.left < endJoint.x -> {
                    // Go right
                    println("Go right to the same X of connector")
                    Offset(
                        x = startJoint.x,
                        y = endJoint.y,
                    )
                }

                anyConnectorAtSameX && endRect.right > endJoint.x -> {
                    // Go left
                    println("Go left to the same X of connector")
                    Offset(
                        x = endRect.right - minimumStartLineDistance,
                        y = startJoint.y,
                    )
                }

                canGoToSameYWithEnd && startJoint.y < endJoint.y -> {
                    // Go down to same Y with end
                    println("Go down to same Y with end")
                    Offset(
                        x = startJoint.x,
                        y = endJoint.y,
                    )
                }

                canGoToSameYWithEnd && startJoint.x < endJoint.x -> {
                    // Go up to same Y with end
                    println("Go up to same Y with end")
                    Offset(
                        x = startJoint.x,
                        y = endJoint.y,
                    )
                }

                overlapDevices.isEmpty() && startJoint.y == endJoint.y && startJoint.x > endJoint.x -> {
                    // Left to end
                    println("Go left to end")
                    Offset(
                        x = endJoint.x,
                        y = endJoint.y,
                    )
                }

                overlapDevices.isEmpty() && startJoint.y == endJoint.y && startJoint.x > endJoint.x -> {
                    // Right to end
                    println("Go right to end")
                    Offset(
                        x = endJoint.x,
                        y = endJoint.y,
                    )
                }

                mostClosestDeviceAtSameY == null &&
                        startJoint.x < endJoint.x &&
                        closestDevice != null &&
                        closestDevice.first.left < endJoint.x &&
                        abs(closestDevice.first.left - endJoint.x) > abs(closestDevice.first.right - endJoint.x) &&
                        (startJoint.y < closestDevice.first.top || startJoint.y >= closestDevice.first.bottom) -> {
                    println("Go right across the device")
                    Offset(
                        x = closestDevice.first.right,
                        y = startJoint.y,
                    )
                }

                mostClosestDeviceAtSameY == null &&
                        startJoint.x > endJoint.x &&
                        closestDevice != null &&
                        closestDevice.first.right > endJoint.x &&
                        abs(closestDevice.first.left - endJoint.x) < abs(closestDevice.first.right - endJoint.x) &&
                        (startJoint.y <= closestDevice.first.top || startJoint.y >= closestDevice.first.bottom) -> {
                    println("Go left across the device")
                    Offset(
                        x = closestDevice.first.left,
                        y = startJoint.y,
                    )
                }

                mostClosestDeviceAtSameY != null &&
                        abs(mostClosestDeviceAtSameY.first.bottom - endJoint.y) < abs(mostClosestDeviceAtSameY.first.top - endJoint.y) -> {
                    // Down to bottom of device
                    val newY = mostClosestDeviceAtSameY.first.bottom
                    println("Go down - prevent from left or right (${startJoint.y} => $newY)")
                    println("endDeviceType $endDeviceType")
                    Offset(
                        x = startJoint.x,
                        y = newY,
                    )
                }

                mostClosestDeviceAtSameY != null &&
                        abs(mostClosestDeviceAtSameY.first.top - endJoint.y) < abs(mostClosestDeviceAtSameY.first.bottom - endJoint.y) -> {
                    // Up to top of device
                    val newY = mostClosestDeviceAtSameY.first.top
                    println("Go up - prevent from left or right (${startJoint.y} => $newY)")
                    println("endDeviceType $endDeviceType")
                    Offset(
                        x = startJoint.x,
                        y = newY,
                    )
                }

                startJoint.y < endJoint.y && mostClosestDeviceAtSameY == null -> {
                    // Down to top of device
                    println("Go down")
                    println("endDeviceType $endDeviceType")
                    val closestByBottomDeviceAtSameX = overlapDevices.filter {
                        it.first.left < startJoint.x && startJoint.x < it.first.right && it.first.top > startJoint.y
                    }.minByOrNull {
                        it.first.top - startJoint.y
                    }

                    val newY = when {
                        closestByBottomDeviceAtSameX != null -> {
                            println("Go down - Before bottom device (${startJoint.y} => ${closestByBottomDeviceAtSameX.first.top})")
                            closestByBottomDeviceAtSameX.first.top
                        }

                        closestDevice != null -> {
                            println("Go down to bottom of closest device")
                            closestDevice.first.bottom
                        }

                        else -> {
                            println("Go down - to end (${startJoint.y} => ${startJoint.y + (abs(startJoint.y - endJoint.y) / 2f)})")
                            endJoint.y
                        }
                    }
                    Offset(
                        x = startJoint.x,
                        y = newY,
                    )
                }

                startJoint.y > endJoint.y && mostClosestDeviceAtSameY == null -> {
                    // Up to bottom of device
                    println("Go up")
                    println("endDeviceType $endDeviceType")
                    val mostClosestByTopDeviceAtSameX = overlapDevices.filter {
                        it.first.left < startJoint.x && startJoint.x < it.first.right && it.first.bottom < startJoint.y
                    }.minByOrNull {
                        startJoint.y - it.first.bottom
                    }

                    val newY = when {
                        mostClosestByTopDeviceAtSameX != null -> {
                            println("Go up - Before top device (${startJoint.y} => ${mostClosestByTopDeviceAtSameX.first.bottom})")
                            mostClosestByTopDeviceAtSameX.first.bottom
                        }

                        closestDevice != null -> {
                            println("Go up to top of closest device")
                            closestDevice.first.top
                        }

                        else -> {
                            println("Go up - to end (${startJoint.y} => ${startJoint.y - (abs(startJoint.y - endJoint.y) / 2f)})")
                            endJoint.y
                        }
                    }
                    Offset(
                        x = startJoint.x,
                        y = newY,
                    )
                }

                overlapDevices.isEmpty() &&
                        overlapConnectors.isEmpty() &&
                        startJoint.y == endJoint.y &&
                        startJoint.x > endJoint.x -> {
                    println("Go left to destination")
                    Offset(
                        x = endJoint.x,
                        y = endJoint.y,
                    )
                }

                overlapDevices.isEmpty() &&
                        overlapConnectors.isEmpty() &&
                        startJoint.y == endJoint.y &&
                        startJoint.x < endJoint.x -> {
                    println("Go right to destination")
                    Offset(
                        x = endJoint.x,
                        y = endJoint.y,
                    )
                }

                else -> {
                    println("Where to go? (2)")
                    return path
                }
            }
        }
    }

    path.run {
        if (this.isEmpty) {
            moveTo(
                x = startJoint.x,
                y = startJoint.y,
            )
        } else {
            lineTo(
                x = startJoint.x,
                y = startJoint.y,
            )
        }
        lineTo(
            x = nextPosition.x,
            y = nextPosition.y,
        )
    }

    onAddDebugPoint(nextPosition)
    println("Moving : ${startJoint.x}, ${startJoint.y} => ${nextPosition.x}, ${nextPosition.y}")

    if (nextPosition.x != endJoint.x || nextPosition.y != endJoint.y) {
        println("Do find path again")
        return findPath(
            path = path,
            devices = devices,
            connectors = connectors,
            startRect = null,
            startJoint = nextPosition,
            endRect = endRect,
            endJoint = endJoint,
            endDeviceType = endDeviceType,
            lineRect = Rect(
                left = min(nextPosition.x, endJoint.x),
                top = min(nextPosition.y, endJoint.y),
                right = max(nextPosition.x, endJoint.x),
                bottom = max(nextPosition.y, endJoint.y),
            ),
            minimumDistanceBetweenLine = minimumDistanceBetweenLine,
            minimumStartLineDistance = minimumStartLineDistance,
            onAddDebugPoint = onAddDebugPoint,
        )
    } else {
        println("Return path")
        return path
    }
}

private fun getTargetConnector(
    coordinates: WorkstationCoordinates,
    connector: DeviceCoordinate.Connector,
): DeviceCoordinate.Connector? {
    return when (connector.connector.target) {
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
    }?.find { it.connector.target == connector.device }
}

private fun getSortedConnectorByBottom(
    coordinates: WorkstationCoordinates,
): List<DeviceCoordinate.Connector> {
    return getAllConnectorsByBottom(coordinates)
        .filter { connector -> connector.side == ConnectorSide.Left }
        .sortedByDescending { it.offset.y }
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

private fun List<Pair<Rect, String>>.getClosest(joint: Offset): Pair<Rect, String> =
    minBy {
        min(
            min(
                abs(it.first.left - joint.x),
                abs(it.first.right - joint.x),
            ),
            min(
                abs(it.first.top - joint.y),
                abs(it.first.bottom - joint.y),
            ),
        )
    }
