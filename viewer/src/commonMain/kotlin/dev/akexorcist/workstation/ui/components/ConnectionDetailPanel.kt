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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
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
import dev.akexorcist.workstation.data.model.Connection
import dev.akexorcist.workstation.data.model.ConnectionCategory
import dev.akexorcist.workstation.ui.theme.ThemeColor
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import dev.akexorcist.workstation.utils.defaultShadow

@Composable
fun ConnectionDetailPanel(
    connection: Connection,
    sourceDeviceName: String,
    targetDeviceName: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
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
                Text(
                    text = "Connection Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = WorkstationTheme.themeColor.onSurface
                )

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Connection type badge
            ConnectionTypeBadge(
                connection = connection,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Connection path
            ConnectionPathSection(
                sourceDeviceName = sourceDeviceName,
                targetDeviceName = targetDeviceName,
                connection = connection,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cable specification
            CableSpecificationSection(
                connection = connection,
            )
        }
    }
}

@Composable
private fun ConnectionTypeBadge(
    connection: Connection,
) {
    val categoryColor = when (connection.connectionType.category) {
        ConnectionCategory.DATA -> ThemeColor.DimBlue500
        ConnectionCategory.VIDEO -> ThemeColor.DimPink500
        ConnectionCategory.AUDIO -> ThemeColor.DimTeal500
        ConnectionCategory.POWER -> ThemeColor.DimAmber500
        ConnectionCategory.NETWORK -> ThemeColor.DimIndigo500
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = categoryColor
    ) {
        Text(
            text = connection.connectionType.name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = ThemeColor.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ConnectionPathSection(
    sourceDeviceName: String,
    targetDeviceName: String,
    connection: Connection,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Connection Path",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
                    color = WorkstationTheme.themeColor.onSurface
        )

        // Source device
        ConnectionPathItem(
            label = "Source",
            deviceName = sourceDeviceName,
            portName = connection.sourcePortId,
        )

        // Arrow
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = WorkstationTheme.themeColor.onSurfaceSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Target device
        ConnectionPathItem(
            label = "Target",
            deviceName = targetDeviceName,
            portName = connection.targetPortId,
        )
    }
}

@Composable
private fun ConnectionPathItem(
    label: String,
    deviceName: String,
    portName: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(WorkstationTheme.themeColor.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                    color = WorkstationTheme.themeColor.onSurfaceSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = deviceName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                    color = WorkstationTheme.themeColor.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Port: $portName",
                style = MaterialTheme.typography.bodySmall,
                    color = WorkstationTheme.themeColor.onSurfaceSecondary
            )
        }
    }
}

@Composable
private fun CableSpecificationSection(
    connection: Connection,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Cable Specification",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
                    color = WorkstationTheme.themeColor.onSurface
        )

        connection.cableSpecification?.let { cable ->
            cable.length?.let { length ->
                SpecificationRow(
                    label = "Length",
                    value = length,
                )
            }

            cable.brand?.let { brand ->
                SpecificationRow(
                    label = "Brand",
                    value = brand,
                )
            }
        } ?: run {
            SpecificationRow(
                label = "Cable",
                value = "Not specified",
            )
        }
    }
}

@Composable
private fun SpecificationRow(
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