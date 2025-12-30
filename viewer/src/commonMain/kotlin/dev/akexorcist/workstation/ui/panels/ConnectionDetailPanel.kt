package dev.akexorcist.workstation.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.data.model.Connection
import dev.akexorcist.workstation.data.model.ConnectionCategory

@Composable
fun ConnectionDetailPanel(
    connection: Connection,
    sourceDeviceName: String,
    targetDeviceName: String,
    isDarkTheme: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
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
                Text(
                    text = "Connection Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color.Black
                )

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Connection type badge
            ConnectionTypeBadge(
                connection = connection,
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Connection path
            ConnectionPathSection(
                sourceDeviceName = sourceDeviceName,
                targetDeviceName = targetDeviceName,
                connection = connection,
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cable specification
            CableSpecificationSection(
                connection = connection,
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
private fun ConnectionTypeBadge(
    connection: Connection,
    isDarkTheme: Boolean
) {
    val categoryColor = when (connection.connectionType.category) {
        ConnectionCategory.DATA -> Color(0xFF2196F3)
        ConnectionCategory.VIDEO -> Color(0xFFBA68C8)
        ConnectionCategory.AUDIO -> Color(0xFF81C784)
        ConnectionCategory.POWER -> Color(0xFFFFD54F)
        ConnectionCategory.NETWORK -> Color(0xFF4DB6AC)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = categoryColor
    ) {
        Text(
            text = connection.connectionType.name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ConnectionPathSection(
    sourceDeviceName: String,
    targetDeviceName: String,
    connection: Connection,
    isDarkTheme: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Connection Path",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color.White else Color.Black
        )

        // Source device
        ConnectionPathItem(
            label = "Source",
            deviceName = sourceDeviceName,
            portName = connection.sourcePortId,
            isDarkTheme = isDarkTheme
        )

        // Arrow
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = if (isDarkTheme) Color.Gray else Color.DarkGray,
                modifier = Modifier.size(24.dp)
            )
        }

        // Target device
        ConnectionPathItem(
            label = "Target",
            deviceName = targetDeviceName,
            portName = connection.targetPortId,
            isDarkTheme = isDarkTheme
        )
    }
}

@Composable
private fun ConnectionPathItem(
    label: String,
    deviceName: String,
    portName: String,
    isDarkTheme: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF3C3C3C) else Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDarkTheme) Color.Gray else Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = deviceName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isDarkTheme) Color.White else Color.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Port: $portName",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkTheme) Color.Gray else Color.DarkGray
            )
        }
    }
}

@Composable
private fun CableSpecificationSection(
    connection: Connection,
    isDarkTheme: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Cable Specification",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color.White else Color.Black
        )

        connection.cableSpecification?.let { cable ->
            cable.length?.let { length ->
                SpecificationRow(
                    label = "Length",
                    value = length,
                    isDarkTheme = isDarkTheme
                )
            }

            cable.brand?.let { brand ->
                SpecificationRow(
                    label = "Brand",
                    value = brand,
                    isDarkTheme = isDarkTheme
                )
            }
        } ?: run {
            SpecificationRow(
                label = "Cable",
                value = "Not specified",
                isDarkTheme = isDarkTheme
            )
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