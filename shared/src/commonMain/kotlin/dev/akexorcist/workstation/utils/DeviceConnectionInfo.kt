package dev.akexorcist.workstation.utils

import dev.akexorcist.workstation.data.model.WorkstationLayout

/**
 * Utility class to determine relationships between devices, connections, and ports
 * when a device or port is hovered over in the diagram.
 */
object DeviceConnectionInfo {

    /**
     * Creates a map of which connections are related to a hovered device.
     * 
     * @param hoveredDeviceId The ID of the device being hovered over, or null if no device is hovered
     * @param layout The current workstation layout containing all devices and connections
     * @return Map of connection IDs to a Boolean indicating if they are related to the hovered device
     */
    fun getRelatedConnectionsMap(
        hoveredDeviceId: String?,
        layout: WorkstationLayout
    ): Map<String, Boolean> {
        if (hoveredDeviceId == null) {
            return emptyMap()
        }

        return layout.connections.associate { connection ->
            val isRelated = connection.sourceDeviceId == hoveredDeviceId || 
                            connection.targetDeviceId == hoveredDeviceId
            connection.id to isRelated
        }
    }

    /**
     * Creates a map of which devices are related to a hovered device.
     * A device is considered related if it's the hovered device itself or 
     * is connected to the hovered device by any connection.
     * 
     * @param hoveredDeviceId The ID of the device being hovered over, or null if no device is hovered
     * @param layout The current workstation layout containing all devices and connections
     * @return Map of device IDs to a Boolean indicating if they are related to the hovered device
     */
    fun getRelatedDevicesMap(
        hoveredDeviceId: String?,
        layout: WorkstationLayout
    ): Map<String, Boolean> {
        if (hoveredDeviceId == null) {
            return emptyMap()
        }

        // Find all devices that are directly connected to the hovered device
        val relatedDeviceIds = mutableSetOf<String>()
        relatedDeviceIds.add(hoveredDeviceId) // Include the hovered device itself

        layout.connections.forEach { connection ->
            if (connection.sourceDeviceId == hoveredDeviceId) {
                relatedDeviceIds.add(connection.targetDeviceId)
            } else if (connection.targetDeviceId == hoveredDeviceId) {
                relatedDeviceIds.add(connection.sourceDeviceId)
            }
        }

        return layout.devices.associate { device ->
            device.id to (device.id in relatedDeviceIds)
        }
    }

    /**
     * Creates a map of which ports are related to a hovered device.
     * A port is considered related if it belongs to the hovered device or 
     * to a device that is connected to the hovered device.
     * 
     * @param hoveredDeviceId The ID of the device being hovered over, or null if no device is hovered
     * @param layout The current workstation layout containing all devices and connections
     * @return Map of port info strings (format: "deviceId:portId") to a Boolean indicating if they are related
     */
    fun getRelatedPortsMap(
        hoveredDeviceId: String?,
        layout: WorkstationLayout
    ): Map<String, Boolean> {
        if (hoveredDeviceId == null) {
            return emptyMap()
        }

        val relatedDeviceIds = mutableSetOf<String>()
        relatedDeviceIds.add(hoveredDeviceId)

        layout.connections.forEach { connection ->
            if (connection.sourceDeviceId == hoveredDeviceId) {
                relatedDeviceIds.add(connection.targetDeviceId)
            } else if (connection.targetDeviceId == hoveredDeviceId) {
                relatedDeviceIds.add(connection.sourceDeviceId)
            }
        }

        val relatedPortsMap = mutableMapOf<String, Boolean>()
        layout.devices.forEach { device ->
            device.ports.forEach { port ->
                val portInfo = "${device.id}:${port.id}"
                relatedPortsMap[portInfo] = device.id in relatedDeviceIds
            }
        }

        return relatedPortsMap
    }

