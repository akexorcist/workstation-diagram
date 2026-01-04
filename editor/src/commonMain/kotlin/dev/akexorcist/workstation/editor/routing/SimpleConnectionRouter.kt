package dev.akexorcist.workstation.editor.routing

import dev.akexorcist.workstation.data.model.Device
import dev.akexorcist.workstation.data.model.Point
import dev.akexorcist.workstation.presentation.config.RenderingConfig
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object SimpleConnectionRouter {
    fun routeConnection(
        sourcePos: Pair<Float, Float>,
        targetPos: Pair<Float, Float>,
        devices: List<Device>,
        existingPaths: List<List<Pair<Float, Float>>>,
        clearance: Float = RenderingConfig.simpleRouterDeviceClearance
    ): List<Point> {
        val candidates = mutableListOf<PathCandidate>()
        
        tryLShapedPaths(sourcePos, targetPos, devices, existingPaths, clearance, candidates)
        tryUShapedPaths(sourcePos, targetPos, devices, existingPaths, clearance, candidates)
        
        if (candidates.isEmpty()) {
            return generateFallbackPath(sourcePos, targetPos)
        }
        
        val bestPath = candidates.minByOrNull { it.score } ?: return generateFallbackPath(sourcePos, targetPos)
        
        if (bestPath.points.size <= 2) {
            return emptyList()
        }
        
        return bestPath.points.drop(1).dropLast(1)
    }
    
    private fun tryLShapedPaths(
        sourcePos: Pair<Float, Float>,
        targetPos: Pair<Float, Float>,
        devices: List<Device>,
        existingPaths: List<List<Pair<Float, Float>>>,
        clearance: Float,
        candidates: MutableList<PathCandidate>
    ) {
        val (sx, sy) = sourcePos
        val (tx, ty) = targetPos
        
        val path1 = listOf(
            Point(sx, sy),
            Point(tx, sy),
            Point(tx, ty)
        )
        
        if (isPathValid(path1, devices, clearance)) {
            val crossings = countCrossings(path1, existingPaths)
            candidates.add(PathCandidate(path1, turns = 1, crossings = crossings))
        }
        
        val path2 = listOf(
            Point(sx, sy),
            Point(sx, ty),
            Point(tx, ty)
        )
        
        if (isPathValid(path2, devices, clearance)) {
            val crossings = countCrossings(path2, existingPaths)
            candidates.add(PathCandidate(path2, turns = 1, crossings = crossings))
        }
    }
    
    private fun tryUShapedPaths(
        sourcePos: Pair<Float, Float>,
        targetPos: Pair<Float, Float>,
        devices: List<Device>,
        existingPaths: List<List<Pair<Float, Float>>>,
        clearance: Float,
        candidates: MutableList<PathCandidate>
    ) {
        val (sx, sy) = sourcePos
        val (tx, ty) = targetPos
        
        val midY = (sy + ty) / 2f
        val path1 = listOf(
            Point(sx, sy),
            Point(sx, midY),
            Point(tx, midY),
            Point(tx, ty)
        )
        
        if (isPathValid(path1, devices, clearance)) {
            val crossings = countCrossings(path1, existingPaths)
            candidates.add(PathCandidate(path1, turns = 2, crossings = crossings))
        }
        
        val midX = (sx + tx) / 2f
        val path2 = listOf(
            Point(sx, sy),
            Point(midX, sy),
            Point(midX, ty),
            Point(tx, ty)
        )
        
        if (isPathValid(path2, devices, clearance)) {
            val crossings = countCrossings(path2, existingPaths)
            candidates.add(PathCandidate(path2, turns = 2, crossings = crossings))
        }
        
        val offset1 = clearance * 2f
        val path3 = listOf(
            Point(sx, sy),
            Point(sx, sy + offset1),
            Point(tx, sy + offset1),
            Point(tx, ty)
        )
        
        if (isPathValid(path3, devices, clearance)) {
            val crossings = countCrossings(path3, existingPaths)
            candidates.add(PathCandidate(path3, turns = 2, crossings = crossings))
        }
        
        val path4 = listOf(
            Point(sx, sy),
            Point(sx + offset1, sy),
            Point(sx + offset1, ty),
            Point(tx, ty)
        )
        
        if (isPathValid(path4, devices, clearance)) {
            val crossings = countCrossings(path4, existingPaths)
            candidates.add(PathCandidate(path4, turns = 2, crossings = crossings))
        }
    }
    
    private fun isPathValid(
        path: List<Point>,
        devices: List<Device>,
        clearance: Float
    ): Boolean {
        if (path.size < 2) return false
        
        for (i in 0 until path.size - 1) {
            val segmentStart = path[i]
            val segmentEnd = path[i + 1]
            
            if (segmentCollidesWithDevices(segmentStart, segmentEnd, devices, clearance)) {
                return false
            }
        }
        
        return true
    }
    
    private fun segmentCollidesWithDevices(
        start: Point,
        end: Point,
        devices: List<Device>,
        clearance: Float
    ): Boolean {
        val segmentRect = createSegmentRect(start, end, clearance)
        
        for (device in devices) {
            val deviceRect = createDeviceRect(device, clearance)
            if (rectanglesIntersect(segmentRect, deviceRect)) {
                return true
            }
        }
        
        return false
    }
    
    private fun createSegmentRect(start: Point, end: Point, clearance: Float): Rect {
        val minX = min(start.x, end.x) - clearance
        val maxX = max(start.x, end.x) + clearance
        val minY = min(start.y, end.y) - clearance
        val maxY = max(start.y, end.y) + clearance
        return Rect(minX, minY, maxX, maxY)
    }
    
    private fun createDeviceRect(device: Device, clearance: Float): Rect {
        return Rect(
            device.position.x - clearance,
            device.position.y - clearance,
            device.position.x + device.size.width + clearance,
            device.position.y + device.size.height + clearance
        )
    }
    
    private fun rectanglesIntersect(rect1: Rect, rect2: Rect): Boolean {
        return !(rect1.right < rect2.left || rect1.left > rect2.right ||
                rect1.bottom < rect2.top || rect1.top > rect2.bottom)
    }
    
    private fun countCrossings(
        path: List<Point>,
        existingPaths: List<List<Pair<Float, Float>>>
    ): Int {
        var crossings = 0
        
        for (i in 0 until path.size - 1) {
            val segmentStart = path[i]
            val segmentEnd = path[i + 1]
            
            for (existingPath in existingPaths) {
                for (j in 0 until existingPath.size - 1) {
                    val existingStart = existingPath[j]
                    val existingEnd = existingPath[j + 1]
                    
                    if (segmentsCross(
                        segmentStart.x, segmentStart.y,
                        segmentEnd.x, segmentEnd.y,
                        existingStart.first, existingStart.second,
                        existingEnd.first, existingEnd.second
                    )) {
                        crossings++
                    }
                }
            }
        }
        
        return crossings
    }
    
    private fun segmentsCross(
        x1: Float, y1: Float, x2: Float, y2: Float,
        x3: Float, y3: Float, x4: Float, y4: Float
    ): Boolean {
        val isHorizontal1 = abs(y2 - y1) < 0.01f
        val isHorizontal2 = abs(y4 - y3) < 0.01f
        
        if (isHorizontal1 && isHorizontal2) {
            if (abs(y1 - y3) < 0.01f) {
                val min1 = min(x1, x2)
                val max1 = max(x1, x2)
                val min2 = min(x3, x4)
                val max2 = max(x3, x4)
                return !(max1 < min2 || max2 < min1)
            }
            return false
        }
        
        if (!isHorizontal1 && !isHorizontal2) {
            if (abs(x1 - x3) < 0.01f) {
                val min1 = min(y1, y2)
                val max1 = max(y1, y2)
                val min2 = min(y3, y4)
                val max2 = max(y3, y4)
                return !(max1 < min2 || max2 < min1)
            }
            return false
        }
        
        if (isHorizontal1 && !isHorizontal2) {
            val y = y1
            val x = x3
            return y >= min(y3, y4) && y <= max(y3, y4) &&
                   x >= min(x1, x2) && x <= max(x1, x2)
        }
        
        if (!isHorizontal1 && isHorizontal2) {
            val y = y3
            val x = x1
            return y >= min(y1, y2) && y <= max(y1, y2) &&
                   x >= min(x3, x4) && x <= max(x3, x4)
        }
        
        return false
    }
    
    private fun generateFallbackPath(
        sourcePos: Pair<Float, Float>,
        targetPos: Pair<Float, Float>
    ): List<Point> {
        return emptyList()
    }
    
    private data class PathCandidate(
        val points: List<Point>,
        val turns: Int,
        val crossings: Int
    ) {
        val score: Float = turns * 1f + crossings * 2f + points.size * 0.5f
    }
    
    private data class Rect(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float
    )
}

