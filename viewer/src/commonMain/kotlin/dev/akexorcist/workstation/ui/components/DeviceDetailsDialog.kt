package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.akexorcist.workstation.data.model.Device
import dev.akexorcist.workstation.data.model.DeviceCategory
import dev.akexorcist.workstation.presentation.config.DialogConfig
import dev.akexorcist.workstation.ui.theme.ThemeColor
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import dev.akexorcist.workstation.utils.openUrl

@Composable
fun DeviceDetailsDialog(
    isVisible: Boolean,
    device: Device,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!isVisible) return
    
    class WindowSize {
        var width by mutableStateOf(0.dp)
    }
    
    val windowSize = remember { WindowSize() }
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.dp)
    ) {
        LaunchedEffect(maxWidth) {
            windowSize.width = maxWidth
        }
    }
    val dialogWidth = remember(windowSize.width) {
        when {
            windowSize.width > DialogConfig.maxDialogWidth.dp -> DialogConfig.maxDialogWidth.dp
            windowSize.width < DialogConfig.narrowScreenThreshold.dp -> 
                windowSize.width.times(DialogConfig.narrowScreenWidthPercentage)
            else -> windowSize.width - DialogConfig.dialogSideMargins.dp.times(2)
        }
    }
    
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        )
    ) {
        BoxWithConstraints(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            val dialogHeight = maxHeight.coerceAtMost(DialogConfig.maxDialogHeight.dp)
            
            Box(
                modifier = Modifier
                    .width(dialogWidth)
                    .height(dialogHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(WorkstationTheme.themeColor.surface)
                    .align(Alignment.Center)
            ) {
                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 24.dp, start = 24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        val websiteUrl = device.url
                        if (websiteUrl != null) {
                            DisableSelection {
                                OutlinedButton(
                                    onClick = { openUrl(websiteUrl) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.25.dp, WorkstationTheme.themeColor.text),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Product's Website",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = WorkstationTheme.themeColor.text,
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (device.category) {
                                DeviceCategory.HUB -> WorkstationTheme.themeColor.hub
                                DeviceCategory.PERIPHERAL -> WorkstationTheme.themeColor.peripheral
                                DeviceCategory.CENTRAL_DEVICE -> WorkstationTheme.themeColor.centralDevice
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = device.label,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = ThemeColor.White
                            )
                        }

                        Text(
                            text = device.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = WorkstationTheme.themeColor.onSurface
                        )

                        Text(
                            text = device.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = WorkstationTheme.themeColor.onSurfaceSecondary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        DeviceSpecificationsTable(device = device)

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = WorkstationTheme.themeColor.onSurfaceSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceSpecificationsTable(device: Device) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        device.specifications.forEach { item ->
            DeviceSpecificationRow(
                label = item.key,
                value = item.value
            )
        }
    }
}

@Composable
private fun DeviceSpecificationRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = WorkstationTheme.themeColor.onSurfaceSecondary,
            modifier = Modifier.weight(0.4f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = WorkstationTheme.themeColor.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}