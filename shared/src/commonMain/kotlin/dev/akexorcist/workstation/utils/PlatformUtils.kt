package dev.akexorcist.workstation.utils

import dev.akexorcist.workstation.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

expect fun openUrl(url: String)

@OptIn(ExperimentalResourceApi::class)
suspend fun readResourceFile(path: String): String {
    return Res.readBytes("files/$path").decodeToString()
}