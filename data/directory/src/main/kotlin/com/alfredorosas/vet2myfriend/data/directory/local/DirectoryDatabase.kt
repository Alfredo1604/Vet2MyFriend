package com.alfredorosas.vet2myfriend.data.directory.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ClinicEntity::class], version = 1, exportSchema = true)
internal abstract class DirectoryDatabase : RoomDatabase() {
    abstract fun clinicDao(): ClinicDao
}
