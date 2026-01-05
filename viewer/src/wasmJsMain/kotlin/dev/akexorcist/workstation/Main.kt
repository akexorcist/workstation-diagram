package dev.akexorcist.workstation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.akexorcist.workstation.presentation.WorkstationViewModel
import dev.akexorcist.workstation.ui.WorkstationDiagramScreen
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js

@OptIn(ExperimentalWasmJsInterop::class)
private val hideLoadingScreen: () -> Unit = js("() => { if (typeof hideLoadingScreen === 'function') hideLoadingScreen(); }")

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "WorkstationDiagram") {
        val viewModel = remember { WorkstationViewModel() }
        val uiState by viewModel.uiState.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.loadLayout()
            hideLoadingScreen()
        }

        WorkstationTheme(darkTheme = uiState.isDarkTheme) {
            WorkstationDiagramScreen(viewModel = viewModel)
        }
    }
}
