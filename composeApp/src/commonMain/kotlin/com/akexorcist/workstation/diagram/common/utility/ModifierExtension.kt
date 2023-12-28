package com.akexorcist.workstation.diagram.common.utility

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.dp
import com.akexorcist.workstation.diagram.common.data.*
import com.akexorcist.workstation.diagram.common.theme.WorkstationDiagramTheme

@Composable
fun Modifier.onDeviceCoordinated(
    deviceType: Device.Type,
    onCoordinated: (DeviceCoordinate.Device) -> Unit,
): Modifier {
    var hasPlaced by remember { mutableStateOf(false) }
    return this.onPlaced { coordinates ->
        if (hasPlaced) return@onPlaced
        hasPlaced = true
        onCoordinated(
            DeviceCoordinate.Device(
                device = deviceType,
                offset = coordinates.positionInRoot(),
                size = coordinates.size,
            )
        )
    }
}

@Composable
fun Modifier.onConnectorCoordinated(
    device: Device.Type,
    connector: Connector,
    side: ConnectorSide,
    onCoordinated: (DeviceCoordinate.Connector) -> Unit,
): Modifier {
    var hasPlaced by remember { mutableStateOf(false) }
    return this.onPlaced { coordinates ->
        if (hasPlaced) return@onPlaced
        hasPlaced = true
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
}

@Composable
fun Modifier.onWorkspaceCoordinated(
    onWorkspaceCoordinated: (WorkspaceCoordinate) -> Unit,
): Modifier {
    var hasPlaced by remember { mutableStateOf(false) }
    return this.onPlaced { coordinates ->
        if (hasPlaced) return@onPlaced
        hasPlaced = true
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
}

@Composable
fun Modifier.informationBackground(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
): Modifier = this
    .shadow(
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
    )
    .hoverable(interactionSource = interactionSource)
    .clip(RoundedCornerShape(8.dp))
    .background(
        color = WorkstationDiagramTheme.themeColor.uiBackground,
        shape = RoundedCornerShape(8.dp),
    )

