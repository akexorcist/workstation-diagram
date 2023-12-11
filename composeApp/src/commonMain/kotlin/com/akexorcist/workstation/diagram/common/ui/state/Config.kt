package com.akexorcist.workstation.diagram.common.ui.state

data class Config(
    val lineIndex: Int,
    val zoomScale: Float,
    val showWorkspaceArea: Boolean,
    val showDeviceArea: Boolean,
    val showOverlapBoundArea: Boolean,
    val showConnectorArea: Boolean,
    val showAllConnectionLines: Boolean,
    val showLineConnectionPoint: Boolean,
)

val DefaultConfig = Config(
    lineIndex = 26,//27
    zoomScale = 1f,
    showWorkspaceArea = false,
    showDeviceArea = false,
    showOverlapBoundArea = false,
    showConnectorArea = false,
    showAllConnectionLines = false,
    showLineConnectionPoint = true,
)