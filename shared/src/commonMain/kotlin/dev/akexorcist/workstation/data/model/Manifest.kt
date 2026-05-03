package dev.akexorcist.workstation.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RevisionManifest(
    val revisions: List<String>
)

sealed class ManifestResult {
    data class Success(val manifest: RevisionManifest) : ManifestResult()
    data class Error(val message: String, val cause: Throwable? = null) : ManifestResult()
}
