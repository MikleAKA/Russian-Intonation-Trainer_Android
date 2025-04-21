# We access to these class members via JNI, so we need to keep their names.

-keepclassmembers class ** {
  @android.support.annotation.Nullable *;
  @android.support.annotation.NonNull *;
}

-keepnames public final class org.studyintonation.dspcore.Pitch
-keepclassmembers public final class org.studyintonation.dspcore.Pitch {
    <init>(long);
}

-keepnames public final class org.studyintonation.dspcore.PitchDetector$Result
-keepclassmembers public final class org.studyintonation.dspcore.PitchDetector$Result {
    <init>(boolean, org.studyintonation.dspcore.Pitch);
}
