package dev.akexorcist.workstation.utils

import java.awt.Desktop
import java.net.URI
import java.time.LocalDate

actual fun openUrl(url: String) {
    try {
        Desktop.getDesktop().browse(URI(url))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

actual fun getCurrentDate(): String {
    return LocalDate.now().toString()
}

actual fun readResourceFile(path: String): String {
    // Remove leading slash if present, as ClassLoader.getResourceAsStream doesn't expect it
    val resourcePath = path.removePrefix("/")
    val inputStream = Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)
        ?: throw IllegalStateException("Resource file not found: $resourcePath")
    return inputStream.bufferedReader().use { it.readText() }
}