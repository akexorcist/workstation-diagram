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
        val connectedDeviceIds = mutableSetOf<String>()
        layout.connections.forEach { connection ->
            if (connection.sourceDeviceId == hoveredDeviceId) {
                connectedDeviceIds.add(connection.targetDeviceId)
            } else if (connection.targetDeviceId == hoveredDeviceId) {
                connectedDeviceIds.add(connection.sourceDeviceId)
            }
        }
        
        // Add the hovered device itself
        connectedDeviceIds.add(hoveredDeviceId)
        
        // Create a map for all devices
        return layout.devices.associate { device ->
            device.id to (device.id in connectedDeviceIds)
        }
    }

    /**
     * Creates a map of which ports are related to a hovered device.
     * A port is considered related if it belongs to the hovered device or
     * is connected to any port on the hovered device.
     * 
     * @param hoveredDeviceId The ID of the device being hovered over, or null if no device is hovered
     * @param layout The current workstation layout containing all devices and connections
     * @return Map of port identifiers (deviceId:portId) to a Boolean indicating if they are related
     */
    fun getRelatedPortsMap(
        hoveredDeviceId: String?,
        layout: WorkstationLayout
    ): Map<String, Boolean> {
        if (hoveredDeviceId == null) {
            return emptyMap()
        }

        // Get all ports on the hovered device
        val hoveredDevice = layout.devices.find { it.id == hoveredDeviceId } ?: return emptyMap()
        val hoveredDevicePorts = hoveredDevice.ports.map { it.id }.toSet()
        
        // Find all ports that are connected to the hovered device's ports
        val connectedPortMap = mutableMapOf<String, Boolean>()
        
        // First, add all ports of the hovered device as related
        hoveredDevicePorts.forEach { portId ->
            connectedPortMap["$hoveredDeviceId:$portId"] = true
        }
        
        // Then add all ports connected to the hovered device
        layout.connections.forEach { connection ->
            if (connection.sourceDeviceId == hoveredDeviceId) {
                // This is a port on another device connected to the hovered device
                connectedPortMap["${connection.targetDeviceId}:${connection.targetPortId}"] = true
            } else if (connection.targetDeviceId == hoveredDeviceId) {
                // This is a port on another device connected to the hovered device
                connectedPortMap["${connection.sourceDeviceId}:${connection.sourcePortId}"] = true
            }
        }
        
        // Create a complete map for all ports
        val result = mutableMapOf<String, Boolean>()
        
        // Add all known ports (and mark them as related or not)
        layout.devices.forEach { device ->
            device.ports.forEach { port ->
                val portKey = "${device.id}:${port.id}"
                result[portKey] = connectedPortMap.containsKey(portKey)
            }
        }
        
        return result
    }
    
    /**
     * Creates a map of which connections are related to a hovered port.
     * 
     * @param hoveredPortInfo The information about the hovered port (deviceId:portId), or null if no port is hovered
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
        
        // Parse the device ID and port ID from the hoveredPortInfo string (deviceId:portId)
        val parts = hoveredPortInfo.split(":")
        if (parts.size != 2) {
            return emptyMap()
        }
        
        val deviceId = parts[0]
        val portId = parts[1]
        
        return layout.connections.associate { connection ->
            val isRelated = (connection.sourceDeviceId == deviceId && connection.sourcePortId == portId) || 
                            (connection.targetDeviceId == deviceId && connection.targetPortId == portId)
            connection.id to isRelated
        }
    }
    
    /**
     * Creates a map of which devices are related to a hovered port.
     * A device is considered related if it's the device containing the hovered port or
     * is connected to the hovered port by any connection.
     * 
     * @param hoveredPortInfo The information about the hovered port (deviceId:portId), or null if no port is hovered
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
        
        // Parse the device ID and port ID from the hoveredPortInfo string (deviceId:portId)
        val parts = hoveredPortInfo.split(":")
        if (parts.size != 2) {
            return emptyMap()
        }
        
        val deviceId = parts[0]
        val portId = parts[1]
        
        // Find all devices that are connected to this port
        val connectedDeviceIds = mutableSetOf<String>()
        // Add the device containing this port
        connectedDeviceIds.add(deviceId)
        
        // Add any devices connected to this port
        layout.connections.forEach { connection ->
            if (connection.sourceDeviceId == deviceId && connection.sourcePortId == portId) {
                connectedDeviceIds.add(connection.targetDeviceId)
            } else if (connection.targetDeviceId == deviceId && connection.targetPortId == portId) {
                connectedDeviceIds.add(connection.sourceDeviceId)
            }
        }
        
        // Create a map for all devices
        return layout.devices.associate { device ->
            device.id to (device.id in connectedDeviceIds)
        }
    }
    
    /**
     * Creates a map of which ports are related to a hovered port.
     * A port is considered related if it's the hovered port itself or
     * is connected to the hovered port by any connection.
     * 
     * @param hoveredPortInfo The information about the hovered port (deviceId:portId), or null if no port is hovered
     * @param layout The current workstation layout containing all devices and connections
     * @return Map of port identifiers (deviceId:portId) to a Boolean indicating if they are related
     */
    fun getRelatedPortsForPort(
        hoveredPortInfo: String?,
        layout: WorkstationLayout
    ): Map<String, Boolean> {
        if (hoveredPortInfo == null) {
            return emptyMap()
        }
        
        // Parse the device ID and port ID from the hoveredPortInfo string (deviceId:portId)
        val parts = hoveredPortInfo.split(":")
        if (parts.size != 2) {
            return emptyMap()
        }
        
        val deviceId = parts[0]
        val portId = parts[1]
        
        // Find all ports that are connected to this port
        val connectedPortMap = mutableMapOf<String, Boolean>()
        
        // Add the hovered port itself
        connectedPortMap[hoveredPortInfo] = true
        
        // Add any ports connected to this port
        layout.connections.forEach { connection ->
            if (connection.sourceDeviceId == deviceId && connection.sourcePortId == portId) {
                connectedPortMap["${connection.targetDeviceId}:${connection.targetPortId}"] = true
            } else if (connection.targetDeviceId == deviceId && connection.targetPortId == portId) {
                connectedPortMap["${connection.sourceDeviceId}:${connection.sourcePortId}"] = true
            }
        }
        
        // Create a complete map for all ports
        val result = mutableMapOf<String, Boolean>()
        
        // Add all known ports (and mark them as related or not)
        layout.devices.forEach { device ->
            device.ports.forEach { port ->
                val portKey = "${device.id}:${port.id}"
                result[portKey] = connectedPortMap.containsKey(portKey)
            }
        }
        
        return result
    }
}