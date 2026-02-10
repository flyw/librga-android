#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/bitmap.h>
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
    im_opt_t opt;
    opt.version = RGA_CURRENT_API_VERSION;
    opt.core = IM_SCHEDULER_RGA3_CORE0 | IM_SCHEDULER_RGA3_CORE1;
    return improcess(srcBuf, dstBuf, {}, {}, {}, {}, -1, NULL, &opt, 0);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imresize(JNIEnv *env, jobject thiz, jobject src, jobject dst, jdouble fx, jdouble fy) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    im_opt_t opt;
    memset(&opt, 0, sizeof(im_opt_t));
    opt.version = RGA_CURRENT_API_VERSION;
    opt.core = IM_SCHEDULER_RGA3_CORE0 | IM_SCHEDULER_RGA3_CORE1;
    return improcess(srcBuf, dstBuf, {}, {}, {}, {}, -1, NULL, &opt, 0);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imrescale(JNIEnv *env, jobject thiz, jobject src, jobject dst, jdouble fx, jdouble fy) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    im_opt_t opt;
    memset(&opt, 0, sizeof(im_opt_t));
    opt.version = RGA_CURRENT_API_VERSION;
    opt.core = IM_SCHEDULER_RGA3_CORE0 | IM_SCHEDULER_RGA3_CORE1;

    im_rect srect = {0, 0, srcBuf.width, srcBuf.height};
    im_rect drect = {0, 0, (int)(srcBuf.width * fx), (int)(srcBuf.height * fy)};

    return improcess(srcBuf, dstBuf, {}, srect, drect, {}, -1, NULL, &opt, 0);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imcrop(JNIEnv *env, jobject thiz, jobject src, jobject dst, jobject rect) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    im_rect imRect = getRgaRect(env, rect);
    im_opt_t opt;
    opt.version = RGA_CURRENT_API_VERSION;
    opt.core = IM_SCHEDULER_RGA3_CORE0 | IM_SCHEDULER_RGA3_CORE1;
    return improcess(srcBuf, dstBuf, {}, imRect, {}, {}, -1, NULL, &opt, 0);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imrotate(JNIEnv *env, jobject thiz, jobject src, jobject dst, jint rotation) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);

    im_opt_t opt;
    opt.version = RGA_CURRENT_API_VERSION;
    opt.core = IM_SCHEDULER_RGA3_CORE0 | IM_SCHEDULER_RGA3_CORE1;
    return improcess(srcBuf, dstBuf, {}, {}, {}, {}, -1, NULL, &opt, rotation);

}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imflip(JNIEnv *env, jobject thiz, jobject src, jobject dst, jint mode) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    im_opt_t opt;
    opt.version = RGA_CURRENT_API_VERSION;
    opt.core = IM_SCHEDULER_RGA3_CORE0 | IM_SCHEDULER_RGA3_CORE1;
    return improcess(srcBuf, dstBuf, {}, {}, {}, {}, -1, NULL, &opt, mode);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imtranslate(JNIEnv *env, jobject thiz, jobject src, jobject dst, jint x, jint y) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    if (x < 0 || y < 0 || x >= srcBuf.width || y >= srcBuf.height) {
        LOGE("Invalid translation parameters: x=%d, y=%d", x, y);
        return -1;
    }
    // 完整初始化 im_opt_t 结构体
    im_opt_t opt;
    memset(&opt, 0, sizeof(im_opt_t));
    opt.version = RGA_CURRENT_API_VERSION;
    opt.core = IM_SCHEDULER_RGA3_CORE0 | IM_SCHEDULER_RGA3_CORE1;
    im_rect srect;
    srect.x = x;
    srect.y = y;
    srect.width = (x + srcBuf.width > srcBuf.wstride) ?
                  (srcBuf.wstride - x) : srcBuf.width;
    srect.height = (y + srcBuf.height > srcBuf.hstride) ?
                   (srcBuf.hstride - y) : srcBuf.height;
    im_rect drect = {0, 0, dstBuf.width, dstBuf.height};
    return improcess(srcBuf, dstBuf, {}, srect, drect, {}, -1, NULL, &opt, 0);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imblend(JNIEnv *env, jobject thiz, jobject src, jobject dst, jint mode) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    im_opt_t opt;
    memset(&opt, 0, sizeof(im_opt_t));
    opt.version = RGA_CURRENT_API_VERSION;
    opt.core = IM_SCHEDULER_RGA3_CORE0 | IM_SCHEDULER_RGA3_CORE1;
    return improcess(srcBuf, dstBuf, {}, {}, {}, {}, -1, NULL, &opt,IM_SYNC | mode);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imcomposite(JNIEnv *env, jobject thiz, jobject srcA, jobject srcB, jobject dst, jint mode) {
    rga_buffer_t srcABuf = getRgaBuffer(env, srcA);
    rga_buffer_t srcBBuf = getRgaBuffer(env, srcB);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    im_opt_t opt;
    memset(&opt, 0, sizeof(im_opt_t));
    opt.version = RGA_CURRENT_API_VERSION;
    opt.core = IM_SCHEDULER_RGA3_CORE0 | IM_SCHEDULER_RGA3_CORE1;
    return improcess(srcABuf, dstBuf, srcBBuf, {}, {}, {}, -1, NULL, &opt, mode);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imcvtcolor(JNIEnv *env, jobject thiz, jobject src, jobject dst, jint sfmt, jint dfmt) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    im_opt_t opt;
    memset(&opt, 0, sizeof(im_opt_t));
    opt.version = RGA_CURRENT_API_VERSION;
    opt.core = IM_SCHEDULER_RGA3_CORE0 | IM_SCHEDULER_RGA3_CORE1;
    return improcess(srcBuf, dstBuf, {}, {}, {}, {}, -1, NULL, &opt, 0);
}

