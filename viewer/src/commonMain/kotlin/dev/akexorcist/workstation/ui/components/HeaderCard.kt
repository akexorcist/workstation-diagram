package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultShadow()
            .clip(RoundedCornerShape(12.dp))
            .background(WorkstationTheme.themeColor.surface)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            }
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

            SelectionContainer {
                Text(
                    text = date.toDateString(),
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    color = WorkstationTheme.themeColor.text,
                )
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
        val year = date.year
        "$month $year"
    }.getOrNull() ?: this
}
