import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "WorkstationDiagram") {
        val windowSize = LocalWindowInfo.current.containerSize
        App(
            windowSize = DpSize(
                width = windowSize.width.dp,
                height = windowSize.height.dp,
            )
        )
    }
}
