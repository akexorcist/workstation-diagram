package dev.akexorcist.workstation.presentation.config

object DialogConfig {
    // Maximum width for dialogs when there's enough screen space
    const val maxDialogWidth: Float = 720f
    
    // Maximum height for dialogs to ensure they fit on the screen
    const val maxDialogHeight: Float = 600f
    
    // Side margins for dialogs in normal screen sizes
    const val dialogSideMargins: Float = 24f
    
    // Threshold for considering a screen as narrow
    const val narrowScreenThreshold: Float = 720f
    
    // Percentage of screen width to use for dialogs in narrow screens
    const val narrowScreenWidthPercentage: Float = 0.9f
}