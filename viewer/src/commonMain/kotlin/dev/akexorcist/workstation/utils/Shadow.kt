package dev.akexorcist.workstation.utils

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.ui.theme.WorkstationTheme

@Composable
fun Modifier.defaultShadow() = this.dropShadow(
    shape = RoundedCornerShape(12.dp),
    shadow = Shadow(
        radius = 2.dp,
        spread = 1.dp,
        color = WorkstationTheme.themeColor.shadow,
        offset = DpOffset(x = 0.dp, y = 2.dp)
    )
)
