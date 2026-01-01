package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.presentation.config.InteractionConfig
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import dev.akexorcist.workstation.utils.defaultShadow

/**
 * Control panel component that displays zoom slider and action buttons.
 *
 * @param zoom Current zoom level
 * @param onZoomChange Callback when zoom level changes
 * @param onReset Callback to reset zoom and pan to default
 * @param connectionAnimationEnabled Whether connection animations are enabled
 * @param onConnectionAnimationToggle Callback to toggle connection animations
 * @param isDarkTheme Whether dark theme is enabled
 * @param onThemeToggle Callback to toggle theme
 * @param viewportConfig Configuration from workstation.json that defines zoom constraints
 * @param modifier Modifier for styling
 */
@Composable
fun ControlPanel(
    zoom: Float,
    onZoomChange: (Float) -> Unit,
    onReset: () -> Unit,
    connectionAnimationEnabled: Boolean,
    onConnectionAnimationToggle: (Boolean) -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    viewportConfig: dev.akexorcist.workstation.data.model.ViewportConfig? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(240.dp)
            .defaultShadow()
            .clip(RoundedCornerShape(12.dp))
            .background(WorkstationTheme.themeColor.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.End)
                        .offset(x = (-2).dp, y = 8.dp),
                    text = "x${(zoom * 10).toInt() / 10f} Zoom",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WorkstationTheme.themeColor.text,
                )

                Slider(
                    value = zoom,
                    onValueChange = onZoomChange,
                    valueRange = (viewportConfig?.minZoom ?: InteractionConfig.minZoom)..(viewportConfig?.maxZoom ?: InteractionConfig.maxZoom),
                    colors = SliderDefaults.colors(
                        thumbColor = WorkstationTheme.themeColor.primary,
                        activeTrackColor = WorkstationTheme.themeColor.primary.copy(alpha = 0.7f),
                        inactiveTrackColor = WorkstationTheme.themeColor.surfaceVariant
                    )
                )
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                FilledIconButton(
                    onClick = onReset,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = WorkstationTheme.themeColor.surfaceVariant,
                        contentColor = WorkstationTheme.themeColor.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Pan and Zoom",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))


                FilledIconToggleButton(
                    checked = connectionAnimationEnabled,
                    onCheckedChange = onConnectionAnimationToggle,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        containerColor = WorkstationTheme.themeColor.surfaceVariant,
                        checkedContainerColor = WorkstationTheme.themeColor.primary,
                        contentColor = WorkstationTheme.themeColor.onSurfaceVariant,
                        checkedContentColor = WorkstationTheme.themeColor.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Animation,
                        contentDescription = "Toggle Connection Animation",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))


                FilledIconToggleButton(
                    checked = isDarkTheme,
                    onCheckedChange = { onThemeToggle() },
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        containerColor = WorkstationTheme.themeColor.surfaceVariant,
                        checkedContainerColor = WorkstationTheme.themeColor.primary,
                        contentColor = WorkstationTheme.themeColor.onSurfaceVariant,
                        checkedContentColor = WorkstationTheme.themeColor.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = "Toggle Dark Theme",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