JNIEXPORT jlong JNICALL
Java_com_rockchip_librga_Rga_imbeginJob(JNIEnv *env, jobject thiz, jlong flags) {
    // 设置当前线程默认调度到 RGA3 核心，这将影响该线程后续创建的任务
    imconfig(IM_CONFIG_SCHEDULER_CORE, IM_SCHEDULER_RGA3_CORE0 | IM_SCHEDULER_RGA3_CORE1);
    return (jlong)imbeginJob((uint64_t)flags);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imendJob(JNIEnv *env, jobject thiz, jlong jobHandle, jint syncMode) {
    return imendJob((im_job_handle_t)jobHandle, (int)syncMode);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imcancelJob(JNIEnv *env, jobject thiz, jlong jobHandle) {
    return imcancelJob((im_job_handle_t)jobHandle);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imcopyTask(JNIEnv *env, jobject thiz, jlong jobHandle, jobject src, jobject dst) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imcopyTask((im_job_handle_t)jobHandle, srcBuf, dstBuf);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imresizeTask(JNIEnv *env, jobject thiz, jlong jobHandle, jobject src, jobject dst, jdouble fx, jdouble fy) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imresizeTask((im_job_handle_t)jobHandle, srcBuf, dstBuf, fx, fy);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imrescaleTask(JNIEnv *env, jobject thiz, jlong jobHandle, jobject src, jobject dst, jdouble fx, jdouble fy) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    im_rect srect = {0, 0, srcBuf.width, srcBuf.height};
    im_rect drect = {0, 0, (int)(srcBuf.width * fx), (int)(srcBuf.height * fy)};

    im_opt_t opt;
    memset(&opt, 0, sizeof(im_opt_t));
    opt.version = RGA_CURRENT_API_VERSION;
    opt.core = IM_SCHEDULER_RGA3_CORE0 | IM_SCHEDULER_RGA3_CORE1;

    return improcessTask((im_job_handle_t)jobHandle, srcBuf, dstBuf, {}, srect, drect, {}, &opt, 0);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imcropTask(JNIEnv *env, jobject thiz, jlong jobHandle, jobject src, jobject dst, jobject rect) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    im_rect imRect = getRgaRect(env, rect);
    return imcropTask((im_job_handle_t)jobHandle, srcBuf, dstBuf, imRect);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imrotateTask(JNIEnv *env, jobject thiz, jlong jobHandle, jobject src, jobject dst, jint rotation) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imrotateTask((im_job_handle_t)jobHandle, srcBuf, dstBuf, rotation);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imflipTask(JNIEnv *env, jobject thiz, jlong jobHandle, jobject src, jobject dst, jint mode) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imflipTask((im_job_handle_t)jobHandle, srcBuf, dstBuf, mode);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imtranslateTask(JNIEnv *env, jobject thiz, jlong jobHandle, jobject src, jobject dst, jint x, jint y) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imtranslateTask((im_job_handle_t)jobHandle, srcBuf, dstBuf, x, y);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imblendTask(JNIEnv *env, jobject thiz, jlong jobHandle, jobject src, jobject dst, jint mode) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imblendTask((im_job_handle_t)jobHandle, srcBuf, dstBuf, mode);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imcompositeTask(JNIEnv *env, jobject thiz, jlong jobHandle, jobject srcA, jobject srcB, jobject dst, jint mode) {
    rga_buffer_t srcABuf = getRgaBuffer(env, srcA);
    rga_buffer_t srcBBuf = getRgaBuffer(env, srcB);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imcompositeTask((im_job_handle_t)jobHandle, srcABuf, srcBBuf, dstBuf, mode);
}

JNIEXPORT jint JNICALL
Java_com_rockchip_librga_Rga_imcvtcolorTask(JNIEnv *env, jobject thiz, jlong jobHandle, jobject src, jobject dst, jint sfmt, jint dfmt) {
    rga_buffer_t srcBuf = getRgaBuffer(env, src);
    rga_buffer_t dstBuf = getRgaBuffer(env, dst);
    return imcvtcolorTask((im_job_handle_t)jobHandle, srcBuf, dstBuf, sfmt, dfmt);
}

} // extern "C"
