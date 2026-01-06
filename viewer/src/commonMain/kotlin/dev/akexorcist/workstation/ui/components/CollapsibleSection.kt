package dev.akexorcist.workstation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import dev.akexorcist.workstation.ui.utils.defaultShadow
import kotlin.math.max
import kotlin.math.min

@Composable
fun CollapsibleSection(
    title: String,
    icon: ImageVector? = null,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    var contentHeight by remember { mutableStateOf(0) }

    val animationDuration = remember(contentHeight) {
        val baseDuration = 150
        val durationPerPixel = 0.5f
        val calculatedDuration = baseDuration + (contentHeight * durationPerPixel).toInt()
        max(150, min(500, calculatedDuration))
    }

    Box(
        modifier = modifier
            .defaultShadow()
            .clickable(interactionSource = null, indication = null) {}
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        if (isExpanded) RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        else RoundedCornerShape(8.dp)
                    )
                    .background(WorkstationTheme.themeColor.surface)
                    .clickable { onExpandChange(!isExpanded) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = WorkstationTheme.themeColor.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = WorkstationTheme.themeColor.onSurface
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = WorkstationTheme.themeColor.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Content section
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(animationDuration)) +
                        expandVertically(animationSpec = tween(animationDuration)),
                exit = fadeOut(animationSpec = tween(animationDuration)) +
                        shrinkVertically(animationSpec = tween(animationDuration))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .background(WorkstationTheme.themeColor.surface)
                        .padding(top = 4.dp, bottom = 12.dp)
                        .onSizeChanged { size ->
                            contentHeight = size.height
                        }
                ) {
                    content()
                }
            }
        }
    }
}