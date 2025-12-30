package dev.akexorcist.workstation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.presentation.WorkstationViewModel
import dev.akexorcist.workstation.presentation.WorkstationUiState
import dev.akexorcist.workstation.ui.components.DiagramCanvas
import dev.akexorcist.workstation.ui.controls.ControlPanel
import dev.akexorcist.workstation.ui.panels.ConnectionDetailPanel
import dev.akexorcist.workstation.ui.panels.DeviceDetailPanel
import dev.akexorcist.workstation.ui.sidebar.DeviceListSidebar
import dev.akexorcist.workstation.ui.states.ErrorState
import dev.akexorcist.workstation.ui.states.LoadingState
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when {
                        // Zoom In: Ctrl/Cmd + Plus or Equals
                        (keyEvent.isCtrlPressed || keyEvent.isMetaPressed) && 
                        (keyEvent.key == Key.Plus || keyEvent.key == Key.Equals) -> {
                            // Zoom towards viewport center (accounting for sidebar and control panel)
                            val viewportCenterX = (canvasSize.width - 300f) / 2f + 300f  // Sidebar is 300px
                            val viewportCenterY = (canvasSize.height - 60f) / 2f + 60f   // Control panel is 60px
                            val centerPoint = dev.akexorcist.workstation.data.model.Offset(viewportCenterX, viewportCenterY)
                            val layoutCanvasSize = uiState.layout?.metadata?.canvasSize 
                                ?: dev.akexorcist.workstation.data.model.Size(canvasSize.width, canvasSize.height)
                            viewModel.handleZoomChangeAtPoint(uiState.zoom + 0.1f, centerPoint, layoutCanvasSize)
                            true
                        }
                        // Zoom Out: Ctrl/Cmd + Minus
                        (keyEvent.isCtrlPressed || keyEvent.isMetaPressed) && 
                        keyEvent.key == Key.Minus -> {
                            // Zoom towards viewport center
                            val viewportCenterX = (canvasSize.width - 300f) / 2f + 300f
                            val viewportCenterY = (canvasSize.height - 60f) / 2f + 60f
                            val centerPoint = dev.akexorcist.workstation.data.model.Offset(viewportCenterX, viewportCenterY)
                            val layoutCanvasSize = uiState.layout?.metadata?.canvasSize 
                                ?: dev.akexorcist.workstation.data.model.Size(canvasSize.width, canvasSize.height)
                            viewModel.handleZoomChangeAtPoint(uiState.zoom - 0.1f, centerPoint, layoutCanvasSize)
                            true
                        }
                        // Reset: R key
                        keyEvent.key == Key.R -> {
                            viewModel.resetZoom()
                            viewModel.resetPan()
                            true
                        }
                        // Pan with arrow keys
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
        // Canvas layer (full screen, behind everything)
        when {
            uiState.isLoading -> {
                LoadingState(
                    message = "Loading workstation data...",
                    isDarkTheme = uiState.isDarkTheme
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
                    isDarkTheme = uiState.isDarkTheme
                )
            }
            uiState.layout == null -> {
                LoadingState(
                    message = "No data available",
                    isDarkTheme = uiState.isDarkTheme
                )
            }
            else -> {
                DiagramCanvas(
                    uiState = uiState,
                    onDeviceClick = viewModel::handleDeviceClick,
                    onConnectionClick = viewModel::handleConnectionClick,
                    onPanChange = viewModel::handlePanChange,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // UI overlay layer (on top)
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            DeviceListSidebar(
                uiState = uiState,
                onDeviceClick = viewModel::handleDeviceClick,
                onSearchQueryChange = viewModel::searchDevices,
                modifier = Modifier.width(300.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                ControlPanel(
                    uiState = uiState,
                    onZoomChange = { newZoom ->
                        // Zoom towards viewport center
                        val viewportCenterX = (canvasSize.width - 300f) / 2f + 300f
                        val viewportCenterY = (canvasSize.height - 60f) / 2f + 60f
                        val centerPoint = dev.akexorcist.workstation.data.model.Offset(viewportCenterX, viewportCenterY)
                        val layoutCanvasSize = uiState.layout?.metadata?.canvasSize 
                            ?: dev.akexorcist.workstation.data.model.Size(canvasSize.width, canvasSize.height)
                        viewModel.handleZoomChangeAtPoint(newZoom, centerPoint, layoutCanvasSize)
                    },
                    onResetZoom = viewModel::resetZoom,
                    onResetPan = viewModel::resetPan,
                    onToggleTheme = viewModel::toggleTheme,
                    onDeselectAll = viewModel::deselectAll,
                    modifier = Modifier.height(60.dp)
                )
                
                // Empty spacer to keep layout but let canvas show through
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    // Device detail panel
    val layout = uiState.layout
    if (uiState.selectedDeviceId != null && layout != null) {
        val selectedDevice = layout.devices.find { it.id == uiState.selectedDeviceId }
        if (selectedDevice != null) {
            DeviceDetailPanel(
                device = selectedDevice,
                isDarkTheme = uiState.isDarkTheme,
                onClose = { viewModel.deselectAll() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }

    // Connection detail panel
    if (uiState.selectedConnectionId != null && layout != null) {
        val selectedConnection = layout.connections.find { it.id == uiState.selectedConnectionId }
        if (selectedConnection != null) {
            val sourceDevice = layout.devices.find { it.id == selectedConnection.sourceDeviceId }
            val targetDevice = layout.devices.find { it.id == selectedConnection.targetDeviceId }

            if (sourceDevice != null && targetDevice != null) {
                ConnectionDetailPanel(
                    connection = selectedConnection,
                    sourceDeviceName = sourceDevice.name,
                    targetDeviceName = targetDevice.name,
                    isDarkTheme = uiState.isDarkTheme,
                    onClose = { viewModel.deselectAll() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}