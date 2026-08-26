package com.example.medtrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.medtrack.data.dao.*
import com.example.medtrack.data.entity.*
import com.example.medtrack.util.DbKeyManager
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        Patient::class,
        LabTest::class,
        LabTestItem::class,
        Prescription::class,
        PrescriptionMedication::class,
        MedicineReminder::class,
        LabTestType::class,
        BmiRecord::class,
        PendingLabOrder::class
    ],
    version = 12,
    exportSchema = true
)
abstract class MedTrackDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun labTestDao(): LabTestDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun medicineReminderDao(): MedicineReminderDao
    abstract fun labTestTypeDao(): LabTestTypeDao
    abstract fun bmiRecordDao(): BmiRecordDao
    abstract fun pendingLabOrderDao(): PendingLabOrderDao

    companion object {
        private const val DB_NAME = "medtrack_database"

        // ------------------------------------------------------------------
        // Schema migrations.
        //
        // Room schemas are exported to app/schemas/ (see app/build.gradle.kts
        // `ksp { arg("room.schemaLocation", ...) }`) so every migration below can
        // be validated with room-testing. Add a new Migration for each bump of
        // the @Database version instead of relying on destructive migration.
        //
        // NOTE: Never open the SQLCipher-encrypted database with the plain
        // Android SQLite driver (e.g. to read user_version) — doing so corrupts
        // the encrypted file. Migrations must be handled by Room + SQLCipher only.
        // ------------------------------------------------------------------

        /** v8 → v9: add per-item image support to lab_test_items. */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE lab_test_items ADD COLUMN imageUri TEXT DEFAULT NULL")
            }
        }

        /** v9 → v10: remove per-item image support from lab_test_items (single photo per lab test card). */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // ALTER TABLE ... DROP COLUMN requires SQLite 3.35+ (Android API 31+),
                // so recreate the table without the imageUri column to stay safe on all levels.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `lab_test_items_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `labTestId` INTEGER NOT NULL,
                        `testName` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `results` TEXT NOT NULL,
                        `normalRange` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        FOREIGN KEY(`labTestId`) REFERENCES `lab_tests`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "INSERT INTO `lab_test_items_new` (`id`, `labTestId`, `testName`, `category`, `results`, `normalRange`, `notes`) " +
                        "SELECT `id`, `labTestId`, `testName`, `category`, `results`, `normalRange`, `notes` FROM `lab_test_items`"
                )
                db.execSQL("DROP TABLE `lab_test_items`")
                db.execSQL("ALTER TABLE `lab_test_items_new` RENAME TO `lab_test_items`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_lab_test_items_labTestId` ON `lab_test_items` (`labTestId`)")
            }
        }

        /** v10 → v11: add optional photo attachment to pending_lab_orders. */
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pending_lab_orders ADD COLUMN imageUri TEXT DEFAULT NULL")
            }
        }

        /** v11 → v12: add missing index on bmi_records.patientId for filtered queries. */
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bmi_records_patientId` ON `bmi_records` (`patientId`)")
            }
        }

        @Volatile
        private var INSTANCE: MedTrackDatabase? = null

        fun getDatabase(context: Context): MedTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedTrackDatabase::class.java,
                    DB_NAME
                )
                    // Encrypt the database at rest with SQLCipher.
                    .openHelperFactory(
                        SupportFactory(DbKeyManager.getOrCreatePassphrase(context.applicationContext))
                    )
                    // Register migrations here as schema versions evolve.
                    // NOTE: No destructive-migration fallback: silently wiping
                    // health records is never acceptable, so a failed migration
                    // throws instead of destroying patient data.
                    .addMigrations(MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
