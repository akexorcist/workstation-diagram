package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.data.model.Device
import dev.akexorcist.workstation.data.model.DeviceCategory
import dev.akexorcist.workstation.presentation.WorkstationUiState
import dev.akexorcist.workstation.ui.theme.WorkstationTheme

@Composable
fun DeviceListSidebar(
    uiState: WorkstationUiState,
    onDeviceClick: (String) -> Unit,
    onHoverDevice: (String?, Boolean) -> Unit = { _, _ -> },
    onHomeClick: () -> Unit,
    onGithubClick: () -> Unit,
    isInstructionExpanded: Boolean,
    onInstructionExpandChange: (Boolean) -> Unit,
    isDeviceListExpanded: Boolean,
    onDeviceListExpandChange: (Boolean) -> Unit,
    showUiPanel: Boolean = true,
    onToggleUiPanelClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        uiState.layout?.metadata?.let { metadata ->
            Row {
                HeaderCard(
                    modifier = Modifier.width(320.dp),
                    title = metadata.title,
                    date = metadata.date,
                    onHomeClick = onHomeClick,
                    onGithubClick = onGithubClick,
                )
                Spacer(modifier = Modifier.width(8.dp))
                HudToggleButton(
                    imageVector = Icons.Default.ChevronLeft,
                    showUiPanel = showUiPanel,
                    onToggleUiPanelClick = onToggleUiPanelClick
                )
            }
        }

        InstructionLegend(
            modifier = Modifier.width(240.dp),
            isExpanded = isInstructionExpanded,
            onExpandChange = onInstructionExpandChange,
        )

        uiState.layout?.let { layout ->
            DeviceListSection(
                modifier = Modifier.width(280.dp).then(
                    if (isDeviceListExpanded) Modifier.weight(1f)
                    else Modifier
                ),
                devices = layout.devices,
                isExpanded = isDeviceListExpanded,
                onExpandChange = onDeviceListExpandChange,
                selectedDeviceId = uiState.selectedDeviceId,
                hoveredDeviceId = uiState.hoveredDeviceId,
                onDeviceClick = onDeviceClick,
                onHoverDevice = onHoverDevice
            )
        }
    }
}

@Composable
private fun DeviceListSection(
    devices: List<Device>,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    selectedDeviceId: String?,
    hoveredDeviceId: String? = null,
    onDeviceClick: (String) -> Unit,
    onHoverDevice: (String?, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    CollapsibleSection(
        title = "Device List",
        icon = Icons.AutoMirrored.Filled.List,
        isExpanded = isExpanded,
        onExpandChange = onExpandChange,
        modifier = modifier,
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isExpanded) Modifier.fillMaxHeight() else Modifier),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(devices) { device ->
                    DeviceListItem(
                        device = device,
                        isSelected = device.id == selectedDeviceId,
                        isHovered = device.id == hoveredDeviceId,
                        onClick = { onDeviceClick(device.id) },
                        onHover = { deviceId, isHovered ->
                            onHoverDevice(deviceId, isHovered)
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun DeviceListItem(
    device: Device,
    isSelected: Boolean,
    isHovered: Boolean = false,
    onClick: () -> Unit,
    onHover: (String, Boolean) -> Unit = { _, _ -> }
) {
    val backgroundColor = when {
        isSelected -> WorkstationTheme.themeColor.surfaceVariant
        isHovered -> WorkstationTheme.themeColor.surfaceVariant.copy(alpha = 0.5f)
        else -> WorkstationTheme.themeColor.surface
    }

    val categoryColor = when (device.category) {
        DeviceCategory.HUB -> WorkstationTheme.themeColor.hub
        DeviceCategory.DEVICE -> WorkstationTheme.themeColor.device
        DeviceCategory.HOST -> WorkstationTheme.themeColor.host
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter -> {
                                onHover(device.id, true)
                            }
                            PointerEventType.Exit -> {
                                onHover(device.id, false)
                            }
                        }
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left colored border accent
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .background(categoryColor, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = WorkstationTheme.themeColor.text
                )
                Text(
                    text = device.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = WorkstationTheme.themeColor.onSurfaceSecondary
                )
            }
        }
    }
}