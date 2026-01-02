package dev.akexorcist.workstation.ui.components

import androidx.compose.animation.animateColorAsState

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.ui.unit.IntOffset

import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.data.model.Device
import dev.akexorcist.workstation.data.model.DeviceCategory
import dev.akexorcist.workstation.presentation.config.RenderingConfig

import dev.akexorcist.workstation.ui.theme.WorkstationTheme

@Composable
fun DeviceNode(
    device: Device,
    screenPosition: Offset,
    screenSize: androidx.compose.ui.geometry.Size,
    isRelatedToHoveredDevice: Boolean = true,
    onClick: () -> Unit,
    onHoverChange: (Boolean) -> Unit,
    density: Float = 1f,
    modifier: Modifier = Modifier
) {
    val deviceColor = getDeviceColor(device.category)
    val zoom = screenSize.width / device.size.width

    // Apply opacity for hover effect - only when not related to hovered device
    val opacityMultiplier = if (isRelatedToHoveredDevice) 1f else RenderingConfig.unrelatedDeviceOpacity

    val backgroundColor by animateColorAsState(
        targetValue = getDeviceBackgroundColor(deviceColor, opacityMultiplier),
        animationSpec = tween(durationMillis = 200),
        label = "backgroundColor"
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
                .size(
                    width = (screenSize.width / density).dp,
                    height = (screenSize.height / density).dp
                )
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape((RenderingConfig.defaultDeviceBorderRadius / density).dp)
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
        ) {
            if (screenSize.width >= RenderingConfig.deviceTextMinWidthToShow * zoom) {
                val paddingScaled = (RenderingConfig.deviceTextPadding * zoom / density).dp

                Column(
                    modifier = Modifier
                        .padding(start = paddingScaled, end = paddingScaled)
                        .align(Alignment.CenterStart)
                ) {
                    // Animate the text color for smooth opacity transitions
                    val textColor by animateColorAsState(
                        targetValue = WorkstationTheme.themeColor.text.copy(alpha = opacityMultiplier),
                        animationSpec = tween(durationMillis = 200),
                        label = "textColor"
                    )

                    Text(
                        text = device.label,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = MaterialTheme.typography.titleMedium.fontSize * zoom * RenderingConfig.deviceTextTitleScale / density,
                            lineHeight = MaterialTheme.typography.titleMedium.lineHeight * zoom * RenderingConfig.deviceTextLineHeightScale / density
                        ),
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height((2f * zoom / density).dp))
                    if (device.title.isNotEmpty()) {
                        Text(
                            text = device.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize * zoom * RenderingConfig.deviceTextBodyScale / density,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * zoom * RenderingConfig.deviceTextLineHeightScale / density,
                            ),
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}


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
    opacityMultiplier: Float,
): Color {
    val alpha = deviceColor.alpha * opacityMultiplier
    return deviceColor.copy(alpha = alpha)
}
