package dev.akexorcist.workstation.ui.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.presentation.WorkstationUiState

@Composable
fun ControlPanel(
    uiState: WorkstationUiState,
    onZoomChange: (Float) -> Unit,
    onResetZoom: () -> Unit,
    onResetPan: () -> Unit,
    onToggleTheme: () -> Unit,
    onDeselectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = if (uiState.isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFF5F5F5),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Zoom: ${(uiState.zoom * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(100.dp)
            )

            Slider(
                value = uiState.zoom,
                onValueChange = onZoomChange,
                valueRange = 0.1f..5.0f,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onResetZoom) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Zoom")
            }

            IconButton(onClick = onResetPan) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Pan")
            }

            IconButton(onClick = onToggleTheme) {
                Icon(
                    if (uiState.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme"
                )
            }

            Button(onClick = onDeselectAll) {
                Text("Deselect All")
            }
        }
    }
}