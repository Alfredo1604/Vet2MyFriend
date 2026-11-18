package com.alfredorosas.vet2myfriend.data.directory.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ClinicDao {
    @Transaction
    @Query("SELECT * FROM clinics ORDER BY name COLLATE NOCASE, id")
    fun observeAllWithServices(): Flow<List<ClinicWithServices>>

    @Transaction
    @Query("SELECT * FROM clinics WHERE id = :id")
    fun observeWithServices(id: String): Flow<ClinicWithServices?>

    @Query("SELECT * FROM clinics ORDER BY name COLLATE NOCASE, id")
    fun observeAll(): Flow<List<ClinicEntity>>

    @Query("SELECT * FROM clinics WHERE id = :id")
    fun observeById(id: String): Flow<ClinicEntity?>

    @Upsert
    suspend fun upsert(clinics: List<ClinicEntity>)

    @Query("DELETE FROM clinic_services WHERE clinicId = :clinicId")
    suspend fun deleteServices(clinicId: String)

    @Upsert
    suspend fun upsertServices(services: List<ClinicServiceEntity>)

    @Transaction
    suspend fun upsertWithServices(clinics: List<ClinicEntity>, services: List<ClinicServiceEntity>) {
        upsert(clinics)
        clinics.forEach { deleteServices(it.id) }
        upsertServices(services)
    }
}
