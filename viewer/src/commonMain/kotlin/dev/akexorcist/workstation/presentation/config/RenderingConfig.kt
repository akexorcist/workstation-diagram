package dev.akexorcist.workstation.presentation.config

object RenderingConfig {
    // Device rendering
    const val defaultDeviceBorderRadius: Float = 8f
    const val defaultDeviceBorderThickness: Float = 2f
    const val deviceTextPadding: Float = 16f
    const val deviceTextTitleScale: Float = 1f
    const val deviceTextBodyScale: Float = 0.85f
    const val deviceTextMinWidthToShow: Float = 80f
    const val deviceTextLineHeightScale: Float = 0.8f

    // Connection paths
    const val connectionWidth: Float = 4f
    const val connectionBackgroundWidth: Float = 10f
    const val connectionForegroundWidth: Float = 4f
    const val connectionCornerRadius: Float = 12f
    
    // Connection animation
    const val connectionAnimationEnabled: Boolean = true
    const val connectionAnimationDuration: Int = 3000
    const val connectionAnimationStampSpacing: Float = 6.0f
    const val connectionAnimationPhaseScale: Float = 21.5f

    // Port capsules
    const val portCapsuleBaseWidth: Float = 6f
    const val portCapsuleWidthPerChar: Float = 4.2f
    const val portCapsuleFontSize: Float = 7f
    const val portCapsuleHeight: Float = 16f
    const val portCapsuleHorizontalPadding: Float = 2f
    const val portCapsuleSidePadding: Float = 6f
    const val portCapsuleDeviceSidePadding: Float = 4f
    const val portDeviceOverlap: Float = 6f
}