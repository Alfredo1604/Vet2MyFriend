package com.alfredorosas.vet2myfriend.data.directory.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ClinicDao {
    @Query("SELECT * FROM clinics ORDER BY name COLLATE NOCASE, id")
    fun observeAll(): Flow<List<ClinicEntity>>

    @Query("SELECT * FROM clinics WHERE id = :id")
    fun observeById(id: String): Flow<ClinicEntity?>

    @Upsert
    suspend fun upsert(clinics: List<ClinicEntity>)
}
