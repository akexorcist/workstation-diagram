package dev.akexorcist.workstation.presentation.config

import dev.akexorcist.workstation.data.model.Size
import dev.akexorcist.workstation.data.model.ViewportConfig
import dev.akexorcist.workstation.data.model.GridConfig
import dev.akexorcist.workstation.data.repository.LoadResult
import dev.akexorcist.workstation.data.repository.WorkstationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object ViewportConfig {
    const val defaultZoom: Float = 1.0f
    const val viewportCullingMargin: Float = 100f
    val defaultCanvasSize: Size = Size(1920f, 1080f)
}

class ViewportConfigManager(
    private val repository: WorkstationRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _viewportConfig = MutableStateFlow(ViewportConfig())
    private val _gridConfig = MutableStateFlow(GridConfig())
    private val _canvasSize = MutableStateFlow(Size(1920f, 1080f))

    val viewportConfig: StateFlow<ViewportConfig> = _viewportConfig.asStateFlow()
    val gridConfig: StateFlow<GridConfig> = _gridConfig.asStateFlow()
    val canvasSize: StateFlow<Size> = _canvasSize.asStateFlow()

    val defaultZoom: Float get() = viewportConfig.value.defaultZoom
    val viewportCullingMargin: Float get() = viewportConfig.value.cullingMargin
    val gridSize: Float get() = gridConfig.value.size
    val gridMajorLineInterval: Int get() = gridConfig.value.majorLineInterval
    val gridEnabled: Boolean get() = gridConfig.value.enabled

    init {
        loadConfig()
    }

    private fun loadConfig() {
        scope.launch {
            when (val result = repository.loadLayout()) {
                is LoadResult.Success -> {
                    updateConfig(result.layout.metadata)
                }
                is LoadResult.PartialSuccess -> {
                    updateConfig(result.layout.metadata)
                }
                is LoadResult.Error -> {
                    // Keep defaults if loading fails
                }
            }
        }
    }

    private fun updateConfig(metadata: dev.akexorcist.workstation.data.model.LayoutMetadata) {
        metadata.viewport?.let { config ->
            _viewportConfig.value = config
        }
        metadata.grid?.let { config ->
            _gridConfig.value = config
        }
        _canvasSize.value = metadata.canvasSize
    }

    companion object {
        private var instance: ViewportConfigManager? = null

        fun getInstance(repository: WorkstationRepository): ViewportConfigManager {
            return instance ?: ViewportConfigManager(repository).also { instance = it }
        }
    }
}
