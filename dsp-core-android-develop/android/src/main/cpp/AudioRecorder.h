//
// Created by Anton Lamtev on 25.09.19.
//

#ifndef DSPCORE_AUDIORECORDER_H
#define DSPCORE_AUDIORECORDER_H

#include <vector>
#include <oboe/AudioStream.h>
#include <oboe/AudioStreamCallback.h>


class AudioRecorder : private oboe::AudioStreamCallback {
private:
    constexpr static int kDefaultAudioSampleRate = 44100;
    constexpr static int kDefaultExpectedRecordDurationInSecs = 4;
    constexpr static int kChannelCount = 1;

    oboe::AudioStream *audioStream;
    std::vector<float> audioSignal;
    int deviceId;
    int audioSampleRate;

public:
    AudioRecorder(int deviceId = oboe::kUnspecified, int audioSampleRate = kDefaultAudioSampleRate) noexcept;
    ~AudioRecorder() noexcept;
    bool start(int expectedRecordDurationInSecs = kDefaultExpectedRecordDurationInSecs) noexcept;
    bool stop() noexcept;
    const std::vector<float> &getAudioSignal() const noexcept;
    int getAudioSampleRate() const noexcept;

private:
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *, void *audioData, int32_t numFrames) noexcept override;
    void onErrorBeforeClose(oboe::AudioStream *, oboe::Result error) noexcept override;
    void onErrorAfterClose(oboe::AudioStream *, oboe::Result error) noexcept override;
};


#endif //DSPCORE_AUDIORECORDER_H
