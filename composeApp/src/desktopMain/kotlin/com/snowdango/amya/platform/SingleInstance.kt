package com.snowdango.amya.platform

import com.snowdango.amya.track.Log
import java.io.File
import java.io.RandomAccessFile
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import kotlin.concurrent.thread

/**
 * アプリの二重起動を防ぐ。
 * 同じDB・設定ファイルに複数のプロセスが書き込むと壊れる可能性があるため、2つ目の起動では既に起動しているアプリに
 * ウィンドウの表示を依頼して終了する
 */
class SingleInstance private constructor(
    private val channel: FileChannel,
    private val lock: FileLock,
    private val server: ServerSocket?,
) {

    /** 2つ目の起動があった時に呼ばれる (呼ばれるスレッドはUIスレッドではない) */
    @Volatile
    var onActivateRequest: () -> Unit = {}

    init {
        server?.let { server ->
            thread(name = "amya-single-instance", isDaemon = true) {
                while (!server.isClosed) {
                    try {
                        server.accept().use { }
                        onActivateRequest.invoke()
                    } catch (_: Throwable) {
                        // close() された時など
                    }
                }
            }
        }
    }

    fun release() {
        try {
            server?.close()
            lock.release()
            channel.close()
        } catch (th: Throwable) {
            Log.e("Failed to release single instance lock: ${th.message}")
        }
    }

    companion object {
        private const val CONNECT_TIMEOUT_MILLIS = 1_000

        /**
         * ロックを取得する。既に起動している場合は、そのアプリにウィンドウの表示を依頼してnullを返す
         */
        fun acquire(dir: File): SingleInstance? {
            dir.mkdirs()
            val portFile = File(dir, "amya.port")
            val channel = RandomAccessFile(File(dir, "amya.lock"), "rw").channel
            val lock = try {
                channel.tryLock()
            } catch (th: Throwable) {
                Log.e("Failed to lock: ${th.message}")
                null
            }
            if (lock == null) {
                channel.close()
                requestActivate(portFile)
                return null
            }
            val server = try {
                ServerSocket(0, 1, InetAddress.getLoopbackAddress()).also {
                    portFile.writeText(it.localPort.toString())
                }
            } catch (th: Throwable) {
                // 表示の依頼を受け取れないだけで、二重起動の防止はできている
                Log.e("Failed to open single instance socket: ${th.message}")
                null
            }
            return SingleInstance(channel, lock, server)
        }

        private fun requestActivate(portFile: File) {
            try {
                val port = portFile.readText().trim().toInt()
                Socket().use {
                    it.connect(
                        InetSocketAddress(InetAddress.getLoopbackAddress(), port),
                        CONNECT_TIMEOUT_MILLIS
                    )
                }
            } catch (th: Throwable) {
                Log.e("Failed to activate running instance: ${th.message}")
            }
        }
    }
}
