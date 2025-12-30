package dev.akexorcist.workstation.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Connection(
    val id: String,
    val sourceDeviceId: String,
    val sourcePortId: String,
    val targetDeviceId: String,
    val targetPortId: String,
    val connectionType: ConnectionType,
    val cableSpecification: CableSpecification? = null,
    val routingPoints: List<Point>? = null
)

@Serializable
data class ConnectionType(
    val name: String,
    val category: ConnectionCategory
)

@Serializable
enum class ConnectionCategory {
    DATA,
    VIDEO,
    AUDIO,
    POWER,
    NETWORK
}

@Serializable
data class CableSpecification(
    val length: String? = null,
    val brand: String? = null,
    val notes: String? = null
)

@Serializable
data class Point(
    val x: Float,
    val y: Float
)