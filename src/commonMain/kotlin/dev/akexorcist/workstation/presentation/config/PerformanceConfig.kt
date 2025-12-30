package dev.akexorcist.workstation.presentation.config

object PerformanceConfig {
    val targetFPS: Int = 60
    val minAcceptableFPS: Int = 30
    val frameTimeBudgetMs: Float = 16.67f

    val maxDevicesForSmoothRendering: Int = 100
    val maxConnectionsForSmoothRendering: Int = 200
    val viewportCullingTargetMs: Float = 1f

    val pathCalculationTimeoutMs: Long = 100
    val maxPathfindingIterations: Int = 10000
    val acceptableComplexity: String = "O(n²) for n < 100"

    val maxMemoryUsageMB: Int = 500
    val maxPathCacheSizeMB: Int = 50
    val maxSpatialIndexSizeMB: Int = 10

    val initialLoadTimeTargetMs: Long = 1000
    val dataProcessingTimeTargetMs: Long = 500
}