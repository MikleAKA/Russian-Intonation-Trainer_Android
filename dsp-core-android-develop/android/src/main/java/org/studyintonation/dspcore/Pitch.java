package org.studyintonation.dspcore;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class Pitch {
    @NonNull
    private final float[] samples;
    @NonNull
    private final float[] timestamps;
    private final int sampleRate;
    private final float startTimestamp;
    private final float endTimestamp;
    private long pitchPtr;

    @Nullable
    public static Pitch of(@NonNull final float[] samples, final int sampleRate, final float startTimestamp, final float endTimestamp) {
        if (samples.length < 3) {
            Log.d("Pitch.of()", String.format("samples length = %d < 3", samples.length));
            return null;
        }

        if (sampleRate <= 0) {
            Log.d("Pitch.of()", String.format("samplesRate = %d <= 0", sampleRate));
            return null;
        }

        return new Pitch(samples, sampleRate, startTimestamp, endTimestamp);
    }

    private Pitch(@NonNull final float[] samples, final int sampleRate, final float startTimestamp, final float endTimestamp) {
        this.pitchPtr = createPitch(samples, sampleRate, startTimestamp, endTimestamp);

        this.samples = samples(this.pitchPtr);
        this.timestamps = timestamps(this.pitchPtr);

        this.sampleRate = sampleRate;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    private Pitch(final long ptr) {
        this.pitchPtr = ptr;

        this.samples = samples(ptr);
        this.timestamps = timestamps(ptr);
        this.sampleRate = sampleRate(ptr);

        final float[] startAndEndTimestamps = startAndEndTimestamps(ptr);
        this.startTimestamp = startAndEndTimestamps[0];
        this.endTimestamp = startAndEndTimestamps[1];
    }

    @NonNull
    public float[] samples() {
        return samples;
    }

    @NonNull
    public float[] timestamps() {
        return timestamps;
    }

    int sampleRate() {
        return sampleRate;
    }

    public float startTimestamp() {
        return startTimestamp;
    }

    public float endTimestamp() {
        return endTimestamp;
    }

    /**
     * Compares this pitch instance to other for similarity.
     * <p>
     * This method works with native pitch instance, so it MUST NOT be invoked
     * since {@link #release()} has been called.
     *
     * @param other the other pith instance. Should not be released also.
     * @return positive metric, comparison result between two pitch instances
     * if the state of both was not changed using {@link #release()} method invocation;
     * otherwise {@code -1.0F}.
     * @see #release()
     */
    public float compareTo(@NonNull final Pitch other) {
        if (hasBeenReleased()) {
            Log.e("Pitch.compareTo()", "method called after this object has been released");
            return -1.0F;
        }

        if (other.hasBeenReleased()) {
            Log.e("Pitch.compareTo()", "method called after other object has been released");
            return -1.0F;
        }

        return compare(pitchPtr, other.pitchPtr);
    }

    /**
     * Releases native pitch memory.
     * <p>
     * Since this method has been called,
     * no other method that works with native pitch instance can be invoked.
     */
    public void release() {
        if (!hasBeenReleased()) {
            releasePitch(pitchPtr);
            pitchPtr = 0L;
        }
    }

    private boolean hasBeenReleased() {
        return pitchPtr == 0L;
    }

    private static native long createPitch(@NonNull final float[] samples, final int sampleRate, final float startTimestamp, final float endTimestamp);

    private static native void releasePitch(final long ptr);

    @NonNull
    private static native float[] samples(final long ptr);

    @NonNull
    private static native float[] timestamps(final long ptr);

    private static native int sampleRate(final long ptr);

    /**
     * Gets both startTimestamp and endTimestamp in one JNI call.
     *
     * @param ptr ptr to native Pitch instance.
     * @return float array containing 2 elements: startTimestamp and endTimestamp.
     */
    @NonNull
    private static native float[] startAndEndTimestamps(final long ptr);

    private static native float compare(final long thisPtr, final long otherPtr);

    static {
        System.loadLibrary("dspcore-android");
    }
}
