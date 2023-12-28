@file:Suppress("FunctionName")

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import com.akexorcist.workstation.diagram.common.theme.WorkstationDiagramTheme
import com.akexorcist.workstation.diagram.common.ui.MainScreen

@Composable
fun App(
    windowSize: DpSize,
) {
    var darkTheme by remember { mutableStateOf(true) }
    WorkstationDiagramTheme(
        darkTheme = darkTheme,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MainScreen(
                darkTheme = darkTheme,
                windowSize = windowSize,
                onDarkThemeToggle = { enable -> darkTheme = enable }
            )
        }
    }
}