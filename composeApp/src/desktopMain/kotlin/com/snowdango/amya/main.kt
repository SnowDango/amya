package com.snowdango.amya

import amya.composeapp.generated.resources.Res
import amya.composeapp.generated.resources.icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.registerSkikoComposeImplementation
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.diamondedge.logging.KmLogging
import com.snowdango.amya.platform.PlatformType
import com.snowdango.amya.platform.SingleInstance
import com.snowdango.amya.platform.getAppDirs
import com.snowdango.amya.platform.getPlatform
import com.snowdango.amya.steam.SteamSyncService
import com.snowdango.amya.track.Log
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import java.awt.Dimension
import java.io.File

fun init() {
    if (GlobalContext.getOrNull() == null) {
        startKoin { modules(module) }
    }
    FileKit.init(appId = "Amya")
    KmLogging.addLogger(Log.getEventLogger())
    Log.i("------ Init App [Platform: ${BuildKonfig.osName}] ------")
    // Steam同期の定期チェック (設定がONの間だけ動作する)
    GlobalContext.get().get<SteamSyncService>().start()
}

fun ApplicationScope.exitApp() {
    Log.i("------ Exit App ------")
    exitApplication()
}

private val minimumDpSize = DpSize(800.dp, 600.dp)

@OptIn(ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
fun main() {
    // Compose 1.13.0-beta01 で Window外 (application直下) の painterResource(xml) が起動直後に落ちる問題の回避策。
    // Windowのiconはapplication直下で生成されるため、以下の2点が必要になる。
    // - Skikoの描画実装は最初のWindow生成時にしか登録されないため、先に登録しておく (同一インスタンスの再登録は無視される)
    registerSkikoComposeImplementation()
    // - rememberVectorPainter が LocalGraphicsResourceCache (LocalOwner 由来) を参照して LocalOwner not present になるため、
    //   ベクター描画キャッシュ共有を無効化して 1.12 までと同じ挙動にする
    ComposeUiFlags.isVectorDrawCacheSharingEnabled = false

    // DB・設定ファイルを開く前に二重起動を確認する。既に起動している場合はそちらのウィンドウを表示して終了する
    val singleInstance = SingleInstance.acquire(getAppDirs().getUserDataDir().let(::File)) ?: return
    val activateRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    singleInstance.onActivateRequest = { activateRequests.tryEmit(Unit) }

    init()
    application {
        val viewModel: MainViewModel by inject(clazz = MainViewModel::class.java) // ViewModelを継承していないが事実上のViewModel
        val isClosedMinimized = viewModel.isClosedMinimize.collectAsState()
        val isShowTray = viewModel.isShowTray.collectAsState()
        var isWindowVisible by remember { mutableStateOf(true) }
        var toFrontRequest by remember { mutableStateOf(0) }
        val windowState = rememberWindowState(size = minimumDpSize * 2)

        // トレイから開く・二重起動された時。最小化されている場合は元に戻して前面に出す
        val showWindow: () -> Unit = {
            isWindowVisible = true
            windowState.isMinimized = false
            toFrontRequest++
        }
        LaunchedEffect(Unit) {
            activateRequests.collect {
                Log.d("Activate Request")
                showWindow()
            }
        }

        tray(
            isShowTray = isShowTray.value,
            onVisibleWindow = showWindow,
        )

        window(
            windowState = windowState,
            isWindowVisible = isWindowVisible,
            toFrontRequest = toFrontRequest,
            isClosedMinimized = isClosedMinimized.value,
            isShowTray = isShowTray.value,
            onInVisibleWindow = {
                isWindowVisible = false
            },
        )
    }
    singleInstance.release()
}

@Composable
fun ApplicationScope.tray(
    isShowTray: Boolean,
    onVisibleWindow: () -> Unit,
) {
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
    windowState: WindowState,
    isWindowVisible: Boolean,
    toFrontRequest: Int,
    isClosedMinimized: Boolean,
    isShowTray: Boolean,
    onInVisibleWindow: () -> Unit,
) {
    // ウィンドウを開いた時 (トレイから表示・最小化から復帰) にSteamの状態を確認する
    LaunchedEffect(isWindowVisible, windowState.isMinimized) {
        if (isWindowVisible && !windowState.isMinimized) {
            GlobalContext.get().get<SteamSyncService>().requestSync()
        }
    }
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
            LaunchedEffect(toFrontRequest) {
                if (toFrontRequest > 0) window.toFront()
            }
            val platform = getPlatform()
            App(
                isWindows = platform.platformType == PlatformType.WINDOWS,
                onQuit = { exitApp() },
            )
        }
    }
}

@Composable
fun DpSize.toSize(): Dimension {
    with(LocalDensity.current) {
        return Dimension(width.toPx().toInt(), height.toPx().toInt())
    }
}
