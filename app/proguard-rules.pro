# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep MediaPipe classes
-keep class com.google.mediapipe.** { *; }
-keep class com.google.protobuf.** { *; }

# Don't warn about missing MediaPipe classes
-dontwarn com.google.mediapipe.framework.image.BitmapExtractor
-dontwarn com.google.mediapipe.framework.image.ByteBufferExtractor
-dontwarn com.google.mediapipe.framework.image.MPImage
-dontwarn com.google.mediapipe.framework.image.MPImageProperties
-dontwarn com.google.mediapipe.framework.image.MediaImageExtractor

# Keep Protocol Buffer annotations and internal classes
-dontwarn com.google.protobuf.**
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }

# Keep DataStore classes
-keep class androidx.datastore.** { *; }
-keep class * extends androidx.datastore.core.Serializer { *; }

# Keep Gson classes for JSON serialization
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep our domain models for JSON serialization
-keep class com.checkstand.domain.model.** { *; }