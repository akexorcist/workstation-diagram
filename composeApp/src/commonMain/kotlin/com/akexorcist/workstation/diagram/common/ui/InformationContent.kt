@file:Suppress("FunctionName")
package com.akexorcist.workstation.diagram.common.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akexorcist.workstation.diagram.common.data.DebugConfig
import com.akexorcist.workstation.diagram.common.data.Device
import com.akexorcist.workstation.diagram.common.data.Workstation
import com.akexorcist.workstation.diagram.common.data.getAllDevices
import com.akexorcist.workstation.diagram.common.theme.*
import com.akexorcist.workstation.diagram.common.utility.informationBackground
import com.akexorcist.workstation.diagram.resources.Res
import com.akexorcist.workstation.diagram.resources.ic_github
import com.akexorcist.workstation.diagram.resources.ic_more_info
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.pow

@Composable
fun InformationContent(
    workStation: Workstation,
    isAnimationOn: Boolean,
    darkTheme: Boolean,
    showUiPanel: Boolean,
    scale: Float,
    onDeviceClick: (Device) -> Unit,
    onDeviceInfoClick: (Device) -> Unit,
    onEnterDeviceHoverInteraction: (Device) -> Unit,
    onExitDeviceHoverInteraction: (Device) -> Unit,
    onAnimationToggleClick: (Boolean) -> Unit,
    onDarkThemeToggle: (Boolean) -> Unit,
    onZoomChanged: (Float) -> Unit,
    onToggleUiPanelClick: (Boolean) -> Unit,
    // Debug
    debugConfig: DebugConfig,
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
    val animatedUiPanelAlpha by animateFloatAsState(
        targetValue = when (showUiPanel) {
            true -> 1f
            false -> 0f
        },
    )
    val animatedUiPanelScale by animateFloatAsState(
        targetValue = when (showUiPanel) {
            true -> 1f
            false -> 1.5f
        },
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
        ),
    )
    val animatedShowUiPanelOffsetX by animateDpAsState(
        targetValue = when (showUiPanel) {
            true -> (-32).dp
            false -> 0.dp
        },
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
        ),
    )
    Box {
        if (animatedUiPanelAlpha != 1f) {
            Box(
                modifier = Modifier
                    .padding(32.dp)
                    .offset(x = animatedShowUiPanelOffsetX)
                    .alpha(1 - animatedUiPanelAlpha)
            ) {
                ToggleHudVisibilityButton(
                    icon = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    onToggleUiPanelClick = { onToggleUiPanelClick(true) },
                )
            }
        }
        if (animatedUiPanelAlpha != 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(animatedUiPanelAlpha)
                    .scale(animatedUiPanelScale),
            ) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Row {
                        Title()
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            ToggleHudVisibilityButton(
                                icon = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                                onToggleUiPanelClick = { onToggleUiPanelClick(false) },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Instruction()
                    Spacer(modifier = Modifier.height(16.dp))
                    DeviceList(
                        devices = workStation.getAllDevices(),
                        onDeviceClick = onDeviceClick,
                        onDeviceInfoClick = onDeviceInfoClick,
                        onEnterHoverInteraction = onEnterDeviceHoverInteraction,
                        onExitHoverInteraction = onExitDeviceHoverInteraction,
                    )
                }
                Column(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalAlignment = Alignment.End,
                ) {
                    SettingMenu(
                        isAnimationOn = isAnimationOn,
                        onAnimationToggleClick = onAnimationToggleClick,
                        darkTheme = darkTheme,
                        onDarkThemeToggle = onDarkThemeToggle,
                        zoom = scale,
                        onZoomChanged = onZoomChanged,
                    )
                    if (debugConfig.visible) {
                        Spacer(modifier = Modifier.height(16.dp))
                        DebugPanel(
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
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingMenu(
    isAnimationOn: Boolean,
    onAnimationToggleClick: (Boolean) -> Unit,
    darkTheme: Boolean,
    onDarkThemeToggle: (Boolean) -> Unit,
    zoom: Float,
    onZoomChanged: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(
                top = 32.dp,
                end = 32.dp,
            )
            .informationBackground()
            .width(300.dp)
            .padding(
                horizontal = 24.dp,
                vertical = 16.dp,
            ),
        horizontalAlignment = Alignment.End,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        SliderSettingMenu(
            label = "x${zoom.display()} Zoom",
            value = zoom,
            valueRange = 0.75f..1.5f,
            onZoomChanged = onZoomChanged,
        )
        ToggleSettingMenu(
            label = "Connection Animation",
            enable = isAnimationOn,
            onSettingToggle = onAnimationToggleClick,
        )
        ToggleSettingMenu(
            label = "Dark Theme",
            enable = darkTheme,
            onSettingToggle = onDarkThemeToggle,
        )
    }
}

@Composable
private fun Title() {
    Column(
        modifier = Modifier
            .informationBackground()
            .padding(
                horizontal = 32.dp,
                vertical = 24.dp,
            )
    ) {
        Text(
            text = "Akexorcist's Workstation",
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            color = WorkstationDiagramTheme.themeColor.text,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Feb 2025",
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            color = WorkstationDiagramTheme.themeColor.text,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            LinkButton(
                url = "https://akexorcist.dev",
                icon = ImageData.Image(Icons.Default.Home),
                description = "Go to home page",
            )
            Spacer(modifier = Modifier.width(8.dp))
            LinkButton(
                url = "https://github.com/akexorcist",
                icon = ImageData.Resource(Res.drawable.ic_github),
                description = "Go to home page",
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Powered by",
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            color = WorkstationDiagramTheme.themeColor.text,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Kotlin Multiplatform & Compose Multiplatform",
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            color = WorkstationDiagramTheme.themeColor.text,
        )
    }
}

@Composable
private fun ToggleHudVisibilityButton(
    icon: ImageVector,
    onToggleUiPanelClick: () -> Unit,
) {
    IconButton(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
            )
            .size(40.dp),
        colors = WorkstationDiagramTheme.themeColor.uiIconButtonColors(),
        onClick = onToggleUiPanelClick,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = "Toggle UI panel visibility",
        )
    }
}

@Composable
private fun LinkButton(
    url: String,
    icon: ImageData,
    description: String,
) {
    val uriHandler = LocalUriHandler.current
    OutlinedButton(
        modifier = Modifier.size(32.dp),
        onClick = { uriHandler.openUri(url) },
        shape = RoundedCornerShape(8.dp),
        colors = WorkstationDiagramTheme.themeColor.outlinedButtonColors(),
        contentPadding = PaddingValues(2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (icon) {
                is ImageData.Image -> {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = icon.image,
                        contentDescription = description,
                        tint = WorkstationDiagramTheme.themeColor.text,
                    )
                }

                is ImageData.Resource -> {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(icon.id),
                        contentDescription = description,
                        tint = WorkstationDiagramTheme.themeColor.text,
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceList(
    devices: List<Device>,
    onDeviceClick: (Device) -> Unit,
    onDeviceInfoClick: (Device) -> Unit,
    onEnterHoverInteraction: (Device) -> Unit,
    onExitHoverInteraction: (Device) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(true) }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .width(320.dp)
            .informationBackground()
            .padding(
                start = 32.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp,
            )
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    coroutineScope.launch {
                        lazyListState.scrollBy(-delta)
                    }
                },
            )
    ) {
        CollapsibleHeader(
            label = "Device List",
            icon = Icons.AutoMirrored.Outlined.List,
            isExpanded = isExpanded,
            onToggleClick = { isExpanded = !isExpanded },
        )

        AnimatedVisibility(visible = isExpanded) {
            Column {
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = lazyListState,
                    ) {
                        itemsIndexed(
                            items = devices,
                            key = { _, device -> device.type.name },
                        ) { _, device ->
                            DeviceItem(
                                device = device,
                                onDeviceClick = onDeviceClick,
                                onDeviceInfoClick = onDeviceInfoClick,
                                onEnterHoverInteraction = onEnterHoverInteraction,
                                onExitHoverInteraction = onExitHoverInteraction,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DeviceItem(
    device: Device,
    onDeviceClick: (Device) -> Unit,
    onDeviceInfoClick: (Device) -> Unit,
    onEnterHoverInteraction: (Device) -> Unit,
    onExitHoverInteraction: (Device) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isHovered by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = when (isHovered) {
            true -> WorkstationDiagramTheme.themeColor.hoveredBackground
            false -> WorkstationDiagramTheme.themeColor.transparentBackground
        }
    )

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is HoverInteraction.Enter -> {
                    onEnterHoverInteraction(device)
                    isHovered = true
                }

                is HoverInteraction.Exit -> {
                    onExitHoverInteraction(device)
                    isHovered = false
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    color = WorkstationDiagramTheme.themeColor.selectedBackground,
                ),
                onClick = { onDeviceClick(device) },
            )
            .fillMaxWidth()
            .background(color = backgroundColor)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(
            modifier = Modifier
                .width(4.dp)
                .height(26.dp)
                .background(
                    color = when {
                        device.type.isComputer() -> WorkstationDiagramTheme.themeColor.computer
                        device.type.isHub() -> WorkstationDiagramTheme.themeColor.hub
                        device.type.isAccessory() -> WorkstationDiagramTheme.themeColor.accessory
                        else -> ThemeColor.Gray200
                    },
                    shape = RoundedCornerShape(2.dp),
                ),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = device.title,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                fontWeight = FontWeight.Medium,
                color = WorkstationDiagramTheme.themeColor.text,
            )
            device.subtitle?.let {
                Text(
                    text = it,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    color = WorkstationDiagramTheme.themeColor.text,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            modifier = Modifier.size(32.dp),
            onClick = { onDeviceInfoClick(device) },
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(Res.drawable.ic_more_info),
                contentDescription = "${device.subtitle} information",
                tint = WorkstationDiagramTheme.themeColor.text,
            )
        }
    }
}

@Composable
private fun Instruction() {
    var isExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .width(260.dp)
            .informationBackground()
            .padding(
                start = 32.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp,
            )
    ) {
        CollapsibleHeader(
            label = "Instruction",
            icon = Icons.Outlined.Info,
            isExpanded = isExpanded,
            onToggleClick = { isExpanded = !isExpanded },
        )
        AnimatedVisibility(
            visible = isExpanded,
        ) {
            Column {
                Spacer(modifier = Modifier.height(4.dp))
                DeviceInstruction(
                    color = WorkstationDiagramTheme.themeColor.computer,
                    label = "Computer"
                )
                Spacer(modifier = Modifier.height(8.dp))
                DeviceInstruction(
                    color = WorkstationDiagramTheme.themeColor.hub,
                    label = "Hub"
                )
                Spacer(modifier = Modifier.height(8.dp))
                DeviceInstruction(
                    color = WorkstationDiagramTheme.themeColor.accessory,
                    label = "Accessory"
                )
                Spacer(modifier = Modifier.height(8.dp))
                DeviceInstruction(
                    color = WorkstationDiagramTheme.themeColor.output,
                    label = "Output Connector"
                )
                Spacer(modifier = Modifier.height(8.dp))
                DeviceInstruction(
                    color = WorkstationDiagramTheme.themeColor.input,
                    label = "Input Connector"
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DeviceInstruction(
    color: Color,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(
            modifier = Modifier
                .size(width = 20.dp, height = 12.dp)
                .background(color = color, shape = RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            color = WorkstationDiagramTheme.themeColor.text,
        )
    }
}

@Composable
private fun CollapsibleHeader(
    label: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggleClick: () -> Unit,
) {
    val rotate by animateFloatAsState(
        targetValue = when (isExpanded) {
            true -> 0f
            false -> 180f
        }
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = icon,
            contentDescription = label,
            tint = WorkstationDiagramTheme.themeColor.text,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = label,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            color = WorkstationDiagramTheme.themeColor.text,
            fontWeight = FontWeight.Bold,
        )
        IconButton(onClick = onToggleClick) {
            Icon(
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotate),
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = when (isExpanded) {
                    true -> "Collapse"
                    false -> "Expand"
                },
                tint = WorkstationDiagramTheme.themeColor.text,
            )
        }
    }
}

@Composable
private fun ToggleSettingMenu(
    label: String,
    enable: Boolean,
    onSettingToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            color = WorkstationDiagramTheme.themeColor.text,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Switch(
            modifier = Modifier.scale(0.75f),
            checked = enable,
            onCheckedChange = { onSettingToggle(!enable) }
        )
    }
}

@Composable
private fun SliderSettingMenu(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onZoomChanged: (Float) -> Unit,
) {
    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.End,
    ) {
        Row(modifier = Modifier.offset(y = 4.dp)) {
            Text(
                text = label,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                color = WorkstationDiagramTheme.themeColor.text,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Slider(
            modifier = Modifier.offset(y = (-4).dp),
            value = value,
            valueRange = valueRange,
            onValueChange = onZoomChanged,
        )
    }
}

sealed class ImageData {
    data class Image(
        val image: ImageVector
    ) : ImageData()

//    data class Painter(
//        val path: String,
//    ) : ImageData()

    data class Resource(
        val id: DrawableResource,
    ) : ImageData()
}

private fun Float.display(digit: Int = 2): String {
    val rounder = 10f.pow(digit).toInt()
    val result = (this * rounder).toInt().toDouble() / rounder.toDouble()
    return when (result % 1 != .0) {
        true -> result
        false -> result.toInt()
    }.toString()
}
