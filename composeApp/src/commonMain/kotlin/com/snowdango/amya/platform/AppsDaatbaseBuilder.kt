package com.snowdango.amya.platform

import androidx.room.RoomDatabase
import com.snowdango.amya.domain.db.AppsDatabase


expect fun getDatabaseBuilder():  RoomDatabase.Builder<AppsDatabase>