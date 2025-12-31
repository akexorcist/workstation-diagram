package dev.akexorcist.workstation.utils

import kotlinx.browser.window
import kotlin.js.Date

actual fun openUrl(url: String) {
    window.open(url, "_blank")
}

actual fun getCurrentDate(): String {
    val date = Date()
    return date.toISOString().split("T")[0]
}

actual fun readResourceFile(path: String): String {
    // For JS, resources are loaded differently
    // This is a simplified version - actual implementation would use fetch API
    throw NotImplementedError("Async resource loading not implemented for JS target")
}