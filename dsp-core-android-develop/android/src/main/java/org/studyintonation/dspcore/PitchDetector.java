package org.studyintonation.dspcore;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class PitchDetector {
    @NonNull
    private final Algorithm algorithm;
    private final int bufferSize;
    private long audioRecorderPtr;

    public PitchDetector(final int inputDeviceId, final int inputSampleRate, @NonNull final Algorithm algorithm, final int bufferSize) {
        this.algorithm = algorithm;
        this.bufferSize = bufferSize;
        this.audioRecorderPtr = createAudioRecorder(inputDeviceId, inputSampleRate);
    }

    public boolean start(final int expectedSpeechDurationInSecs) {
        if (hasBeenReleased()) {
            logMethodCalledAfterObjectHasBeenReleased("start");
            return false;
        }

        return start(audioRecorderPtr, expectedSpeechDurationInSecs);
    }

    @NonNull
    public Result stopAndGetResult() {
        if (hasBeenReleased()) {
            logMethodCalledAfterObjectHasBeenReleased("stopAndGetResult");
            return new Result(false, null);
        }

        return stopAndGetResult(audioRecorderPtr, bufferSize, algorithm.rawValue);
    }

    public void forceStop() {
        if (hasBeenReleased()) {
            logMethodCalledAfterObjectHasBeenReleased("forceStop");
        }

        forceStop(audioRecorderPtr);
    }

    /**
     * Releases native audio recorder memory.
     * <p>
     * Since this method has been called, no other method such as {@link #start(int)}, {@link #stopAndGetResult()}, {@link #forceStop()} can be invoked.
     *
     * @see #start(int)
     * @see #stopAndGetResult()
     * @see #forceStop()
     */
    public void release() {
        if (!hasBeenReleased()) {
            releaseAudioRecorder(audioRecorderPtr);
            audioRecorderPtr = 0L;
        }
    }

    private boolean hasBeenReleased() {
        return audioRecorderPtr == 0L;
    }

    private static void logMethodCalledAfterObjectHasBeenReleased(@NonNull final String method) {
        Log.e("PitchDetector." + method + "()", "method called after object has been released");
    }

    private static native long createAudioRecorder(final int deviceId, final int audioSampleRate);

    private static native void releaseAudioRecorder(final long ptr);

    private static native boolean start(final long audioRecorderPtr, final int expectedSpeechDurationInSecs);

    @NonNull
    private static native Result stopAndGetResult(final long audioRecorderPtr, final int frameSize, final int algorithm);

    private static native void forceStop(final long audioRecorderPtr);

    public enum Algorithm {
        YIN(0),
        ;

        private final int rawValue;

        Algorithm(final int rawValue) {
            this.rawValue = rawValue;
        }
    }

    public static final class Result {
        private final boolean isStoppedSuccessfully;
        @Nullable
        private final Pitch pitch;

        Result(final boolean isStoppedSuccessfully, @Nullable final Pitch pitch) {
            this.isStoppedSuccessfully = isStoppedSuccessfully;
            this.pitch = pitch;
        }

        public boolean isStoppedSuccessfully() {
            return isStoppedSuccessfully;
        }

        @Nullable
        public Pitch pitch() {
            return pitch;
        }
    }

    static {
        System.loadLibrary("dspcore-android");
    }
}
