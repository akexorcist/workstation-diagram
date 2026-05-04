package dev.akexorcist.workstation.utils

import kotlin.js.js
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
private val openWindow: (String) -> Unit = js("url => window.open(url, '_blank')")

actual fun openUrl(url: String) {
    try {
        openWindow(url)
    } catch (e: Exception) {
        println("Error opening URL: $e")
    }
}
