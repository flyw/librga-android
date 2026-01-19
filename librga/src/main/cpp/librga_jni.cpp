#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/hardware_buffer.h>
#include <android/hardware_buffer_jni.h>
#include "im2d.h"
#include "RgaUtils.h"

#define TAG "LibrgaJni"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Define native_handle_t as it is not strictly in NDK headers but needed to extract FD
typedef struct native_handle {
    int version;        /* sizeof(native_handle_t) */
    int numFds;         /* number of file-descriptors at &data[0] */
    int numInts;        /* number of ints at &data[numFds] */
    int data[0];        /* numFds + numInts ints */
} native_handle_t;

// Manually declare AHardwareBuffer_getNativeHandle as it's not exposed in NDK headers but available in libandroid.so
extern "C" const native_handle_t* AHardwareBuffer_getNativeHandle(const AHardwareBuffer* buffer);

extern "C" {

// Helper to convert Kotlin RgaBuffer to rga_buffer_t
rga_buffer_t getRgaBuffer(JNIEnv *env, jobject jRgaBuffer) {
    jclass clazz = env->GetObjectClass(jRgaBuffer);
    
    jfieldID widthId = env->GetFieldID(clazz, "width", "I");
    jfieldID heightId = env->GetFieldID(clazz, "height", "I");
    jfieldID formatId = env->GetFieldID(clazz, "format", "I");
    jfieldID wstrideId = env->GetFieldID(clazz, "wstride", "I");
    jfieldID hstrideId = env->GetFieldID(clazz, "hstride", "I");
    jfieldID fdId = env->GetFieldID(clazz, "fd", "I");
    // handle is ignored in this simple wrapper for now, usually requires more complex mapping
    jfieldID ptrId = env->GetFieldID(clazz, "ptr", "Ljava/nio/ByteBuffer;");
    jfieldID hbId = env->GetFieldID(clazz, "hardwareBuffer", "Ljava/lang/Object;");

    int width = env->GetIntField(jRgaBuffer, widthId);
    int height = env->GetIntField(jRgaBuffer, heightId);
    int format = env->GetIntField(jRgaBuffer, formatId);
    int wstride = env->GetIntField(jRgaBuffer, wstrideId);
    int hstride = env->GetIntField(jRgaBuffer, hstrideId);
    int fd = env->GetIntField(jRgaBuffer, fdId);
    jobject ptrObj = env->GetObjectField(jRgaBuffer, ptrId);
    jobject hbObj = env->GetObjectField(jRgaBuffer, hbId);

    rga_buffer_t buffer;
    memset(&buffer, 0, sizeof(rga_buffer_t));

    if (fd >= 0) {
        buffer = wrapbuffer_fd_t(fd, width, height, wstride, hstride, format);
    } else if (hbObj != nullptr) {
        AHardwareBuffer *ahb = AHardwareBuffer_fromHardwareBuffer(env, hbObj);
        if (ahb != nullptr) {
            const native_handle_t *handle = AHardwareBuffer_getNativeHandle(ahb);
            if (handle != nullptr && handle->numFds > 0) {
                int buffFd = handle->data[0];
                buffer = wrapbuffer_fd_t(buffFd, width, height, wstride, hstride, format);
            } else {
                 LOGE("Failed to get native handle or fd from HardwareBuffer");
            }
        } else {
             LOGE("Failed to get AHardwareBuffer from HardwareBuffer");
        }
    } else if (ptrObj != nullptr) {
        void *addr = env->GetDirectBufferAddress(ptrObj);
        if (addr != nullptr) {
            buffer = wrapbuffer_virtualaddr_t(addr, width, height, wstride, hstride, format);
        } else {
             LOGE("Failed to get direct buffer address");
        }
    } else {
        LOGE("RgaBuffer has neither valid fd, HardwareBuffer nor valid ByteBuffer");
    }
    return buffer;
}

// Helper to convert Kotlin RgaRect to im_rect
im_rect getRgaRect(JNIEnv *env, jobject jRgaRect) {
    jclass clazz = env->GetObjectClass(jRgaRect);
    jfieldID xId = env->GetFieldID(clazz, "x", "I");
    jfieldID yId = env->GetFieldID(clazz, "y", "I");
    jfieldID wId = env->GetFieldID(clazz, "width", "I");
    jfieldID hId = env->GetFieldID(clazz, "height", "I");

    im_rect rect;
    rect.x = env->GetIntField(jRgaRect, xId);
    rect.y = env->GetIntField(jRgaRect, yId);
    rect.width = env->GetIntField(jRgaRect, wId);
    rect.height = env->GetIntField(jRgaRect, hId);
    return rect;
}


JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imcopy(JNIEnv *env, jobject thiz, jobject src, jobject dst) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imcopy(srcBuf, dstBuf);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imresize(JNIEnv *env, jobject thiz, jobject src, jobject dst, jdouble fx, jdouble fy) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imresize(srcBuf, dstBuf, fx, fy);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imcrop(JNIEnv *env, jobject thiz, jobject src, jobject dst, jobject rect) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    im_rect imRect = getRgaRect(env, rect);
    return imcrop(srcBuf, dstBuf, imRect);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imrotate(JNIEnv *env, jobject thiz, jobject src, jobject dst, jint rotation) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imrotate(srcBuf, dstBuf, rotation);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imflip(JNIEnv *env, jobject thiz, jobject src, jobject dst, jint mode) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imflip(srcBuf, dstBuf, mode);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imtranslate(JNIEnv *env, jobject thiz, jobject src, jobject dst, jint x, jint y) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imtranslate(srcBuf, dstBuf, x, y);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imblend(JNIEnv *env, jobject thiz, jobject src, jobject dst, jint mode) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imblend(srcBuf, dstBuf, mode);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imcomposite(JNIEnv *env, jobject thiz, jobject srcA, jobject srcB, jobject dst, jint mode) {
    rga_buffer_t srcABuf = getRgaBuffer(env, srcA);
    rga_buffer_t srcBBuf = getRgaBuffer(env, srcB);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imcomposite(srcABuf, srcBBuf, dstBuf, mode);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imcvtcolor(JNIEnv *env, jobject thiz, jobject src, jobject dst, jint sfmt, jint dfmt) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imcvtcolor(srcBuf, dstBuf, sfmt, dfmt);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imfill(JNIEnv *env, jobject thiz, jobject dst, jobject rect, jint color) {
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    im_rect imRect = getRgaRect(env, rect);
    LOGE("imfill: dst(w=%d, h=%d, ws=%d, hs=%d), rect(x=%d, y=%d, w=%d, h=%d)", 
         dstBuf.width, dstBuf.height, dstBuf.wstride, dstBuf.hstride,
         imRect.x, imRect.y, imRect.width, imRect.height);
    return imfill(dstBuf, imRect, color);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imconfig(JNIEnv *env, jobject thiz, jint name, jlong value) {
    return imconfig((IM_CONFIG_NAME)name, (uint64_t)value);
}

} // extern "C"