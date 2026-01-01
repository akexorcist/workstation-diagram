package dev.akexorcist.workstation.presentation.config

import dev.akexorcist.workstation.data.repository.WorkstationRepositoryImpl

object ConfigFactory {
    fun createViewportConfigManager(): ViewportConfigManager {
        return ViewportConfigManager.getInstance(WorkstationRepositoryImpl())
    }
    
    fun createGridSystem(): GridSystem {
        return GridSystem(createViewportConfigManager())
    }
    
    fun createVirtualUnitSystem(): VirtualUnitSystem {
        return VirtualUnitSystem(createViewportConfigManager())
    }
}