package com.alfredorosas.vet2myfriend.data.directory.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DirectoryMigrationTest {
    @Test
    fun migratesVersionTwoWithServicesAndPreservesCustomData() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "directory-services-migration-test.db"
        context.deleteDatabase(name)
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { old ->
            old.execSQL("CREATE TABLE clinics (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, " +
                "address TEXT NOT NULL, municipality TEXT NOT NULL, latitude REAL, longitude REAL)")
            old.execSQL("INSERT INTO clinics VALUES (?, ?, ?, ?, ?, ?)", arrayOf<Any?>(
                "guadalupe-la-hacienda", "Nombre conservado", "Av. la Hacienda 1312, Guadalupe, N.L.",
                "Guadalupe", 25.67, -100.25))
            old.execSQL("INSERT INTO clinics VALUES (?, ?, ?, ?, NULL, NULL)", arrayOf(
                "guadalupe-los-pequenos-amigos", "Otro centro", "Dirección corregida por el usuario", "Guadalupe"))
            old.version = 2
        }
        val database = Room.databaseBuilder(context, DirectoryDatabase::class.java, name)
            .addMigrations(MIGRATION_2_3).build()
        try {
            val rows = database.clinicDao().observeAllWithServices().first().associateBy { it.clinic.id }
            val first = requireNotNull(rows["guadalupe-la-hacienda"])
            assertEquals("Nombre conservado", first.clinic.name)
            assertEquals(25.67, requireNotNull(first.clinic.latitude), 0.0)
            assertEquals(5, first.services.size)
            first.services.forEach { assertEquals("0", it.price); assertNull(it.doctor) }
            assertEquals("Dirección corregida por el usuario", rows["guadalupe-los-pequenos-amigos"]?.clinic?.address)
            assertEquals(2, rows["guadalupe-los-pequenos-amigos"]?.services?.size)

            val clinic = first.clinic
            database.clinicDao().upsertWithServices(listOf(clinic), listOf(
                ClinicServiceEntity(clinic.id, "Consulta", "0.10", null)))
            val updated = database.clinicDao().observeWithServices(clinic.id).first()
            assertEquals("0.10", updated?.services?.single()?.price)
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun preservesExistingClinicsAndAllowsMissingCoordinates() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "directory-migration-test.db"
        context.deleteDatabase(name)
        val oldDatabase = context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null)
        oldDatabase.execSQL(
            "CREATE TABLE clinics (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, " +
                "address TEXT NOT NULL, municipality TEXT NOT NULL)",
        )
        oldDatabase.execSQL(
            "INSERT INTO clinics VALUES ('existing', 'Centro', 'Dirección', 'Guadalupe')",
        )
        oldDatabase.version = 1
        oldDatabase.close()

        val database = Room.databaseBuilder(context, DirectoryDatabase::class.java, name)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
        try {
            val row = database.clinicDao().observeById("existing").first()
            assertEquals("Centro", row?.name)
            assertNull(row?.latitude)
            assertNull(row?.longitude)

            database.clinicDao().upsert(
                listOf(ClinicEntity("existing", "Centro", "Dirección", "Guadalupe", 25.67, -100.25)),
            )
            val updated = database.clinicDao().observeById("existing").first()
            assertEquals(25.67, requireNotNull(updated?.latitude), 0.0)
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }
}
