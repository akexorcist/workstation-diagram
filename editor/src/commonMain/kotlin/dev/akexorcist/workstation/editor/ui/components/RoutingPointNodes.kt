package dev.akexorcist.workstation.editor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.data.model.Offset as DataOffset
import dev.akexorcist.workstation.data.model.Position
import dev.akexorcist.workstation.routing.RoutedConnection
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import dev.akexorcist.workstation.utils.CoordinateTransformer

@Composable
fun RoutingPointNodes(
    layout: dev.akexorcist.workstation.data.model.WorkstationLayout,
    routedConnectionMap: Map<String, RoutedConnection>,
    canvasSize: dev.akexorcist.workstation.data.model.Size,
    zoom: Float,
    panOffset: DataOffset
) {
    val density = LocalDensity.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        layout.connections.forEach { connection ->
            val routedConnection = routedConnectionMap[connection.id] ?: return@forEach
            val virtualWaypoints = routedConnection.virtualWaypoints
            
            if (virtualWaypoints.size < 2) return@forEach
            
            for (i in 1 until virtualWaypoints.size - 1) {
                val waypoint = virtualWaypoints[i]
                val virtualPosition = Position(waypoint.first, waypoint.second)
                val screenPosition = CoordinateTransformer.transformPosition(
                    virtualPosition,
                    layout.metadata,
                    canvasSize,
                    zoom,
                    panOffset
                )
                
                val nodeSizeDp = 12.dp
                val nodeSizePx = nodeSizeDp.value * density.density
                RoutingPointNode(
                    screenPosition = Offset(screenPosition.x, screenPosition.y),
                    density = density,
                    modifier = Modifier
                        .offset(x = (screenPosition.x - nodeSizePx / 2f).dp, y = (screenPosition.y - nodeSizePx / 2f).dp)
                        .size(nodeSizeDp)
                )
            }
        }
    }
}

@Composable
private fun RoutingPointNode(
    screenPosition: Offset,
    density: androidx.compose.ui.unit.Density,
    modifier: Modifier = Modifier
) {
    val surfaceColor = WorkstationTheme.themeColor.surface
    val primaryColor = WorkstationTheme.themeColor.primary
    val borderWidthPx = 1.dp.value * density.density
    
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f
            
            drawCircle(
                color = surfaceColor,
                radius = radius + borderWidthPx,
                center = center
            )
            
            drawCircle(
                color = primaryColor,
                radius = radius,
                center = center
            )
        }
    }
}

