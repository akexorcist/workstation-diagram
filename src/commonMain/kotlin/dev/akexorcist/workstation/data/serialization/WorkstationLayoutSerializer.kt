package dev.akexorcist.workstation.data.serialization

import dev.akexorcist.workstation.data.model.WorkstationLayout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator

object WorkstationLayoutSerializer {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        classDiscriminator = "#class"
    }

    fun fromJson(jsonString: String): Result<WorkstationLayout> {
        return try {
            val layout = json.decodeFromString<WorkstationLayout>(jsonString)
            Result.success(layout)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun toJson(layout: WorkstationLayout): String {
        return json.encodeToString(WorkstationLayout.serializer(), layout)
    }
}