package com.akexorcist.workstation.diagram.common.ui.component

import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akexorcist.workstation.diagram.common.data.Connector
import com.akexorcist.workstation.diagram.common.data.ConnectorSide
import com.akexorcist.workstation.diagram.common.data.Device
import com.akexorcist.workstation.diagram.common.data.DeviceCoordinate
import com.akexorcist.workstation.diagram.common.theme.DeviceComponentTheme
import com.akexorcist.workstation.diagram.common.utility.onDeviceCoordinated

@Composable
internal fun ComputerDeviceComponent(
    device: Device,
    height: Dp? = null,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: () -> Unit,
    onEnterHoveDeviceInteraction: () -> Unit,
    onExitHoverDeviceInteraction: () -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ConnectorRenderer(
            device = device.type,
            side = ConnectorSide.Left,
            connectors = device.leftConnections,
            onConnectorCoordinated = onConnectorCoordinated,
            onEnterHoverInteraction = onEnterHoveConnectorInteraction,
            onExitHoverInteraction = onExitHoverConnectorInteraction,
        )
        ComputerDeviceContent(
            modifier = Modifier.onDeviceCoordinated(
                deviceType = device.type,
                onCoordinated = onDeviceCoordinated,
            ),
            title = device.title,
            subtitle = device.subtitle,
            height = height,
            onClick = onDeviceClick,
            onEnterHoverInteraction = onEnterHoveDeviceInteraction,
            onExitHoverInteraction = onExitHoverDeviceInteraction,
        )
        ConnectorRenderer(
            device = device.type,
            side = ConnectorSide.Right,
            connectors = device.rightConnections,
            onConnectorCoordinated = onConnectorCoordinated,
            onEnterHoverInteraction = onEnterHoveConnectorInteraction,
            onExitHoverInteraction = onExitHoverConnectorInteraction,
        )
    }
}

@Composable
private fun ComputerDeviceContent(
    modifier: Modifier,
    title: String,
    subtitle: String? = null,
    height: Dp? = null,
    onClick: () -> Unit,
    onEnterHoverInteraction: () -> Unit,
    onExitHoverInteraction: () -> Unit,
) {
    DeviceComponent(
        modifier = modifier,
        width = 190.dp,
        height = height ?: 80.dp,
        colors = DeviceComponentTheme.Computer.buttonColors(),
        shape = RoundedCornerShape(DeviceComponentTheme.End.cornerRadius),
        onClick = onClick,
        title = title,
        subtitle = subtitle,
        onEnterHoverInteraction = onEnterHoverInteraction,
        onExitHoverInteraction = onExitHoverInteraction,
    )
}

@Composable
internal fun HubDeviceComponent(
    device: Device,
    width: Dp? = null,
    height: Dp? = null,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: () -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ConnectorRenderer(
            device = device.type,
            side = ConnectorSide.Left,
            connectors = device.leftConnections,
            onConnectorCoordinated = onConnectorCoordinated,
            onEnterHoverInteraction = onEnterHoveConnectorInteraction,
            onExitHoverInteraction = onExitHoverConnectorInteraction,
        )

        HubDeviceContent(
            modifier = Modifier.onDeviceCoordinated(
                deviceType = device.type,
                onCoordinated = onDeviceCoordinated,
            ),
            title = device.title,
            subtitle = device.subtitle,
            width = width,
            height = height,
            onClick = onDeviceClick,
            onEnterHoverInteraction = { onEnterHoveDeviceInteraction(device) },
            onExitHoverInteraction = { onExitHoverDeviceInteraction(device) },
        )

        ConnectorRenderer(
            device = device.type,
            side = ConnectorSide.Right,
            connectors = device.rightConnections,
            onConnectorCoordinated = onConnectorCoordinated,
            onEnterHoverInteraction = onEnterHoveConnectorInteraction,
            onExitHoverInteraction = onExitHoverConnectorInteraction,
        )
    }
}

@Composable
private fun HubDeviceContent(
    modifier: Modifier,
    title: String,
    subtitle: String? = null,
    width: Dp? = null,
    height: Dp? = null,
    onClick: () -> Unit,
    onEnterHoverInteraction: () -> Unit,
    onExitHoverInteraction: () -> Unit,
) {
    DeviceComponent(
        modifier = modifier,
        width = width ?: 140.dp,
        height = height ?: 220.dp,
        colors = DeviceComponentTheme.Hub.buttonColors(),
        shape = RoundedCornerShape(DeviceComponentTheme.End.cornerRadius),
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        onEnterHoverInteraction = onEnterHoverInteraction,
        onExitHoverInteraction = onExitHoverInteraction,

        )
}

@Composable
internal fun EndDeviceComponent(
    device: Device,
    width: Dp? = null,
    height: Dp? = null,
    onDeviceCoordinated: (DeviceCoordinate.Device) -> Unit,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onDeviceClick: () -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ConnectorRenderer(
            device = device.type,
            side = ConnectorSide.Left,
            connectors = device.leftConnections,
            onConnectorCoordinated = onConnectorCoordinated,
            onEnterHoverInteraction = onEnterHoveConnectorInteraction,
            onExitHoverInteraction = onExitHoverConnectorInteraction,
        )

        EndDeviceContent(
            modifier = Modifier.onDeviceCoordinated(
                deviceType = device.type,
                onCoordinated = onDeviceCoordinated,
            ),
            title = device.title,
            subtitle = device.subtitle,
            width = width,
            height = height,
            onClick = onDeviceClick,
            onEnterHoverInteraction = { onEnterHoveDeviceInteraction(device) },
            onExitHoverInteraction = { onExitHoverDeviceInteraction(device) },
        )

        ConnectorRenderer(
            device = device.type,
            side = ConnectorSide.Right,
            connectors = device.rightConnections,
            onConnectorCoordinated = onConnectorCoordinated,
            onEnterHoverInteraction = onEnterHoveConnectorInteraction,
            onExitHoverInteraction = onExitHoverConnectorInteraction,
        )
    }
}

@Composable
private fun EndDeviceContent(
    modifier: Modifier,
    title: String,
    subtitle: String? = null,
    width: Dp? = null,
    height: Dp? = null,
    onClick: () -> Unit,
    onEnterHoverInteraction: () -> Unit,
    onExitHoverInteraction: () -> Unit,
) {
    DeviceComponent(
        modifier = modifier,
        width = width ?: 200.dp,
        height = height ?: 80.dp,
        colors = DeviceComponentTheme.End.buttonColors(),
        shape = RoundedCornerShape(DeviceComponentTheme.End.cornerRadius),
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        onEnterHoverInteraction = onEnterHoverInteraction,
        onExitHoverInteraction = onExitHoverInteraction,
    )
}

@Composable
private fun DeviceComponent(
    modifier: Modifier,
    width: Dp,
    height: Dp,
    title: String,
    subtitle: String? = null,
    colors: ButtonColors,
    shape: Shape,
    onClick: () -> Unit,
    onEnterHoverInteraction: () -> Unit,
    onExitHoverInteraction: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is HoverInteraction.Enter -> onEnterHoverInteraction()

                is HoverInteraction.Exit -> onExitHoverInteraction()
            }
        }
    }
    Button(
        modifier = Modifier
            .requiredWidth(width)
            .requiredHeight(height)
            .then(modifier),
        interactionSource = interactionSource,
        colors = colors,
        shape = shape,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                fontWeight = FontWeight.Bold,
            )
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                )
            }
        }
    }
}
