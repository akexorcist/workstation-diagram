package dev.akexorcist.workstation.editor.routing

import dev.akexorcist.workstation.data.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SimpleConnectionRouterTest {

    private fun device(id: String, x: Float, y: Float, width: Float, height: Float) = Device(
        id = id, description = id, title = id, label = id,
        category = DeviceCategory.DEVICE,
        position = Position(x, y), size = Size(width, height),
        ports = emptyList(), specifications = listOf(InformationItem("k", "v"))
    )

    private fun route(
        sx: Float, sy: Float,
        tx: Float, ty: Float,
        devices: List<Device> = emptyList(),
        clearance: Float = 15f
    ) = SimpleConnectionRouter.routeConnection(
        sourcePos = sx to sy,
        targetPos = tx to ty,
        devices = devices,
        existingPaths = emptyList(),
        clearance = clearance
    )

    // L-shape: 3 points → drop first/last → 1 intermediate point
    @Test
    fun clearPath_lShapeChosen_returnsExactlyOneIntermediatePoint() {
        val result = route(0f, 0f, 300f, 200f)
        assertEquals(1, result.size)
    }

    // L-shape corner must be at one of the two valid L-turn positions
    @Test
    fun clearPath_lShapeCorner_isAtExpectedCoordinate() {
        val result = route(0f, 0f, 300f, 200f)
        val corner = result.first()
        val isValidLCorner = (corner.x == 300f && corner.y == 0f) ||
                             (corner.x == 0f   && corner.y == 200f)
        assertTrue(isValidLCorner, "Expected L-shape corner at (300,0) or (0,200), got (${ corner.x},${ corner.y})")
    }

    // When both L-paths are blocked, the router falls back to U-shape (2 intermediate points)
    @Test
    fun lPathsBlockedByDevices_uShapeChosen_returnsTwoIntermediatePoints() {
        // blockRight blocks the right-edge approach used by L1 (horizontal-first path)
        // blockBottom blocks the bottom approach used by L2 (vertical-first path)
        // The midpoint U-path (going through y=150) is left clear by both blockers
        val blockRight  = device("block-right",  x = 280f, y = -20f, width = 40f, height = 50f)
        val blockBottom = device("block-bottom",  x = -20f, y = 280f, width = 50f, height = 40f)
        val result = route(0f, 0f, 300f, 300f, devices = listOf(blockRight, blockBottom))
        assertEquals(2, result.size)
    }

    // L-shape (1 turn) scores lower than U-shape (2 turns) — it must win when both are valid
    @Test
    fun lShapeAndUShapeValid_lShapePreferred() {
        val result = route(0f, 0f, 300f, 200f) // open space — both L and U are valid
        assertEquals(1, result.size, "L-shape (1 turn) should beat U-shape (2 turns)")
    }

    // When a large device blocks every candidate path, the fallback returns empty
    @Test
    fun allPathsBlocked_returnsEmptyList() {
        val blocker = device("blocker", x = -500f, y = -500f, width = 2000f, height = 2000f)
        val result = route(0f, 0f, 100f, 100f, devices = listOf(blocker))
        assertTrue(result.isEmpty())
    }
}
