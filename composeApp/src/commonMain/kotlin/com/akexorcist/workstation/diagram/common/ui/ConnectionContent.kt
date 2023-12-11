package com.akexorcist.workstation.diagram.common.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.akexorcist.workstation.diagram.common.data.Connection
import com.akexorcist.workstation.diagram.common.data.Connector
import com.akexorcist.workstation.diagram.common.data.Device
import com.akexorcist.workstation.diagram.common.theme.ConnectionLineComponentTheme

@Composable
internal fun ConnectionContent(
    connections: List<Connection>,
    currentHoveredDevice: Device?,
    currentHoveredConnector: Connector?,
) {
//    println("############ Recomposition ############")
    println("currentHoveredConnector $currentHoveredConnector")
    Box(modifier = Modifier.fillMaxSize()) {
        connections.forEach { connection ->
            ConnectionLine(
                path = connection.path,
                isActive = when {
                    currentHoveredConnector == null -> true
                    currentHoveredConnector == connection.connector.sourceConnector -> true
//                    currentHoveredConnector == connection.connector. -> true

                    else -> false
                }
            )
        }
    }
}

@Composable
private fun ConnectionLine(
    path: Path,
    isActive: Boolean,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawPath(
            path = path,
            color = when (isActive) {
                true -> ConnectionLineComponentTheme.default.activeColor
                false -> ConnectionLineComponentTheme.default.inactiveColor
            },
            style = Stroke(2.dp.toPx()),
        )
    }
}
