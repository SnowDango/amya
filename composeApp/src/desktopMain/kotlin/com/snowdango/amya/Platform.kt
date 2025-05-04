package com.snowdango.amya

import androidx.room.Room
import com.snowdango.amya.domain.db.AppsDatabase
import com.snowdango.amya.domain.db.getRoomDatabaseBuild
import java.io.File

class JVMPlatform {
    val name: String = "Java ${System.getProperty("java.version")}"
}

fun getPlatform() = JVMPlatform()

fun getDatabaseBuilder(): AppsDatabase{
    val dbFile = File(System.getProperty("java.io.tmpdir"), "amya_apps.db")
    return Room.databaseBuilder<AppsDatabase>(
        name = dbFile.absolutePath
    ).getRoomDatabaseBuild()
}