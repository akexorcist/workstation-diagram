package dev.akexorcist.workstation.routing

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

data class GridPoint(val x: Int, val y: Int) {
    fun manhattanDistanceTo(other: GridPoint): Int = abs(x - other.x) + abs(y - other.y)
    
    fun euclideanDistanceTo(other: GridPoint): Float {
        val dx = x - other.x
        val dy = y - other.y
        return kotlin.math.sqrt((dx * dx + dy * dy).toFloat())
    }
    
    override fun toString(): String = "($x, $y)"
}

enum class GridDirection {
    NORTH, SOUTH, EAST, WEST;

    fun isPerpendicular(other: GridDirection): Boolean =
        (isVertical() && other.isHorizontal()) || (isHorizontal() && other.isVertical())

    fun isVertical(): Boolean = this == NORTH || this == SOUTH
    fun isHorizontal(): Boolean = this == EAST || this == WEST
    
    fun opposite(): GridDirection = when(this) {
        NORTH -> SOUTH
        SOUTH -> NORTH
        EAST -> WEST
        WEST -> EAST
    }

    companion object {
        fun fromPoints(from: GridPoint, to: GridPoint): GridDirection = when {
            from == to -> EAST // Default for identical points
            to.x > from.x -> EAST
            to.x < from.x -> WEST
            to.y > from.y -> SOUTH
            to.y < from.y -> NORTH
            else -> EAST // Fallback
        }
    }
}

class GridCell(val x: Int, val y: Int) {
    private val occupancy = mutableMapOf<GridDirection, MutableSet<String>>()
    private var densityValue = 0f

    fun canOccupy(connectionId: String, direction: GridDirection): Boolean {
        val existing = occupancy[direction]
        return existing == null || existing.isEmpty() || connectionId in existing
    }

    fun occupy(connectionId: String, direction: GridDirection) {
        occupancy.getOrPut(direction) { mutableSetOf() }.add(connectionId)
        densityValue += 1.0f
    }
}

class RoutingGrid(val width: Int, val height: Int, val cellSize: Float) {
    private val blocked = Array(width) { BooleanArray(height) }
    private val cells = Array(width) { x -> Array(height) { y -> GridCell(x, y) } }
    private val deviceRegions = mutableListOf<DeviceRegion>()

    data class DeviceRegion(
        val left: Int, 
        val top: Int, 
        val right: Int, 
        val bottom: Int
    )

    fun toGridPoint(virtualX: Float, virtualY: Float): GridPoint = GridPoint(
        x = (virtualX / cellSize).toInt().coerceIn(0, width - 1),
        y = (virtualY / cellSize).toInt().coerceIn(0, height - 1)
    )

    fun toVirtualPoint(gridPoint: GridPoint): Pair<Float, Float> = Pair(
        gridPoint.x * cellSize + cellSize / 2f,
        gridPoint.y * cellSize + cellSize / 2f
    )

    fun snapSize(width: Float, height: Float): Pair<Float, Float> = Pair(
        ceil(width / cellSize) * cellSize,
        ceil(height / cellSize) * cellSize
    )

    fun markDeviceObstacle(deviceX: Float, deviceY: Float, deviceWidth: Float, deviceHeight: Float, clearance: Float) {
        val x1 = ((deviceX - clearance) / cellSize).toInt().coerceAtLeast(0)
        val y1 = ((deviceY - clearance) / cellSize).toInt().coerceAtLeast(0)
        val x2 = ((deviceX + deviceWidth + clearance) / cellSize).toInt().coerceAtMost(width - 1)
        val y2 = ((deviceY + deviceHeight + clearance) / cellSize).toInt().coerceAtMost(height - 1)

        deviceRegions.add(DeviceRegion(
            left = ((deviceX) / cellSize).toInt(),
            top = ((deviceY) / cellSize).toInt(),
            right = ((deviceX + deviceWidth) / cellSize).toInt(),
            bottom = ((deviceY + deviceHeight) / cellSize).toInt()
        ))

        for (x in x1..x2) {
            for (y in y1..y2) {
                if (x in 0 until width && y in 0 until height) {
                    blocked[x][y] = true
                }
            }
        }
    }
    
