package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.presentation.config.InteractionConfig
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import dev.akexorcist.workstation.ui.utils.defaultShadow

/**
 * Control panel component that displays zoom slider and action buttons.
 *
 * @param zoom Current zoom level
 * @param onZoomChange Callback when zoom level changes
 * @param onReset Callback to reset zoom and pan to default
 * @param connectionAnimationEnabled Whether connection animations are enabled (optional, button hidden if null)
 * @param onConnectionAnimationToggle Callback to toggle connection animations (optional, button hidden if null)
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
    connectionAnimationEnabled: Boolean? = null,
    onConnectionAnimationToggle: ((Boolean) -> Unit)? = null,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    viewportConfig: dev.akexorcist.workstation.data.model.ViewportConfig? = null,
    revisionDate: String? = null,
    hasPreviousRevision: Boolean = false,
    hasNextRevision: Boolean = false,
    onPreviousRevision: (() -> Unit)? = null,
    onNextRevision: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(240.dp)
            .defaultShadow()
            .clip(RoundedCornerShape(12.dp))
            .background(WorkstationTheme.themeColor.surface)
            .clickable(interactionSource = null, indication = null) {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (revisionDate != null) {
                RevisionNavigator(
                    date = revisionDate,
                    hasPrevious = hasPreviousRevision,
                    hasNext = hasNextRevision,
                    onPrevious = onPreviousRevision ?: {},
                    onNext = onNextRevision ?: {},
                )
            }

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

                if (connectionAnimationEnabled != null && onConnectionAnimationToggle != null) {
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
                }


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

@Composable
private fun RevisionNavigator(
    date: String,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledIconButton(
            onClick = onPrevious,
            enabled = hasPrevious,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = WorkstationTheme.themeColor.surfaceVariant,
                contentColor = WorkstationTheme.themeColor.onSurfaceVariant,
                disabledContainerColor = WorkstationTheme.themeColor.surfaceVariant.copy(alpha = 0.4f),
                disabledContentColor = WorkstationTheme.themeColor.onSurfaceVariant.copy(alpha = 0.3f),
            )
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous revision",
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = date,
            style = MaterialTheme.typography.bodySmall,
            color = WorkstationTheme.themeColor.text,
        )

        FilledIconButton(
            onClick = onNext,
            enabled = hasNext,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = WorkstationTheme.themeColor.surfaceVariant,
                contentColor = WorkstationTheme.themeColor.onSurfaceVariant,
                disabledContainerColor = WorkstationTheme.themeColor.surfaceVariant.copy(alpha = 0.4f),
                disabledContentColor = WorkstationTheme.themeColor.onSurfaceVariant.copy(alpha = 0.3f),
            )
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next revision",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
