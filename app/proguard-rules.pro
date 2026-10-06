# Room Database Keep Rules
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.fitnesstracker.data.local.** { *; }
-keepclassmembers class * {
    @androidx.room.Entity <fields>;
    @androidx.room.PrimaryKey <fields>;
    @androidx.room.ColumnInfo <fields>;
    @androidx.room.TypeConverter <methods>;
}

# Hilt & Dependency Injection
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.android.HandlerDispatcher {
    public <init>(...);
}

# Retain Android Resource Names & Build Config
-keep class com.example.fitnesstracker.BuildConfig { *; }
