package dev.akexorcist.workstation.editor

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import dev.akexorcist.workstation.editor.presentation.EditorViewModel
import dev.akexorcist.workstation.editor.ui.EditorScreen
import dev.akexorcist.workstation.ui.theme.WorkstationTheme

fun main() = application {
    val viewModel = remember { EditorViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Workstation Diagram Editor",
        state = WindowState(width = 1280.dp, height = 720.dp)
    ) {
        LaunchedEffect(Unit) {
            viewModel.loadLayout()
        }

        WorkstationTheme(darkTheme = uiState.isDarkTheme) {
            EditorScreen(viewModel = viewModel)
        }
    }
}

