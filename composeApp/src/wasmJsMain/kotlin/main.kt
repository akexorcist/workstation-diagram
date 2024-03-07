import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "WorkstationDiagram") {
        App(
            windowSize = DpSize(
                width = 1280.dp,
                height = 720.dp,
            )
        )
    }
}
