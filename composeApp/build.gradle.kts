import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "workstation-diagram"
        browser {
            commonWebpackConfig {
                outputFileName = "workstation-diagram.js"
            }
        }
        binaries.executable()
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}


compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi)
            packageName = "Workstation Diagram"
            packageVersion = "1.0.0"
            description = "My workstation's interactive diagram"

            macOS {
                bundleID = "com.akexorcist.workstation.diagram"
                iconFile.set(project.file("image/icons/icon.icns"))
            }

            windows {
                iconFile.set(project.file("image/icons/icon.ico"))
            }

            linux {
                packageName = "com-akexorcist-workstation-diagram"
                debMaintainer = "akexorcist@gmail.com"
                appRelease = "1"
                debPackageVersion = "1.0.0"
                appCategory = "Utility"
                iconFile.set(project.file("image/icons/icon.png"))
            }
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.akexorcist.workstation.diagram.resources"
    generateResClass = ResourcesExtension.ResourceClassGeneration.Auto
}

compose.experimental {
    web.application {}
}
