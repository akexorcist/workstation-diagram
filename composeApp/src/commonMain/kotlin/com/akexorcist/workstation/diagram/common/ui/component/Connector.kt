package com.akexorcist.workstation.diagram.common.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akexorcist.workstation.diagram.common.data.*
import com.akexorcist.workstation.diagram.common.theme.ThemeColor
import com.akexorcist.workstation.diagram.common.utility.onConnectorCoordinated

private val ConnectorWidth = 110.dp
private val ConnectorSpacing = 20.dp

@Composable
internal fun InputConnectorComponent(
    device: Device.Type,
    connector: Connector,
    side: ConnectorSide,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
) {
    ConnectorComponent(
        modifier = Modifier.onConnectorCoordinated(
            device = device,
            connector = connector,
            onCoordinated = onConnectorCoordinated,
            side = side,
        ),
        label = connector.type.value,
        color = ThemeColor.Purple50,
        direction = side,
    )
}

@Composable
internal fun OutputConnectorComponent(
    device: Device.Type,
    connector: Connector,
    side: ConnectorSide,
    onConnectorCoordinated: (DeviceCoordinate.Connector) -> Unit,
) {
    ConnectorComponent(
        modifier = Modifier.onConnectorCoordinated(
            device = device,
            connector = connector,
            onCoordinated = onConnectorCoordinated,
            side = side,
        ),
        label = connector.type.value,
        color = ThemeColor.Pink50,
        direction = side,
    )
}

@Composable
private fun ConnectorComponent(
    modifier: Modifier,
    label: String,
    color: Color,
    direction: ConnectorSide,
) {
    Box(
        modifier = modifier
            .background(
                color = color,
                shape = when (direction) {
                    ConnectorSide.Left -> RoundedCornerShape(
                        topStart = 8.dp,
                        bottomStart = 8.dp,
                    )

                    ConnectorSide.Right -> RoundedCornerShape(
                        topEnd = 8.dp,
                        bottomEnd = 8.dp,
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
                )

                ConnectorDirection.Output -> OutputConnectorComponent(
                    device = device,
                    connector = connector,
                    side = side,
                    onConnectorCoordinated = onConnectorCoordinated,
                )
            }

            if (index < connectors.size - 1) {
                Spacer(modifier = Modifier.height(ConnectorSpacing))
            }
        }
    }
}