    /**
     * Creates a map of which connections are related to a hovered port.
     * A connection is considered related if it uses the hovered port.
     * 
     * @param hoveredPortInfo The port info string (format: "deviceId:portId"), or null if no port is hovered
     * @param layout The current workstation layout containing all devices and connections
     * @return Map of connection IDs to a Boolean indicating if they are related to the hovered port
     */
    fun getRelatedConnectionsForPort(
        hoveredPortInfo: String?,
        layout: WorkstationLayout
    ): Map<String, Boolean> {
        if (hoveredPortInfo == null) {
            return emptyMap()
        }

        val parts = hoveredPortInfo.split(":")
        if (parts.size != 2) {
            return emptyMap()
        }

        val (hoveredDeviceId, hoveredPortId) = parts

        return layout.connections.associate { connection ->
            val isRelated = (connection.sourceDeviceId == hoveredDeviceId && connection.sourcePortId == hoveredPortId) ||
                            (connection.targetDeviceId == hoveredDeviceId && connection.targetPortId == hoveredPortId)
            connection.id to isRelated
        }
    }

    /**
     * Creates a map of which devices are related to a hovered port.
     * A device is considered related if it's the device that owns the hovered port or 
     * is connected to that port by any connection.
     * 
     * @param hoveredPortInfo The port info string (format: "deviceId:portId"), or null if no port is hovered
     * @param layout The current workstation layout containing all devices and connections
     * @return Map of device IDs to a Boolean indicating if they are related to the hovered port
     */
    fun getRelatedDevicesForPort(
        hoveredPortInfo: String?,
        layout: WorkstationLayout
    ): Map<String, Boolean> {
        if (hoveredPortInfo == null) {
            return emptyMap()
        }

        val parts = hoveredPortInfo.split(":")
        if (parts.size != 2) {
            return emptyMap()
        }

        val (hoveredDeviceId, hoveredPortId) = parts
        val relatedDeviceIds = mutableSetOf<String>()
        relatedDeviceIds.add(hoveredDeviceId) // Include the device that owns the port

        // Find all devices connected to this port
        layout.connections.forEach { connection ->
            if (connection.sourceDeviceId == hoveredDeviceId && connection.sourcePortId == hoveredPortId) {
                relatedDeviceIds.add(connection.targetDeviceId)
            } else if (connection.targetDeviceId == hoveredDeviceId && connection.targetPortId == hoveredPortId) {
                relatedDeviceIds.add(connection.sourceDeviceId)
            }
        }

        return layout.devices.associate { device ->
            device.id to (device.id in relatedDeviceIds)
        }
    }

    /**
     * Creates a map of which ports are related to a hovered port.
     * A port is considered related if it's the hovered port itself or 
     * is connected to the hovered port by any connection.
     * 
     * @param hoveredPortInfo The port info string (format: "deviceId:portId"), or null if no port is hovered
     * @param layout The current workstation layout containing all devices and connections
     * @return Map of port info strings (format: "deviceId:portId") to a Boolean indicating if they are related
     */
    fun getRelatedPortsForPort(
        hoveredPortInfo: String?,
        layout: WorkstationLayout
    ): Map<String, Boolean> {
        if (hoveredPortInfo == null) {
            return emptyMap()
        }

        val parts = hoveredPortInfo.split(":")
        if (parts.size != 2) {
            return emptyMap()
        }

        val (hoveredDeviceId, hoveredPortId) = parts
        val relatedPortInfoSet = mutableSetOf<String>()
        relatedPortInfoSet.add(hoveredPortInfo) // Include the hovered port itself

        // Find all ports connected to this port
        layout.connections.forEach { connection ->
            if (connection.sourceDeviceId == hoveredDeviceId && connection.sourcePortId == hoveredPortId) {
                relatedPortInfoSet.add("${connection.targetDeviceId}:${connection.targetPortId}")
            } else if (connection.targetDeviceId == hoveredDeviceId && connection.targetPortId == hoveredPortId) {
                relatedPortInfoSet.add("${connection.sourceDeviceId}:${connection.sourcePortId}")
            }
        }

        val relatedPortsMap = mutableMapOf<String, Boolean>()
        layout.devices.forEach { device ->
            device.ports.forEach { port ->
                val portInfo = "${device.id}:${port.id}"
                relatedPortsMap[portInfo] = portInfo in relatedPortInfoSet
            }
        }

        return relatedPortsMap
    }
}

