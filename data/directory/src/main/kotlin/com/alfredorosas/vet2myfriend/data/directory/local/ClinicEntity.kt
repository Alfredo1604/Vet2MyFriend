package com.alfredorosas.vet2myfriend.data.directory.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clinics")
internal data class ClinicEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val municipality: String,
)
