package com.example.medtrack.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.medtrack.util.DbKeyManager
import net.sqlcipher.database.SupportFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Validates the SQLCipher-backed Room migrations against the exported schemas
 * (see app/schemas). Run on a device/emulator:
 *   ./gradlew :app:connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class MedTrackDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MedTrackDatabase::class.java,
        emptyList(),
        SupportFactory(
            DbKeyManager.getOrCreatePassphrase(InstrumentationRegistry.getInstrumentation().targetContext)
        )
    )

    @Test
    fun migrate10To12_addsPendingOrderImageAndBmiIndex_keepingData() {
        // Create the database at version 10 using the exported v10 schema.
        helper.createDatabase(TEST_DB, 10).apply {
            execSQL(
                "INSERT INTO patients (fullName, dateOfBirth, gender, bloodType, allergies, emergencyContact, photoUri, createdAt) " +
                    "VALUES ('Test Patient', '1990-01-01', 'Male', '', '', '', '', 0)"
            )
            close()
        }

        // Migrate 10 -> 11 -> 12 and validate against the exported v12 schema.
        val migrated = helper.runMigrationsAndValidate(
            TEST_DB, 12, true,
            MedTrackDatabase.MIGRATION_10_11,
            MedTrackDatabase.MIGRATION_11_12
        )

        // Data survived and both schema changes are present.
        migrated.use {
            val patientCount = it.query("SELECT COUNT(*) FROM patients").use { c ->
                c.moveToFirst()
                c.getInt(0)
            }
            assertEquals(1, patientCount)

            val pendingCols = mutableListOf<String>()
            it.query("PRAGMA table_info(pending_lab_orders)").use { c ->
                while (c.moveToNext()) pendingCols.add(c.getString(1))
            }
            // v10 -> v11 added the optional photo column.
            assertTrue("imageUri" in pendingCols)

            val bmiIndexes = mutableListOf<String>()
            it.query("PRAGMA index_list(bmi_records)").use { c ->
                while (c.moveToNext()) bmiIndexes.add(c.getString(1))
            }
            // v11 -> v12 added the missing FK index.
            assertTrue("index_bmi_records_patientId" in bmiIndexes)
        }
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
