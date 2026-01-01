package dev.akexorcist.workstation.routing

import dev.akexorcist.workstation.data.model.Device
import kotlin.math.max
import kotlin.math.min

class PathDensityTracker(
    val width: Int,
    val height: Int,
    private val config: RoutingConfig = RoutingConfig
) {
    private val densityMap: Array<FloatArray> = Array(width) { FloatArray(height) }
    private val deviceBoundaries = mutableListOf<DeviceBoundary>()
    private val pathCounts: Array<IntArray> = Array(width) { IntArray(height) }

    data class DeviceBoundary(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
    )

    fun reset() {
        deviceBoundaries.clear()
        densityMap.forEach { it.fill(0f) }
        pathCounts.forEach { it.fill(0) }
    }

    fun registerDevice(device: Device, gridCellSize: Float) {
        val left = (device.position.x / gridCellSize).toInt()
        val top = (device.position.y / gridCellSize).toInt()
        val right = ((device.position.x + device.size.width) / gridCellSize).toInt()
        val bottom = ((device.position.y + device.size.height) / gridCellSize).toInt()
        
        deviceBoundaries.add(DeviceBoundary(left, top, right, bottom))
    }

    fun recordPath(path: List<GridPoint>) {
        path.forEach { point ->
            if (point.x in 0 until width && point.y in 0 until height) {
                pathCounts[point.x][point.y]++
                updateDensity(point)
            }
        }
    }

    fun getDensityCost(point: GridPoint): Float {
        if (point.x !in 0 until width || point.y !in 0 until height) return 0f

        val densityValue = densityMap[point.x][point.y]
        val distributionPenalty = calculateDistributionPenalty(point)

        return densityValue * config.pathDensityPenalty + distributionPenalty
    }

    private fun updateDensity(point: GridPoint) {
        val radius = (config.minPathSpacing / config.gridCellSize).toInt().coerceAtLeast(1)
        
        for (x in max(0, point.x - radius)..min(width - 1, point.x + radius)) {
            for (y in max(0, point.y - radius)..min(height - 1, point.y + radius)) {
                val dx = x - point.x
                val dy = y - point.y
                val distance = kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                
                if (distance <= radius) {
                    val influence = 1.0f - (distance / radius)
                    densityMap[x][y] += influence
                }
            }
        }
    }

    private fun calculateDistributionPenalty(point: GridPoint): Float {
        if (deviceBoundaries.size < 2) return 0f
        
        // Find boundaries for the nearest devices on the left and right
        var leftmostRightBoundary = -1
        var rightmostLeftBoundary = width
        
        for (boundary in deviceBoundaries) {
            // Device is to the left of the point
            if (boundary.right < point.x && boundary.right > leftmostRightBoundary) {
                leftmostRightBoundary = boundary.right
            }
            
            // Device is to the right of the point
            if (boundary.left > point.x && boundary.left < rightmostLeftBoundary) {
                rightmostLeftBoundary = boundary.left
            }
        }
        
        // If we found devices on both sides, calculate how balanced the path is
        if (leftmostRightBoundary >= 0 && rightmostLeftBoundary < width) {
            val availableSpace = rightmostLeftBoundary - leftmostRightBoundary
            if (availableSpace <= 0) return 0f
            
            val center = leftmostRightBoundary + (availableSpace / 2)
            val distanceFromCenter = kotlin.math.abs(point.x - center)
            
            // Normalize to 0-1 range
            val normalizedOffset = distanceFromCenter.toFloat() / (availableSpace / 2f)
            
            // Apply distribution factor (higher means more aggressive centering)
            return normalizedOffset * config.distributionFactor
        }
        
        return 0f
    }

    fun getDevicePathwayCenter(source: GridPoint, target: GridPoint): GridPoint? {
        if (deviceBoundaries.size < 2) return null
        
        // Find boundaries for the nearest devices on the left and right
        var leftmostRightBoundary = -1
        var rightmostLeftBoundary = width
        var topBoundary = 0
        var bottomBoundary = height
        
        for (boundary in deviceBoundaries) {
            // Consider horizontal bounds if this device is between source and target
            if (isDeviceInPathway(boundary, source, target)) {
                if (boundary.right > leftmostRightBoundary) {
                    leftmostRightBoundary = boundary.right
                }
                if (boundary.left < rightmostLeftBoundary) {
                    rightmostLeftBoundary = boundary.left
                }
                if (boundary.top > topBoundary) {
                    topBoundary = boundary.top
                }
                if (boundary.bottom < bottomBoundary) {
                    bottomBoundary = boundary.bottom
                }
            }
        }
        
        // If we found devices on both sides, calculate center
        if (leftmostRightBoundary >= 0 && rightmostLeftBoundary < width) {
            val horizontalCenter = leftmostRightBoundary + ((rightmostLeftBoundary - leftmostRightBoundary) / 2)
            val verticalCenter = topBoundary + ((bottomBoundary - topBoundary) / 2)
            return GridPoint(horizontalCenter, verticalCenter)
        }
        
        return null
    }
    
    private fun isDeviceInPathway(boundary: DeviceBoundary, source: GridPoint, target: GridPoint): Boolean {
        // Create bounding box for source and target
        val minX = min(source.x, target.x)
        val maxX = max(source.x, target.x)
        val minY = min(source.y, target.y)
        val maxY = max(source.y, target.y)
        
        // Check if the device boundary overlaps or is contained within the path corridor
        return boundary.right >= minX && boundary.left <= maxX &&
               boundary.bottom >= minY && boundary.top <= maxY
    }
}