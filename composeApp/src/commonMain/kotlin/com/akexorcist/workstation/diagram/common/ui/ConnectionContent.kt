@file:Suppress("FunctionName")

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
    val spacingBackgroundColor = ConnectionLineComponentTheme.default.spacingColor
    val spacingBackgroundStrokeWidth by animateFloatAsState(
        when (isActive) {
            true -> 16.dp.px()
            false -> 12.dp.px()
        }
    )
    val inputBackgroundColor by animateColorAsState(
        when (isActive) {
            true -> ConnectionLineComponentTheme.default.inputBackgroundActiveColor
            false -> ConnectionLineComponentTheme.default.inputBackgroundInactiveColor
        }
    )
    val outputBackgroundColor by animateColorAsState(
        when (isActive) {
            true -> ConnectionLineComponentTheme.default.outputBackgroundActiveColor
            false -> ConnectionLineComponentTheme.default.outputBackgroundInactiveColor
        }
    )
    val inputColor by animateColorAsState(
        when (isActive) {
            true -> ConnectionLineComponentTheme.default.inputActiveColor
            false -> ConnectionLineComponentTheme.default.inputInactiveColor
        }
    )
    val outputColor by animateColorAsState(
        when (isActive) {
            true -> ConnectionLineComponentTheme.default.outputActiveColor
            false -> ConnectionLineComponentTheme.default.outputInactiveColor
        }
    )
    val lineBackgroundStrokeWidth by animateFloatAsState(
        when (isActive) {
            true -> 8.dp.px()
            false -> 6.dp.px()
        }
    )
    val lineStrokeWidth by animateFloatAsState(
        when (isActive) {
            true -> 4.dp.px()
            false -> 3.dp.px()
        }
    )

    val ovalPath = createOvalPath(lineStrokeWidth)

    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = if (isReverseDirection) 0.dp.px() else 20.dp.px(),
        targetValue = if (isReverseDirection) 20.dp.px() else 0.dp.px(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
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
            color = spacingBackgroundColor,
            style = Stroke(
                width = spacingBackgroundStrokeWidth,
                pathEffect = PathEffect.cornerPathEffect(lineCornerRadius),
            ),
        )
        drawPath(
            path = drawPath,
            brush = Brush.linearGradient(
                colors = when (isReverseDirection) {
                    true -> listOf(inputBackgroundColor, outputBackgroundColor)
                    false -> listOf(outputBackgroundColor, inputBackgroundColor)
                },
                start = path.lines.first().start,
                end = path.lines.last().end,
            ),
            style = Stroke(
                width = lineBackgroundStrokeWidth,
                pathEffect = PathEffect.cornerPathEffect(lineCornerRadius),
            ),
        )
        drawPath(
            path = drawPath,
            brush = Brush.linearGradient(
                colors = when (isReverseDirection) {
                    true -> listOf(inputColor, outputColor)
                    false -> listOf(outputColor, inputColor)
                },
                start = path.lines.first().start,
                end = path.lines.last().end,
            ),
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
