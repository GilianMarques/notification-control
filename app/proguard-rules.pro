-printusage usage.txt
-printseeds seeds.txt

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

-keep class android.util.Log { *; }
-keepclassmembers class * {
    public void *(android.util.Log);
}

# Mantém todas as classes do Google Credentials API
-keep class com.google.android.gms.auth.** { *; }

# Mantém classes do Firebase UI Auth relacionadas à autenticação
-keep class com.firebase.ui.auth.util.CredentialUtils { *; }
-keep class com.firebase.ui.auth.data.remote.SignInKickstarter { *; }
-keep class com.firebase.ui.auth.ui.email.CheckEmailHandler { *; }
-keep class com.firebase.ui.auth.viewmodel.AuthViewModelBase { *; }
-keep class com.firebase.ui.auth.util.GoogleApiUtils { *; }

# Mantém classes do Google Play Services necessárias para autenticação
-keep class com.google.android.gms.common.api.** { *; }
-keep class com.google.android.gms.tasks.** { *; }

# === Android Transitions Framework ===
# Keep all public classes in the androidx.transition package and its subpackages.
# This prevents renaming of core Transition classes.
-keep public class androidx.transition.** { *; }

# Keep specific members within Transition classes that might be accessed via reflection.
# For example, ChangeBounds uses mViewBounds, which needs to be preserved.
-keepclassmembers class androidx.transition.ChangeBounds$* extends android.animation.AnimatorListenerAdapter {
    androidx.transition.ChangeBounds$ViewBounds mViewBounds;
}

