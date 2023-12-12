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
import com.akexorcist.workstation.diagram.common.data.*
import com.akexorcist.workstation.diagram.common.ui.state.*
import com.akexorcist.workstation.diagram.common.utility.*


private val workspaceWidth = 2850.dp
private val workspaceHeight = 1350.dp
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
    val config by remember { mutableStateOf(DefaultConfig) }
    var debugConfig by remember { mutableStateOf(DefaultDebugConfig) }
    val deviceCoordinateHostState = rememberWorkstationCoordinateState()
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
                debugConfig = debugConfig,
            )
        }
        DebugPanel(
            debugConfig = debugConfig,
            onNextIndex = { debugConfig = debugConfig.copy(lineIndex = it + 1) },
            onPreviousIndex = {
                if (debugConfig.lineIndex > 0) {
                    debugConfig = debugConfig.copy(lineIndex = it - 1)
                }
            },
            onToggleShowWorkspaceArea = {
                debugConfig = debugConfig.copy(showWorkspaceArea = it)
            },
            onToggleShowDeviceArea = {
                debugConfig = debugConfig.copy(showDeviceArea = it)
            },
            onToggleShowOverlapBoundArea = {
                debugConfig = debugConfig.copy(showOverlapBoundArea = it)
            },
            onToggleShowConnectorArea = {
                debugConfig = debugConfig.copy(showConnectorArea = it)
            },
            onToggleShowAllConnectionLines = {
                debugConfig = debugConfig.copy(showAllConnectionLines = it)
            },
            onToggleLineConnectionPoint = {
                debugConfig = debugConfig.copy(showLineConnectionPoint = it)
            },
            onToggleLineOptimization = {
                debugConfig = debugConfig.copy(disableLineOptimization = it)
            },
        )
    }
}

@Composable
private fun WorkspaceContent(
    state: WorkstationCoordinateState,
    config: Config,
    debugConfig: DebugConfig,
) {
    var currentHoveredConnector: Connector? by remember { mutableStateOf(null) }
    var currentHoveredDevice: Device? by remember { mutableStateOf(null) }

    val connections: List<Connection> = state.currentWorkstationCoordinates
        .takeIf { it.areAvailable() }
        ?.let { coordinates ->
            val deviceAreas = coordinates.getSortedDeviceConnectorsByLeft()
                .mapToMinimumBound(
                    horizontalBoundDistance = config.minimumHorizontalDistanceToDevice.px(),
                    verticalBoundDistance = config.minimumVerticalDistanceToDevice.px(),
                )
            val connectors = coordinates.getSortedConnectorByBottom()
            val connectorAreas = connectors.map { it.rect }

            val recordedVerticalLine: MutableList<VerticalLine> = mutableListOf()
            connectors.run {
                if (debugConfig.showAllConnectionLines) {
                    this
                } else {
                    filterIndexed { index, _ -> index == debugConfig.lineIndex }
//                    filterIndexed { index, _ -> listOf(17, 25, 26, 27).contains(index) }
//                    filterIndexed { index, _ -> (0..12).contains(index) || (15..30).contains(index) }
                }
            }.map { connector ->
                Connection(
                    path = getConnectorPath(
                        connectionLine = connector,
                        devices = deviceAreas,
                        connectors = connectorAreas,
                        coordinates = coordinates,
                        minimumDistanceBetweenLine = config.minimumDistanceBetweenLine.px(),
                        minimumStartLineDistance = config.minimumStartLineDistance.px(),
                        recordedVerticalLine = recordedVerticalLine,
                        onRecordVerticalPath = { recordedVerticalLine += it },
                        debugConfig = debugConfig,
                    ),
                    line = connector,
                )
            }
        } ?: listOf()
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
            currentHoveredDevice = currentHoveredDevice,
            currentHoveredConnector = currentHoveredConnector,
            onDeviceClick = {
                println("onDeviceClick ${it.title}")
            },
            onEnterHoveDeviceInteraction = {
                currentHoveredDevice = it
            },
            onExitHoverDeviceInteraction = {
                currentHoveredDevice = null
            },
            onEnterHoveConnectorInteraction = {
                currentHoveredConnector = it
            },
            onExitHoverConnectorInteraction = {
                currentHoveredConnector = null
            },
        )
        ConnectionContent(
            connections = connections,
            currentHoveredDevice = currentHoveredDevice,
            currentHoveredConnector = currentHoveredConnector,
        )
        DebugContent(
            coordinates = state.currentWorkstationCoordinates,
            config = config,
            debugConfig = debugConfig,
            connections = connections,
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
