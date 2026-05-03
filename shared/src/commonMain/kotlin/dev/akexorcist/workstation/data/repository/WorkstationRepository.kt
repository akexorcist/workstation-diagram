package dev.akexorcist.workstation.data.repository

import dev.akexorcist.workstation.data.model.ManifestResult
import dev.akexorcist.workstation.data.model.RevisionManifest
import dev.akexorcist.workstation.data.model.WorkstationLayout
import dev.akexorcist.workstation.data.serialization.WorkstationLayoutSerializer
import dev.akexorcist.workstation.data.validation.DataValidator
import dev.akexorcist.workstation.data.validation.ValidationResult
import dev.akexorcist.workstation.utils.readResourceFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

sealed class LoadResult {
    data class Success(val layout: WorkstationLayout) : LoadResult()
    data class PartialSuccess(val layout: WorkstationLayout, val errors: List<String>) : LoadResult()
    data class Error(val message: String, val cause: Throwable? = null) : LoadResult()
}

interface WorkstationRepository {
    suspend fun loadManifest(): ManifestResult
    suspend fun loadLayout(): LoadResult
    suspend fun loadLayoutFromFile(path: String): LoadResult
    suspend fun loadLayoutFromJson(jsonString: String): LoadResult
    fun validateLayout(layout: WorkstationLayout): ValidationResult
}

class WorkstationRepositoryImpl : WorkstationRepository {
    private val manifestJson = Json { ignoreUnknownKeys = true }

    override suspend fun loadManifest(): ManifestResult = withContext(Dispatchers.Default) {
        try {
            val jsonString = readResourceFile("data/manifest.json")
            val manifest = manifestJson.decodeFromString<RevisionManifest>(jsonString)
            ManifestResult.Success(manifest)
        } catch (e: Exception) {
            ManifestResult.Error("Failed to load manifest: ${e.message}", e)
        }
    }

    override suspend fun loadLayout(): LoadResult = withContext(Dispatchers.Default) {
        try {
            val jsonString = readResourceFile("data/workstation.json")
            loadLayoutFromJson(jsonString)
        } catch (e: Exception) {
            LoadResult.Error("Failed to load workstation data: ${e.message}", e)
        }
    }

    override suspend fun loadLayoutFromFile(path: String): LoadResult = withContext(Dispatchers.Default) {
        try {
            val jsonString = readResourceFile(path)
            loadLayoutFromJson(jsonString)
        } catch (e: Exception) {
            LoadResult.Error("Failed to load layout from $path: ${e.message}", e)
        }
    }

    override suspend fun loadLayoutFromJson(jsonString: String): LoadResult = withContext(Dispatchers.Default) {
        try {
            val layout = WorkstationLayoutSerializer.fromJson(jsonString)
                .getOrElse { error -> return@withContext LoadResult.Error("Failed to parse JSON: ${error.message}", error) }

            when (val validationResult = DataValidator.validateLayout(layout)) {
                is ValidationResult.Success -> LoadResult.Success(layout)
                is ValidationResult.Error -> LoadResult.PartialSuccess(layout, listOf(validationResult.message))
            }
        } catch (e: Exception) {
            LoadResult.Error("Failed to load layout: ${e.message}", e)
        }
    }

    override fun validateLayout(layout: WorkstationLayout): ValidationResult {
        return DataValidator.validateLayout(layout)
    }
}