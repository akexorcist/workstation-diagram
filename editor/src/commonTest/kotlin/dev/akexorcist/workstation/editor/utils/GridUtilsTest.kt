package dev.akexorcist.workstation.editor.utils

import dev.akexorcist.workstation.data.model.GridConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class GridUtilsTest {

    // region snapToGrid — pure math

    @Test
    fun valueExactlyOnGridLine_returnsUnchanged() {
        assertEquals(100f, GridUtils.snapToGrid(100f, 20f))
    }

    @Test
    fun valueBelowMidpoint_snapsToLowerGridLine() {
        // 109 / 20 = 5.45 → rounds to 5 → 5 * 20 = 100
        assertEquals(100f, GridUtils.snapToGrid(109f, 20f))
    }

    @Test
    fun valueAboveMidpoint_snapsToUpperGridLine() {
        // 111 / 20 = 5.55 → rounds to 6 → 6 * 20 = 120
        assertEquals(120f, GridUtils.snapToGrid(111f, 20f))
    }

    @Test
    fun customGridSize_snapsToCorrectLine() {
        // 37 / 10 = 3.7 → rounds to 4 → 4 * 10 = 40
        assertEquals(40f, GridUtils.snapToGrid(37f, 10f))
    }

    // endregion

    // region snapToGrid — GridConfig overload

    @Test
    fun gridEnabled_valueIsSnapped() {
        val config = GridConfig(enabled = true, size = 20f)
        assertEquals(100f, GridUtils.snapToGrid(109f, config))
    }

    @Test
    fun gridDisabled_rawValuePassesThroughUnchanged() {
        val config = GridConfig(enabled = false, size = 20f)
        assertEquals(109f, GridUtils.snapToGrid(109f, config))
    }

    @Test
    fun nullGridConfig_usesDefaultGridSizeOf20() {
        // 109 / 20 = 5.45 → rounds to 5 → 100
        assertEquals(100f, GridUtils.snapToGrid(109f, null))
    }

    // endregion
}
