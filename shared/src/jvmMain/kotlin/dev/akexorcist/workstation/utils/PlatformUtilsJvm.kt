package dev.akexorcist.workstation.utils

import java.awt.Desktop
import java.net.URI

actual fun openUrl(url: String) {
    try {
        Desktop.getDesktop().browse(URI(url))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}