package dev.akexorcist.workstation.data.validation

import dev.akexorcist.workstation.data.model.*
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DataValidatorTest {

    // region helpers

    private fun port(
        id: String,
        direction: PortDirection = PortDirection.OUTPUT,
        position: Float = 0f
    ) = Port(
        id = id,
        name = id,
        direction = direction,
        position = PortPosition(DeviceSide.RIGHT, position)
    )

    private fun device(
        id: String = "d1",
        posX: Float = 100f,
        posY: Float = 100f,
        width: Float = 200f,
        height: Float = 100f,
        specs: List<InformationItem> = listOf(InformationItem("CPU", "M4")),
        ports: List<Port> = emptyList()
    ) = Device(
        id = id,
        description = "Device $id",
        title = id,
        label = id,
        category = DeviceCategory.DEVICE,
        position = Position(posX, posY),
        size = Size(width, height),
        ports = ports,
        specifications = specs
    )

    private fun layout(
        devices: List<Device> = listOf(device()),
        connections: List<Connection> = emptyList(),
        canvasWidth: Float = 10000f,
        canvasHeight: Float = 10000f
    ) = WorkstationLayout(
        devices = devices,
        connections = connections,
        metadata = LayoutMetadata(
            title = "Test",
            date = "2025-01-01",
            canvasSize = Size(canvasWidth, canvasHeight)
        )
    )

    // endregion

    @Test
    fun validLayout_returnsSuccess() {
        val result = DataValidator.validateLayout(layout())
        assertIs<ValidationResult.Success>(result)
    }

    @Test
    fun duplicateDeviceId_returnsError() {
        val same = device(id = "dup")
        val result = DataValidator.validateLayout(layout(devices = listOf(same, same.copy(description = "Other"))))
        val error = assertIs<ValidationResult.Error>(result)
        assertTrue(error.message.contains("Duplicate device ID: dup"))
    }

    @Test
    fun devicePositionOutsideCanvasBounds_returnsError() {
        val outside = device(posX = 15000f, posY = 100f)
        val result = DataValidator.validateLayout(layout(devices = listOf(outside), canvasWidth = 10000f))
        assertIs<ValidationResult.Error>(result)
    }

    @Test
    fun deviceZeroWidth_returnsError() {
        val zeroWidth = device(width = 0f)
        val result = DataValidator.validateLayout(layout(devices = listOf(zeroWidth)))
        val error = assertIs<ValidationResult.Error>(result)
        assertTrue(error.message.contains("invalid size"))
    }

    @Test
    fun deviceEmptySpecifications_returnsError() {
        val noSpecs = device(specs = emptyList())
        val result = DataValidator.validateLayout(layout(devices = listOf(noSpecs)))
        val error = assertIs<ValidationResult.Error>(result)
        assertTrue(error.message.contains("empty specifications"))
    }

    @Test
    fun duplicatePortId_returnsError() {
        val p1 = port("p1", PortDirection.OUTPUT)
        val p2 = port("p1", PortDirection.INPUT)
        val dev = device(ports = listOf(p1, p2))
        val result = DataValidator.validateLayout(layout(devices = listOf(dev)))
        val error = assertIs<ValidationResult.Error>(result)
        assertTrue(error.message.contains("duplicate port ID: p1"))
    }

    @Test
    fun portNegativePosition_returnsError() {
        val negativePort = Port("p1", "p1", PortDirection.OUTPUT, PortPosition(DeviceSide.RIGHT, -1f))
        val dev = device(ports = listOf(negativePort))
        val result = DataValidator.validateLayout(layout(devices = listOf(dev)))
        val error = assertIs<ValidationResult.Error>(result)
        assertTrue(error.message.contains("must be non-negative"))
    }

    @Test
    fun connectionReferencingUnknownDevice_returnsError() {
        val connection = Connection(
            id = "c1",
            sourceDeviceId = "ghost",
            sourcePortId = "p1",
            targetDeviceId = "d1",
            targetPortId = "p2"
        )
        val result = DataValidator.validateLayout(layout(connections = listOf(connection)))
        val error = assertIs<ValidationResult.Error>(result)
        assertTrue(error.message.contains("non-existent device: ghost"))
    }

    @Test
    fun selfConnection_returnsError() {
        val outPort = port("out", PortDirection.OUTPUT)
        val inPort = port("in", PortDirection.INPUT)
        val dev = device(id = "d1", ports = listOf(outPort, inPort))
        val self = Connection(
            id = "c1",
            sourceDeviceId = "d1",
            sourcePortId = "out",
            targetDeviceId = "d1",
            targetPortId = "in"
        )
        val result = DataValidator.validateLayout(layout(devices = listOf(dev), connections = listOf(self)))
        val error = assertIs<ValidationResult.Error>(result)
        assertTrue(error.message.contains("cannot connect device to itself"))
    }

    @Test
    fun inputToInputConnection_returnsError() {
        val dev1 = device(id = "d1", ports = listOf(port("p1", PortDirection.INPUT)))
        val dev2 = device(id = "d2", ports = listOf(port("p2", PortDirection.INPUT)))
        val connection = Connection("c1", "d1", "p1", "d2", "p2")
        val result = DataValidator.validateLayout(layout(devices = listOf(dev1, dev2), connections = listOf(connection)))
        val error = assertIs<ValidationResult.Error>(result)
        assertTrue(error.message.contains("INPUT to INPUT"))
    }

    @Test
    fun outputToOutputConnection_returnsError() {
        val dev1 = device(id = "d1", ports = listOf(port("p1", PortDirection.OUTPUT)))
        val dev2 = device(id = "d2", ports = listOf(port("p2", PortDirection.OUTPUT)))
        val connection = Connection("c1", "d1", "p1", "d2", "p2")
        val result = DataValidator.validateLayout(layout(devices = listOf(dev1, dev2), connections = listOf(connection)))
        val error = assertIs<ValidationResult.Error>(result)
        assertTrue(error.message.contains("OUTPUT to OUTPUT"))
    }

    @Test
    fun portUsedInMultipleConnections_returnsError() {
        val dev1 = device(id = "d1", ports = listOf(port("out", PortDirection.OUTPUT)))
        val dev2 = device(id = "d2", ports = listOf(port("in1", PortDirection.INPUT)))
        val dev3 = device(id = "d3", ports = listOf(port("in2", PortDirection.INPUT)))
        val c1 = Connection("c1", "d1", "out", "d2", "in1")
        val c2 = Connection("c2", "d1", "out", "d3", "in2")
        val result = DataValidator.validateLayout(
            layout(devices = listOf(dev1, dev2, dev3), connections = listOf(c1, c2))
        )
        assertIs<ValidationResult.Error>(result)
    }
}
