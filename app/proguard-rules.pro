# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers @kotlinx.serialization.Serializable class ru.rfdocs.wallet.** {
    *** Companion;
    *** serializer(...);
    <fields>;
}
-keep,includedescriptorclasses class ru.rfdocs.wallet.**$$serializer { *; }

# ZXing
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# AndroidX Security / Biometric
-keep class androidx.security.crypto.** { *; }
-keep class androidx.biometric.** { *; }

# Android Keystore
-keep class android.security.keystore.** { *; }
-dontwarn android.security.keystore.**