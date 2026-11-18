package com.alfredorosas.vet2myfriend.data.directory.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

internal object InitialClinicCatalog : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        val clinics = listOf(
            ClinicEntity(
                id = "guadalupe-la-hacienda",
                name = "Centro de Bienestar Animal",
                address = "Av. la Hacienda 1312, Sin Nombre de Col 30, 67155 Guadalupe, N.L.",
                municipality = "Guadalupe",
            ),
            ClinicEntity(
                id = "guadalupe-los-pequenos-amigos",
                name = "Los Pequeños Amigos",
                address = "20 de Noviembre, Nuevo San Rafael, 67110 Guadalupe, N.L.",
                municipality = "Guadalupe",
            ),
        )
        for (clinic in clinics) {
            db.execSQL(
                "INSERT INTO clinics (id, name, address, municipality) VALUES (?, ?, ?, ?)",
                arrayOf(clinic.id, clinic.name, clinic.address, clinic.municipality),
            )
        }
        insertReportedServices(db)
    }
}
