//
// Created by Anton Lamtev on 25.09.19.
//

#include "AudioRecorder.h"
#include <oboe/AudioStreamBuilder.h>
#include <oboe/Utilities.h>
#include <android/log.h>


AudioRecorder::AudioRecorder(int deviceId, int audioSampleRate) noexcept :
    audioStream(nullptr),
    audioSignal(),
    deviceId(deviceId != 0 ? deviceId : oboe::kUnspecified),
    audioSampleRate(audioSampleRate) {}

bool AudioRecorder::start(int expectedRecordDurationInSecs) noexcept {
    if (audioStream) {
        __android_log_print(ANDROID_LOG_DEBUG, "AudioRecorder::start()", "Unable to start due to recording has already been started");
        return false;
    }

    audioSignal.clear();
    audioSignal.reserve(static_cast<size_t>(expectedRecordDurationInSecs * audioSampleRate));

    oboe::AudioStreamBuilder audioStreamBuilder;
    audioStreamBuilder.setDeviceId(deviceId);
    audioStreamBuilder.setDirection(oboe::Direction::Input);
    audioStreamBuilder.setSampleRate(audioSampleRate);
    audioStreamBuilder.setChannelCount(kChannelCount);
    audioStreamBuilder.setFormat(oboe::AudioFormat::Float);
    audioStreamBuilder.setPerformanceMode(oboe::PerformanceMode::PowerSaving);
    audioStreamBuilder.setSharingMode(oboe::SharingMode::Shared);
    audioStreamBuilder.setInputPreset(oboe::InputPreset::Unprocessed);
    audioStreamBuilder.setCallback(this);

    oboe::Result result = audioStreamBuilder.openStream(&audioStream);

    if (result != oboe::Result::OK) {
        __android_log_print(ANDROID_LOG_ERROR, "AudioRecorder::start()", "Unable to open input stream. %s", oboe::convertToText(result));
        return false;
    }

    audioSampleRate = audioStream->getSampleRate();

    result = audioStream->requestStart();

    if (result != oboe::Result::OK) {
        __android_log_print(ANDROID_LOG_ERROR, "AudioRecorder::start()", "Unable to start input stream. %s", oboe::convertToText(result));
        return false;
    }

    __android_log_print(ANDROID_LOG_DEBUG, "AudioRecorder::start()", "success, return true");

    return true;
}

AudioRecorder::~AudioRecorder() noexcept {
    if (audioStream) {
        oboe::Result result = audioStream->close();
        if (result != oboe::Result::OK) {
            __android_log_print(ANDROID_LOG_ERROR, "AudioRecorder::~AudioRecorder()", "Unable to close input stream. %s", oboe::convertToText(result));
        }
    }
}

bool AudioRecorder::stop() noexcept {
    if (!audioStream) {
        __android_log_print(ANDROID_LOG_DEBUG, "AudioRecorder::stop()", "Unable to stop due to recording has not been started");

        return false;
    }

    oboe::Result result = audioStream->requestStop();

    if (result != oboe::Result::OK) {
        __android_log_print(ANDROID_LOG_ERROR, "AudioRecorder::stop()", "Unable to stop input stream. %s", oboe::convertToText(result));
        return false;
    }

    result = audioStream->close();

    if (result != oboe::Result::OK) {
        __android_log_print(ANDROID_LOG_ERROR, "AudioRecorder::stop()", "Unable to close input stream. %s", oboe::convertToText(result));
        return false;
    }

    audioStream = nullptr;

    __android_log_print(ANDROID_LOG_DEBUG, "AudioRecorder::stop()", "success, return true");

    return true;
}

const std::vector<float> &AudioRecorder::getAudioSignal() const noexcept {
    return audioSignal;
}

int AudioRecorder::getAudioSampleRate() const noexcept {
    return audioSampleRate;
}

//TODO: perform callback not in main thread
oboe::DataCallbackResult AudioRecorder::onAudioReady(oboe::AudioStream *, void *audioData, int32_t numFrames) noexcept {
    auto capacity = audioSignal.capacity();
    auto size = audioSignal.size();

    if (capacity - size < numFrames) {
        audioSignal.reserve(capacity + numFrames);
    }
    auto floatAudioData = static_cast<float *>(audioData);

#if 0 //TODO: choose fastest one
    audioSignal.resize(size + numFrames);
    memcpy(&audioSignal[size], floatAudioData, static_cast<size_t>(numFrames));
#else
    audioSignal.insert(audioSignal.end(), &floatAudioData[0], &floatAudioData[numFrames]);
#endif

    return oboe::DataCallbackResult::Continue;
}

void AudioRecorder::onErrorBeforeClose(oboe::AudioStream *, oboe::Result error) noexcept {
    __android_log_print(ANDROID_LOG_ERROR, "AudioRecorder::onErrorBeforeClose()", "Error occurred: %s", oboe::convertToText(error));
}

void AudioRecorder::onErrorAfterClose(oboe::AudioStream *, oboe::Result error) noexcept {
    __android_log_print(ANDROID_LOG_ERROR, "AudioRecorder::onErrorAfterClose()", "Error occurred: %s", oboe::convertToText(error));
}
