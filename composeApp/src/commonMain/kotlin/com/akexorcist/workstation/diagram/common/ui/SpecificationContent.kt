@file:Suppress("FunctionName")

package com.akexorcist.workstation.diagram.common.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akexorcist.workstation.diagram.common.data.DeviceSpecification
import com.akexorcist.workstation.diagram.common.theme.ContentColorTheme
import com.akexorcist.workstation.diagram.common.theme.DeviceComponentTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun SpecificationContent(
    specification: DeviceSpecification,
    onWebsiteClick: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheet(
        modifier = Modifier
            .fillMaxWidth(0.4f)
            .fillMaxHeight(0.8f),
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = ContentColorTheme.default.background,
        contentColor = ContentColorTheme.default.text,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 24.dp,
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 16.dp,
                    )
            ) {
                specification.website?.let { website ->
                    ProductWebsiteButton(
                        url = website,
                        onClick = { onWebsiteClick(website) },
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                if (specification.subtitle != null) {
                    DeviceTag(
                        deviceSpecification = specification,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = specification.subtitle ?: specification.title,
                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                    color = ContentColorTheme.default.text,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.offset(x = 2.dp),
                    text = specification.description,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    color = ContentColorTheme.default.text,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier
                        .verticalScroll(state = scrollState)
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                coroutineScope.launch {
                                    scrollState.scrollBy(-delta)
                                }
                            },
                        )
                ) {
                    Image(
                        painter = painterResource(specification.image),
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentDescription = specification.title,
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    AdditionalInformation(specification.information)
                }
            }
            CloseButton(
                onClick = {
                    coroutineScope.launch {
                        sheetState.hide()
                        onDismissRequest()
                    }
                },
            )
        }
    }
}

@Composable
private fun AdditionalInformation(
    information: List<Pair<String, String>>
) {
    Column {
        if (information.isEmpty()) {
            Text(
                text = "No additional information",
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                color = ContentColorTheme.default.text,
            )
        } else {
            information.forEach { (label, value) ->
                Row(
                    modifier = Modifier.offset(x = 2.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        modifier = Modifier.weight(0.3f),
                        text = label,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        color = ContentColorTheme.default.text,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Text(
                        modifier = Modifier.weight(0.7f),
                        text = value,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        color = ContentColorTheme.default.text,
                        lineHeight = 28.sp,
                    )

                }
                Spacer(modifier = Modifier.height(32.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DeviceTag(deviceSpecification: DeviceSpecification) {
    Box(
        modifier = Modifier
            .background(
                color = when {
                    deviceSpecification.type.isComputer() -> DeviceComponentTheme.Computer.color
                    deviceSpecification.type.isHub() -> DeviceComponentTheme.Hub.color
                    deviceSpecification.type.isAccessory() -> DeviceComponentTheme.End.color
                    else -> ContentColorTheme.default.transparentBackground
                },
                shape = RoundedCornerShape(4.dp),
            )
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp,
            )
    ) {
        Text(
            text = deviceSpecification.title,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            color = ContentColorTheme.default.text,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ProductWebsiteButton(url: String, onClick: (String) -> Unit) {
    OutlinedButton(
        onClick = { onClick(url) },
        shape = RoundedCornerShape(8.dp),
        colors = ContentColorTheme.default.outlinedButtonColors(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Product's Website",
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                color = ContentColorTheme.default.text,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Open product website",
                tint = ContentColorTheme.default.text,
            )
        }
    }
}

@Composable
private fun BoxScope.CloseButton(onClick: () -> Unit) {
    IconButton(
        modifier = Modifier.align(Alignment.TopEnd),
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            imageVector = Icons.Default.Close,
            contentDescription = "Close device specification",
            tint = ContentColorTheme.default.text,
        )
    }
}
