package dev.akexorcist.workstation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.akexorcist.workstation.data.model.Port
import dev.akexorcist.workstation.data.model.PortDirection
import dev.akexorcist.workstation.presentation.config.RenderingConfig
import dev.akexorcist.workstation.ui.theme.WorkstationTheme

@Composable
fun CapsulePortNode(
    port: Port,
    deviceId: String,
    zoom: Float,
    clipEdge: String? = null,
    isRelatedToHoveredDevice: Boolean = true,
    isHovered: Boolean = false,
    density: Float = 1f,
    onHoverChange: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val capsuleColor = getPortDirectionColor(port.direction)
    
    val opacityMultiplier = if (isRelatedToHoveredDevice) 1f else RenderingConfig.unrelatedPortOpacity
    

    val adjustedCapsuleColor by animateColorAsState(
        targetValue = capsuleColor.copy(alpha = capsuleColor.alpha * opacityMultiplier),
        animationSpec = tween(durationMillis = 200),
        label = "capsuleColor"
    )
    

    val textColor by animateColorAsState(
        targetValue = WorkstationTheme.themeColor.text.copy(alpha = opacityMultiplier),
        animationSpec = tween(durationMillis = 200),
        label = "portTextColor"
    )
    
    val sidePaddingDp = (RenderingConfig.portCapsuleSidePadding * zoom / density).dp
    val deviceSidePaddingDp = (RenderingConfig.portCapsuleDeviceSidePadding * zoom / density).dp
    val textSizeSp = (RenderingConfig.portCapsuleFontSize * zoom / density).sp
    val capsuleHeightDp = (RenderingConfig.portCapsuleHeight * zoom / density).dp
    
    val cornerRadius = (capsuleHeightDp.value / 2).dp
    
    val shape = when(clipEdge) {
        "left" -> RoundedCornerShape(0.dp, cornerRadius, cornerRadius, 0.dp)
        "right" -> RoundedCornerShape(cornerRadius, 0.dp, 0.dp, cornerRadius)
        "top" -> RoundedCornerShape(0.dp, 0.dp, cornerRadius, cornerRadius)
        "bottom" -> RoundedCornerShape(cornerRadius, cornerRadius, 0.dp, 0.dp)
        else -> RoundedCornerShape(cornerRadius)
    }
    
    val textAlignment = when(clipEdge) {
        "right" -> TextAlign.End
        "left" -> TextAlign.Start
        else -> TextAlign.Center
    }
    
    val boxAlignment = when(clipEdge) {
        "right" -> Alignment.CenterEnd
        "left" -> Alignment.CenterStart
        else -> Alignment.Center
    }
    
    Surface(
        shape = shape,
        color = adjustedCapsuleColor,
        modifier = modifier
            .height(capsuleHeightDp)
            .clip(shape)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter -> {
                                onHoverChange("$deviceId:${port.id}", true)
                            }
                            PointerEventType.Exit -> {
                                onHoverChange("$deviceId:${port.id}", false)
                            }
                        }
                    }
                }
            }
    ) {
        Box(
            contentAlignment = boxAlignment,
            modifier = Modifier.fillMaxSize()
        ) {
            val startPadding = when(clipEdge) {
                "right" -> deviceSidePaddingDp
                "top", "bottom" -> if (boxAlignment == Alignment.CenterStart) deviceSidePaddingDp else sidePaddingDp
                else -> sidePaddingDp
            }
            val endPadding = when(clipEdge) {
                "left" -> deviceSidePaddingDp
                "top", "bottom" -> if (boxAlignment == Alignment.CenterEnd) deviceSidePaddingDp else sidePaddingDp
                else -> sidePaddingDp
            }
            
            Text(
                text = port.name,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = textSizeSp,
                textAlign = textAlignment,
                softWrap = false,
                overflow = TextOverflow.Visible,
                lineHeight = textSizeSp,
                modifier = Modifier.padding(start = startPadding, end = endPadding, top = 0.dp, bottom = 0.dp)
            )
        }
    }
}

@Composable
private fun getPortDirectionColor(direction: PortDirection): Color {
    return when (direction) {
        PortDirection.INPUT -> WorkstationTheme.themeColor.connection.inputActiveColor
        PortDirection.OUTPUT -> WorkstationTheme.themeColor.connection.outputActiveColor
        PortDirection.BIDIRECTIONAL -> WorkstationTheme.themeColor.connection.inputActiveColor.copy(alpha = 0.7f)
    }
}
