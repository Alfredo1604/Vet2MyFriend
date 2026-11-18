package com.alfredorosas.vet2myfriend.data.directory.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE clinics ADD COLUMN latitude REAL")
        db.execSQL("ALTER TABLE clinics ADD COLUMN longitude REAL")
    }
}

internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS clinic_services (clinicId TEXT NOT NULL, " +
                "service TEXT NOT NULL, price TEXT, doctor TEXT, PRIMARY KEY(clinicId, service), " +
                "FOREIGN KEY(clinicId) REFERENCES clinics(id) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        insertReportedServices(db)
        // Correct only unchanged seed addresses; preserve any later edits/imports.
        db.execSQL(
            "UPDATE clinics SET address = ? WHERE id = ? AND address = ?",
            arrayOf("20 de Noviembre, Nuevo San Rafael, 67110 Guadalupe, N.L.",
                "guadalupe-los-pequenos-amigos",
                "Av. Circunvalación y 20 de Noviembre, Nuevo San Rafael, Guadalupe, N.L."),
        )
        db.execSQL(
            "UPDATE clinics SET address = ? WHERE id = ? AND address = ?",
            arrayOf("Av. la Hacienda 1312, Sin Nombre de Col 30, 67155 Guadalupe, N.L.",
                "guadalupe-la-hacienda", "Av. la Hacienda 1312, Guadalupe, N.L."),
        )
    }
}

/** Initial reports from the founder's visits, not a live guarantee of availability. */
internal fun insertReportedServices(db: SupportSQLiteDatabase) {
    val reported = mapOf(
        "guadalupe-la-hacienda" to listOf("Consulta", "Vacunación", "Esterilización", "Cirugía", "Corte de pelo"),
        "guadalupe-los-pequenos-amigos" to listOf("Consulta", "Vacunación"),
    )
    for ((clinicId, services) in reported) {
        for (service in services) {
            db.execSQL(
                "INSERT OR IGNORE INTO clinic_services (clinicId, service, price, doctor) " +
                    "SELECT id, ?, '0', NULL FROM clinics WHERE id = ?",
                arrayOf(service, clinicId),
            )
        }
    }
}
