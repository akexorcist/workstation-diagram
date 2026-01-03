package dev.akexorcist.workstation.editor.utils

import kotlin.js.ExperimentalWasmJsInterop
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.clipboard.Clipboard

@OptIn(ExperimentalWasmJsInterop::class)
private val getNavigator: () -> org.w3c.dom.Navigator? = js("() => navigator")

@OptIn(ExperimentalWasmJsInterop::class)
private val getDocument: () -> org.w3c.dom.Document? = js("() => document")

@OptIn(ExperimentalWasmJsInterop::class)
private val execCommand: (String) -> Boolean? = js("(cmd) => document.execCommand(cmd)")

@OptIn(ExperimentalWasmJsInterop::class)
actual fun copyToClipboard(text: String) {
    val navigator = getNavigator()
    val clipboard: Clipboard? = navigator?.clipboard
    
    if (clipboard != null) {
        try {
            clipboard.writeText(text)
        } catch (e: Throwable) {
            fallbackCopyToClipboard(text)
        }
    } else {
        fallbackCopyToClipboard(text)
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun fallbackCopyToClipboard(text: String) {
    val document = getDocument()
    val body = document?.body
    
    if (document != null && body != null) {
        val textArea = document.createElement("textarea") as? HTMLTextAreaElement
        if (textArea != null) {
            textArea.value = text
            textArea.style.position = "fixed"
            textArea.style.left = "-999999px"
            body.appendChild(textArea)
            textArea.select()
            
            execCommand("copy")
            
            body.removeChild(textArea)
        }
    }
}
