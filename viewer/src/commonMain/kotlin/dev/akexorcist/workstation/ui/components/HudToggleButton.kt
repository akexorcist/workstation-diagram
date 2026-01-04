package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import dev.akexorcist.workstation.ui.utils.defaultShadow


@Composable
fun HudToggleButton(
    imageVector: ImageVector,
    showUiPanel: Boolean,
    onToggleUiPanelClick: () -> Unit
) {
    IconButton(
        modifier = Modifier
            .defaultShadow()
            .size(40.dp),
        enabled = showUiPanel,
        onClick = onToggleUiPanelClick,
        shape = RoundedCornerShape(8.dp),
        colors = WorkstationTheme.themeColor.iconButtonColors(),
    ) {
        Icon(
            modifier = Modifier
                .size(24.dp),
            imageVector = imageVector,
            contentDescription = "Toggle UI panel visibility",
        )
    }
}