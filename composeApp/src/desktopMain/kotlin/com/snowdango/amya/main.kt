package com.snowdango.amya

import amya.composeapp.generated.resources.Res
import amya.composeapp.generated.resources.icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.snowdango.amya.platform.Log
import com.snowdango.amya.platform.getPlatform
import io.github.vinceglb.filekit.FileKit
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import java.awt.Dimension

fun main() {
    Log.d("App Launched Platform: ${getPlatform().platformType}")
    application {
        val minimumDpSize = DpSize(800.dp, 600.dp)
        val windowState = rememberWindowState(size = minimumDpSize * 2)
        init()
        val viewModel: MainViewModel by inject(clazz = MainViewModel::class.java) // ViewModelを継承していないが事実上のViewModel
        val isClosedMinimized = viewModel.isClosedMinimize.collectAsState()
        val isShowTray = viewModel.isShowTray.collectAsState()
        var isWindowVisible by remember { mutableStateOf(true) }

        if (isShowTray.value) {
            Tray(
                icon = painterResource(Res.drawable.icon),
                onAction = {
                    isWindowVisible = true
                },
                menu = {
                    Item(
                        text = "Open",
                        onClick = {
                            Log.d("Open Window")
                            isWindowVisible = true
                        }
                    )
                    Item(
                        text = "Quit",
                        onClick = {
                            Log.d("Quit App")
                            exitApplication()
                        }
                    )
                }
            )
        }
        if (isWindowVisible) {
            Window(
                onCloseRequest = {
                    if (isClosedMinimized.value) {
                        Log.d("Window Close Request")
                        windowState.isMinimized = true
                    } else {
                        Log.d("Window Minimize Request")
                        if (isShowTray.value){
                            isWindowVisible = false
                        }else{
                            exitApplication()
                        }
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
}

fun init() {
    if (GlobalContext.getOrNull() == null) {
        startKoin { modules(module) }
    }
    FileKit.init(appId = "Amya")
}

@Composable
fun DpSize.toSize(): Dimension {
    with (LocalDensity.current) {
        return Dimension(width.toPx().toInt(), height.toPx().toInt())
    }
}