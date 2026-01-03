import org.jetbrains.compose.resources.ResourcesExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(17)

    jvm()

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("workstation-editor")
        browser {
            commonWebpackConfig {
                outputFileName = "workstation-editor.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":shared"))
                
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.kotlinx.datetime)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(compose.ui)
                implementation(compose.runtime)
            }
        }
    }
}

compose {
    desktop {
        application {
            mainClass = "dev.akexorcist.workstation.editor.MainKt"
        }
    }
    resources {
        publicResClass = true
        packageOfResClass = "com.akexorcist.workstation.editor.resources"
        generateResClass = ResourcesExtension.ResourceClassGeneration.Auto
    }
}

tasks.named("wasmJsProcessResources") {
    (this as ProcessResources).duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

