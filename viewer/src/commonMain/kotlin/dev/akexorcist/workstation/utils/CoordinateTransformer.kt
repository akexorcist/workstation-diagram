package dev.akexorcist.workstation.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import dev.akexorcist.workstation.data.model.LayoutMetadata
import dev.akexorcist.workstation.data.model.Position

/**
 * Transforms coordinates between different coordinate systems:
 * - Absolute: Direct pixel coordinates (legacy system)
 * - Virtual: Scaled coordinates on a virtual canvas (new system)
 * 
 * This transformer is backward compatible - it automatically detects
 * the coordinate system and applies the correct transformation.
 */
object CoordinateTransformer {
    
    /**
     * Checks if the layout uses virtual coordinates
     */
    fun isVirtualCoordinates(metadata: LayoutMetadata): Boolean {
        return metadata.coordinateSystem == "virtual" && metadata.virtualCanvas != null
    }
    
    /**
     * Transforms a position from data space to screen space
     * 
     * Flow: Data Space -> World Space -> Screen Space
     * - Data Space: Raw coordinates from JSON (absolute or virtual)
     * - World Space: Normalized coordinates (canvas pixels)
     * - Screen Space: Actual rendered position (with zoom and pan)
     * 
     * @param dataPosition Position from data model
     * @param metadata Layout metadata with coordinate system info
     * @param actualCanvasSize Current canvas size in pixels
     * @param zoom Current zoom level
     * @param panOffset Current pan offset
     * @return Screen position ready for rendering
     */
    fun transformPosition(
        dataPosition: dev.akexorcist.workstation.data.model.Position,
        metadata: LayoutMetadata,
        actualCanvasSize: dev.akexorcist.workstation.data.model.Size,
        zoom: Float,
        panOffset: dev.akexorcist.workstation.data.model.Offset
    ): Offset {
        // Step 1: Data space to world space
        val worldPosition = if (isVirtualCoordinates(metadata)) {
            // Virtual coordinates: scale to actual canvas size
            val virtualCanvas = metadata.virtualCanvas!!
            val scaleX = actualCanvasSize.width / virtualCanvas.width
            val scaleY = actualCanvasSize.height / virtualCanvas.height
            
            Position(
                x = dataPosition.x * scaleX,
                y = dataPosition.y * scaleY
            )
        } else {
            // Absolute coordinates: already in world space
            dataPosition
        }
        
        // Step 2: World space to screen space (apply zoom and pan)
        return Offset(
            x = worldPosition.x * zoom + panOffset.x,
            y = worldPosition.y * zoom + panOffset.y
        )
    }
    
    /**
     * Transforms a size from data space to screen space
     * 
     * @param dataSize Size from data model
     * @param metadata Layout metadata with coordinate system info
     * @param actualCanvasSize Current canvas size in pixels
     * @param zoom Current zoom level
     * @return Screen size ready for rendering
     */
    fun transformSize(
        dataSize: dev.akexorcist.workstation.data.model.Size,
        metadata: LayoutMetadata,
        actualCanvasSize: dev.akexorcist.workstation.data.model.Size,
        zoom: Float
    ): Size {
        // Step 1: Data space to world space
        val worldSize = if (isVirtualCoordinates(metadata)) {
            // Virtual coordinates: scale to actual canvas size
            val virtualCanvas = metadata.virtualCanvas!!
            val scaleX = actualCanvasSize.width / virtualCanvas.width
            val scaleY = actualCanvasSize.height / virtualCanvas.height
            
            dev.akexorcist.workstation.data.model.Size(
                width = dataSize.width * scaleX,
                height = dataSize.height * scaleY
            )
        } else {
            // Absolute coordinates: already in world space
            dataSize
        }
        
        // Step 2: World space to screen space (apply zoom)
        return Size(
            width = worldSize.width * zoom,
            height = worldSize.height * zoom
        )
    }
    
    /**
     * Helper to create a Size object from canvas dimensions
     */
    fun canvasSize(width: Float, height: Float): dev.akexorcist.workstation.data.model.Size {
        return dev.akexorcist.workstation.data.model.Size(width, height)
    }
}
