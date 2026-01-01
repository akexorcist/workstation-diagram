package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.data.model.Device
import dev.akexorcist.workstation.data.model.DeviceCategory
import dev.akexorcist.workstation.data.model.Port
import dev.akexorcist.workstation.data.model.PortType
import dev.akexorcist.workstation.ui.theme.ThemeColor
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import dev.akexorcist.workstation.utils.defaultShadow

@Composable
fun DeviceDetailPanel(
    device: Device,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .defaultShadow()
            .clip(RoundedCornerShape(12.dp))
            .background(WorkstationTheme.themeColor.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = device.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = WorkstationTheme.themeColor.onSurface
                    )
                    Text(
                        text = device.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WorkstationTheme.themeColor.onSurfaceSecondary
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category badge
            CategoryBadge(
                category = device.category,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Specifications
            SpecificationsSection(
                device = device,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ports
            Text(
                text = "Ports (${device.ports.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = WorkstationTheme.themeColor.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(device.ports) { port ->
                    PortListItem(
                        port = port,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(
    category: DeviceCategory,
) {
    val (backgroundColor, textColor) = when (category) {
        DeviceCategory.HUB -> WorkstationTheme.themeColor.hub to ThemeColor.White
        DeviceCategory.PERIPHERAL -> WorkstationTheme.themeColor.peripheral to ThemeColor.White
        DeviceCategory.CENTRAL_DEVICE -> WorkstationTheme.themeColor.centralDevice to ThemeColor.White
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = category.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SpecificationsSection(
    device: Device,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Display specifications
        device.specifications.forEach { item ->
            DeviceDetailSpecificationRow(
                label = item.key,
                value = item.value,
            )
        }
    }
}

@Composable
private fun DeviceDetailSpecificationRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = WorkstationTheme.themeColor.onSurfaceSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = WorkstationTheme.themeColor.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PortListItem(
    port: Port,
) {
    val portColor = when (port.type) {
        PortType.USB_C -> ThemeColor.DimBlue500
        PortType.USB_A_2_0,
        PortType.USB_A_3_0,
        PortType.USB_A_3_1,
        PortType.USB_A_3_2 -> WorkstationTheme.themeColor.hub

        PortType.HDMI,
        PortType.HDMI_2_1,
        PortType.DISPLAY_PORT,
        PortType.MINI_HDMI,
        PortType.MICRO_HDMI -> WorkstationTheme.themeColor.peripheral

        PortType.ETHERNET -> ThemeColor.Purple500
        PortType.AUX -> ThemeColor.Pink500
        PortType.POWER -> ThemeColor.DimAmber500
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = WorkstationTheme.themeColor.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(portColor, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = port.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = WorkstationTheme.themeColor.onSurface
                )
                Text(
                    text = port.type.name.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = WorkstationTheme.themeColor.onSurfaceSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = portColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = port.direction.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = portColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}