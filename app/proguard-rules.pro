# ProGuard rules for Muslim app

# Keep Adhan library
-keep class com.batoulapps.adhan.** { *; }

# Keep shared module
-keep class com.Blackbox.muslim.shared.** { *; }

# Keep all app classes
-keep class com.Blackbox.muslim.** { *; }

# Keep kotlinx.datetime
-keep class kotlinx.datetime.** { *; }

# Keep adhan library data classes
-keep class com.batoulapps.adhan.data.** { *; }
-keep class com.batoulapps.adhan.CalculationMethod { *; }
-keep class com.batoulapps.adhan.CalculationParameters { *; }
-keep class com.batoulapps.adhan.Coordinates { *; }
-keep class com.batoulapps.adhan.PrayerTimes { *; }
-keep class com.batoulapps.adhan.Prayer { *; }

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