    fun markPortObstacle(portX: Float, portY: Float, clearance: Float) {
        val radius = clearance / cellSize
        val centerX = (portX / cellSize).toInt()
        val centerY = (portY / cellSize).toInt()
        
        val radiusInt = radius.toInt() + 1
        
        val x1 = (centerX - radiusInt).coerceAtLeast(0)
        val y1 = (centerY - radiusInt).coerceAtLeast(0)
        val x2 = (centerX + radiusInt).coerceAtMost(width - 1)
        val y2 = (centerY + radiusInt).coerceAtMost(height - 1)
        
        for (x in x1..x2) {
            for (y in y1..y2) {
                if (x in 0 until width && y in 0 until height) {
                    val dx = x - centerX
                    val dy = y - centerY
                    val distance = kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                    
                    if (distance <= radius) {
                        blocked[x][y] = true
                    }
                }
            }
        }
    }

    fun isBlocked(point: GridPoint): Boolean =
        point.x !in 0 until width || point.y !in 0 until height || blocked[point.x][point.y]

    fun getCell(point: GridPoint): GridCell? =
        if (point.x in 0 until width && point.y in 0 until height) cells[point.x][point.y] else null

    fun canOccupy(point: GridPoint, connectionId: String, direction: GridDirection): Boolean =
        !isBlocked(point) && (getCell(point)?.canOccupy(connectionId, direction) ?: false)

    fun occupyPath(connectionId: String, path: List<GridPoint>) {
        path.zipWithNext().forEach { (from, to) ->
            if (from != to) { // Prevent identical points
                val direction = GridDirection.fromPoints(from, to)
                getCell(from)?.occupy(connectionId, direction)
                getCell(to)?.occupy(connectionId, direction.opposite())
            }
        }
    }

    fun getNeighbors(point: GridPoint): List<Pair<GridPoint, GridDirection>> = buildList {
        if (point.y > 0) add(GridPoint(point.x, point.y - 1) to GridDirection.NORTH)
        if (point.y < height - 1) add(GridPoint(point.x, point.y + 1) to GridDirection.SOUTH)
        if (point.x > 0) add(GridPoint(point.x - 1, point.y) to GridDirection.WEST)
        if (point.x < width - 1) add(GridPoint(point.x + 1, point.y) to GridDirection.EAST)
    }
    
    fun findDeviceCorridor(source: GridPoint, target: GridPoint): Pair<GridPoint, GridPoint>? {
        if (deviceRegions.isEmpty()) return null
        
        // Find the bounding box for source and target
        val minX = min(source.x, target.x)
        val maxX = max(source.x, target.x)
        val minY = min(source.y, target.y)
        val maxY = max(source.y, target.y)
        
        // Find devices that potentially create a corridor
        val relevantDevices = deviceRegions.filter { region ->
            // Check if device is in the general pathway between source and target
            val inXRange = (region.left <= maxX && region.right >= minX)
            val inYRange = (region.top <= maxY && region.bottom >= minY)
            inXRange && inYRange
        }
        
        if (relevantDevices.size < 2) return null
        
        // Find leftmost and rightmost devices in the corridor
        var leftmostRight = -1
        var rightmostLeft = width
        var topmost = height
        var bottommost = 0
        
        for (region in relevantDevices) {
            if (region.right in (leftmostRight + 1)..<maxX) {
                leftmostRight = region.right
            }
            if (region.left in (minX + 1)..<rightmostLeft) {
                rightmostLeft = region.left
            }
            if (region.bottom > bottommost) {
                bottommost = region.bottom
            }
            if (region.top < topmost) {
                topmost = region.top
            }
        }
        
        if (leftmostRight == -1 || rightmostLeft == width || leftmostRight >= rightmostLeft) {
            return null
        }
        
        // Calculate corridor center points
        val corridorCenterX = leftmostRight + ((rightmostLeft - leftmostRight) / 2)
        val corridorTop = GridPoint(corridorCenterX, topmost)
        val corridorBottom = GridPoint(corridorCenterX, bottommost)
        
        return corridorTop to corridorBottom
    }
}
