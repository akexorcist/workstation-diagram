package dev.akexorcist.workstation.editor.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.akexorcist.workstation.editor.presentation.EditorViewModel
import dev.akexorcist.workstation.editor.utils.copyToClipboard
import dev.akexorcist.workstation.ui.theme.WorkstationTheme
import kotlinx.coroutines.launch

@Composable
fun ExportButton(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }

    FloatingActionButton(
        onClick = {
            if (!isExporting) {
                isExporting = true
                coroutineScope.launch {
                    try {
                        val json = viewModel.exportToJson(prettyPrint = true)
                        copyToClipboard(json)
                    } catch (e: Exception) {
                    } finally {
                        isExporting = false
                    }
                }
            }
        },
        modifier = modifier.size(56.dp),
        containerColor = WorkstationTheme.themeColor.primary,
        contentColor = WorkstationTheme.themeColor.onPrimary
    ) {
        Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Export JSON to Clipboard",
            modifier = Modifier.size(24.dp)
        )
    }
}

