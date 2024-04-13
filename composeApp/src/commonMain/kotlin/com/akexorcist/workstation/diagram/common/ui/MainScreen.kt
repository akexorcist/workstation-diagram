@file:Suppress("FunctionName")

package com.akexorcist.workstation.diagram.common.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.akexorcist.workstation.diagram.common.data.*
import com.akexorcist.workstation.diagram.common.theme.WorkstationDiagramTheme
import com.akexorcist.workstation.diagram.common.ui.state.*
import com.akexorcist.workstation.diagram.common.utility.*
import kotlin.math.max

private val workspaceWidth = 2540.dp
private val workspaceHeight = 1340.dp
private const val boundOffsetRatio = 0.8f
private const val maxZoomScale = 3f
private const val minZoomScale = 0.5f

@Composable
fun MainScreen(
    darkTheme: Boolean,
    windowSize: DpSize,
    onDarkThemeToggle: (Boolean) -> Unit,
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

    WorkspaceContainer(
        screenInPx = screenInPx,
        workspaceInDp = workspaceInDp,
        workspaceInPx = workspaceInPx,
        boundOffset = boundOffset,
        darkTheme = darkTheme,
        onDarkThemeToggle = onDarkThemeToggle,
    )
//    LaunchedEffect(Unit) {
//        val fpsCounter = org.jetbrains.skiko.FPSCounter(logOnTick = true)
//        while (true) {
//            withFrameNanos {
//                fpsCounter.tick()
//            }
//        }
//    }
}

@Composable
private fun WorkspaceContainer(
    screenInPx: SizePx,
    workspaceInDp: SizeDp,
    workspaceInPx: SizePx,
    boundOffset: Offset,
    darkTheme: Boolean,
    onDarkThemeToggle: (enable: Boolean) -> Unit,
) {
    var config by remember { mutableStateOf(DefaultConfig) }
    var debugConfig by remember { mutableStateOf(DefaultDebugConfig) }
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

    val animatedOffset by animateOffsetAsState(
        targetValue = offset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryLow,
            visibilityThreshold = Offset.VisibilityThreshold,
        ),
    )

    WorkspaceContent(
        workStation = MyWorkstation,
        config = config,
        debugConfig = debugConfig,
        workspaceInDp = workspaceInDp,
        scale = scale,
        offset = animatedOffset,
        transformableState = transformableState,
        darkTheme = darkTheme,
        onAnimationToggleClick = { config = config.copy(isAnimationOn = it) },
        onRequestDeviceFocus = { offset = it },
        onDarkThemeToggle = onDarkThemeToggle,
        // Debug
        onNextIndex = { debugConfig = debugConfig.copy(lineIndex = debugConfig.lineIndex + 1) },
        onPreviousIndex = { debugConfig = debugConfig.copy(lineIndex = max(debugConfig.lineIndex - 1, 0)) },
        onToggleShowWorkspaceArea = { debugConfig = debugConfig.copy(showWorkspaceArea = it) },
        onToggleShowDeviceArea = { debugConfig = debugConfig.copy(showDeviceArea = it) },
        onToggleShowOverlapBoundArea = { debugConfig = debugConfig.copy(showOverlapBoundArea = it) },
        onToggleShowConnectorArea = { debugConfig = debugConfig.copy(showConnectorArea = it) },
        onToggleShowAllConnectionLines = { debugConfig = debugConfig.copy(showAllConnectionLines = it) },
        onToggleLineConnectionPoint = { debugConfig = debugConfig.copy(showLineConnectionPoint = it) },
        onToggleLineOptimization = { debugConfig = debugConfig.copy(disableLineOptimization = it) },
    )
}

