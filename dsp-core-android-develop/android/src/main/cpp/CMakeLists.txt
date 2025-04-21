# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
project(dspcore-android)

set(BUILD_TESTING OFF)

include(conan.cmake)

conan_add_remote(NAME gitlab URL https://gitlab.com/api/v4/projects/29919231/packages/conan)


if(DEFINED ENV{CONAN_LOGIN_USERNAME_GITLAB} AND DEFINED ENV{CONAN_PASSWORD_GITLAB})
    conan_add_user(
            REMOTE gitlab
            NAME $ENV{CONAN_LOGIN_USERNAME_GITLAB}
            PASSWORD $ENV{CONAN_PASSWORD_GITLAB}
    )
endif()

conan_cmake_configure(
        REQUIRES dspcore/0.1.0-5-g117093b@studyintonation/develop
        GENERATORS cmake_find_package
        GENERATORS cmake_paths
        OPTIONS dspcore:fPIC=True)

# Map Android arch values to valid Conan arch values
set(archmap_armeabi-v7a armv7)
set(archmap_arm64-v8a armv8)
set(archmap_x86 x86)
set(archmap_x86_64 x86_64)

# Provide variables for the cmake wrapper
# (see https://stackoverflow.com/questions/62327296/cross-compile-for-android-on-linux-with-android-api-predefinition-fails-my-p)
set(ENV{ANDROID_PLATFORM} android-21)
set(ENV{ANDROID_ABI} ${CMAKE_ANDROID_ARCH_ABI})

# Set variables for generating arch specific conan profile
set(DSPCORE_CONAN_ARCH "${archmap_${CMAKE_ANDROID_ARCH_ABI}}")
set(DSPCORE_CONAN_CMAKE_PROGRAM "${CMAKE_CURRENT_SOURCE_DIR}/cmake_wrapper")

message("Using DSPCORE_CONAN_ARCH=${DSPCORE_CONAN_ARCH}")

configure_file(
    ${CMAKE_CURRENT_SOURCE_DIR}/conan-profile-arch.in 
    ${CMAKE_CURRENT_BINARY_DIR}/conan-profile-${CMAKE_ANDROID_ARCH_ABI} 
    @ONLY
)

conan_cmake_install(PATH_OR_REFERENCE .
        BUILD all
        PROFILE_HOST
            ${CMAKE_CURRENT_SOURCE_DIR}/conan-profile-host
            ${CMAKE_CURRENT_BINARY_DIR}/conan-profile-${CMAKE_ANDROID_ARCH_ABI})

list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_BINARY_DIR}")
find_package(dspcore REQUIRED)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        dspcore-android

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        dspcore-jni.cpp
        AudioRecorder.h
        AudioRecorder.cpp
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        dspcore-android

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

target_link_libraries(dspcore-android dspcore::dspcore)

# Set the path to the Oboe directory.
set (OBOE_DIR oboe)

# Add the Oboe library as a subdirectory in your project.
# add_subdirectory tells CMake to look in this directory to
# compile oboe source files using oboe's CMake file.
# ./oboe specifies where the compiled binaries will be stored
add_subdirectory (${OBOE_DIR} ./oboe)

# Specify the path to the Oboe header files.
# This allows targets compiled with this CMake (application code)
# to see public Oboe headers, in order to access its API.
include_directories (${OBOE_DIR}/include)

target_link_libraries(dspcore-android oboe)
