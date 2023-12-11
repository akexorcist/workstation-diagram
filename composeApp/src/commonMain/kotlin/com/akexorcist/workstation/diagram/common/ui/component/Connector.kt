package com.akexorcist.workstation.diagram.common.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akexorcist.workstation.diagram.common.data.*
import com.akexorcist.workstation.diagram.common.theme.ConnectorComponentTheme
import com.akexorcist.workstation.diagram.common.theme.DeviceComponentTheme
import com.akexorcist.workstation.diagram.common.utility.onConnectorCoordinated

private val ConnectorWidth = 110.dp
private val ConnectorSpacing = 20.dp

@Composable
internal fun InputConnectorComponent(
    device: Device.Type,
    connector: Connector,
    side: ConnectorSide,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onEnterHoverInteraction: (Connector) -> Unit,
    onExitHoverInteraction: (Connector) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is HoverInteraction.Enter -> onEnterHoverInteraction(connector)

                is HoverInteraction.Exit -> onExitHoverInteraction(connector)
            }
        }
    }
    ConnectorComponent(
        modifier = Modifier.onConnectorCoordinated(
            device = device,
            connector = connector,
            onCoordinated = onConnectorCoordinated,
            side = side,
        ).indication(
            interactionSource = interactionSource,
            indication = rememberRipple(),
        ),
        label = connector.type.value,
        color = ConnectorComponentTheme.Input.color,
        cornerRadius = ConnectorComponentTheme.Input.cornerRadius,
        direction = side,
        onEnterHoverInteraction = { onEnterHoverInteraction(connector) },
        onExitHoverInteraction = { onExitHoverInteraction(connector) },
    )
}

@Composable
internal fun OutputConnectorComponent(
    device: Device.Type,
    connector: Connector,
    side: ConnectorSide,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onEnterHoverInteraction: (Connector) -> Unit,
    onExitHoverInteraction: (Connector) -> Unit,
) {
    ConnectorComponent(
        modifier = Modifier.onConnectorCoordinated(
            device = device,
            connector = connector,
            onCoordinated = onConnectorCoordinated,
            side = side,
        ),
        label = connector.type.value,
        color = ConnectorComponentTheme.Output.color,
        cornerRadius = ConnectorComponentTheme.Output.cornerRadius,
        direction = side,
        onEnterHoverInteraction = { onEnterHoverInteraction(connector) },
        onExitHoverInteraction = { onExitHoverInteraction(connector) },
    )
}

@Composable
private fun ConnectorComponent(
    modifier: Modifier,
    label: String,
    color: Color,
    cornerRadius: Dp,
    direction: ConnectorSide,
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
    Box(
        modifier = modifier
            .hoverable(interactionSource = interactionSource)
            .background(
                color = color,
                shape = when (direction) {
                    ConnectorSide.Left -> RoundedCornerShape(
                        topStart = cornerRadius,
                        bottomStart = cornerRadius,
                    )

                    ConnectorSide.Right -> RoundedCornerShape(
                        topEnd = cornerRadius,
                        bottomEnd = cornerRadius,
                    )
                },
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = label,
            fontSize = MaterialTheme.typography.labelSmall.fontSize,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
internal fun ConnectorRenderer(
    device: Device.Type,
    side: ConnectorSide,
    connectors: List<Connector>,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
    onEnterHoverInteraction: (Connector) -> Unit,
    onExitHoverInteraction: (Connector) -> Unit,
) {
    Column(
        modifier = Modifier.requiredWidth(ConnectorWidth),
        horizontalAlignment = when (side) {
            ConnectorSide.Left -> Alignment.End
            ConnectorSide.Right -> Alignment.Start
        },
        verticalArrangement = Arrangement.Center,
    ) {
        connectors.forEachIndexed { index, connector ->
            when (connector.direction) {
                ConnectorDirection.Input -> InputConnectorComponent(
                    device = device,
                    connector = connector,
                    side = side,
                    onConnectorCoordinated = onConnectorCoordinated,
                    onEnterHoverInteraction = onEnterHoverInteraction,
                    onExitHoverInteraction = onExitHoverInteraction,
                )

                ConnectorDirection.Output -> OutputConnectorComponent(
                    device = device,
                    connector = connector,
                    side = side,
                    onConnectorCoordinated = onConnectorCoordinated,
                    onEnterHoverInteraction = onEnterHoverInteraction,
                    onExitHoverInteraction = onExitHoverInteraction,
                )
            }

            if (index < connectors.size - 1) {
                Spacer(modifier = Modifier.height(ConnectorSpacing))
            }
        }
    }
}
