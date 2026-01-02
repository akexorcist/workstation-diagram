package dev.akexorcist.workstation.ui.states

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.ui.theme.ThemeColor
import dev.akexorcist.workstation.ui.theme.WorkstationTheme

@Composable
fun LoadingState(
    message: String = "Loading workstation data...",
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
                color = WorkstationTheme.themeColor.primary
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = WorkstationTheme.themeColor.text
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
                color = WorkstationTheme.themeColor.onSurfaceSecondary
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = WorkstationTheme.themeColor.onSurfaceSecondary
            )
        }
    }
}