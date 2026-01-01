package dev.akexorcist.workstation.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.draw.alpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.data.model.Offset
import dev.akexorcist.workstation.data.model.Size
import dev.akexorcist.workstation.presentation.WorkstationViewModel
import dev.akexorcist.workstation.ui.components.DiagramCanvas
import dev.akexorcist.workstation.ui.components.ControlPanel
import dev.akexorcist.workstation.ui.components.DeviceDetailsDialog
import dev.akexorcist.workstation.ui.components.DeviceListSidebar
import dev.akexorcist.workstation.ui.components.HudToggleButton
import dev.akexorcist.workstation.ui.states.ErrorState
import dev.akexorcist.workstation.ui.states.LoadingState
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import dev.akexorcist.workstation.utils.openUrl
import kotlinx.coroutines.launch

@Composable
fun WorkstationDiagramScreen(
    viewModel: WorkstationViewModel = remember { WorkstationViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Track canvas size for zoom calculations
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    // Center viewport only on first load
    var hasInitialCentered by remember { mutableStateOf(false) }

    // UI visibility toggle state
    var showUiPanel by remember { mutableStateOf(true) }

    LaunchedEffect(canvasSize, uiState.layout) {
        if (!hasInitialCentered && canvasSize.width > 0 && canvasSize.height > 0 && uiState.layout != null) {
            viewModel.centerViewportOnDevices(canvasSize.width, canvasSize.height)
            hasInitialCentered = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .background(WorkstationTheme.themeColor.background)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when {
                        // Zoom In: Ctrl/Cmd + Plus or Equals
                        (keyEvent.isCtrlPressed || keyEvent.isMetaPressed) &&
                                (keyEvent.key == Key.Plus || keyEvent.key == Key.Equals) -> {
                            // Zoom towards viewport center (full window)
                            val viewportCenterX = canvasSize.width / 2f
                            val viewportCenterY = canvasSize.height / 2f
                            val centerPoint = dev.akexorcist.workstation.data.model.Offset(viewportCenterX, viewportCenterY)
                            val layoutCanvasSize = uiState.layout?.metadata?.canvasSize
                                ?: dev.akexorcist.workstation.data.model.Size(canvasSize.width, canvasSize.height)
                            viewModel.handleZoomChangeAtPoint(uiState.zoom + 0.1f, centerPoint, layoutCanvasSize)
                            true
                        }
                        // Zoom Out: Ctrl/Cmd + Minus
                        (keyEvent.isCtrlPressed || keyEvent.isMetaPressed) &&
                                keyEvent.key == Key.Minus -> {
                            // Zoom towards viewport center (full window)
                            val viewportCenterX = canvasSize.width / 2f
                            val viewportCenterY = canvasSize.height / 2f
                            val centerPoint = dev.akexorcist.workstation.data.model.Offset(viewportCenterX, viewportCenterY)
                            val layoutCanvasSize = uiState.layout?.metadata?.canvasSize
                                ?: dev.akexorcist.workstation.data.model.Size(canvasSize.width, canvasSize.height)
                            viewModel.handleZoomChangeAtPoint(uiState.zoom - 0.1f, centerPoint, layoutCanvasSize)
                            true
                        }
                        // Pan: Arrow keys
                        keyEvent.key == Key.DirectionUp -> {
                            viewModel.handlePanChange(
                                dev.akexorcist.workstation.data.model.Offset(
                                    x = uiState.panOffset.x,
                                    y = uiState.panOffset.y + 20f
                                )
                            )
                            true
                        }

                        keyEvent.key == Key.DirectionDown -> {
                            viewModel.handlePanChange(
                                dev.akexorcist.workstation.data.model.Offset(
                                    x = uiState.panOffset.x,
                                    y = uiState.panOffset.y - 20f
                                )
                            )
                            true
                        }

                        keyEvent.key == Key.DirectionLeft -> {
                            viewModel.handlePanChange(
                                dev.akexorcist.workstation.data.model.Offset(
                                    x = uiState.panOffset.x + 20f,
                                    y = uiState.panOffset.y
                                )
                            )
                            true
                        }

                        keyEvent.key == Key.DirectionRight -> {
                            viewModel.handlePanChange(
                                dev.akexorcist.workstation.data.model.Offset(
                                    x = uiState.panOffset.x - 20f,
                                    y = uiState.panOffset.y
                                )
                            )
                            true
                        }
                        // Escape: Deselect all
                        keyEvent.key == Key.Escape -> {
                            viewModel.deselectAll()
                            true
                        }

                        else -> false
                    }
                } else {
                    false
                }
            }
            .onGloballyPositioned { coordinates ->
                canvasSize = androidx.compose.ui.geometry.Size(
                    coordinates.size.width.toFloat(),
                    coordinates.size.height.toFloat()
                )
            }
    ) {
        when {
            uiState.isLoading -> {
                LoadingState(
                    message = "Loading workstation data...",
                    isDarkTheme = uiState.isDarkTheme,
                    modifier = Modifier.fillMaxSize()
                )
            }

            uiState.errorMessage != null -> {
                val errorMessage = uiState.errorMessage ?: "Unknown error"
                ErrorState(
                    message = errorMessage,
                    onRetry = {
                        coroutineScope.launch {
                            viewModel.loadLayout()
                        }
                    },
                    isDarkTheme = uiState.isDarkTheme,
                    modifier = Modifier.fillMaxSize()
                )
            }

            uiState.layout == null -> {
                LoadingState(
                    message = "No data available",
                    isDarkTheme = uiState.isDarkTheme,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                DiagramCanvas(
                    uiState = uiState,
                    onDeviceClick = viewModel::handleDeviceClick,
                    onConnectionClick = viewModel::handleConnectionClick,
                    onPanChange = viewModel::handlePanChange,
                    onHoverDevice = { deviceId, isHovered -> viewModel.handleDeviceHover(deviceId, isHovered) },
                    onHoverConnection = { connectionId, isHovered -> viewModel.handleConnectionHover(connectionId, isHovered) },
                    onHoverPort = { portInfo, isHovered -> viewModel.handlePortHover(portInfo, isHovered) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        val sidebarAlpha by animateFloatAsState(
            targetValue = when (showUiPanel) {
                true -> 1f
                false -> 0f
            },
        )
        val sidebarScale by animateFloatAsState(
            targetValue = when (showUiPanel) {
                true -> 1f
                false -> 1.5f
            },
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
            ),
        )

        // Only show UI panels when the layout is successfully loaded and no errors
        val shouldShowUiElements = !uiState.isLoading && uiState.errorMessage == null && uiState.layout != null
        
        if (shouldShowUiElements) {
            Box(
                modifier = Modifier
                    .alpha(sidebarAlpha)
                    .scale(sidebarScale)
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                // Floating Control Panel at top-right (overlay)
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    ControlPanel(
                        zoom = uiState.zoom,
                        onZoomChange = { newZoom ->
                            // Zoom towards viewport center (full window)
                            val viewportCenterX = canvasSize.width / 2f
                            val viewportCenterY = canvasSize.height / 2f
                            val centerPoint = Offset(viewportCenterX, viewportCenterY)
                            val layoutCanvasSize = uiState.layout?.metadata?.canvasSize
                                ?: Size(canvasSize.width, canvasSize.height)
                            viewModel.handleZoomChangeAtPoint(newZoom, centerPoint, layoutCanvasSize)
                        },
                        onReset = {
                            viewModel.resetZoom()
                            // Use actual canvas size for proper centering, same as initial load
                            viewModel.centerViewportOnDevices(canvasSize.width, canvasSize.height)
                        },
                        connectionAnimationEnabled = uiState.connectionAnimationEnabled,
                        onConnectionAnimationToggle = { viewModel.toggleConnectionAnimation() },
                        isDarkTheme = uiState.isDarkTheme,
                        onThemeToggle = viewModel::toggleTheme
                    )
                }
    
                Box(modifier = Modifier.align(Alignment.TopStart)) {
                    DeviceListSidebar(
                        uiState = uiState,
                        onDeviceClick = viewModel::handleDeviceClick,
                        onHoverDevice = { deviceId, isHovered -> viewModel.handleDeviceHover(deviceId, isHovered) },
                        onHomeClick = { openUrl("https://akexorcist.dev/") },
                        onGithubClick = { openUrl("https://github.com/akexorcist") },
                        isInstructionExpanded = uiState.isInstructionExpanded,
                        onInstructionExpandChange = { viewModel.toggleInstructionExpanded() },
                        isDeviceListExpanded = uiState.isDeviceListExpanded,
                        onDeviceListExpandChange = { viewModel.toggleDeviceListExpanded() },
                        showUiPanel = showUiPanel,
                        onToggleUiPanelClick = { showUiPanel = !showUiPanel }
                    )
                }
            }
    
            ShowUiPanelButton(
                showUiPanel = showUiPanel,
                onToggleUiPanelClick = { showUiPanel = true },
            )
        }

        // Device details dialog
        val layout = uiState.layout
        if (layout != null && uiState.selectedDeviceId != null) {
            val selectedDevice = layout.devices.find { it.id == uiState.selectedDeviceId }
            if (selectedDevice != null) {
                DeviceDetailsDialog(
                    isVisible = true,
                    device = selectedDevice,
                    onDismissRequest = { viewModel.deselectAll() }
                )
            }
        }
    }
}

@Composable
private fun ShowUiPanelButton(
    showUiPanel: Boolean,
    onToggleUiPanelClick: (Boolean) -> Unit,
) {
    AnimatedVisibility(
        visible = !showUiPanel,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                slideIn(
                    initialOffset = { IntOffset(-100, 0) },
                    animationSpec = tween(durationMillis = 500)
                ),
        exit = fadeOut(animationSpec = tween(durationMillis = 500)) +
                slideOut(
                    targetOffset = { IntOffset(-100, 0) },
                    animationSpec = tween(durationMillis = 500)
                )
    ) {
        Box(modifier = Modifier.padding(24.dp)) {
            HudToggleButton(
                imageVector = Icons.Default.ChevronRight,
                showUiPanel = !showUiPanel,
                onToggleUiPanelClick = { onToggleUiPanelClick(true) },
            )
        }
    }
}
