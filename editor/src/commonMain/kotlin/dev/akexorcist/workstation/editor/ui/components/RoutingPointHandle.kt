package dev.akexorcist.workstation.editor.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.data.model.Offset as DataOffset
import dev.akexorcist.workstation.ui.theme.WorkstationTheme

@Composable
fun RoutingPointHandle(
    screenPosition: Offset,
    isHovered: Boolean,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDrag: (DataOffset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val handleSize = if (isDragging) 12.dp else if (isHovered) 10.dp else 8.dp
    val handleSizePx = with(density) { handleSize.toPx() }

    val alpha by animateFloatAsState(
        targetValue = if (isDragging) 1f else if (isHovered) 0.9f else 0.7f,
        animationSpec = tween(durationMillis = 150)
    )

    val handleColor = WorkstationTheme.themeColor.primary.copy(alpha = alpha)
    val borderColor = WorkstationTheme.themeColor.surface.copy(alpha = alpha)

    Box(
        modifier = modifier
            .offset(x = screenPosition.x.dp, y = screenPosition.y.dp)
            .size(handleSize)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(DataOffset(x = dragAmount.x, y = dragAmount.y))
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = handleSizePx / 2f

            val borderWidth = with(density) { 1.dp.toPx() }
            drawCircle(
                color = borderColor,
                radius = radius + borderWidth,
                center = center
            )

            drawCircle(
                color = handleColor,
                radius = radius,
                center = center
            )
        }
    }
}

