package dev.akexorcist.workstation.data.validation

import dev.akexorcist.workstation.data.model.*

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

object DataValidator {
    fun validateLayout(layout: WorkstationLayout): ValidationResult {
        val deviceIds = layout.devices.map { it.id }.toSet()
        val errors = mutableListOf<String>()

        layout.devices.forEach { device ->
            validateDevice(device, deviceIds, layout.metadata.canvasSize)?.let { errors.add(it) }
        }

        layout.connections.forEach { connection ->
            validateConnection(connection, deviceIds, layout.devices)?.let { errors.add(it) }
        }

        when (val portValidation = validatePortUsage(layout)) {
            is ValidationResult.Error -> errors.add(portValidation.message)
            is ValidationResult.Success -> {}
        }

        return if (errors.isEmpty()) ValidationResult.Success else ValidationResult.Error(errors.joinToString("\n"))
    }

    private fun validateDevice(device: Device, allDeviceIds: Set<String>, canvasSize: Size): String? {
        if (device.id in allDeviceIds && allDeviceIds.count { it == device.id } > 1) {
            return "Duplicate device ID: ${device.id}"
        }

        if (device.position.x < 0 || device.position.x > canvasSize.width ||
            device.position.y < 0 || device.position.y > canvasSize.height) {
            return "Device '${device.name}' position is outside canvas bounds"
        }

        if (device.size.width <= 0 || device.size.height <= 0) {
            return "Device '${device.name}' has invalid size (must be positive)"
        }

        val portIds = device.ports.map { it.id }.toSet()
        device.ports.forEach { port ->
            if (port.id in portIds && portIds.count { it == port.id } > 1) {
                return "Device '${device.id}' has duplicate port ID: ${port.id}"
            }

            // Port position validation is not strict in the validator
            // as the UI will handle boundary checking at runtime
            // We only validate that the position is not negative
            if (port.position.position < 0f) {
                return "Port '${port.id}' position must be non-negative"
            }
        }

        return null
    }

    private fun validateConnection(connection: Connection, deviceIds: Set<String>, devices: List<Device>): String? {
        if (connection.sourceDeviceId !in deviceIds) {
            return "Connection '${connection.id}' references non-existent device: ${connection.sourceDeviceId}"
        }

        if (connection.targetDeviceId !in deviceIds) {
            return "Connection '${connection.id}' references non-existent device: ${connection.targetDeviceId}"
        }

        if (connection.sourceDeviceId == connection.targetDeviceId) {
            return "Connection '${connection.id}' cannot connect device to itself"
        }

        val sourceDevice = devices.find { it.id == connection.sourceDeviceId }
        val targetDevice = devices.find { it.id == connection.targetDeviceId }

        if (sourceDevice != null && connection.sourcePortId !in sourceDevice.ports.map { it.id }) {
            return "Connection '${connection.id}' references non-existent port: ${connection.sourcePortId}"
        }

        if (targetDevice != null && connection.targetPortId !in targetDevice.ports.map { it.id }) {
            return "Connection '${connection.id}' references non-existent port: ${connection.targetPortId}"
        }

        if (sourceDevice != null && targetDevice != null) {
            val sourcePort = sourceDevice.ports.find { it.id == connection.sourcePortId }
            val targetPort = targetDevice.ports.find { it.id == connection.targetPortId }

            if (sourcePort != null && targetPort != null) {
                if (sourcePort.direction == PortDirection.INPUT && targetPort.direction == PortDirection.INPUT) {
                    return "Connection '${connection.id}' has invalid port direction combination (INPUT to INPUT)"
                }

                if (sourcePort.direction == PortDirection.OUTPUT && targetPort.direction == PortDirection.OUTPUT) {
                    return "Connection '${connection.id}' has invalid port direction combination (OUTPUT to OUTPUT)"
                }
            }
        }

        return null
    }

    fun validatePortUsage(layout: WorkstationLayout): ValidationResult {
        val errors = mutableListOf<String>()
        val portUsage = mutableMapOf<String, String>()

        layout.connections.forEach { connection ->
            val sourceKey = "${connection.sourceDeviceId}:${connection.sourcePortId}"
            val targetKey = "${connection.targetDeviceId}:${connection.targetPortId}"

            portUsage[sourceKey]?.let {
                errors.add("Port ${connection.sourcePortId} on device ${connection.sourceDeviceId} is used by multiple connections (1-to-1 only)")
            } ?: run { portUsage[sourceKey] = connection.id }

            portUsage[targetKey]?.let {
                errors.add("Port ${connection.targetPortId} on device ${connection.targetDeviceId} is used by multiple connections (1-to-1 only)")
            } ?: run { portUsage[targetKey] = connection.id }
        }

        return if (errors.isEmpty()) ValidationResult.Success else ValidationResult.Error(errors.joinToString("\n"))
    }
}