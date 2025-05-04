import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
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
            }
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.composeIcons.tablerIcons)
            implementation(libs.bundles.koin)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.bundles.paging)
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


compose.desktop {
    application {
        mainClass = "com.snowdango.amya.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.snowdango.amya"
            packageVersion = libs.versions.app.version.get()
        }
    }
}

