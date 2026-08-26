# VitalsIQ ProGuard/R8 rules
# Keep this file in sync with any libraries that require explicit keep rules.

# ---------------------------------------------------------------------------
# kotlinx.serialization (used by Navigation3 @Serializable NavKeys)
# ---------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------------------------------------------------------------------------
# Room
# Room ships consumer rules; these keeps are a safety net for generated code.
# ---------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

# ---------------------------------------------------------------------------
# SQLCipher (net.sqlcipher) - native library + database classes
# ---------------------------------------------------------------------------
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**
-dontwarn net.sqlcipher.database.**

# ---------------------------------------------------------------------------
# Coil (image loading)
# ---------------------------------------------------------------------------
-dontwarn coil.**
