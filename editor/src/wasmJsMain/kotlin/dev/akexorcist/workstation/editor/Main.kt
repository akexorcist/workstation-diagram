package dev.akexorcist.workstation.editor

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.akexorcist.workstation.editor.presentation.EditorViewModel
import dev.akexorcist.workstation.editor.ui.EditorScreen
import dev.akexorcist.workstation.ui.theme.WorkstationTheme

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "WorkstationDiagramEditor") {
        val viewModel = remember { EditorViewModel() }
        val uiState by viewModel.uiState.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.loadLayout()
        }

        WorkstationTheme(darkTheme = uiState.isDarkTheme) {
            EditorScreen(viewModel = viewModel)
        }
    }
}

