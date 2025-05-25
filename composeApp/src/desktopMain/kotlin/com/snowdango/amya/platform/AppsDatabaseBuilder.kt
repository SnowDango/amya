package com.snowdango.amya.platform

import androidx.room.Room
import androidx.room.RoomDatabase
import com.snowdango.amya.domain.db.AppsDatabase
import com.snowdango.amya.domain.db.addCommonOptions
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppsDatabase> {
    val dbFile = File(getAppDirs().getUserDataDir(), "amya_apps.db")
    return Room.databaseBuilder<AppsDatabase>(
        name = dbFile.absolutePath
    ).addCommonOptions()
}
