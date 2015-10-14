# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Ruler\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
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

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

# Butterknife
-dontwarn butterknife.internal.**
-keep class butterknife.** { *; }
-keep class **$$ViewInjector { *; }

-keepclasseswithmembernames class * {
    @butterknife.InjectView <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.OnClick <methods>;
    @butterknife.OnEditorAction <methods>;
    @butterknife.OnItemClick <methods>;
    @butterknife.OnItemLongClick <methods>;
    @butterknife.OnLongClick <methods>;
}

# Simple-Xml Proguard Config
# NOTE: You should also include the Android Proguard config found with the build tools:
# $ANDROID_HOME/tools/proguard/proguard-android.txt

# Keep public classes and methods.
-dontwarn com.bea.xml.stream.**
-keep class org.simpleframework.xml.**{ *; }
-keepclassmembers,allowobfuscation class * {
    @org.simpleframework.xml.* <fields>;
    @org.simpleframework.xml.* <init>(...);
}


-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class * implements android.os.Parcelable {
      public static final android.os.Parcelable$Creator *;
   }

-keep public class com.rukiasoft.androidapps.cocinaconroll.classes.ZipToDownload {
  public *** get*();
  public void set*(***);
}

-keep public class com.rukiasoft.androidapps.cocinaconroll.classes.RecipeItem {
  public *** get*();
  public void set*(***);
}

-keepattributes *Annotation*,EnclosingMethod, Signature

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-dump class_files.txt
-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-allowaccessmodification
-keepattributes *Annotation*
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-repackageclasses ''

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

-keepclasseswithmembers class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep public class * {
    public protected *;
}

-dontwarn android.support.**,de.greenrobot.**,org.simpleframework.xml.**
-keep class com.crittercism.**{ *; }
-keepclassmembernames class com.crittercism.**{ *; }
-keepclasseswithmembers class com.crittercism.**{ *; }

-keep class org.simpleframework.**{ *; }
-keepclassmembernames class org.simpleframework.**{ *; }
-keepclasseswithmembers class org.simpleframework.**{ *; }

-keep class crittercism.android.**


-dontwarn android.support.**,de.greenrobot.**,org.simpleframework.xml.**
-keep class com.crittercism.**{ *; }
-keepclassmembernames class com.crittercism.**{ *; }
-keepclasseswithmembers class com.crittercism.**{ *; }

-keep class org.simpleframework.**{ *; }
-keepclassmembernames class org.simpleframework.**{ *; }
-keepclasseswithmembers class org.simpleframework.**{ *; }

-keep class crittercism.android.**
-keepclassmembers public class com.crittercism.*{ *;}

-keep public class database.** {
    public static <fields>;
}

-keep public class org.simpleframework.** { *; }
-keep class org.simpleframework.xml.** { *; }
-keep class org.simpleframework.xml.core.** { *; }
-keep class org.simpleframework.xml.util.** { *; }
-keep class org.simpleframework.xml.stream.**{ *; }

-keepattributes ElementList, Root

-keepclassmembers class * {
    @org.simpleframework.xml.* *;
}

-dontwarn javax.xml.**


-keep class android.support.**
-keepclasseswithmembers class android.support.** { *;}

-keep class org.simpleframeork.**
-keepclasseswithmembers class org.simpleframeork.** { *;}

-keep class javax.**
-keepclasseswithmembers class javax.** { *;}

-keep class com.test.category.**
-keepclassmembernames class com.test.category.** { *; }
-keepclasseswithmembers class com.test.category.** { *;}

-keep class com.test.download.**
-keepclassmembernames class com.test.download.** { *; }
-keepclasseswithmembers class com.test.download.** { *;}

-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Preserve all native method names and the names of their classes.
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Your library may contain more items that need to be preserved;
# typically classes that are dynamically created using Class.forName:

-keep interface org.simpleframework.xml.core.Label {
   public *;
}
-keep class * implements org.simpleframework.xml.core.Label {
   public *;
}
-keep interface org.simpleframework.xml.core.Parameter {
   public *;
}
-keep class * implements org.simpleframework.xml.core.Parameter {
   public *;
}
-keep interface org.simpleframework.xml.core.Extractor {
   public *;
}
-keep class * implements org.simpleframework.xml.core.Extractor {
   public *;
}

-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

-keep public class javax.xml.stream.** {public private protected *;}
-dontwarn javax.xml.stream.**
-keep public class org.joda.time.** {public private protected *;}
-dontwarn org.joda.time.**
-keep public class org.w3c.dom.** {public private protected *;}
-dontwarn org.w3c.dom.**


-keepattributes *Annotation*
-keepattributes Signature
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions

