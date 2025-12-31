package dev.akexorcist.workstation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.data.model.Device
import dev.akexorcist.workstation.data.model.DeviceCategory
import dev.akexorcist.workstation.presentation.config.RenderingConfig
import dev.akexorcist.workstation.ui.theme.ThemeColor
import dev.akexorcist.workstation.ui.theme.WorkstationTheme

@Composable
fun DeviceNode(
    device: Device,
    screenPosition: Offset,
    screenSize: androidx.compose.ui.geometry.Size,
    isSelected: Boolean,
    isHovered: Boolean,
    isFiltered: Boolean,
    onClick: () -> Unit,
    onHoverChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val deviceColor = getDeviceColor(device.category)
    val zoom = screenSize.width / device.size.width

    val backgroundColor by animateColorAsState(
        targetValue = getDeviceBackgroundColor(deviceColor, isHovered, isSelected),
        animationSpec = tween(durationMillis = 200),
        label = "backgroundColor"
    )

    val borderColor by animateColorAsState(
        targetValue = getDeviceBorderColor(deviceColor, isHovered, isSelected),
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    val borderWidth by animateDpAsState(
        targetValue = getDeviceBorderWidth(isHovered, isSelected, screenSize).dp,
        animationSpec = tween(durationMillis = 200),
        label = "borderWidth"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        screenPosition.x.toInt(),
                        screenPosition.y.toInt()
                    )
                }
                .size(screenSize.width.dp, screenSize.height.dp)
                .scale(scale)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(RenderingConfig.defaultDeviceBorderRadius.dp)
                )
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = RoundedCornerShape(RenderingConfig.defaultDeviceBorderRadius.dp)
                )
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Enter -> {
                                    onHoverChange(true)
                                }
                                PointerEventType.Exit -> {
                                    onHoverChange(false)
                                }
                            }
                        }
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        )


    }
}

// Helper functions for styling and positioning

@Composable
private fun getDeviceColor(category: DeviceCategory): Color {
    return when (category) {
        DeviceCategory.HUB -> WorkstationTheme.themeColor.hub
        DeviceCategory.PERIPHERAL -> WorkstationTheme.themeColor.peripheral
        DeviceCategory.CENTRAL_DEVICE -> WorkstationTheme.themeColor.centralDevice
    }
}

private fun getDeviceBackgroundColor(
    deviceColor: Color,
    isHovered: Boolean,
    isSelected: Boolean
): Color {
    return when {
        isSelected -> deviceColor.copy(alpha = 0.4f)
        isHovered -> deviceColor.copy(alpha = 0.3f)
        else -> deviceColor.copy(alpha = 0.2f)
    }
}

@Composable
private fun getDeviceBorderColor(
    deviceColor: Color,
    isHovered: Boolean,
    isSelected: Boolean
): Color {
    return when {
        isSelected -> WorkstationTheme.themeColor.onPrimary
        isHovered -> deviceColor.copy(alpha = 1f)
        else -> deviceColor
    }
}

private fun getDeviceBorderWidth(
    isHovered: Boolean,
    isSelected: Boolean,
    screenSize: androidx.compose.ui.geometry.Size
): Float {
    val baseThickness = RenderingConfig.defaultDeviceBorderThickness
    return when {
        isSelected -> baseThickness * 2
        isHovered -> baseThickness * 1.5f
        else -> baseThickness
    }
}

