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
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.diamondedge.logging.KmLogging
import com.diamondedge.logging.logging
import com.snowdango.amya.exitApp
import com.snowdango.amya.platform.getPlatform
import com.snowdango.amya.track.Log
import io.github.vinceglb.filekit.FileKit
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import java.awt.Dimension


fun init() {
    if (GlobalContext.getOrNull() == null) {
        startKoin { modules(module) }
    }
    FileKit.init(appId = "Amya")
    KmLogging.addLogger(Log.getEventLogger())
    Log.i("------ Init App [Platform: ${BuildKonfig.osName}] ------")
}

fun ApplicationScope.exitApp() {
    Log.i("------ Exit App ------")
    exitApplication()
}


fun main() {
    init()
    application {
        val viewModel: MainViewModel by inject(clazz = MainViewModel::class.java) // ViewModelを継承していないが事実上のViewModel
        val isClosedMinimized = viewModel.isClosedMinimize.collectAsState()
        val isShowTray = viewModel.isShowTray.collectAsState()
        var isWindowVisible by remember { mutableStateOf(true) }

        tray(
            isShowTray = isShowTray.value,
            onVisibleWindow = {
                isWindowVisible = true
            }
        )

        window(
            isWindowVisible = isWindowVisible,
            isClosedMinimized = isClosedMinimized.value,
            isShowTray = isShowTray.value,
            onInVisibleWindow = {
                isWindowVisible = false
            },
        )
    }
}

@Composable
fun ApplicationScope.tray(
    isShowTray: Boolean,
    onVisibleWindow: () -> Unit,
){
    if (isShowTray) {
        Tray(
            icon = painterResource(Res.drawable.icon),
            onAction = {
                onVisibleWindow.invoke()
            },
            menu = {
                Item(
                    text = "Open",
                    onClick = {
                        Log.d("Open Window")
                        onVisibleWindow.invoke()
                    }
                )
                Item(
                    text = "Quit",
                    onClick = {
                        Log.d("Quit App")
                        exitApp()
                    }
                )
            }
        )
    }
}


@Composable
fun ApplicationScope.window(
    isWindowVisible: Boolean,
    isClosedMinimized: Boolean,
    isShowTray: Boolean,
    onInVisibleWindow: () -> Unit,
) {
    val minimumDpSize = DpSize(800.dp, 600.dp)
    val windowState = rememberWindowState(size = minimumDpSize * 2)
    if (isWindowVisible) {
        Window(
            onCloseRequest = {
                if (isClosedMinimized) {
                    Log.d("Window Close Request")
                    windowState.isMinimized = true
                } else {
                    Log.d("Window Minimize Request")
                    if (isShowTray) {
                        onInVisibleWindow.invoke()
                    } else {
                        exitApp()
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

@Composable
fun DpSize.toSize(): Dimension {
    with (LocalDensity.current) {
        return Dimension(width.toPx().toInt(), height.toPx().toInt())
    }
}