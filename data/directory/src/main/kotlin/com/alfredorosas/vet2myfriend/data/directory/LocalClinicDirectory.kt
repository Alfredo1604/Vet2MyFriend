package com.alfredorosas.vet2myfriend.data.directory

import android.content.Context
import androidx.room.Room
import com.alfredorosas.vet2myfriend.data.directory.local.ClinicDao
import com.alfredorosas.vet2myfriend.data.directory.local.ClinicEntity
import com.alfredorosas.vet2myfriend.data.directory.local.DirectoryDatabase
import com.alfredorosas.vet2myfriend.data.directory.local.InitialClinicCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ClinicRecord(
    val id: String,
    val name: String,
    val address: String,
    val municipality: String,
)

/** Local source for the directory. The app should keep one instance for its lifetime. */
class LocalClinicDirectory private constructor(private val dao: ClinicDao) {

    fun observeClinics(): Flow<List<ClinicRecord>> =
        dao.observeAll().map { clinics -> clinics.map { it.toRecord() } }

    fun observeClinic(id: String): Flow<ClinicRecord?> =
        dao.observeById(id).map { it?.toRecord() }

    suspend fun upsertClinics(clinics: List<ClinicRecord>) {
        dao.upsert(clinics.map { ClinicEntity(it.id, it.name, it.address, it.municipality) })
    }

    companion object {
        fun open(context: Context): LocalClinicDirectory {
            val database = Room.databaseBuilder(
                context.applicationContext,
                DirectoryDatabase::class.java,
                "vet2myfriend-directory.db",
            ).addCallback(InitialClinicCatalog).build()
            return LocalClinicDirectory(database.clinicDao())
        }
    }
}

private fun ClinicEntity.toRecord(): ClinicRecord =
    ClinicRecord(id, name, address, municipality)
