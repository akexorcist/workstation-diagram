package dev.akexorcist.workstation.utils

/**
 * Opens a URL in the default browser.
 * Platform-specific implementation required.
 */
expect fun openUrl(url: String)

/**
 * Reads a resource file from the classpath.
 * Platform-specific implementation required.
 */
expect suspend fun readResourceFile(path: String): String