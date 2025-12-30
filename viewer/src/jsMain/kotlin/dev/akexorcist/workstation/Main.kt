package dev.akexorcist.workstation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.akexorcist.workstation.presentation.WorkstationViewModel
import dev.akexorcist.workstation.ui.WorkstationDiagramScreen
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        val viewModel = remember { WorkstationViewModel() }

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
        androidx.compose.material3.darkColorScheme()
    } else {
        androidx.compose.material3.lightColorScheme()
    }

    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}