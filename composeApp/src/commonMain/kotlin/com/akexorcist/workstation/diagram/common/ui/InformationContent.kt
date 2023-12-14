package com.akexorcist.workstation.diagram.common.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akexorcist.workstation.diagram.common.data.Device
import com.akexorcist.workstation.diagram.common.data.WorkStation
import com.akexorcist.workstation.diagram.common.data.getAllDevices
import com.akexorcist.workstation.diagram.common.theme.ConnectorComponentTheme
import com.akexorcist.workstation.diagram.common.theme.DeviceComponentTheme
import com.akexorcist.workstation.diagram.common.theme.ContentColorTheme
import com.akexorcist.workstation.diagram.common.theme.ThemeColor
import com.akexorcist.workstation.diagram.common.utility.informationBackground
import kotlinx.coroutines.launch

@Composable
fun InformationContent(
    workStation: WorkStation,
    onEnterDeviceHoverInteraction: (Device) -> Unit,
    onExitDeviceHoverInteraction: (Device) -> Unit,
) {
    Column(modifier = Modifier.padding(32.dp)) {
        Title()
        Spacer(modifier = Modifier.height(16.dp))
        Instruction()
        Spacer(modifier = Modifier.height(16.dp))
        DeviceList(
            devices = workStation.getAllDevices(),
            onEnterHoverInteraction = onEnterDeviceHoverInteraction,
            onExitHoverInteraction = onExitDeviceHoverInteraction,
        )
    }
}

@Composable
private fun Title(
) {
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
            color = ContentColorTheme.default.text,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "April 2023",
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            color = ContentColorTheme.default.text,
        )
    }
}

@Composable
private fun DeviceList(
    devices: List<Device>,
    onEnterHoverInteraction: (Device) -> Unit,
    onExitHoverInteraction: (Device) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .width(260.dp)
            .informationBackground()
            .padding(
                start = 24.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 16.dp,
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
            icon = Icons.Outlined.List,
            isExpanded = true,
            onToggleClick = {},
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            LazyColumn(
                state = lazyListState,
            ) {
                itemsIndexed(
                    items = devices,
                    key = { _, device -> device.type.name },
                ) { index, device ->
                    DeviceItem(
                        device = device,
                        onEnterHoverInteraction = onEnterHoverInteraction,
                        onExitHoverInteraction = onExitHoverInteraction,
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceItem(
    device: Device,
    onEnterHoverInteraction: (Device) -> Unit,
    onExitHoverInteraction: (Device) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isHovered by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = when (isHovered) {
            true -> ContentColorTheme.default.hoveredBackground
            false -> ContentColorTheme.default.transparentBackground
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
            .padding(end = 16.dp)
            .hoverable(interactionSource = interactionSource)
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(
            modifier = Modifier
                .width(4.dp)
                .height(26.dp)
                .background(
                    color = when {
                        device.isComputer() -> DeviceComponentTheme.Computer.color
                        device.isHub() -> DeviceComponentTheme.Hub.color
                        device.isAccessory() -> DeviceComponentTheme.End.color
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
                color = ContentColorTheme.default.text,
            )
            device.subtitle?.let {
                Text(
                    text = it,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    color = ContentColorTheme.default.text,
                )
            }
        }
    }
}

@Composable
private fun Instruction(
) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .informationBackground()
            .padding(
                start = 32.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 24.dp,
            )
    ) {
        CollapsibleHeader(
            label = "Instruction",
            icon = Icons.Outlined.Info,
            isExpanded = true,
            onToggleClick = {},
        )
        Spacer(modifier = Modifier.height(4.dp))
        DeviceIntruction(
            color = DeviceComponentTheme.Computer.color,
            label = "Computer"
        )
        Spacer(modifier = Modifier.height(8.dp))
        DeviceIntruction(
            color = DeviceComponentTheme.Hub.color,
            label = "Hub"
        )
        Spacer(modifier = Modifier.height(8.dp))
        DeviceIntruction(
            color = DeviceComponentTheme.End.color,
            label = "Accessory"
        )
        Spacer(modifier = Modifier.height(8.dp))
        DeviceIntruction(
            color = ConnectorComponentTheme.Output.color,
            label = "Output Connector"
        )
        Spacer(modifier = Modifier.height(8.dp))
        DeviceIntruction(
            color = ConnectorComponentTheme.Input.color,
            label = "Input Connector"
        )
    }
}

@Composable
private fun DeviceIntruction(
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
            color = ContentColorTheme.default.text,
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = icon,
            contentDescription = label,
            tint = ContentColorTheme.default.text,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = label,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            color = ContentColorTheme.default.text,
            fontWeight = FontWeight.Bold,
        )
        IconButton(onClick = onToggleClick) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = when (isExpanded) {
                    true -> "Collapse"
                    false -> "Expand"
                },
                tint = ContentColorTheme.default.text,
            )
        }
    }
}