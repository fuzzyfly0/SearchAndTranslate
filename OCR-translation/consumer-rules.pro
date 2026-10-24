# Keep rules for the translator module; they apply to the consuming app's R8 run.

# Native code (JNI, JNA, uniffi) looks up classes, fields and methods by name,
# so the module is shipped without obfuscation, like the standalone translator app.
-dontobfuscate

-keepclasseswithmembernames class * {
    native <methods>;
}

# JNA: native code looks up methods/fields reflectively via initIDs
-dontwarn java.awt.*
-dontwarn com.sun.jna.**
-keep class com.sun.jna.** { *; }
-keepclassmembers class * extends com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }

# uniffi-generated bindings: Structure subclasses, Callback interfaces, and
# enum/data classes crossing the FFI boundary must retain their fields.
-keep class uniffi.bindings.** { *; }
-keep class uniffi.translator.** { *; }

# uCrop contains optional HTTP download features depending on OkHttp.
# Ignore missing okhttp3 classes since OkHttp is not included as a dependency.
-dontwarn okhttp3.**
-dontwarn okio.**
