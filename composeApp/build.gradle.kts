import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.buildKonfig)
    alias(libs.plugins.aboutLibraries)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.materialKolor)
                implementation(libs.composeIcons.tablerIcons)
                implementation(libs.bundles.room)
                implementation(libs.bundles.koin)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.bundles.paging)
                api(libs.bundles.logging)
                implementation(libs.bundles.coil)
                implementation(libs.ktor.client.core)
                implementation(libs.bundles.filekit)
                implementation(libs.kmp.process)
                api(libs.datastore.preferences)
                api(libs.aboutlibraries.core)
            }
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.bundles.coroutines)
            implementation(libs.composeIcons.tablerIcons)
            implementation(libs.bundles.koin)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.bundles.paging)
            implementation(libs.bundles.coil)
            implementation(libs.ktor.client.java)
            implementation(libs.kmp.process)
            implementation(libs.bundles.settings)
            implementation(libs.appDirs)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspDesktop", libs.androidx.room.compiler)
}

buildkonfig {
    packageName = "com.snowdango.amya"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "osName", "MaxOS")
        buildConfigField(FieldSpec.Type.STRING, "appVersion", libs.versions.app.version.get())
    }
    targetConfigs {
        create("macos") {
            buildConfigField(FieldSpec.Type.STRING, "osName", "MacOS")
        }
        create("mingw") {
            buildConfigField(FieldSpec.Type.STRING, "osName", "Windows")
        }
        create("linux") {
            buildConfigField(FieldSpec.Type.STRING, "osName", "Linux")
        }
    }
}

aboutLibraries {
    offlineMode = false
    android {
        registerAndroidTasks = false
    }
    export {
        outputFile = file("src/commonMain/composeResources/files/aboutlibraries.json")
        prettyPrint = true
    }
}

compose.desktop {
    application {
        mainClass = "com.snowdango.amya.MainKt"

        nativeDistributions {
            outputBaseDir.set(project.layout.buildDirectory.dir("customOutputDir"))
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.snowdango.amya"
            packageVersion = libs.versions.app.version.get()
            linux {
                modules("jdk.security.auth")
                iconFile.set(project.file("icons/icon.png"))
            }
            windows {
                iconFile.set(project.file("icons/icon.ico"))
            }
            macOS {
                iconFile.set(project.file("icons/icon.icns"))
            }
        }
    }
}

