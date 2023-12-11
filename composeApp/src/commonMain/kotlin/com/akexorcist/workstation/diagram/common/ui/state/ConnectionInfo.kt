package com.akexorcist.workstation.diagram.common.ui.state

import androidx.compose.ui.geometry.Rect
import com.akexorcist.workstation.diagram.common.data.Device
import com.akexorcist.workstation.diagram.common.data.DeviceCoordinate
import com.akexorcist.workstation.diagram.common.data.WorkstationCoordinates

data class ConnectionInfo(
    val coordinates: WorkstationCoordinates,
    val deviceAreas: List<Pair<Rect, Device.Type>>,
    val connectors: List<DeviceCoordinate.Connector>,
    val connectorAreas: List<Rect>,
) {
    fun areDevicesAndConnectorsAvailable() = coordinates.areAvailable()
}
