package com.akexorcist.workstation.diagram.common.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
internal fun ConnectionContent(
    paths: List<Path>,
) {
    println("############ Recomposition ############")
    Box(modifier = Modifier.fillMaxSize()) {
        paths.forEach { path ->
            ConnectionLine(path = path)
        }
    }
}

@Composable
private fun ConnectionLine(path: Path) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawPath(
            path = path,
            color = Color.Green,
            style = Stroke(2.dp.toPx()),
        )
    }
}
