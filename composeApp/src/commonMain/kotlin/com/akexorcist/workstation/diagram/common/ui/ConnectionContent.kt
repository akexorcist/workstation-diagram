package com.akexorcist.workstation.diagram.common.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.akexorcist.workstation.diagram.common.data.*
import com.akexorcist.workstation.diagram.common.theme.ConnectionLineComponentTheme
import com.akexorcist.workstation.diagram.common.utility.px

private val LineCornerRadius = 15.dp

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
                },
                isReverseDirection = connection.line.source.direction == ConnectorDirection.Input,
                lineCornerRadius = LineCornerRadius.px(),
            )
        }
    }
}

@Composable
private fun ConnectionLine(
    path: ConnectionPath,
    isActive: Boolean,
    isReverseDirection: Boolean,
    lineCornerRadius: Float,
) {
    val drawPath = path.toPath()
    val whiteBackgroundColor = Color.White
    val whiteBackgroundStrokeWidth by animateFloatAsState(
        when (isActive) {
            true -> 24.dp.px()
            false -> 16.dp.px()
        }
    )
    val backgroundColor by animateColorAsState(
        when (isActive) {
            true -> ConnectionLineComponentTheme.default.backgroundActiveColor
            false -> ConnectionLineComponentTheme.default.backgroundInactiveColor
        }
    )
    val color by animateColorAsState(
        when (isActive) {
            true -> ConnectionLineComponentTheme.default.activeColor
            false -> ConnectionLineComponentTheme.default.inactiveColor
        }
    )
    val lineBackgroundStrokeWidth by animateFloatAsState(
        when (isActive) {
            true -> 12.dp.px()
            false -> 8.dp.px()
        }
    )
    val lineStrokeWidth by animateFloatAsState(
        when (isActive) {
            true -> 6.dp.px()
            false -> 4.dp.px()
        }
    )

    val ovalPath = createOvalPath(lineStrokeWidth)

    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = if (isReverseDirection) 0f else 100f,
        targetValue = if (isReverseDirection) 100f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 5000,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart
        ),
    )
    val stampSpacing = 20.dp.px()

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(if (isActive) 1f else 0f),
    ) {
        drawPath(
            path = drawPath,
            color = whiteBackgroundColor,
            style = Stroke(
                width = whiteBackgroundStrokeWidth,
                pathEffect = PathEffect.cornerPathEffect(lineCornerRadius),
            ),
        )
        drawPath(
            path = drawPath,
            color = backgroundColor,
            style = Stroke(
                width = lineBackgroundStrokeWidth,
                pathEffect = PathEffect.cornerPathEffect(lineCornerRadius),
            ),
        )
        drawPath(
            path = drawPath,
            color = color,
            style = Stroke(
                width = lineStrokeWidth,
                pathEffect = PathEffect.chainPathEffect(
                    outer = PathEffect.stampedPathEffect(
                        shape = ovalPath,
                        style = StampedPathEffectStyle.Translate,
                        phase = phase,
                        advance = stampSpacing,
                    ),
                    inner = PathEffect.cornerPathEffect(lineCornerRadius),
                ),
            ),
        )
    }
}

private fun createOvalPath(size: Float): Path = Path().apply {
    addOval(
        Rect(
            offset = Offset(
                x = -size * 0.5f,
                y = -size * 0.5f,
            ),
            size = Size(
                width = size,
                height = size,
            ),
        )
    )
}
