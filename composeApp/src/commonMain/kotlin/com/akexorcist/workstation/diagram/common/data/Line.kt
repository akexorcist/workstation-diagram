package com.akexorcist.workstation.diagram.common.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

data class ConnectionPath(
    val lines: MutableList<Line> = mutableListOf()
) {
    data class Line(
        val start: Offset,
        val end: Offset,
    )

    fun add(line: Line): ConnectionPath {
        lines += line
        return this
    }

    fun isLineEmpty() = lines.isEmpty()

    fun toPath(): Path {
        val path = Path()
        lines.forEachIndexed { index, line ->
            if (index == 0) {
                path.moveTo(line.start.x, line.start.y)
            }
            path.lineTo(line.end.x, line.end.y)
        }
        return path
    }
}
