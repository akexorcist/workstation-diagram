package com.akexorcist.workstation.diagram.common.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.akexorcist.workstation.diagram.common.data.*
import com.akexorcist.workstation.diagram.common.theme.ConnectionLineComponentTheme

@Composable
internal fun ConnectionContent(
    connections: List<Connection>,
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
) {
//    println("############ Recomposition ############")
    Box(modifier = Modifier.fillMaxSize()) {
        connections.forEach { connection ->
            ConnectionLine(
                path = connection.path,
                isActive = when {
                    currentHoveredConnector == null &&
                            currentHoveredDevice == null
                    -> true

                    currentHoveredConnector != null &&
                            currentHoveredConnector.toConnection() == connection.line.source &&
                            currentHoveredConnector.target == connection.line.target?.owner
                    -> true

                    currentHoveredConnector != null &&
                            currentHoveredConnector.toConnection() == connection.line.target &&
                            currentHoveredConnector.target == connection.line.source.owner
                    -> true

                    currentHoveredDevice != null &&
                            currentHoveredDevice.type == connection.line.source.owner
                    -> true

                    currentHoveredDevice != null &&
                            currentHoveredDevice.type == connection.line.target?.owner
                    -> true

                    else -> false
                }
            )
        }
    }
}

@Composable
private fun ConnectionLine(
    path: ConnectionPath,
    isActive: Boolean,
) {
    val color by animateColorAsState(
        when (isActive) {
            true -> ConnectionLineComponentTheme.default.activeColor
            false -> ConnectionLineComponentTheme.default.inactiveColor
        }
    )
    Canvas(
        modifier = Modifier.fillMaxSize()
            .zIndex(if (isActive) 1f else 0f),
    ) {
        drawPath(
            path = path.toRoundedCornerPath(15.dp.toPx()),
            color = Color.White,
            style = Stroke(12.dp.toPx()),
        )
        drawPath(
            path = path.toRoundedCornerPath(15.dp.toPx()),
            color = color,
            style = Stroke(3.dp.toPx()),
        )
    }
}
