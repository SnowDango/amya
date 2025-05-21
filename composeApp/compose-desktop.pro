-keep class com.sun.jna.** { *; }
-keep class androidx.compose.material3.** { *; }
-ignorewarnings

-keep class androidx.datastore.preferences.** { *; }
-keep class io.ktor.* { *; }
-keep class coil3.* { *; }
-keep class ui.navigation.* { *; }
-keep class com.github.oshi.** { *; }

-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

-keepclassmembers public class **$$serializer {
    private ** descriptor;
}

-keep class * extends androidx.room.RoomDatabase

-dontwarn androidx.compose.material.**
-dontwarn androidx.paging.**
-dontwarn com.github.oshi.**
-dontwarn coil3.**
