package dev.akexorcist.workstation.data.repository

import dev.akexorcist.workstation.data.model.ManifestResult
import dev.akexorcist.workstation.data.model.RevisionManifest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResolveLayoutPathTest {

    @Test
    fun manifestWithRevisions_returnsLastRevisionPath() {
        val manifest = RevisionManifest(revisions = listOf("data/workstation_1.json", "data/workstation_2.json"))
        val result = resolveLayoutPath(ManifestResult.Success(manifest))
        assertEquals("data/workstation_2.json", result.getOrNull())
    }

    @Test
    fun manifestWithSingleRevision_returnsThatRevisionPath() {
        val manifest = RevisionManifest(revisions = listOf("data/workstation_1.json"))
        val result = resolveLayoutPath(ManifestResult.Success(manifest))
        assertEquals("data/workstation_1.json", result.getOrNull())
    }

    @Test
    fun manifestWithNoRevisions_returnsFailure() {
        val manifest = RevisionManifest(revisions = emptyList())
        val result = resolveLayoutPath(ManifestResult.Success(manifest))
        assertTrue(result.isFailure)
    }

    @Test
    fun manifestError_returnsFailure() {
        val result = resolveLayoutPath(ManifestResult.Error("Failed to load"))
        assertTrue(result.isFailure)
    }
}
