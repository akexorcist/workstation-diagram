@file:Suppress("FunctionName")

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.akexorcist.workstation.diagram.common.theme.WorkstationDiagramTheme
import com.akexorcist.workstation.diagram.common.ui.MainScreen

@Composable
fun App() {
    var darkTheme by remember { mutableStateOf<Boolean?>(null) }
    WorkstationDiagramTheme(
        darkTheme = darkTheme ?: isSystemInDarkTheme(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MainScreen(
                darkTheme = darkTheme ?: isSystemInDarkTheme(),
                onDarkThemeToggle = { enable -> darkTheme = enable }
            )
        }
    }
}
