package com.akexorcist.workstation.diagram.common.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.max
import kotlin.math.min

data class VerticalLine(
    val start: Offset,
    val end: Offset,
    val owner: ConnectionLine
) {
    fun getBoundRect(horizontal: Float) = Rect(
        topLeft = Offset(
            x = min(start.x, end.x) - horizontal,
            y = min(start.y, end.y),
        ),
        bottomRight = Offset(
            x = max(start.x, end.x) + horizontal,
            y = max(start.y, end.y),
        ),
    )
}
