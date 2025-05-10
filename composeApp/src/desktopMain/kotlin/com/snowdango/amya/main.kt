package com.snowdango.amya

import amya.composeapp.generated.resources.Res
import amya.composeapp.generated.resources.icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snowdango.amya.platform.Log
import com.snowdango.amya.platform.getPlatform
import io.github.vinceglb.filekit.FileKit
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import java.awt.Dimension

fun main() {
    Log.d("App Launched Platform: ${getPlatform().platformType}")
    application {
        val minimumDpSize = DpSize(800.dp, 600.dp)
        val windowState = rememberWindowState(size = minimumDpSize * 2)
        init()
        val viewModel: MainViewModel = koinViewModel()
        val isClosedMinimized = viewModel.isClosedMinimize().collectAsStateWithLifecycle(initialValue = true)
        Window(
            onCloseRequest = {
                if (isClosedMinimized.value) {
                    windowState.isMinimized = true
                } else {
                    exitApplication()
                }
            },
            state = windowState,
            title = "Amya",
            icon = painterResource(Res.drawable.icon)
        ) {
            window.minimumSize = minimumDpSize.toSize()
            App()
        }
    }
}

fun init() {
    startKoin { modules(module) }
    FileKit.init(appId = "Amya")
}

@Composable
fun DpSize.toSize(): Dimension {
    with (LocalDensity.current) {
        return Dimension(width.toPx().toInt(), height.toPx().toInt())
    }
}