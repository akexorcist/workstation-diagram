package com.akexorcist.workstation.diagram.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.akexorcist.workstation.diagram.common.data.MyWorkStation
import com.akexorcist.workstation.diagram.common.data.SizeDp
import com.akexorcist.workstation.diagram.common.data.SizePx
import com.akexorcist.workstation.diagram.common.ui.state.*
import com.akexorcist.workstation.diagram.common.utility.*


private val workspaceWidth = 2800.dp
private val workspaceHeight = 1400.dp
private const val boundOffsetRatio = 0.8f
private const val maxZoomScale = 3f
private const val minZoomScale = 0.5f

@Composable
fun MainScreen(
    windowSize: DpSize,
) {
    val screenInPx = SizePx(
        width = windowSize.width.px(),
        height = windowSize.height.px(),
    )
    val workspaceInDp = SizeDp(
        width = workspaceWidth,
        height = workspaceHeight,
    )
    val workspaceInPx = SizePx(
        width = workspaceInDp.width.px(),
        height = workspaceInDp.height.px(),
    )
    val boundOffset = Offset(
        x = screenInPx.width * boundOffsetRatio,
        y = screenInPx.height * boundOffsetRatio,
    )

    WorkspaceArea(
        screenInPx = screenInPx,
        workspaceInDp = workspaceInDp,
        workspaceInPx = workspaceInPx,
        boundOffset = boundOffset,
    )
}

@Composable
fun WorkspaceArea(
    screenInPx: SizePx,
    workspaceInDp: SizeDp,
    workspaceInPx: SizePx,
    boundOffset: Offset,
) {
    var config by remember { mutableStateOf(DefaultConfig) }
    val deviceCoordinateHostState = rememberWorkstationCoordinateHostState()
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        val newScale = scale * zoomChange
        if (newScale < maxZoomScale && newScale > minZoomScale) {
            scale = newScale
        }

        offset = getWorkspaceOffset(
            offset = offset,
            offsetChange = offsetChange,
            zoom = scale,
//            zoom = config.zoomScale,
            screenInPx = screenInPx,
            workspaceInPx = workspaceInPx,
            boundOffset = boundOffset,
        )
    }

//    Box {
    Box(modifier = Modifier.transformable(state = transformableState)) {
        Box(
            modifier = Modifier
                .requiredWidth(workspaceInDp.width)
                .requiredHeight(workspaceInDp.height)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
//                    scaleX = config.zoomScale,
//                    scaleY = config.zoomScale,
                    translationX = offset.x,
                    translationY = offset.y,
                )
                .background(Color.White)
                .border(
                    width = 2.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(16.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            WorkspaceContent(
                state = deviceCoordinateHostState,
                config = config,
            )
        }
        DebugPanel(
            config = config,
            onNextIndex = { config = config.copy(lineIndex = it + 1) },
            onPreviousIndex = {
                if (config.lineIndex > 0) {
                    config = config.copy(lineIndex = it - 1)
                }
            },
            onToggleShowWorkspaceArea = {
                config = config.copy(showWorkspaceArea = it)
            },
            onToggleShowDeviceArea = {
                config = config.copy(showDeviceArea = it)
            },
            onToggleShowOverlapBoundArea = {
                config = config.copy(showOverlapBoundArea = it)
            },
            onToggleShowConnectorArea = {
                config = config.copy(showConnectorArea = it)
            },
            onToggleShowAllConnectionLines = {
                config = config.copy(showAllConnectionLines = it)
            },
            onToggleLineConnectionPoint = {
                config = config.copy(showLineConnectionPoint = it)
            }
        )
    }
}

@Composable
private fun WorkspaceContent(
    state: WorkstationCoordinateHostState,
    config: Config,
) {
    val lineConnectionPoints = mutableStateListOf<Offset>()
    val connectionInfo = state.currentWorkstationCoordinates.let { coordinates ->
        val deviceAreas = coordinates.getSortedDeviceConnectorsByLeft()
            .mapToMinimumBound(
                horizontalBoundDistance = MinimumHorizontalDistanceToDevice.px(),
                verticalBoundDistance = MinimumVerticalDistanceToDevice.px(),
            )
        val connectors = coordinates.getSortedConnectorByBottom()
        val connectorAreas = connectors.map { it.rect }
        ConnectionInfo(
            coordinates = state.currentWorkstationCoordinates,
            deviceAreas = deviceAreas,
            connectors = connectors,
            connectorAreas = connectorAreas,
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onWorkspaceCoordinated(
                onWorkspaceCoordinated = { state.update(it) },
            ),
    ) {
        DeviceContent(
            workStation = MyWorkStation,
            state = state,
        )
        ConnectionContent(
            connectionInfo = connectionInfo,
            config = config,
            onAddDebugPoint = { lineConnectionPoints += it }
        )
        DebugContent(
            coordinates = state.currentWorkstationCoordinates,
            config = config,
            lineConnectionPoints = lineConnectionPoints,
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun MainScreenPreview() {
//    InteractiveDiagramTheme {
//        MainScreen()
//    }
//}

//@Preview(widthDp = 2000, heightDp = 1200, showBackground = true)
//@Composable
//fun WorkspaceContentPreview() {
//    val state = rememberWorkstationCoordinateHostState()
//    InteractiveDiagramTheme {
//        WorkspaceContent(
//            state = state,
//        )
//    }
//}

private fun getWorkspaceOffset(
    offset: Offset,
    offsetChange: Offset,
    zoom: Float,
    screenInPx: SizePx,
    workspaceInPx: SizePx,
    boundOffset: Offset,
): Offset {
    val boundedOffsetX = getBoundedOffset(
        screenSize = screenInPx.width,
        workspaceSize = workspaceInPx.width,
        boundOffset = boundOffset.x,
    )
    val boundedOffsetY = getBoundedOffset(
        screenSize = screenInPx.height,
        workspaceSize = workspaceInPx.height,
        boundOffset = boundOffset.y,
    )
    val x = when {
        offsetChange.x > 0 && offset.x < boundedOffsetX -> {
            // Swipe to right, Pan to left
            offset.x + offsetChange.x
        }

        offsetChange.x < 0 && offset.x > -boundedOffsetX -> {
            // Swipe to left, Pan to right
            offset.x + offsetChange.x
        }

        else -> offset.x
    }
    val y = when {
        offsetChange.y > 0 && offset.y < boundedOffsetY -> {
            // Swipe to right, Pan to left
            offset.y + offsetChange.y
        }

        offsetChange.y < 0 && offset.y > -boundedOffsetY -> {
            // Swipe to left, Pan to right
            offset.y + offsetChange.y
        }

        else -> offset.y
    }
    return Offset(x, y)
}

private fun getBoundedOffset(
    screenSize: Float,
    workspaceSize: Float,
    boundOffset: Float,
): Float = (screenSize / 2) + (workspaceSize / 2) - boundOffset
