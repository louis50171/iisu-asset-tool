# iiSU Asset Tool ProGuard Rules

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes Signature

# Keep data classes
-keep class com.iisu.assettool.data.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Jsoup
-keeppackagenames org.jsoup.nodes
