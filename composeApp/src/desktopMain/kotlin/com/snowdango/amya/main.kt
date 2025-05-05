package com.snowdango.amya

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.snowdango.amya.platform.Log
import com.snowdango.amya.platform.getPlatform
import io.github.vinceglb.filekit.FileKit
import org.koin.core.context.startKoin
import java.awt.Dimension

fun main() {
    Log.d("App Launched Platform: ${getPlatform().platformType}")
    application {
        val minimumDpSize = DpSize(800.dp, 600.dp)
        val windowState = rememberWindowState(size = minimumDpSize * 2)
        startKoin { modules(module) }
        FileKit.init(appId = "Amya")
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Amya",
        ) {
            window.minimumSize = minimumDpSize.toSize()
            App()
        }
    }
}

@Composable
fun DpSize.toSize(): Dimension {
    with (LocalDensity.current) {
        return Dimension(width.toPx().toInt(), height.toPx().toInt())
    }
}