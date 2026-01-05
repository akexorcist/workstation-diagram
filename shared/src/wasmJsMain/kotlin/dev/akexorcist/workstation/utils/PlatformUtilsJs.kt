package dev.akexorcist.workstation.utils

import kotlin.js.js
import kotlin.js.JsAny
import kotlin.js.ExperimentalWasmJsInterop
import kotlinx.coroutines.await

@OptIn(ExperimentalWasmJsInterop::class)
private val openWindow: (String) -> Unit = js("url => window.open(url, '_blank')")

@OptIn(ExperimentalWasmJsInterop::class)
private val fetchJs: (String) -> kotlin.js.Promise<JsAny> = js("fetch")

@OptIn(ExperimentalWasmJsInterop::class)
private val getOk: (JsAny) -> Boolean = js("(response) => response.ok")

@OptIn(ExperimentalWasmJsInterop::class)
private val getStatus: (JsAny) -> Int = js("(response) => response.status")

@OptIn(ExperimentalWasmJsInterop::class)
private val callText: (JsAny) -> kotlin.js.Promise<JsAny> = js("(response) => response.text()")

@OptIn(ExperimentalWasmJsInterop::class)
private val toStringJs: (JsAny) -> String = js("(value) => String(value)")

@OptIn(ExperimentalWasmJsInterop::class)
private val resolvePath: (String) -> String = js("(path) => { const base = window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/') + 1); return base + path; }")

actual fun openUrl(url: String) {
    try {
        openWindow(url)
    } catch (e: Exception) {
        println("Error opening URL: $e")
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
actual suspend fun readResourceFile(path: String): String {
    val resolvedPath = resolvePath(path)
    val response = fetchJs(resolvedPath).await<JsAny>()
    if (!getOk(response)) {
        val status = getStatus(response)
        throw IllegalStateException("Failed to load resource: $resolvedPath ($status)")
    }
    val textPromise = callText(response)
    val textValue = textPromise.await<JsAny>()
    return toStringJs(textValue)
}
