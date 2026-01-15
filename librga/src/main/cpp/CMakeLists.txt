cmake_minimum_required(VERSION 3.4.1)

project(rga_kotlin_wrapper)

# Include current directory for headers
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)

# Import prebuilt librga
add_library(librga SHARED IMPORTED)
set_target_properties(librga PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/librga.so)

# Build the JNI wrapper
add_library(rga_jni SHARED
        librga_jni.cpp)

# Link against librga and Android log
find_library(log-lib log)

target_link_libraries(rga_jni
        librga
        ${log-lib}
        android)