package dev.akexorcist.workstation.presentation.config

object ErrorMessages {
    const val FILE_NOT_FOUND = "Unable to load workstation data. File not found: {path}"
    const val JSON_PARSE_ERROR = "Invalid data format. Please check your JSON file. Error: {error}"
    const val VALIDATION_ERROR = "Data validation failed: {error}"
    const val NETWORK_ERROR = "Unable to load data. Please check your internet connection."

    const val DUPLICATE_DEVICE_ID = "Duplicate device ID: {id}"
    const val INVALID_DEVICE_POSITION = "Device '{name}' position is outside canvas bounds"
    const val INVALID_DEVICE_SIZE = "Device '{name}' has invalid size (must be positive)"
    const val INVALID_PORT_POSITION = "Device '{name}' port '{portId}' has invalid position offset"

    const val MISSING_DEVICE = "Connection '{id}' references non-existent device: {deviceId}"
    const val MISSING_PORT = "Connection '{id}' references non-existent port: {portId}"
    const val INVALID_PORT_DIRECTION = "Connection '{id}' has invalid port direction combination"
    const val SELF_CONNECTION = "Connection '{id}' cannot connect device to itself"

    const val DUPLICATE_PORT_ID = "Device '{deviceId}' has duplicate port ID: {portId}"
    const val INVALID_PORT_OFFSET = "Port '{portId}' offset must be between 0.0 and 1.0"

    const val PATH_CALCULATION_FAILURE = "Unable to calculate connection path. Using straight line."
    const val RENDERING_ERROR = "Rendering error occurred. Some elements may not display correctly."
    const val PERFORMANCE_WARNING = "Large diagram detected. Rendering may be slow."

    const val LOAD_SUCCESS = "Workstation diagram loaded successfully"
    const val LOAD_WARNING = "Some data issues were found but diagram loaded with warnings"
    const val LOADING_INFO = "Loading workstation data..."
    const val PROCESSING_INFO = "Processing diagram layout..."
}