package com.rockchip.librga

import android.util.Log
import androidx.core.graphics.createBitmap
import java.nio.ByteBuffer

/**
 * Kotlin wrapper for Rockchip Librga (im2d API).
 */
object Rga {
    init {
        System.loadLibrary("rga_jni")
    }

    // Constants from RgaUtils.h / im2d_type.h (Simplified for common usage)
    const val IM_STATUS_SUCCESS = 1

    // Rotation
    const val IM_HAL_TRANSFORM_ROT_90     = 1 shl 0
    const val IM_HAL_TRANSFORM_ROT_180    = 1 shl 1
    const val IM_HAL_TRANSFORM_ROT_270    = 1 shl 2
    const val IM_HAL_TRANSFORM_FLIP_H     = 1 shl 3
    const val IM_HAL_TRANSFORM_FLIP_V     = 1 shl 4
    const val IM_HAL_TRANSFORM_FLIP_H_V   = 1 shl 5

    // Blend modes (Porter-Duff)
    const val IM_ALPHA_BLEND_SRC_OVER     = 1 shl 6
    const val IM_ALPHA_BLEND_SRC          = 1 shl 7
    const val IM_ALPHA_BLEND_DST          = 1 shl 8
    const val IM_ALPHA_BLEND_SRC_IN       = 1 shl 9
    const val IM_ALPHA_BLEND_DST_IN       = 1 shl 10
    const val IM_ALPHA_BLEND_SRC_OUT      = 1 shl 11
    const val IM_ALPHA_BLEND_DST_OUT      = 1 shl 12
    const val IM_ALPHA_BLEND_DST_OVER     = 1 shl 13
    const val IM_ALPHA_BLEND_SRC_ATOP     = 1 shl 14
    const val IM_ALPHA_BLEND_DST_ATOP     = 1 shl 15
    const val IM_ALPHA_BLEND_XOR          = 1 shl 16

    // Common Formats (Add more as needed from rk_drm_rga.h or similar)
    const val RK_FORMAT_RGBA_8888 = 0x0
    const val RK_FORMAT_RGBX_8888 = 0x1
    const val RK_FORMAT_RGB_888 = 0x2
    const val RK_FORMAT_BGRA_8888 = 0x3
    const val RK_FORMAT_RGB_565 = 0x4
    const val RK_FORMAT_RGBA_5551 = 0x5
    const val RK_FORMAT_RGBA_4444 = 0x6
    const val RK_FORMAT_BGR_888 = 0x7

    const val RK_FORMAT_YCbCr_422_SP = 0x8
    const val RK_FORMAT_YCbCr_422_P  = 0x9
    const val RK_FORMAT_YCbCr_420_SP = 0xa
    const val RK_FORMAT_YCbCr_420_P  = 0xb
    const val RK_FORMAT_YCrCb_422_SP = 0xc
    const val RK_FORMAT_YCrCb_422_P  = 0xd
    const val RK_FORMAT_YCrCb_420_SP = 0xe
    const val RK_FORMAT_YCrCb_420_P  = 0xf

    // Sync modes
    const val IM_SYNC = 1 shl 19
    const val IM_ASYNC = 1 shl 26

    // Scheduler configuration
    const val IM_CONFIG_SCHEDULER_CORE = 0
    const val IM_SCHEDULER_RGA3_CORE0 = 1 shl 0
    const val IM_SCHEDULER_RGA3_CORE1 = 1 shl 1
    const val IM_SCHEDULER_RGA2_CORE0 = 1 shl 2
    const val IM_SCHEDULER_RGA2_CORE1 = 1 shl 3

    /**
     * Represents an image buffer for RGA.
     * Use helper methods to construct.
     */
    data class RgaBuffer(
        val width: Int,
        val height: Int,
        val format: Int,
        val wstride: Int = width,
        val hstride: Int = height,
        val fd: Int = -1,
        val handle: Int = 0, // buffer_handle_t
        val ptr: ByteBuffer? = null, // Direct ByteBuffer
        val hardwareBuffer: Any? = null // android.hardware.HardwareBuffer (Type Any to avoid build issues if class not found in older contexts, but expected HardwareBuffer)
    )

    data class RgaRect(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )

    // --- Native Methods ---

    /**
     * Create an RGA image processing job.
     */
    external fun imbeginJob(flags: Long = 0): Long

    /**
     * Submit and execute RGA image processing job.
     */
    external fun imendJob(jobHandle: Long, syncMode: Int = IM_SYNC): Int

    /**
     * Cancel and delete the RGA image processing job.
     */
    external fun imcancelJob(jobHandle: Long): Int

    /**
     * Copy src to dst.
     */
    external fun imcopy(src: RgaBuffer, dst: RgaBuffer): Int

    /**
     * Add an image copy operation to the specified job.
     */
    external fun imcopyTask(jobHandle: Long, src: RgaBuffer, dst: RgaBuffer): Int

