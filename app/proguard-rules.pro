# Proguard rules for Kinolog Walker
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}

-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class com.kinolog.walker.model.** { *; }
