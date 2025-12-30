package dev.akexorcist.workstation.utils

import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.withContext
import org.w3c.fetch.RequestInit
import kotlin.js.Date

actual fun getCurrentDate(): String {
    val date = Date()
    return date.toISOString().split("T")[0]
}

actual fun readResourceFile(path: String): String {
    // For JS, we'll need to fetch the resource file
    // This is a simplified version - in production you'd want proper async handling
    throw NotImplementedError("Async resource loading not implemented for JS target")
}