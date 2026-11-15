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
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        ignoreFailures = true
        basePath = file("$rootDir/../").absolutePath
        source.setFrom(rootProject.file("src/commonMain/kotlin"), rootProject.file("src/desktopMain/kotlin"))
    }

    // detekt 1.23.x を JDK 25 以降で動かすための回避策
    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        // --jvm-target は 22 までしか受け付けない
        jvmTarget = "22"
        // jdkHome を渡すと同梱の古い Kotlin コンパイラが JDK のバージョン文字列(25.0.x)を解析できず落ちるため渡さない
        // (型解決は使っていないので解析結果には影響しない)
        jdkHome.convention(null as Directory?)
        jdkHome.set(null as Directory?)
    }

    dependencies {
        detektPlugins(rootProject.libs.detekt.formatting)
    }
}