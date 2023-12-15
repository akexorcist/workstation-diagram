package com.akexorcist.workstation.diagram.common.utility

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.toSize
import com.akexorcist.workstation.diagram.common.data.*
import kotlin.math.*

fun getTargetConnector(
    coordinates: WorkstationCoordinates,
    connectionLine: ConnectionLine,
): DeviceCoordinate.Connector? = when (connectionLine.target?.owner) {
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
    else -> null
}?.find { it.connector.target == connectionLine.source.owner }

fun getConnectorPath(
    connectionLine: ConnectionLine,
    coordinates: WorkstationCoordinates,
    devices: List<Pair<Rect, Device.Type>>,
    connectors: List<Rect>,
    minimumDistanceBetweenLine: Float,
    minimumStartLineDistance: Float,
    recordedVerticalLine: List<VerticalLine>,
    onRecordVerticalPath: (VerticalLine) -> Unit,
    debugConfig: DebugConfig,
    debugLog: Boolean = false,
): ConnectionPath {
    if (debugLog) println("########## getConnectorPath ################")
    val endConnector = getTargetConnector(
        coordinates = coordinates,
        connectionLine = connectionLine,
    ) ?: return ConnectionPath()
    val startRect = connectionLine.let { Rect(it.offset, it.size.toSize()) }
    val startJoint = connectionLine.getJoint()
    val endRect = endConnector.let { Rect(it.offset, it.size.toSize()) }
    val endJoint = endConnector.getJoint()
    val lineRect = Rect(
        left = min(startJoint.x, endJoint.x),
        top = min(startJoint.y, endJoint.y),
        right = max(startJoint.x, endJoint.x),
        bottom = max(startJoint.y, endJoint.y),
    )
    if (debugLog) println("${connectionLine.source} (${connectionLine.source.type}) => ${connectionLine.target} (${connectionLine.target?.type})")
    return findPath(
        path = ConnectionPath(),
        devices = devices,
        connectionLine = connectionLine,
        connectors = connectors,
        startRect = startRect,
        startJoint = startJoint,
        endRect = endRect,
        endJoint = endJoint,
        endDeviceType = connectionLine.target?.owner,
        lineRect = lineRect,
        minimumDistanceBetweenLine = minimumDistanceBetweenLine,
        minimumStartLineDistance = minimumStartLineDistance,
        recordedVerticalLine = recordedVerticalLine,
        onRecordVerticalPath = onRecordVerticalPath,
        debugConfig = debugConfig,
        debugLog = debugLog,
    ).simplified()
}

