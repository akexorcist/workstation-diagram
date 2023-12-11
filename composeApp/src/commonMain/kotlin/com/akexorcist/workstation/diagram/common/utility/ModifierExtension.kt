package com.akexorcist.workstation.diagram.common.utility

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.*
import com.akexorcist.workstation.diagram.common.data.*

@Composable
fun Modifier.onDeviceCoordinated(
    deviceType: Device.Type,
    onCoordinated: (DeviceCoordinate.Device) -> Unit,
): Modifier = this.onPlaced { coordinates ->
    onCoordinated(
        DeviceCoordinate.Device(
            device = deviceType,
            offset = coordinates.positionInRoot(),
            size = coordinates.size,
        )
    )
}

@Composable
fun Modifier.onConnectorCoordinated(
    device: Device.Type,
    connector: Connector,
    side: ConnectorSide,
    onCoordinated: (DeviceCoordinate.Connector) -> Unit,
): Modifier = this.onPlaced { coordinates ->
    onCoordinated(
        DeviceCoordinate.Connector(
            device = device,
            connector = connector,
            offset = coordinates.positionInRoot(),
            size = coordinates.size,
            side = side,
        )
    )
}

@Composable
fun Modifier.onWorkspaceCoordinated(
    onWorkspaceCoordinated: (WorkspaceCoordinate) -> Unit,
): Modifier = this.onPlaced { coordinates ->
    val positionInParent = coordinates.positionInParent()
    val positionInRoot = coordinates.positionInRoot()
    onWorkspaceCoordinated(
        WorkspaceCoordinate(
            offset = positionInParent,
            size = coordinates.size,
            adjustment = Offset(
                x = positionInParent.x - positionInRoot.x,
                y = positionInParent.y - positionInRoot.y,
            )
        )
    )
}
