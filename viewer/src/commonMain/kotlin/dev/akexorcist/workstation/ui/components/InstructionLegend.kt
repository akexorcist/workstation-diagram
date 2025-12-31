package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.akexorcist.workstation.ui.theme.WorkstationTheme

data class LegendItem(
    val label: String,
    val color: Color
)

@Composable
fun InstructionLegend(
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val legendItems = listOf(
        LegendItem("Computer", WorkstationTheme.themeColor.centralDevice),
        LegendItem("Hub", WorkstationTheme.themeColor.hub),
        LegendItem("Accessory", WorkstationTheme.themeColor.peripheral),
        LegendItem("Output Connector", WorkstationTheme.themeColor.connection.outputActiveColor),
        LegendItem("Input Connector", WorkstationTheme.themeColor.connection.inputActiveColor)
    )

    CollapsibleSection(
        title = "Instruction",
        icon = Icons.Default.Info,
        isExpanded = isExpanded,
        onExpandChange = onExpandChange,
        modifier = modifier,
        content = {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                legendItems.forEach { item ->
                    LegendItemRow(
                        label = item.label,
                        color = item.color,
                    )
                }
            }
        }
    )
}

@Composable
private fun LegendItemRow(
    label: String,
    color: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(4.dp))
        )

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = WorkstationTheme.themeColor.onSurfaceVariant
        )
    }
}