    /**
     * Resize src to dst.
     */
    external fun imresize(src: RgaBuffer, dst: RgaBuffer, fx: Double = 0.0, fy: Double = 0.0): Int

    /**
     * Add an image resize operation to the specified job.
     */
    external fun imresizeTask(jobHandle: Long, src: RgaBuffer, dst: RgaBuffer, fx: Double = 0.0, fy: Double = 0.0): Int

    /**
     * Rescale src to dst by factors fx, fy.
     */
    external fun imrescale(src: RgaBuffer, dst: RgaBuffer, fx: Double, fy: Double): Int

    /**
     * Add an image rescale operation to the specified job.
     */
    external fun imrescaleTask(jobHandle: Long, src: RgaBuffer, dst: RgaBuffer, fx: Double, fy: Double): Int

    /**
     * Crop src to dst using rect.
     */
    external fun imcrop(src: RgaBuffer, dst: RgaBuffer, rect: RgaRect): Int

    /**
     * Add an image crop operation to the specified job.
     */
    external fun imcropTask(jobHandle: Long, src: RgaBuffer, dst: RgaBuffer, rect: RgaRect): Int

    /**
     * Rotate src to dst.
     * rotation: One of IM_HAL_TRANSFORM_*
     */
    external fun imrotate(src: RgaBuffer, dst: RgaBuffer, rotation: Int): Int

    /**
     * Add an image rotation operation to the specified job.
     */
    external fun imrotateTask(jobHandle: Long, src: RgaBuffer, dst: RgaBuffer, rotation: Int): Int

    /**
     * Flip src to dst.
     * mode: One of IM_HAL_TRANSFORM_FLIP_*
     */
    external fun imflip(src: RgaBuffer, dst: RgaBuffer, mode: Int): Int

    /**
     * Add an image flip operation to the specified job.
     */
    external fun imflipTask(jobHandle: Long, src: RgaBuffer, dst: RgaBuffer, mode: Int): Int

    /**
     * Translate src to dst.
     */
    external fun imtranslate(src: RgaBuffer, dst: RgaBuffer, x: Int, y: Int): Int

    /**
     * Add an image translation operation to the specified job.
     */
    external fun imtranslateTask(jobHandle: Long, src: RgaBuffer, dst: RgaBuffer, x: Int, y: Int): Int

    /**
     * Blend src (foreground) and dst (background) -> dst.
     */
    external fun imblend(src: RgaBuffer, dst: RgaBuffer, mode: Int = IM_ALPHA_BLEND_SRC_OVER): Int

    /**
     * Add an image blend operation to the specified job.
     */
    external fun imblendTask(jobHandle: Long, src: RgaBuffer, dst: RgaBuffer, mode: Int = IM_ALPHA_BLEND_SRC_OVER): Int

    /**
     * Composite srcA (foreground) and srcB (background) -> dst.
     */
    external fun imcomposite(srcA: RgaBuffer, srcB: RgaBuffer, dst: RgaBuffer, mode: Int = IM_ALPHA_BLEND_SRC_OVER): Int

    /**
     * Add an image composite operation to the specified job.
     */
    external fun imcompositeTask(jobHandle: Long, srcA: RgaBuffer, srcB: RgaBuffer, dst: RgaBuffer, mode: Int = IM_ALPHA_BLEND_SRC_OVER): Int

    /**
     * Convert color format.
     */
    external fun imcvtcolor(src: RgaBuffer, dst: RgaBuffer, sfmt: Int, dfmt: Int): Int

    /**
     * Add an image format conversion operation to the specified job.
     */
    external fun imcvtcolorTask(jobHandle: Long, src: RgaBuffer, dst: RgaBuffer, sfmt: Int, dfmt: Int): Int

    // Helpers to create RgaBuffer
    fun createBufferFromFd(fd: Int, width: Int, height: Int, format: Int, wstride: Int = width, hstride: Int = height): RgaBuffer {
        return RgaBuffer(width, height, format, wstride, hstride, fd = fd)
    }

    fun createBufferFromByteBuffer(buffer: ByteBuffer, width: Int, height: Int, format: Int, wstride: Int = width, hstride: Int = height): RgaBuffer {
        if (!buffer.isDirect) {
            throw kotlin.IllegalArgumentException("ByteBuffer must be direct")
        }
        return RgaBuffer(width, height, format, wstride, hstride, ptr = buffer)
    }

    // Helper methods for Android Bitmap integration
    fun createRgaBufferFromBitmap(bitmap: android.graphics.Bitmap, format: Int = Rga.RK_FORMAT_RGBA_8888): RgaBuffer {
        val byteBuffer = bitmapToByteBuffer(bitmap)
        return createBufferFromByteBuffer(
            byteBuffer,
            bitmap.width,
            bitmap.height,
            format
        )
    }