private fun Rect.getOverlapDevices(
    startRect: Rect?,
    devices: List<Pair<Rect, Device.Type>>,
    exclude: Device.Type?,
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
    path: ConnectionPath,
    devices: List<Pair<Rect, Device.Type>>,
    connectionLine: ConnectionLine,
    connectors: List<Rect>,
    startRect: Rect?,
    startJoint: Offset,
    endRect: Rect,
    endJoint: Offset,
    endDeviceType: Device.Type?,
    lineRect: Rect,
    minimumDistanceBetweenLine: Float,
    minimumStartLineDistance: Float,
    recordedVerticalLine: List<VerticalLine>,
    onRecordVerticalPath: (VerticalLine) -> Unit,
    debugConfig: DebugConfig,
    debugLog: Boolean,
): ConnectionPath {
    val overlapDevices = lineRect.getOverlapDevices(
        startRect = startRect,
        devices = devices,
        exclude = endDeviceType,
    )
    val overlapConnectors = lineRect.getOverlapConnectors(
        connectors = connectors,
    )
    if (debugLog) {
        println("##########################")
        overlapDevices.forEach {
            println("Overlap devices: ${it.second}, ${it.first}")
        }
        overlapConnectors.forEach {
            println("Overlap connectors: $it")
        }
        println("lineRect: $lineRect")
    }

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
            if (debugLog) {
                println("closestOverlapDeviceByX: $closestOverlapDeviceByX")
                println("isEndConnectorSameX: $isEndConnectorSameX")
                println("closestByLeftDeviceAtSameY: $closestByLeftDeviceAtSameY")
                println("closestByRightDeviceAtSameY: $closestByRightDeviceAtSameY")
                println("anyOverlapDeviceAtSameY: $anyOverlapDeviceAtSameY")
            }
            when {
                isEndConnectorSameX && startRect.left == startJoint.x -> {
                    if (debugLog) println("Start with go left with start line distance")
                    Offset(
                        x = startJoint.x - abs(minimumStartLineDistance - startRect.width),
                        y = startJoint.y,
                    )
                }

                isEndConnectorSameX && startRect.right == startJoint.x -> {
                    if (debugLog) println("Start with go right with start line distance")
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
                            if (debugLog) println("Start with go left to right of closest device on the left")
                            closestByLeftDeviceAtSameY.first.right
                        }

                        startRect.right < endRect.left -> {
                            if (debugLog) println("Start with go left with start line distance")
                            startJoint.x - abs(minimumStartLineDistance - startRect.width)
                        }

                        else -> {
                            if (debugLog) println("Start with go left (1)")
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
                            if (debugLog) println("Start with go right to left of closest device on the right")
                            closestByRightDeviceAtSameY.first.left
                        }

                        startRect.left < endRect.right -> {
                            if (debugLog) println("Start with go right with start line distance")
                            startJoint.x + minimumStartLineDistance
                        }

                        else -> {
                            if (debugLog) println("Start with go right (1)")
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
                    if (debugLog) println("Start with go left with overlap device on the left")
                    Offset(
                        x = ((startJoint.x - minimumStartLineDistance) + closestOverlapDeviceByX.first.right) / 2f,
                        y = startJoint.y,
                    )
                }

                closestOverlapDeviceByX != null && startJoint.x < endJoint.x -> {
                    // Go right
                    if (debugLog) println("Start with go right with overlap device on the right")
                    Offset(
                        x = ((startJoint.x + minimumStartLineDistance) + closestOverlapDeviceByX.first.left) / 2f,
                        y = startJoint.y,
                    )
                }

                overlapDevices.isEmpty() && startJoint.x > endJoint.x -> {
                    // Go left
                    if (debugLog) println("Start with go left (2)")
                    Offset(
                        x = startJoint.x - (abs(startJoint.x - endJoint.x) / 2f),
                        y = startJoint.y,
                    )
                }

                overlapDevices.isEmpty() && startJoint.x < endJoint.x -> {
                    // Go right
                    if (debugLog) println("Start with go right (2)")
                    Offset(
                        x = startJoint.x + (abs(startJoint.x - endJoint.x) / 2f),
                        y = startJoint.y,
                    )
                }

                else -> {
                    if (debugLog) println("Where to go? (1)")
                    return path
                }
            }
        }

        else -> {
            if (debugLog) {
                println("startJoint $startJoint")
                println("endJoint $endJoint")
                println("lineRect $lineRect")
            }
            val closestDevice = overlapDevices.minByOrNull {
                min(
                    min(abs(startJoint.x - it.first.left), abs(startJoint.x - it.first.right)),
                    min(abs(startJoint.y - it.first.top), abs(startJoint.y - it.first.bottom)),
                )
            }
            if (debugLog) println("closestDevice $closestDevice")
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
                if (debugLog) println("Overlap connector $connector")
                (connector.left < startJoint.x && startJoint.x < connector.right)
            }
            if (debugLog) println("anyConnectorAtSameX $anyConnectorAtSameX")
            when {
                anyConnectorAtSameX && endRect.left < endJoint.x -> {
                    // Go right
                    if (debugLog) println("Go right to the same X of connector")
                    Offset(
                        x = startJoint.x,
                        y = endJoint.y,
                    )
                }

                anyConnectorAtSameX && endRect.right > endJoint.x -> {
                    // Go left
                    if (debugLog) println("Go left to the same X of connector")
                    Offset(
                        x = endRect.right - minimumStartLineDistance,
                        y = startJoint.y,
                    )
                }

                canGoToSameYWithEnd && startJoint.y < endJoint.y -> {
                    // Go down to same Y with end
                    if (debugLog) println("Go down to same Y with end")
                    Offset(
                        x = startJoint.x,
                        y = endJoint.y,
                    )
                }

                canGoToSameYWithEnd && startJoint.x < endJoint.x -> {
                    // Go up to same Y with end
                    if (debugLog) println("Go up to same Y with end")
                    Offset(
                        x = startJoint.x,
                        y = endJoint.y,
                    )
                }

                overlapDevices.isEmpty() && startJoint.y == endJoint.y && startJoint.x > endJoint.x -> {
                    // Left to end
                    if (debugLog) println("Go left to end")
                    Offset(
                        x = endJoint.x,
                        y = endJoint.y,
                    )
                }

                overlapDevices.isEmpty() && startJoint.y == endJoint.y && startJoint.x > endJoint.x -> {
                    // Right to end
                    if (debugLog) println("Go right to end")
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
                    // Go right across the device
                    if (debugLog) println("Go right across the device")
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
                    // Go left across the device
                    if (debugLog) println("Go left across the device")
                    Offset(
                        x = closestDevice.first.left,
                        y = startJoint.y,
                    )
                }

                mostClosestDeviceAtSameY != null &&
                        abs(mostClosestDeviceAtSameY.first.bottom - endJoint.y) < abs(mostClosestDeviceAtSameY.first.top - endJoint.y) -> {
                    // Down to bottom of device
                    val newY = mostClosestDeviceAtSameY.first.bottom
                    if (debugLog) println("Go down - prevent from left or right (${startJoint.y} => $newY)")
                    Offset(
                        x = startJoint.x,
                        y = newY,
                    )
                }

                mostClosestDeviceAtSameY != null &&
                        abs(mostClosestDeviceAtSameY.first.top - endJoint.y) < abs(mostClosestDeviceAtSameY.first.bottom - endJoint.y) -> {
                    // Up to top of device
                    val newY = mostClosestDeviceAtSameY.first.top
                    if (debugLog) println("Go up - prevent from left or right (${startJoint.y} => $newY)")
                    Offset(
                        x = startJoint.x,
                        y = newY,
                    )
                }

                startJoint.y < endJoint.y && mostClosestDeviceAtSameY == null -> {
                    // Down to top of device
                    val closestByBottomDeviceAtSameX = overlapDevices.filter {
                        it.first.left < startJoint.x && startJoint.x < it.first.right && it.first.top > startJoint.y
                    }.minByOrNull {
                        it.first.top - startJoint.y
                    }

                    val newY = when {
                        closestByBottomDeviceAtSameX != null -> {
                            if (debugLog) println("Go down - Before bottom device (${startJoint.y} => ${closestByBottomDeviceAtSameX.first.top})")
                            closestByBottomDeviceAtSameX.first.top
                        }

                        closestDevice != null -> {
                            if (debugLog) println("Go down to bottom of closest device")
                            closestDevice.first.bottom
                        }

                        else -> {
                            if (debugLog) println("Go down - to end (${startJoint.y} => ${startJoint.y + (abs(startJoint.y - endJoint.y) / 2f)})")
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
                    val mostClosestByTopDeviceAtSameX = overlapDevices.filter {
                        it.first.left < startJoint.x && startJoint.x < it.first.right && it.first.bottom < startJoint.y
                    }.minByOrNull {
                        startJoint.y - it.first.bottom
                    }

                    val newY = when {
                        mostClosestByTopDeviceAtSameX != null -> {
                            if (debugLog) println("Go up - Before top device (${startJoint.y} => ${mostClosestByTopDeviceAtSameX.first.bottom})")
                            mostClosestByTopDeviceAtSameX.first.bottom
                        }

                        closestDevice != null -> {
                            if (debugLog) println("Go up to top of closest device")
                            closestDevice.first.top
                        }

                        else -> {
                            if (debugLog) println("Go up - to end (${startJoint.y} => ${startJoint.y - (abs(startJoint.y - endJoint.y) / 2f)})")
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
                    // Go left to destination
                    if (debugLog) println("Go left to destination")
                    Offset(
                        x = endJoint.x,
                        y = endJoint.y,
                    )
                }

                overlapDevices.isEmpty() &&
                        overlapConnectors.isEmpty() &&
                        startJoint.y == endJoint.y &&
                        startJoint.x < endJoint.x -> {
                    // Go right to destination
                    if (debugLog) println("Go right to destination")
                    Offset(
                        x = endJoint.x,
                        y = endJoint.y,
                    )
                }

                else -> {
                    if (debugLog) println("Where to go? (2)")
                    return path
                }
            }
        }
    }

    val optimizedPosition = when {
        debugConfig.disableLineOptimization -> nextPosition
        startJoint.x != nextPosition.x -> nextPosition.getOptimizedOffset(
            connectionLine = connectionLine,
            endJoint = endJoint,
            recordedVerticalLine = recordedVerticalLine,
            distanceBetweenLine = minimumDistanceBetweenLine,
            isStartLine = startRect != null,
            debugLog = debugLog,
        )

        else -> nextPosition
    }

    path.add(
        ConnectionPath.Line(
            start = startJoint,
            end = optimizedPosition,
        )
    )
    if (startJoint.y != optimizedPosition.y) {
        onRecordVerticalPath(
            VerticalLine(
                start = Offset(startJoint.x, startJoint.y),
                end = Offset(optimizedPosition.x, optimizedPosition.y),
                owner = connectionLine,
            )
        )
    }

    if (debugLog) println("Moving : ${startJoint.x}, ${startJoint.y} => ${optimizedPosition.x}, ${optimizedPosition.y}")

    if (optimizedPosition.x != endJoint.x || optimizedPosition.y != endJoint.y) {
        if (debugLog) println("Do find path again")
        return findPath(
            path = path,
            devices = devices,
            connectionLine = connectionLine,
            connectors = connectors,
            startRect = null,
            startJoint = optimizedPosition,
            endRect = endRect,
            endJoint = endJoint,
            endDeviceType = endDeviceType,
            lineRect = Rect.from(optimizedPosition, endJoint),
            minimumDistanceBetweenLine = minimumDistanceBetweenLine,
            minimumStartLineDistance = minimumStartLineDistance,
            recordedVerticalLine = recordedVerticalLine,
            onRecordVerticalPath = onRecordVerticalPath,
            debugConfig = debugConfig,
            debugLog = debugLog,
        )
    } else {
        if (debugLog) println("Return path")
        return path
    }
}

private fun Offset.getOptimizedOffset(
    connectionLine: ConnectionLine,
    endJoint: Offset,
    recordedVerticalLine: List<VerticalLine>,
    distanceBetweenLine: Float,
    isStartLine: Boolean,
    debugLog: Boolean,
): Offset {
    val startLineArea =
        if (isStartLine)
            Rect.from(this, connectionLine.offset)
                .getBoundRect(
                    top = when {
                        endJoint.y < this.y -> this.y - endJoint.y
                        else -> 0f
                    },
                    bottom = when {
                        this.y < endJoint.y -> endJoint.y - this.y
                        else -> 0f
                    },
                )
        else null

    val endLineArea = Rect.from(this, endJoint)
        .getBoundRect(
            top = when {
                endJoint.y < this.y -> this.y - endJoint.y
                else -> 0f
            },
            bottom = when {
                this.y < endJoint.y -> endJoint.y - this.y
                else -> 0f
            },
        )

    val verticalLine = recordedVerticalLine
        .filterNot { it.owner == connectionLine }
        .map { it.getBoundRect(horizontal = distanceBetweenLine) }

    val combineMultipleRect = { acc: Rect?, rect: Rect -> Rect.from(acc, rect) }

    val startOverlapVerticalLine: Rect? = startLineArea?.let {
        verticalLine
            .filter { it.overlaps(startLineArea) }
            .takeIf { it.isNotEmpty() }
            ?.fold(null as Rect?, combineMultipleRect)
    }

    val endOverlapVerticalLine: Rect? = verticalLine
        .filter { it.overlaps(endLineArea) }
        .takeIf { it.isNotEmpty() }
        ?.fold(null as Rect?, combineMultipleRect)

    val newX = when {
        connectionLine.offset.x < endJoint.x && endOverlapVerticalLine != null && this.x > endJoint.x -> {
            if (debugLog) println("Keep distance at left of end overlap vertical line")
            endOverlapVerticalLine.left
        }

        endOverlapVerticalLine != null && endJoint.x < this.x -> {
            if (debugLog) println("Keep distance at right of end overlap vertical line")
            endOverlapVerticalLine.right
        }

        endOverlapVerticalLine != null && this.x <= endJoint.x -> {
            if (debugLog) println("Keep distance at left of end overlap vertical line")
            endOverlapVerticalLine.left
        }

        startOverlapVerticalLine != null && connectionLine.offset.x < this.x -> {
            if (debugLog) println("Keep distance at right of start overlap vertical line")
            startOverlapVerticalLine.right
        }

        startOverlapVerticalLine != null && this.x <= connectionLine.offset.x -> {
            if (debugLog) println("Keep distance at left of start overlap vertical line")
            startOverlapVerticalLine.left
        }

        else -> {
            if (debugLog) println("No overlap vertical line")
            this.x
        }
    }
    return this.copy(x = newX)
}

private fun Rect.Companion.from(offset1: Offset, offset2: Offset): Rect = Rect(
    topLeft = Offset(
        x = min(offset1.x, offset2.x),
        y = min(offset1.y, offset2.y),
    ),
    bottomRight = Offset(
        x = max(offset1.x, offset2.x),
        y = max(offset1.y, offset2.y),
    ),
)

private fun Rect.Companion.from(rect1: Rect?, rect2: Rect): Rect = Rect(
    topLeft = Offset(
        x = min((rect1?.left ?: Float.MAX_VALUE), rect2.left),
        y = min((rect1?.top ?: Float.MAX_VALUE), rect2.top),
    ),
    bottomRight = Offset(
        x = max((rect1?.right ?: Float.MIN_VALUE), rect2.right),
        y = max((rect1?.bottom ?: Float.MIN_VALUE), rect2.bottom),
    ),
)

private fun Rect.getBoundRect(top: Float, bottom: Float) = this.copy(
    top = this.top - top,
    bottom = this.bottom + bottom,
)

private fun ConnectionPath.simplified(): ConnectionPath {
    val simplifiedLines = mutableListOf<ConnectionPath.Line>()
    var currentLine: ConnectionPath.Line? = null
    this.lines.forEachIndexed { index, line ->
        if (index == 0) {
            currentLine = line
        } else if (index == this.lines.lastIndex && isSameDirection(currentLine, line)) {
            currentLine?.let { simplifiedLines.add(ConnectionPath.Line(it.start, line.end)) }
        } else if (index == this.lines.lastIndex) {
            currentLine?.let { simplifiedLines.add(it) }
            simplifiedLines.add(line)
        } else if (isSameDirection(currentLine, line)) {
            currentLine = currentLine
                ?.let { ConnectionPath.Line(it.start, line.end) }
                ?: line
        } else {
            currentLine?.let { simplifiedLines.add(it) }
            currentLine = line
        }

    }
    return ConnectionPath(simplifiedLines)
}

private fun isSameDirection(line1: ConnectionPath.Line?, line2: ConnectionPath.Line): Boolean {
    line1 ?: return false
    return (line1.start.x == line2.end.x && line1.start.y != line2.end.y) ||
            (line1.start.x != line2.end.x && line1.start.y == line2.end.y)
}
