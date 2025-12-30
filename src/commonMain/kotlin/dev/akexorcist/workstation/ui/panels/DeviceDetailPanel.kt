package dev.akexorcist.workstation.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.data.model.Device
import dev.akexorcist.workstation.data.model.DeviceCategory
import dev.akexorcist.workstation.data.model.Port
import dev.akexorcist.workstation.data.model.PortDirection

@Composable
fun DeviceDetailPanel(
    device: Device,
    isDarkTheme: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                        text = device.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                    Text(
                        text = device.model,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDarkTheme) Color.Gray else Color.DarkGray
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
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Specifications
            SpecificationsSection(
                device = device,
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ports
            Text(
                text = "Ports (${device.ports.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color.White else Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(device.ports) { port ->
                    PortListItem(
                        port = port,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(
    category: DeviceCategory,
    isDarkTheme: Boolean
) {
    val (backgroundColor, textColor) = when (category) {
        DeviceCategory.HUB -> Color(0xFF4CAF50) to Color.White
        DeviceCategory.PERIPHERAL -> Color(0xFFFF9800) to Color.White
        DeviceCategory.CENTRAL_DEVICE -> Color(0xFF2196F3) to Color.White
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
    isDarkTheme: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        device.specifications.manufacturer?.let { manufacturer ->
            SpecificationRow(
                label = "Manufacturer",
                value = manufacturer,
                isDarkTheme = isDarkTheme
            )
        }

        device.specifications.modelNumber?.let { modelNumber ->
            SpecificationRow(
                label = "Model Number",
                value = modelNumber,
                isDarkTheme = isDarkTheme
            )
        }

        if (device.specifications.technicalSpecs.isNotEmpty()) {
            device.specifications.technicalSpecs.forEach { (key, value) ->
                SpecificationRow(
                    label = key,
                    value = value,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
private fun SpecificationRow(
    label: String,
    value: String,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDarkTheme) Color.Gray else Color.DarkGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isDarkTheme) Color.White else Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PortListItem(
    port: Port,
    isDarkTheme: Boolean
) {
    val portColor = when (port.type) {
        dev.akexorcist.workstation.data.model.PortType.USB_C -> Color(0xFF2196F3)
        dev.akexorcist.workstation.data.model.PortType.USB_A_2_0,
        dev.akexorcist.workstation.data.model.PortType.USB_A_3_0,
        dev.akexorcist.workstation.data.model.PortType.USB_A_3_1,
        dev.akexorcist.workstation.data.model.PortType.USB_A_3_2 -> Color(0xFF4CAF50)
        dev.akexorcist.workstation.data.model.PortType.HDMI,
        dev.akexorcist.workstation.data.model.PortType.HDMI_2_1,
        dev.akexorcist.workstation.data.model.PortType.DISPLAY_PORT,
        dev.akexorcist.workstation.data.model.PortType.MINI_HDMI,
        dev.akexorcist.workstation.data.model.PortType.MICRO_HDMI -> Color(0xFFFF9800)
        dev.akexorcist.workstation.data.model.PortType.ETHERNET -> Color(0xFF9C27B0)
        dev.akexorcist.workstation.data.model.PortType.AUX -> Color(0xFFE91E63)
        dev.akexorcist.workstation.data.model.PortType.POWER -> Color(0xFFFFD54F)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF3C3C3C) else Color(0xFFF5F5F5)
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
                    color = if (isDarkTheme) Color.White else Color.Black
                )
                Text(
                    text = port.type.name.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkTheme) Color.Gray else Color.DarkGray
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