package com.alfredorosas.vet2myfriend.data.directory

import android.content.Context
import androidx.room.Room
import com.alfredorosas.vet2myfriend.domain.GeoPoint
import com.alfredorosas.vet2myfriend.domain.model.ClinicService
import com.alfredorosas.vet2myfriend.data.directory.local.ClinicServiceEntity
import com.alfredorosas.vet2myfriend.data.directory.local.ClinicWithServices
import com.alfredorosas.vet2myfriend.data.directory.local.MIGRATION_2_3
import com.alfredorosas.vet2myfriend.data.directory.local.MIGRATION_1_2
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
    val location: GeoPoint? = null,
    val services: List<ClinicService> = emptyList(),
)

/** Local source for the directory. The app should keep one instance for its lifetime. */
class LocalClinicDirectory private constructor(private val dao: ClinicDao) {

    fun observeClinics(): Flow<List<ClinicRecord>> =
        dao.observeAllWithServices().map { clinics ->
            clinics.map { it.toRecord() }
        }

    fun observeClinic(id: String): Flow<ClinicRecord?> =
        dao.observeWithServices(id).map { it?.toRecord() }

    suspend fun upsertClinics(clinics: List<ClinicRecord>) {
        dao.upsertWithServices(
            clinics.map { clinic ->
                ClinicEntity(
                    id = clinic.id,
                    name = clinic.name,
                    address = clinic.address,
                    municipality = clinic.municipality,
                    latitude = clinic.location?.latitude,
                    longitude = clinic.location?.longitude,
                )
            },
            clinics.flatMap { clinic ->
                clinic.services.map {
                    ClinicServiceEntity(clinic.id, it.service, it.price?.toPlainString(), it.doctor)
                }
            },
        )
    }

    companion object {
        fun open(context: Context): LocalClinicDirectory {
            val database = Room.databaseBuilder(
                context.applicationContext,
                DirectoryDatabase::class.java,
                "vet2myfriend-directory.db",
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(InitialClinicCatalog)
                .build()
            return LocalClinicDirectory(database.clinicDao())
        }
    }
}

private fun ClinicEntity.toRecord(): ClinicRecord =
    ClinicRecord(
        id = id,
        name = name,
        address = address,
        municipality = municipality,
        location = if (latitude != null && longitude != null) GeoPoint(latitude, longitude) else null,
    )

private fun ClinicWithServices.toRecord(): ClinicRecord = clinic.toRecord().copy(
    services = services.sortedBy { it.service }.map {
        ClinicService(it.price?.toBigDecimal(), it.service, it.doctor)
    },
)
