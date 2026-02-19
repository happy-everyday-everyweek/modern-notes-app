# Add project specific ProGuard rules here.
-keep public class * extends android.app.Activity
-keep class com.modernnotes.data.model.** { *; }
-keep class com.modernnotes.data.local.** { *; }

-keepclassmembers class * {
    @androidx.room.** <methods>;
}

-keepattributes *Annotation*
-dontwarn kotlin.**
-keep class kotlin.** { *; }
