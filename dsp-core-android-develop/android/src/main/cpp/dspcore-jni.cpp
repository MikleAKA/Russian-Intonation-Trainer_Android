#include <jni.h>
#include <android/log.h>
#include "AudioRecorder.h"
#include <dspcore/pitch_detector.h>
#include <dspcore/pitch.h>

extern "C" {

JNIEXPORT jlong JNICALL
Java_org_studyintonation_dspcore_Pitch_createPitch(
        JNIEnv *env, jclass, jfloatArray samples, jint sample_rate,
        jfloat start_timestamp, jfloat end_timestamp
) {
    jsize samples_size = env->GetArrayLength(samples);
    std::vector<float> samples_vector(static_cast<size_t>(samples_size));
    env->GetFloatArrayRegion(samples, 0, samples_size, &samples_vector[0]);

    return reinterpret_cast<jlong>(
            new dspcore::Pitch(samples_vector, sample_rate, start_timestamp, end_timestamp)
    );
}

dspcore::Pitch *to_pitch(jlong ptr) {
    return reinterpret_cast<dspcore::Pitch *>(ptr);
}

JNIEXPORT void JNICALL
Java_org_studyintonation_dspcore_Pitch_releasePitch(
        JNIEnv *, jclass, jlong ptr
) {
    delete to_pitch(ptr);
}

jfloatArray vector_to_java_array(JNIEnv *env, const std::vector<float> &vector) {
    auto array = env->NewFloatArray(vector.size());
    env->SetFloatArrayRegion(array, 0, vector.size(), &vector[0]);

    return array;
}

JNIEXPORT jfloatArray JNICALL
Java_org_studyintonation_dspcore_Pitch_samples(
        JNIEnv *env, jclass, jlong ptr
) {
    auto sample_vector = to_pitch(ptr)->samples;

    return vector_to_java_array(env, sample_vector);
}

JNIEXPORT jfloatArray JNICALL
Java_org_studyintonation_dspcore_Pitch_timestamps(
        JNIEnv *env, jclass, jlong ptr
) {
    auto timestamp_vector = to_pitch(ptr)->timestamps();

    return vector_to_java_array(env, timestamp_vector);
}

JNIEXPORT jint JNICALL
Java_org_studyintonation_dspcore_Pitch_sampleRate(
        JNIEnv *, jclass, jlong ptr
) {
    return to_pitch(ptr)->sample_rate;
}

JNIEXPORT jfloatArray JNICALL
Java_org_studyintonation_dspcore_Pitch_startAndEndTimestamps(
        JNIEnv *env, jclass, jlong ptr
) {
    auto pitch = to_pitch(ptr);

    auto start_timestamp = pitch->start_timestamp;
    auto end_timestamp = pitch->end_timestamp;

    std::vector<float> timestamps;
    timestamps.reserve(2);
    timestamps.push_back(start_timestamp);
    timestamps.push_back(end_timestamp);

    return vector_to_java_array(env, timestamps);
}

JNIEXPORT jfloat JNICALL
Java_org_studyintonation_dspcore_Pitch_compare(
        JNIEnv *, jclass, jlong this_ptr, jlong other_ptr
) {
    return to_pitch(this_ptr)->compare_to(to_pitch(other_ptr));
}

JNIEXPORT jlong JNICALL
Java_org_studyintonation_dspcore_PitchDetector_createAudioRecorder(
        JNIEnv *, jclass, jint device_id, jint sample_rate
) {
    return reinterpret_cast<jlong>(new AudioRecorder(device_id, sample_rate));
}

AudioRecorder *to_audio_recorder(jlong ptr) {
    return reinterpret_cast<AudioRecorder *>(ptr);
}

JNIEXPORT void JNICALL
Java_org_studyintonation_dspcore_PitchDetector_releaseAudioRecorder(
        JNIEnv *, jclass, jlong ptr
) {
    delete to_audio_recorder(ptr);
}

JNIEXPORT jboolean JNICALL
Java_org_studyintonation_dspcore_PitchDetector_start(
        JNIEnv *, jclass, jlong audio_recorder_ptr, jint expected_duration_in_secs
) {
    auto audio_recorder = to_audio_recorder(audio_recorder_ptr);

    return static_cast<jboolean>(audio_recorder->start(expected_duration_in_secs));
}

jobject result_constructor(JNIEnv *env, bool is_stopped_successfully, jobject pitch) {
    jclass result_class = env->FindClass("org/studyintonation/dspcore/PitchDetector$Result");
    jmethodID result_constructor = env->GetMethodID(result_class, "<init>",
                                                    "(ZLorg/studyintonation/dspcore/Pitch;)V");

    return env->NewObject(result_class, result_constructor,
                          static_cast<jboolean>(is_stopped_successfully), pitch);
}

jobject pitch_constructor(JNIEnv *env, const dspcore::Pitch *pitch) {
    jclass pitch_class = env->FindClass("org/studyintonation/dspcore/Pitch");
    jmethodID pitch_constructor = env->GetMethodID(pitch_class, "<init>", "(J)V");

    return env->NewObject(pitch_class, pitch_constructor, reinterpret_cast<jlong>(pitch));
}

dspcore::PitchDetector::Algorithm PitchDetector_algorithm(int rawValue) {
    switch (rawValue) { // NOLINT(hicpp-multiway-paths-covered)
        case 0:
        default:
            return dspcore::PitchDetector::Algorithm::YIN;
    }
}

JNIEXPORT jobject JNICALL
Java_org_studyintonation_dspcore_PitchDetector_stopAndGetResult(
        JNIEnv *env, jclass, jlong audio_recorder_ptr, jint frame_size, jint algorithm
) {
    auto audio_recorder = to_audio_recorder(audio_recorder_ptr);

    bool stopped_successfully = audio_recorder->stop();
    if (!stopped_successfully) {
        return result_constructor(env, false, nullptr);
    }

    auto audio_signal = audio_recorder->getAudioSignal();
    auto sample_rate = audio_recorder->getAudioSampleRate();

    if (audio_signal.empty()) {
        __android_log_print(ANDROID_LOG_DEBUG, "PitchDetector::stopAndGetResult()",
                            "No audio signal captured");
        return result_constructor(env, true, nullptr);
    }

    auto pitch = dspcore::PitchDetector::detect(audio_signal, sample_rate, frame_size, 160,
                                                PitchDetector_algorithm(algorithm));
    if (!pitch) {
        __android_log_print(ANDROID_LOG_DEBUG, "PitchDetector::stopAndGetResult()",
                            "Pitch detection or smoothing failed");
        return result_constructor(env, true, nullptr);
    }

    return result_constructor(env, true, pitch_constructor(env, pitch));
}

JNIEXPORT void JNICALL
Java_org_studyintonation_dspcore_PitchDetector_forceStop(
        JNIEnv *, jclass, jlong audio_recorder_ptr
) {
    to_audio_recorder(audio_recorder_ptr)->stop();
}

}
