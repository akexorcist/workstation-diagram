package dev.akexorcist.workstation.ui.states

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LoadingState(
    message: String = "Loading workstation data...",
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = if (isDarkTheme) Color(0xFF2196F3) else Color(0xFF1976D2)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDarkTheme) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun EmptyState(
    message: String = "No workstation data available",
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "No Data",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color.Gray else Color.DarkGray
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDarkTheme) Color.Gray else Color.DarkGray
            )
        }
    }
}