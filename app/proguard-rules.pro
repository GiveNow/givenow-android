# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/Android Studio.app/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class io.card.**
-keepclassmembers class io.card.** {
    *;
}

-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

-keep class com.parse.* { *; }
-dontwarn com.parse.**


-dontwarn java.lang.invoke.*
-dontwarn com.squareup.okhttp.**
-dontwarn javax.annotation.**
-dontwarn sun.misc.Unsafe
-dontwarn java.util.function.Consumer
-dontwarn com.facebook.*
-dontwarn okio.*
-dontwarn android.net.http.AndroidHttpClient
-dontwarn fj.function.*

