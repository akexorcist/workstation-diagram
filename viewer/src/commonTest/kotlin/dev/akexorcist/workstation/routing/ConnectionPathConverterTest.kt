package dev.akexorcist.workstation.routing

import dev.akexorcist.workstation.data.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConnectionPathConverterTest {

    // region helpers

    private fun device(
        id: String,
        x: Float, y: Float,
        width: Float, height: Float,
        ports: List<Port> = emptyList()
    ) = Device(
        id = id, description = id, title = id, label = id,
        category = DeviceCategory.DEVICE,
        position = Position(x, y),
        size = Size(width, height),
        ports = ports,
        specifications = listOf(InformationItem("k", "v"))
    )

    private fun port(id: String, side: DeviceSide, position: Float, direction: PortDirection = PortDirection.OUTPUT) =
        Port(id = id, name = id, direction = direction, position = PortPosition(side, position))

    private fun connection(
        id: String = "c1",
        sourceDeviceId: String = "d1", sourcePortId: String = "p-out",
        targetDeviceId: String = "d2", targetPortId: String = "p-in",
        routingPoints: List<Point>? = null
    ) = Connection(id, sourceDeviceId, sourcePortId, targetDeviceId, targetPortId, routingPoints)

    // endregion

    // region port world position

    @Test
    fun portOnRightSide_worldPositionIsDeviceRightEdgePlusOffset() {
        // Device at (100, 100), size (200, 80), port at RIGHT position 40
        // Expected: x = 100 + 200 = 300, y = 100 + 40 = 140
        val d1 = device("d1", 100f, 100f, 200f, 80f, listOf(port("p-out", DeviceSide.RIGHT, 40f)))
        val d2 = device("d2", 500f, 100f, 200f, 80f, listOf(port("p-in", DeviceSide.LEFT, 0f)))

        val result = ConnectionPathConverter.convertConnections(listOf(d1, d2), listOf(connection()))
        val source = result.first().virtualWaypoints.first()

        assertEquals(300f, source.first)
        assertEquals(140f, source.second)
    }

    @Test
    fun portOnLeftSide_worldPositionIsDeviceLeftEdgePlusOffset() {
        // Device at (500, 100), size (200, 80), port at LEFT position 40
        // Expected: x = 500, y = 100 + 40 = 140
        val d1 = device("d1", 100f, 100f, 200f, 80f, listOf(port("p-out", DeviceSide.RIGHT, 0f)))
        val d2 = device("d2", 500f, 100f, 200f, 80f, listOf(port("p-in", DeviceSide.LEFT, 40f, PortDirection.INPUT)))

        val result = ConnectionPathConverter.convertConnections(listOf(d1, d2), listOf(connection()))
        val target = result.first().virtualWaypoints.last()

        assertEquals(500f, target.first)
        assertEquals(140f, target.second)
    }

    @Test
    fun portOnTopSide_worldPositionIsDeviceTopEdgePlusOffset() {
        // Device at (100, 200), size (200, 80), port at TOP position 60
        // Expected: x = 100 + 60 = 160, y = 200
        val d1 = device("d1", 100f, 200f, 200f, 80f, listOf(port("p-out", DeviceSide.TOP, 60f)))
        val d2 = device("d2", 500f, 200f, 200f, 80f, listOf(port("p-in", DeviceSide.LEFT, 0f, PortDirection.INPUT)))

        val result = ConnectionPathConverter.convertConnections(listOf(d1, d2), listOf(connection()))
        val source = result.first().virtualWaypoints.first()

        assertEquals(160f, source.first)
        assertEquals(200f, source.second)
    }

    @Test
    fun portOnBottomSide_worldPositionIsDeviceBottomEdgePlusOffset() {
        // Device at (100, 200), size (200, 80), port at BOTTOM position 60
        // Expected: x = 100 + 60 = 160, y = 200 + 80 = 280
        val d1 = device("d1", 100f, 200f, 200f, 80f, listOf(port("p-out", DeviceSide.BOTTOM, 60f)))
        val d2 = device("d2", 500f, 200f, 200f, 80f, listOf(port("p-in", DeviceSide.LEFT, 0f, PortDirection.INPUT)))

        val result = ConnectionPathConverter.convertConnections(listOf(d1, d2), listOf(connection()))
        val source = result.first().virtualWaypoints.first()

        assertEquals(160f, source.first)
        assertEquals(280f, source.second)
    }

    @Test
    fun portPositionExceedingDeviceBounds_isClamped() {
        // Device height = 80, port at RIGHT position 999 → clamped to 80
        // Expected: y = 100 + 80 = 180
        val d1 = device("d1", 100f, 100f, 200f, 80f, listOf(port("p-out", DeviceSide.RIGHT, 999f)))
        val d2 = device("d2", 500f, 100f, 200f, 80f, listOf(port("p-in", DeviceSide.LEFT, 0f, PortDirection.INPUT)))

        val result = ConnectionPathConverter.convertConnections(listOf(d1, d2), listOf(connection()))
        val source = result.first().virtualWaypoints.first()

        assertEquals(180f, source.second)
    }

    // endregion

    // region connection path assembly

    @Test
    fun validConnectionWithoutWaypoints_pathStartsAtSourceAndEndsAtTarget() {
        val d1 = device("d1", 100f, 100f, 200f, 80f, listOf(port("p-out", DeviceSide.RIGHT, 40f)))
        val d2 = device("d2", 500f, 100f, 200f, 80f, listOf(port("p-in", DeviceSide.LEFT, 40f, PortDirection.INPUT)))

        val result = ConnectionPathConverter.convertConnections(listOf(d1, d2), listOf(connection()))
        val waypoints = result.first().virtualWaypoints

        assertEquals(2, waypoints.size)
        // Source: RIGHT of d1 → x=300, y=140
        assertEquals(300f, waypoints.first().first)
        // Target: LEFT of d2 → x=500, y=140
        assertEquals(500f, waypoints.last().first)
    }

    @Test
    fun connectionWithRoutingWaypoints_intermediatePointsInsertedBetweenPorts() {
        val d1 = device("d1", 100f, 100f, 200f, 80f, listOf(port("p-out", DeviceSide.RIGHT, 40f)))
        val d2 = device("d2", 500f, 100f, 200f, 80f, listOf(port("p-in", DeviceSide.LEFT, 40f, PortDirection.INPUT)))
        val waypoint = Point(350f, 200f)

        val result = ConnectionPathConverter.convertConnections(
            listOf(d1, d2), listOf(connection(routingPoints = listOf(waypoint)))
        )
        val virtualWaypoints = result.first().virtualWaypoints

        assertEquals(3, virtualWaypoints.size)
        assertEquals(350f, virtualWaypoints[1].first)
        assertEquals(200f, virtualWaypoints[1].second)
    }

    @Test
    fun connectionWithRoutingWaypoints_successIsTrue() {
        val d1 = device("d1", 100f, 100f, 200f, 80f, listOf(port("p-out", DeviceSide.RIGHT, 0f)))
        val d2 = device("d2", 500f, 100f, 200f, 80f, listOf(port("p-in", DeviceSide.LEFT, 0f, PortDirection.INPUT)))

        val result = ConnectionPathConverter.convertConnections(
            listOf(d1, d2), listOf(connection(routingPoints = listOf(Point(350f, 100f))))
        )
        assertTrue(result.first().success)
    }

    @Test
    fun connectionWithoutRoutingWaypoints_successIsFalse() {
        val d1 = device("d1", 100f, 100f, 200f, 80f, listOf(port("p-out", DeviceSide.RIGHT, 0f)))
        val d2 = device("d2", 500f, 100f, 200f, 80f, listOf(port("p-in", DeviceSide.LEFT, 0f, PortDirection.INPUT)))

        val result = ConnectionPathConverter.convertConnections(listOf(d1, d2), listOf(connection()))
        assertFalse(result.first().success)
    }

    @Test
    fun connectionReferencingMissingDevice_successIsFalse() {
        val d1 = device("d1", 100f, 100f, 200f, 80f, listOf(port("p-out", DeviceSide.RIGHT, 0f)))
        val badConnection = connection(targetDeviceId = "ghost")

        val result = ConnectionPathConverter.convertConnections(listOf(d1), listOf(badConnection))

        assertFalse(result.first().success)
        assertTrue(result.first().virtualWaypoints.isEmpty())
    }

    @Test
    fun connectionReferencingMissingPort_successIsFalse() {
        val d1 = device("d1", 100f, 100f, 200f, 80f, listOf(port("p-out", DeviceSide.RIGHT, 0f)))
        val d2 = device("d2", 500f, 100f, 200f, 80f, listOf(port("p-in", DeviceSide.LEFT, 0f, PortDirection.INPUT)))
        val badConnection = connection(targetPortId = "ghost-port")

        val result = ConnectionPathConverter.convertConnections(listOf(d1, d2), listOf(badConnection))

        assertFalse(result.first().success)
        assertTrue(result.first().virtualWaypoints.isEmpty())
    }

    // endregion

    // region shared port convergence

    @Test
    fun twoConnectionsSharingTargetPort_bothHaveSameConvergenceWaypointBeforePort() {
        // d1 and d3 both connect to d2's LEFT port at position 40
        // d2 LEFT port at position 40 → world pos = (300, 40)
        // Convergence point (LEFT side) = (300 - 40, 40) = (260, 40)
        val d1 = device("d1", 0f, 0f, 100f, 80f, listOf(port("out1", DeviceSide.RIGHT, 40f)))
        val d2 = device("d2", 300f, 0f, 100f, 80f, listOf(port("in", DeviceSide.LEFT, 40f, PortDirection.INPUT)))
        val d3 = device("d3", 0f, 200f, 100f, 80f, listOf(port("out2", DeviceSide.RIGHT, 40f)))

        val c1 = connection("c1", "d1", "out1", "d2", "in", routingPoints = listOf(Point(200f, 40f)))
        val c2 = connection("c2", "d3", "out2", "d2", "in", routingPoints = listOf(Point(200f, 240f)))

        val result = ConnectionPathConverter.convertConnections(listOf(d1, d2, d3), listOf(c1, c2))

        val w1 = result[0].virtualWaypoints
        val w2 = result[1].virtualWaypoints

        val expectedConvergence = Pair(260f, 40f)
        val expectedPort = Pair(300f, 40f)

        assertEquals(expectedConvergence, w1[w1.size - 2])
        assertEquals(expectedConvergence, w2[w2.size - 2])
        assertEquals(expectedPort, w1.last())
        assertEquals(expectedPort, w2.last())
    }

    @Test
    fun twoConnectionsSharingSourcePort_bothHaveSameDivergenceWaypointAfterPort() {
        // d1's RIGHT port at position 40 → world pos = (100, 40)
        // Divergence point (RIGHT side) = (100 + 40, 40) = (140, 40)
        val d1 = device("d1", 0f, 0f, 100f, 80f, listOf(port("out", DeviceSide.RIGHT, 40f)))
        val d2 = device("d2", 300f, 0f, 100f, 80f, listOf(port("in1", DeviceSide.LEFT, 40f, PortDirection.INPUT)))
        val d3 = device("d3", 300f, 200f, 100f, 80f, listOf(port("in2", DeviceSide.LEFT, 40f, PortDirection.INPUT)))

        val c1 = connection("c1", "d1", "out", "d2", "in1", routingPoints = listOf(Point(200f, 40f)))
        val c2 = connection("c2", "d1", "out", "d3", "in2", routingPoints = listOf(Point(200f, 240f)))

        val result = ConnectionPathConverter.convertConnections(listOf(d1, d2, d3), listOf(c1, c2))

        val w1 = result[0].virtualWaypoints
        val w2 = result[1].virtualWaypoints

        val expectedDivergence = Pair(140f, 40f)
        val expectedPort = Pair(100f, 40f)

        assertEquals(expectedDivergence, w1[1])
        assertEquals(expectedDivergence, w2[1])
        assertEquals(expectedPort, w1.first())
        assertEquals(expectedPort, w2.first())
    }

    @Test
    fun singleConnectionPerPort_noConvergenceWaypointInjected() {
        val d1 = device("d1", 100f, 100f, 200f, 80f, listOf(port("p-out", DeviceSide.RIGHT, 40f)))
        val d2 = device("d2", 500f, 100f, 200f, 80f, listOf(port("p-in", DeviceSide.LEFT, 40f, PortDirection.INPUT)))

        val result = ConnectionPathConverter.convertConnections(
            listOf(d1, d2), listOf(connection(routingPoints = listOf(Point(350f, 140f))))
        )

        // Source + 1 routing point + target = 3 waypoints, no extra convergence injected
        assertEquals(3, result.first().virtualWaypoints.size)
    }

    @Test
    fun twoConnectionsWithDifferentPorts_noConvergenceWaypointInjected() {
        // Each connection uses a unique source and target port — no sharing, no injection
        val d1 = device("d1", 0f, 0f, 100f, 80f, listOf(
            port("out1", DeviceSide.RIGHT, 20f),
            port("out2", DeviceSide.RIGHT, 60f)
        ))
        val d2 = device("d2", 300f, 0f, 100f, 80f, listOf(
            port("in1", DeviceSide.LEFT, 20f, PortDirection.INPUT),
            port("in2", DeviceSide.LEFT, 60f, PortDirection.INPUT)
        ))

        val c1 = connection("c1", "d1", "out1", "d2", "in1", routingPoints = listOf(Point(200f, 20f)))
        val c2 = connection("c2", "d1", "out2", "d2", "in2", routingPoints = listOf(Point(200f, 60f)))

        val result = ConnectionPathConverter.convertConnections(listOf(d1, d2), listOf(c1, c2))

        // Each: source + 1 routing point + target = 3 waypoints, nothing extra
        assertEquals(3, result[0].virtualWaypoints.size)
        assertEquals(3, result[1].virtualWaypoints.size)
    }

    // endregion
}
