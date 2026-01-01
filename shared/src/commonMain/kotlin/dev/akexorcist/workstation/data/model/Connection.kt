package dev.akexorcist.workstation.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Connection(
    val id: String,
    val sourceDeviceId: String,
    val sourcePortId: String,
    val targetDeviceId: String,
    val targetPortId: String,
    val routingPoints: List<Point>? = null
)

@Serializable
data class Point(
    val x: Float,
    val y: Float
)