    // Helper methods for NV21 data integration
    fun createRgaBufferFromNv21(nv21Data: ByteArray, width: Int, height: Int, format: Int = Rga.RK_FORMAT_YCrCb_420_SP): RgaBuffer {
        val byteBuffer = java.nio.ByteBuffer.allocateDirect(nv21Data.size)
        byteBuffer.put(nv21Data)
        byteBuffer.rewind()
        return createBufferFromByteBuffer(
            byteBuffer,
            width,
            height,
            format
        )
    }

    /**
     * Helper to create RgaBuffer from an existing direct ByteBuffer containing NV21 data.
     */
    fun fillRgaBufferWithNv21(buffer: ByteBuffer, nv21Data: ByteArray, width: Int, height: Int, format: Int = Rga.RK_FORMAT_YCrCb_420_SP): RgaBuffer {
        buffer.clear()
        if (buffer.capacity() < nv21Data.size) {
            throw IllegalArgumentException("ByteBuffer capacity too small")
        }
        buffer.put(nv21Data)
        buffer.rewind()
        return createBufferFromByteBuffer(buffer, width, height, format)
    }

    /**
     * Crop a region from srcBuffer and return as a Bitmap.
     * This handles both the hardware cropping and the format conversion to RGBA.
     */
    fun cropToBitmap(srcBuffer: RgaBuffer, rect: android.graphics.Rect): android.graphics.Bitmap {
        Log.v("Rga", "cropToBitmap")
        val cropWidth = rect.width()
        val cropHeight = rect.height()

        // 1. Create target bitmap (Ensure even dimensions if required by hardware, though RGA handles most)
        val bitmap = createBitmap(cropWidth, cropHeight, android.graphics.Bitmap.Config.ARGB_8888)

        // 2. Prepare destination RGA buffer using a direct ByteBuffer
        val byteCount = cropWidth * cropHeight * 4 // RGBA_8888 is 4 bytes per pixel
        val dstByteBuffer = java.nio.ByteBuffer.allocateDirect(byteCount)
        val dstBuffer = createBufferFromByteBuffer(dstByteBuffer, cropWidth, cropHeight, RK_FORMAT_RGBA_8888)

        // 3. Execute crop (RGA imcrop will crop the 'rect' from 'srcBuffer' and scale/copy to 'dstBuffer')
        val rgaRect = RgaRect(rect.left, rect.top, cropWidth, cropHeight)
        imcrop(srcBuffer, dstBuffer, rgaRect)

        // 4. Copy results from the hardware-filled buffer back to the bitmap
        dstByteBuffer.rewind()
        bitmap.copyPixelsFromBuffer(dstByteBuffer)

        return bitmap
    }

    /**
     * Step 1: Convert an NV21/YUV RgaBuffer to an RGBA RgaBuffer using RGA hardware.
     * Use this to perform the hardware acceleration part separately.
     */
    fun convertNv21ToRgba(srcBuffer: RgaBuffer, dstBuffer: RgaBuffer) {
        if (dstBuffer.format != RK_FORMAT_RGBA_8888) {
            throw IllegalArgumentException("Destination buffer must be in RK_FORMAT_RGBA_8888 format")
        }
        imcvtcolor(srcBuffer, dstBuffer, srcBuffer.format, RK_FORMAT_RGBA_8888)
    }

    /**
     * Step 2: Copy the content of an RGBA RgaBuffer to a Bitmap.
     * This should be called after the buffer has been filled (e.g., by convertNv21ToRgba).
     */
    fun copyRgbaToBitmap(rgbaBuffer: RgaBuffer, dstBitmap: android.graphics.Bitmap) {
        if (rgbaBuffer.ptr == null) {
            throw IllegalArgumentException("RgaBuffer must have a valid direct ByteBuffer (ptr)")
        }
        if (rgbaBuffer.format != RK_FORMAT_RGBA_8888) {
            // In some cases, other formats might work, but RGBA_8888 is standard for Bitmap.copyPixelsFromBuffer
            Log.w("Rga", "Format is not RGBA_8888, copy might fail or show wrong colors")
        }
        rgbaBuffer.ptr.rewind()
        dstBitmap.copyPixelsFromBuffer(rgbaBuffer.ptr)
    }

    fun copyRgaBufferToBitmap(srcBuffer: RgaBuffer, dstBitmap: android.graphics.Bitmap) {
        // Copy data from RGA buffer back to bitmap if needed
        // This is a simplified approach - in practice, the RGA operation should modify the bitmap directly
        // depending on how the RGA library is implemented
        if (srcBuffer.ptr != null) {
            srcBuffer.ptr.rewind()
            dstBitmap.copyPixelsFromBuffer(srcBuffer.ptr)
        }
    }

    private fun bitmapToByteBuffer(bitmap: android.graphics.Bitmap): java.nio.ByteBuffer {
        val byteCount = bitmap.rowBytes * bitmap.height
        val buffer = java.nio.ByteBuffer.allocateDirect(byteCount)
        bitmap.copyPixelsToBuffer(buffer)
        buffer.rewind() // Reset position to beginning
        return buffer
    }


}
