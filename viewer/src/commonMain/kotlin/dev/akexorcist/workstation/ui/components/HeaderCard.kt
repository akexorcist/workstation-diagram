package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import dev.akexorcist.workstation.ui.utils.defaultShadow
import kotlinx.datetime.LocalDate

@Composable
fun HeaderCard(
    title: String,
    date: String,
    onHomeClick: () -> Unit,
    onGithubClick: () -> Unit,
    hasPreviousRevision: Boolean = false,
    hasNextRevision: Boolean = false,
    onPreviousRevision: (() -> Unit)? = null,
    onNextRevision: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultShadow()
            .clip(RoundedCornerShape(12.dp))
            .background(WorkstationTheme.themeColor.surface)
            .clickable(interactionSource = null, indication = null) {}
    ) {
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SelectionContainer {
                Text(
                    modifier = Modifier.offset(y = 4.dp),
                    text = title,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    color = WorkstationTheme.themeColor.text,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (onPreviousRevision != null || onNextRevision != null) {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledIconButton(
                        onClick = { onPreviousRevision?.invoke() },
                        enabled = hasPreviousRevision,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = WorkstationTheme.themeColor.surfaceVariant,
                            contentColor = WorkstationTheme.themeColor.onSurfaceVariant,
                            disabledContainerColor = WorkstationTheme.themeColor.surfaceVariant.copy(alpha = 0.4f),
                            disabledContentColor = WorkstationTheme.themeColor.onSurfaceVariant.copy(alpha = 0.3f),
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous revision",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = date.toDateString(),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        color = WorkstationTheme.themeColor.text,
                    )
                    FilledIconButton(
                        onClick = { onNextRevision?.invoke() },
                        enabled = hasNextRevision,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = WorkstationTheme.themeColor.surfaceVariant,
                            contentColor = WorkstationTheme.themeColor.onSurfaceVariant,
                            disabledContainerColor = WorkstationTheme.themeColor.surfaceVariant.copy(alpha = 0.4f),
                            disabledContentColor = WorkstationTheme.themeColor.onSurfaceVariant.copy(alpha = 0.3f),
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next revision",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                SelectionContainer {
                    Text(
                        text = date.toDateString(),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        color = WorkstationTheme.themeColor.text,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onHomeClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = WorkstationTheme.themeColor.outlinedButtonColors(),
                    modifier = Modifier.size(32.dp),
                    border = BorderStroke(0.5.dp, WorkstationTheme.themeColor.border),
                    contentPadding = PaddingValues(2.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = WorkstationTheme.themeColor.text,
                        modifier = Modifier.size(24.dp)
                    )
                }

                OutlinedButton(
                    onClick = onGithubClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = WorkstationTheme.themeColor.outlinedButtonColors(),
                    modifier = Modifier.size(32.dp),
                    border = BorderStroke(0.5.dp, WorkstationTheme.themeColor.border),
                    contentPadding = PaddingValues(2.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "GitHub",
                        tint = WorkstationTheme.themeColor.text,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        modifier = Modifier.offset(y = 4.dp),
                        text = "Powered by",
                        style = MaterialTheme.typography.bodySmall,
                        color = WorkstationTheme.themeColor.text,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Kotlin Multiplatform & Compose Multiplatform",
                        style = MaterialTheme.typography.bodySmall,
                        color = WorkstationTheme.themeColor.text,
                    )
                }
            }
        }
    }
}

private fun String.toDateString(): String {
    return runCatching {
        val date = LocalDate.parse(this)
        val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
        "${date.day} $month ${date.year}"
    }.getOrNull() ?: this
}
