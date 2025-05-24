plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.buildKonfig) apply false
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.detekt)
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.buildKonfig)
    }
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        autoCorrect = true
        parallel = true
        config = files("${rootProject.projectDir}/config/detekt/detekt.yml")
        buildUponDefaultConfig = true
        ignoreFailures = true
        basePath = file("$rootDir/../").absolutePath
        source = files("src/commonMain/kotlin", "src/desktopMain/kotlin")
    }

    dependencies {
        detektPlugins(rootProject.libs.detekt.formatting)
    }
}