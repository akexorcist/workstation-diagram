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

            if (port.position.offset < 0f || port.position.offset > 1f) {
                return "Port '${port.id}' offset must be between 0.0 and 1.0"
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
}