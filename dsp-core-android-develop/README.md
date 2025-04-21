# dsp-core

## Android

### Getting started

Since dspcore for Android depends on some 3rd party libraries which are connected to the project using a git-submodules mechanism, git submodules MUST present in the repository.

1. For the first-time cloning use the command `git clone --recursive <url>` which will clone all required submodules as well as the main dspcore module.
2. If you have dspcore repository cloned already, use the `git submodule update --init --recursive` command to fetch required submodules.

#### How to open the project in the Android Studio

1. Open root build.gradle as a project in Android studio
2. Make sure that Android NDK is installed
3. Set the following environment variables:
    1. ANDROID_HOME - your Android SDK directory
    2. CONAN_LOGIN_USERNAME_GITLAB - your Gitlab username (needed to fetch the dspcore from the conan repo)
    3. CONAN_PASSWORD_GITLAB - your personal access token for Gitlab (needed to fetch the dspcore from the conan repo)
4. Run Gradle `android:assemble` task, which will automatically
    1. build c++ code using CMake
    2. build java code
    3. pack all compiled binaries into a `android-debug.aar` and `android-release.aar`

