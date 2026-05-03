package dev.akexorcist.workstation.utils

import dev.akexorcist.workstation.data.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeviceConnectionInfoTest {

    // region helpers

    private fun device(id: String) = Device(
        id = id, description = id, title = id, label = id,
        category = DeviceCategory.DEVICE,
        position = Position(0f, 0f), size = Size(100f, 50f),
        ports = listOf(Port("$id-port", "$id-port", PortDirection.OUTPUT, PortPosition(DeviceSide.RIGHT, 0f))),
        specifications = listOf(InformationItem("k", "v"))
    )

    private fun connection(id: String, sourceDeviceId: String, targetDeviceId: String) = Connection(
        id = id,
        sourceDeviceId = sourceDeviceId, sourcePortId = "$sourceDeviceId-port",
        targetDeviceId = targetDeviceId, targetPortId = "$targetDeviceId-port"
    )

    private fun layout(devices: List<Device>, connections: List<Connection>) = WorkstationLayout(
        devices = devices,
        connections = connections,
        metadata = LayoutMetadata("Test", "2025-01-01", Size(10000f, 10000f))
    )

    // endregion

    // region device hover — connection relationships

    @Test
    fun hoveredDevice_connectionsInvolvingItAreRelated() {
        val a = device("a"); val b = device("b"); val c = device("c")
        val cAB = connection("c-ab", "a", "b")
        val cCD = connection("c-cd", "b", "c") // unrelated to hover on 'a'
        val l = layout(listOf(a, b, c), listOf(cAB, cCD))

        val result = DeviceConnectionInfo.getRelatedConnectionsMap("a", l)

        assertTrue(result["c-ab"] == true)
        assertTrue(result["c-cd"] == false)
    }

    @Test
    fun hoveredDevice_connectedPeersAreRelated_disconnectedDevicesAreNot() {
        val a = device("a"); val b = device("b"); val c = device("c")
        val l = layout(listOf(a, b, c), listOf(connection("c1", "a", "b")))

        val result = DeviceConnectionInfo.getRelatedDevicesMap("a", l)

        assertTrue(result["a"] == true)  // self
        assertTrue(result["b"] == true)  // connected peer
        assertTrue(result["c"] == false) // not connected
    }

    @Test
    fun nullHoveredDevice_getRelatedConnectionsMap_returnsEmptyMap() {
        val l = layout(listOf(device("a")), listOf(connection("c1", "a", "a")))
        assertTrue(DeviceConnectionInfo.getRelatedConnectionsMap(null, l).isEmpty())
    }

    @Test
    fun nullHoveredDevice_getRelatedDevicesMap_returnsEmptyMap() {
        val l = layout(listOf(device("a")), emptyList())
        assertTrue(DeviceConnectionInfo.getRelatedDevicesMap(null, l).isEmpty())
    }

    // endregion

    // region port hover — connection relationships

    @Test
    fun hoveredPort_onlyConnectionUsingThatPortIsRelated() {
        val a = device("a"); val b = device("b"); val c = device("c")
        val cAB = connection("c-ab", "a", "b") // uses a-port
        val cBC = connection("c-bc", "b", "c") // does NOT use a-port
        val l = layout(listOf(a, b, c), listOf(cAB, cBC))

        val result = DeviceConnectionInfo.getRelatedConnectionsForPort("a:a-port", l)

        assertTrue(result["c-ab"] == true)
        assertTrue(result["c-bc"] == false)
    }

    @Test
    fun hoveredPort_deviceOnOtherEndOfConnectionIsRelated() {
        val a = device("a"); val b = device("b"); val c = device("c")
        val l = layout(listOf(a, b, c), listOf(connection("c1", "a", "b")))

        val result = DeviceConnectionInfo.getRelatedDevicesForPort("a:a-port", l)

        assertTrue(result["a"] == true)  // device owning the port
        assertTrue(result["b"] == true)  // connected device
        assertTrue(result["c"] == false) // not connected
    }

    @Test
    fun hoveredPort_ownPortAndConnectedPortAreRelated_othersAreNot() {
        val a = device("a"); val b = device("b"); val c = device("c")
        val l = layout(listOf(a, b, c), listOf(connection("c1", "a", "b")))

        val result = DeviceConnectionInfo.getRelatedPortsForPort("a:a-port", l)

        assertTrue(result["a:a-port"] == true)  // the hovered port itself
        assertTrue(result["b:b-port"] == true)  // the port on the other end
        assertTrue(result["c:c-port"] == false) // unconnected port
    }

    @Test
    fun nullHoveredPort_getRelatedConnectionsForPort_returnsEmptyMap() {
        val l = layout(listOf(device("a")), emptyList())
        assertTrue(DeviceConnectionInfo.getRelatedConnectionsForPort(null, l).isEmpty())
    }

    @Test
    fun malformedPortInfo_missingColon_returnsEmptyMap() {
        val l = layout(listOf(device("a")), emptyList())
        assertTrue(DeviceConnectionInfo.getRelatedConnectionsForPort("invalidformat", l).isEmpty())
    }

    // endregion
}
