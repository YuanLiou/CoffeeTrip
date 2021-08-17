-keep class tw.com.louis383.coffeefinder.core.data.dto.** { *; }
-keep interface tw.com.louis383.coffeefinder.core.data.api.CoffeeTripService { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Google Play Service
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Retrofit 2
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp 3
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.*

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class tw.com.louis383.coffeefinder.**$$serializer { *; }
-keepclassmembers class tw.com.louis383.coffeefinder.** {
    *** Companion;
}
-keepclasseswithmembers class tw.com.louis383.coffeefinder.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Remove Log
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
}