@Composable
private fun WorkspaceContent(
    workStation: Workstation,
    config: Config,
    debugConfig: DebugConfig,
    workspaceInDp: SizeDp,
    scale: Float,
    offset: Offset,
    transformableState: TransformableState,
    darkTheme: Boolean,
    onAnimationToggleClick: (Boolean) -> Unit,
    onRequestDeviceFocus: (Offset) -> Unit,
    onDarkThemeToggle: (enable: Boolean) -> Unit,
    // Debug
    onNextIndex: (Int) -> Unit,
    onPreviousIndex: (Int) -> Unit,
    onToggleShowWorkspaceArea: (Boolean) -> Unit,
    onToggleShowDeviceArea: (Boolean) -> Unit,
    onToggleShowOverlapBoundArea: (Boolean) -> Unit,
    onToggleShowConnectorArea: (Boolean) -> Unit,
    onToggleShowAllConnectionLines: (Boolean) -> Unit,
    onToggleLineConnectionPoint: (Boolean) -> Unit,
    onToggleLineOptimization: (Boolean) -> Unit,
) {
    val deviceCoordinateHostState = rememberWorkstationCoordinateState()
    var currentHoveredConnector: Connector? by remember { mutableStateOf(null) }
    var currentHoveredDevice: Device? by remember { mutableStateOf(null) }
    var currentSelectedDevice: Device? by remember { mutableStateOf(null) }
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier.background(color = WorkstationDiagramTheme.themeColor.outerBackground)
    ) {
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
                    .background(
                        color = WorkstationDiagramTheme.themeColor.background,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .border(
                        width = 2.dp,
                        color = WorkstationDiagramTheme.themeColor.outerBorder,
                        shape = RoundedCornerShape(16.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                WorkspaceContent(
                    workStation = workStation,
                    state = deviceCoordinateHostState,
                    currentHoveredConnector = currentHoveredConnector,
                    currentHoveredDevice = currentHoveredDevice,
                    config = config,
                    debugConfig = debugConfig,
                    onDeviceClick = { currentSelectedDevice = it },
                    onEnterHoveDeviceInteraction = { currentHoveredDevice = it },
                    onExitHoverDeviceInteraction = { currentHoveredDevice = null },
                    onEnterHoveConnectorInteraction = { currentHoveredConnector = it },
                    onExitHoverConnectorInteraction = { currentHoveredConnector = null },
                )
            }
        }
        InformationContent(
            workStation = workStation,
            isAnimationOn = config.isAnimationOn,
            darkTheme = darkTheme,
            onDeviceClick = { device ->
                deviceCoordinateHostState.currentWorkstationCoordinates
                    .getAllDeviceRect()
                    .find { it.device == device.type }
                    ?.let { targetDevice ->
                        deviceCoordinateHostState
                            .currentWorkstationCoordinates
                            .workspace
                            ?.let { workspace ->
                                onRequestDeviceFocus(
                                    Offset(
                                        x = ((workspace.size.width / 2f) - targetDevice.offset.x) - ((targetDevice.size.width / 2f) - 25f),
                                        y = ((workspace.size.height / 2f) - targetDevice.offset.y) - ((targetDevice.size.height / 2f) - 25f),
                                    )
                                )
                            }
                    }
            },
            onEnterDeviceHoverInteraction = { device ->
                currentHoveredDevice = device
            },
            onExitDeviceHoverInteraction = { currentHoveredDevice = null },
            onAnimationToggleClick = onAnimationToggleClick,
            onDarkThemeToggle = onDarkThemeToggle,
            debugConfig = debugConfig,
            onNextIndex = onNextIndex,
            onPreviousIndex = onPreviousIndex,
            onToggleShowWorkspaceArea = onToggleShowWorkspaceArea,
            onToggleShowDeviceArea = onToggleShowDeviceArea,
            onToggleShowOverlapBoundArea = onToggleShowOverlapBoundArea,
            onToggleShowConnectorArea = onToggleShowConnectorArea,
            onToggleShowAllConnectionLines = onToggleShowAllConnectionLines,
            onToggleLineConnectionPoint = onToggleLineConnectionPoint,
            onToggleLineOptimization = onToggleLineOptimization,
        )
        currentSelectedDevice?.let { device ->
            SpecificationContent(
                specification = device.toDeviceSpecification(),
                onWebsiteClick = { url -> uriHandler.openUri(url) },
                onDismissRequest = { currentSelectedDevice = null },
            )
        }
    }
}

@Composable
private fun WorkspaceContent(
    workStation: Workstation,
    state: WorkstationCoordinateState,
    currentHoveredConnector: Connector?,
    currentHoveredDevice: Device?,
    config: Config,
    debugConfig: DebugConfig,
    onDeviceClick: (Device) -> Unit,
    onEnterHoveDeviceInteraction: (Device) -> Unit,
    onExitHoverDeviceInteraction: (Device) -> Unit,
    onEnterHoveConnectorInteraction: (Connector) -> Unit,
    onExitHoverConnectorInteraction: (Connector) -> Unit,
) {
    val connections: List<Connection> = state.currentWorkstationCoordinates
        .takeIf { it.areAvailable() }
        ?.let { coordinates ->
            val deviceAreas = coordinates.getSortedDevicesByLeft()
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
            .onWorkspaceCoordinated(onWorkspaceCoordinated = { state.update(it) }),
    ) {
        ConnectionContent(
            isAnimationOn = config.isAnimationOn,
            connections = connections,
            currentHoveredDevice = currentHoveredDevice,
            currentHoveredConnector = currentHoveredConnector,
        )
        DeviceContent(
            workStation = workStation,
            state = state,
            currentHoveredDevice = currentHoveredDevice,
            currentHoveredConnector = currentHoveredConnector,
            onDeviceClick = onDeviceClick,
            onEnterHoveDeviceInteraction = onEnterHoveDeviceInteraction,
            onExitHoverDeviceInteraction = onExitHoverDeviceInteraction,
            onEnterHoveConnectorInteraction = onEnterHoveConnectorInteraction,
            onExitHoverConnectorInteraction = onExitHoverConnectorInteraction,
        )
        DebugContent(
            coordinates = state.currentWorkstationCoordinates,
            config = config,
            debugConfig = debugConfig,
            connections = connections,
        )
    }
}

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
