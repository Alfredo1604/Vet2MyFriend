package com.alfredorosas.vet2myfriend.data.directory.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Relation

@Entity(
    tableName = "clinic_services",
    primaryKeys = ["clinicId", "service"],
    foreignKeys = [ForeignKey(
        entity = ClinicEntity::class,
        parentColumns = ["id"],
        childColumns = ["clinicId"],
        onDelete = ForeignKey.CASCADE,
    )],
)
internal data class ClinicServiceEntity(
    val clinicId: String,
    val service: String,
    // Decimal text preserves the exact monetary amount; null is unknown.
    val price: String?,
    val doctor: String?,
)

internal data class ClinicWithServices(
    @Embedded val clinic: ClinicEntity,
    @Relation(parentColumn = "id", entityColumn = "clinicId")
    val services: List<ClinicServiceEntity>,
)
