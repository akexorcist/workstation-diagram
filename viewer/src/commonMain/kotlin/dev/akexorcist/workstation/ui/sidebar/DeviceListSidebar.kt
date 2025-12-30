package dev.akexorcist.workstation.ui.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.data.model.DeviceCategory
import dev.akexorcist.workstation.presentation.WorkstationUiState

@Composable
fun DeviceListSidebar(
    uiState: WorkstationUiState,
    onDeviceClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(if (uiState.isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Text(
            text = "Devices",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search devices...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        uiState.layout?.let { layout ->
            val devices = if (uiState.searchQuery.isBlank()) {
                layout.devices
            } else {
                layout.devices.filter { device ->
                    device.name.contains(uiState.searchQuery, ignoreCase = true) ||
                    device.model.contains(uiState.searchQuery, ignoreCase = true)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(devices) { device ->
                    DeviceListItem(
                        device = device,
                        isSelected = device.id == uiState.selectedDeviceId,
                        isDarkTheme = uiState.isDarkTheme,
                        onClick = { onDeviceClick(device.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceListItem(
    device: dev.akexorcist.workstation.data.model.Device,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        if (isDarkTheme) Color(0xFF2196F3) else Color(0xFFBBDEFB)
    } else {
        if (isDarkTheme) Color(0xFF2C2C2C) else Color.White
    }

    val categoryColor = when (device.category) {
        DeviceCategory.HUB -> Color(0xFF4CAF50)
        DeviceCategory.PERIPHERAL -> Color(0xFFFF9800)
        DeviceCategory.CENTRAL_DEVICE -> Color(0xFF2196F3)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(categoryColor, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
                Text(
                    text = device.model,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkTheme) Color.Gray else Color.DarkGray
                )
            }

            Text(
                text = "${device.ports.size} ports",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkTheme) Color.Gray else Color.DarkGray
            )
        }
    }
}