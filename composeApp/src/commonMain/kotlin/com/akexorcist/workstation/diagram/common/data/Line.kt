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

    fun toRoundedCornerPath(cornerRadius: Float): Path {
        val path = Path()
        var lastDirection: LineDirection? = null
        lines.forEachIndexed { index, line ->
            if (index == 0) path.moveTo(line.start.x, line.start.y)
            val isLastIndex = index == lines.lastIndex
            val direction = line.getLineDirection() ?: return path
            when (direction) {
                LineDirection.Up -> {
                    if (isLastIndex ||
                        lastDirection == LineDirection.Left ||
                        lastDirection == LineDirection.Right
                    ) {
                        path.quadraticBezierTo(
                            x1 = line.start.x,
                            y1 = line.start.y,
                            x2 = line.start.x,
                            y2 = line.start.y - cornerRadius,
                        )
                    }
                    if (isLastIndex) {
                        path.lineTo(line.end.x, line.end.y)
                    } else {
                        path.lineTo(line.end.x, line.end.y + cornerRadius)
                    }
                }

                LineDirection.Down -> {
                    if (isLastIndex ||
                        lastDirection == LineDirection.Left ||
                        lastDirection == LineDirection.Right
                    ) {
                        path.quadraticBezierTo(
                            x1 = line.start.x,
                            y1 = line.start.y,
                            x2 = line.start.x,
                            y2 = line.start.y + cornerRadius,
                        )
                    }
                    if (isLastIndex) {
                        path.lineTo(line.end.x, line.end.y)
                    } else {
                        path.lineTo(line.end.x, line.end.y - cornerRadius)
                    }
                }

                LineDirection.Left -> {
                    if (isLastIndex ||
                        lastDirection == LineDirection.Up ||
                        lastDirection == LineDirection.Down
                    ) {
                        path.quadraticBezierTo(
                            x1 = line.start.x,
                            y1 = line.start.y,
                            x2 = line.start.x - cornerRadius,
                            y2 = line.start.y,
                        )
                    }
                    if (isLastIndex) {
                        path.lineTo(line.end.x, line.end.y)
                    } else {
                        path.lineTo(line.end.x + cornerRadius, line.end.y)
                    }
                }

                LineDirection.Right -> {
                    if (isLastIndex ||
                        lastDirection == LineDirection.Up ||
                        lastDirection == LineDirection.Down
                    ) {
                        path.quadraticBezierTo(
                            x1 = line.start.x,
                            y1 = line.start.y,
                            x2 = line.start.x + cornerRadius,
                            y2 = line.start.y,
                        )
                    }
                    if (isLastIndex) {
                        path.lineTo(line.end.x, line.end.y)
                    } else {
                        path.lineTo(line.end.x - cornerRadius, line.end.y)
                    }
                }
            }
            lastDirection = direction
        }
        return path
    }

    private fun Line.getLineDirection() = when {
        start.y > end.y -> LineDirection.Up
        start.y < end.y -> LineDirection.Down
        start.x > end.x -> LineDirection.Left
        start.x < end.x -> LineDirection.Right
        else -> null
    }

    private enum class LineDirection {
        Left, Up, Right, Down
    }
}
