import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val state = rememberWindowState(placement = WindowPlacement.Maximized)
//    val state = rememberWindowState()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Workstation Diagram",
        state = state,
    ) {
        App(
            windowSize = state.size
        )
    }
}

@Preview
@Composable
fun AppDesktopPreview() {
    val state = rememberWindowState(placement = WindowPlacement.Maximized)
    App(
        windowSize = state.size
    )
}