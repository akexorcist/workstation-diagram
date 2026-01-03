package dev.akexorcist.workstation.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.data.model.Offset
import dev.akexorcist.workstation.editor.presentation.EditorViewModel
import dev.akexorcist.workstation.editor.ui.components.EditorCanvas
import dev.akexorcist.workstation.editor.ui.components.ExportButton
import dev.akexorcist.workstation.ui.components.ControlPanel
import dev.akexorcist.workstation.utils.CoordinateTransformer
import dev.akexorcist.workstation.ui.states.ErrorState
import dev.akexorcist.workstation.ui.states.LoadingState
import dev.akexorcist.workstation.ui.theme.WorkstationTheme

@Composable
fun EditorScreen(
    viewModel: EditorViewModel = remember { EditorViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLayout()
    }

    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var hasInitialCentered by remember { mutableStateOf(false) }

    LaunchedEffect(canvasSize, uiState.layout) {
        if (!hasInitialCentered && canvasSize.width > 0 && canvasSize.height > 0 && uiState.layout != null) {
            viewModel.centerViewportOnDevices(canvasSize.width, canvasSize.height)
            hasInitialCentered = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusable()
            .background(WorkstationTheme.themeColor.background)
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
                    modifier = Modifier.fillMaxSize()
                )
            }

            uiState.errorMessage != null -> {
                ErrorState(
                    message = uiState.errorMessage ?: "Unknown error",
                    onRetry = { viewModel.loadLayout() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            uiState.layout == null -> {
                LoadingState(
                    message = "No data available",
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                EditorCanvas(
                    uiState = uiState,
                    onPanChange = viewModel::handlePanChange,
                    onHoverSegment = viewModel::setSelectedLineSegment,
                    onDragStartSegment = { connectionId, segmentIndex ->
                        viewModel.setSelectedLineSegment(connectionId, segmentIndex)
                    },
                    onDragSegment = { connectionId, segmentIndex, dragDelta, isHorizontal ->
                        val canvasSize = CoordinateTransformer.canvasSize(
                            width = canvasSize.width,
                            height = canvasSize.height
                        )
                        viewModel.updateLineSegmentEndpoints(
                            connectionId = connectionId,
                            segmentIndex = segmentIndex,
                            screenPosition = androidx.compose.ui.geometry.Offset.Zero,
                            screenDragDelta = androidx.compose.ui.geometry.Offset.Zero,
                            canvasSize = canvasSize,
                            isHorizontal = isHorizontal
                        )
                    },
                    onDragEndSegment = {
                        // Keep the segment selected after drag ends (it's still hovered/selected)
                        // Only clear if user moves away from segment
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        val shouldShowUiElements = !uiState.isLoading && uiState.errorMessage == null && uiState.layout != null

        if (shouldShowUiElements) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    ControlPanel(
                        zoom = uiState.zoom,
                        onZoomChange = { newZoom ->
                            val viewportCenterX = canvasSize.width / 2f
                            val viewportCenterY = canvasSize.height / 2f
                            val centerPoint = Offset(viewportCenterX, viewportCenterY)
                            viewModel.handleZoomChangeAtPoint(newZoom, centerPoint)
                        },
                        onReset = {
                            viewModel.resetZoom()
                            viewModel.centerViewportOnDevices(canvasSize.width, canvasSize.height)
                        },
                        connectionAnimationEnabled = uiState.connectionAnimationEnabled,
                        onConnectionAnimationToggle = { viewModel.toggleConnectionAnimation() },
                        isDarkTheme = uiState.isDarkTheme,
                        onThemeToggle = viewModel::toggleTheme,
                        viewportConfig = uiState.layout?.metadata?.viewport
                    )
                }

                Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                    ExportButton(viewModel = viewModel)
                }
            }
        }
    }
}

