-keep class com.sun.jna.** { *; }
-keep class androidx.compose.material3.** { *; }
-ignorewarnings

-keep class androidx.datastore.preferences.** { *; }
-keep class io.ktor.** { *; }
-keep class coil3.** { *; }
-keep class okio.** { *; }
-keep class ui.navigation.* { *; }
-keep class com.github.oshi.** { *; }
-keep class java.net.http.** { *; }

# ProGuardの最適化でJobSupport.cancel()がVerifyErrorになるためcoroutinesは保持する
-keep class kotlinx.coroutines.** { *; }

-keepclassmembers public class **$$serializer {
    private ** descriptor;
}

-keepclasseswithmembers class androidx.sqlite.driver.bundled.** { native <methods>; }
-keep class * extends androidx.room.RoomDatabase

-dontwarn androidx.compose.material.**
-dontwarn androidx.paging.**
-dontwarn com.github.oshi.**
-dontwarn coil3.**
