package dev.akexorcist.workstation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.WindowState
import dev.akexorcist.workstation.presentation.WorkstationViewModel
import dev.akexorcist.workstation.ui.WorkstationDiagramScreen

fun main() = application {
    val viewModel = remember { WorkstationViewModel() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Workstation Diagram",
        state = WindowState(width = 1280.dp, height = 720.dp)
    ) {
        WorkstationTheme(viewModel = viewModel) {
            WorkstationDiagramScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun WorkstationTheme(
    viewModel: WorkstationViewModel,
    content: @Composable () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLayout()
    }

    val colorScheme = if (uiState.isDarkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}