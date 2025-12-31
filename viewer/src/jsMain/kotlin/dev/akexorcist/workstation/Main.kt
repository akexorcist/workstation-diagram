package dev.akexorcist.workstation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.akexorcist.workstation.presentation.WorkstationViewModel
import dev.akexorcist.workstation.ui.WorkstationDiagramScreen
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        val viewModel = remember { WorkstationViewModel() }
        val uiState by viewModel.uiState.collectAsState()
        
        LaunchedEffect(Unit) {
            viewModel.loadLayout()
        }

        WorkstationTheme(darkTheme = uiState.isDarkTheme) {
            WorkstationDiagramScreen(viewModel = viewModel)
        }
    }
}
