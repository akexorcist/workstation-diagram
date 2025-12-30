package dev.akexorcist.workstation.presentation.config

import dev.akexorcist.workstation.data.model.Offset
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val zoom: Float = 1.0f,
    val panOffset: Offset = Offset(0f, 0f),
    val isDarkTheme: Boolean = true,
    val showConnectionAnimation: Boolean = false,
    val sidebarCollapsed: Boolean = false,
    val version: String = "1.0"
) {
    companion object {
        val DEFAULT = UserPreferences()
    }
}