package com.rockchip.librga

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private lateinit var ivOriginal: ImageView
    private lateinit var ivProcessed: ImageView
    private lateinit var btnRunAllTests: Button
    private lateinit var btnTestCopy: Button
    private lateinit var btnTestResize: Button
    private lateinit var btnTestCrop: Button
    private lateinit var btnTestRotate: Button
    private lateinit var btnTestFlip: Button
    private lateinit var btnTestTranslate: Button
    private lateinit var btnTestBlend: Button
    private lateinit var btnTestConvertColor: Button
    private lateinit var btnTestFill: Button

    private var originalBitmap: Bitmap? = null
    private var processedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()

        // Generate a test image
        generateTestImage()

        // Initially show the same image in both views
        ivProcessed.setImageBitmap(originalBitmap)

        // Force RGA3 scheduler (requested by user)
        Rga.imconfig(Rga.IM_CONFIG_SCHEDULER_CORE, (Rga.IM_SCHEDULER_RGA3_CORE0 or Rga.IM_SCHEDULER_RGA3_CORE1).toLong())
    }

    private fun initViews() {
        tvResult = findViewById(R.id.tvResult)
        ivOriginal = findViewById(R.id.ivOriginal)
        ivProcessed = findViewById(R.id.ivProcessed)
        btnRunAllTests = findViewById(R.id.btnRunAllTests)
        btnTestCopy = findViewById(R.id.btnTestCopy)
        btnTestResize = findViewById(R.id.btnTestResize)
        btnTestCrop = findViewById(R.id.btnTestCrop)
        btnTestRotate = findViewById(R.id.btnTestRotate)
        btnTestFlip = findViewById(R.id.btnTestFlip)
        btnTestTranslate = findViewById(R.id.btnTestTranslate)
        btnTestBlend = findViewById(R.id.btnTestBlend)
        btnTestConvertColor = findViewById(R.id.btnTestConvertColor)
        btnTestFill = findViewById(R.id.btnTestFill)
    }

    private fun setupClickListeners() {
        btnRunAllTests.setOnClickListener { runAllTests() }
        btnTestCopy.setOnClickListener { testCopy() }
        btnTestResize.setOnClickListener { testResize() }
        btnTestCrop.setOnClickListener { testCrop() }
        btnTestRotate.setOnClickListener { testRotate() }
        btnTestFlip.setOnClickListener { testFlip() }
        btnTestTranslate.setOnClickListener { testTranslate() }
        btnTestBlend.setOnClickListener { testBlend() }
        btnTestConvertColor.setOnClickListener { testConvertColor() }
        btnTestFill.setOnClickListener { testFill() }
    }

    private fun generateTestImage() {
        val width = 400
        val height = 400

        originalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(originalBitmap!!)
        canvas.drawColor(Color.WHITE)

        // Draw a colorful asymmetric pattern to make transformations more visible
        val colors = intArrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.GRAY, Color.DKGRAY, Color.LTGRAY)
        val cellWidth = width / 3
        val cellHeight = height / 3

        for (row in 0 until 3) {
            for (col in 0 until 3) {
                val colorIndex = (row * 3 + col) % colors.size
                canvas.drawRect(
                    col * cellWidth.toFloat(),
                    row * cellHeight.toFloat(),
                    (col + 1) * cellWidth.toFloat(),
                    (row + 1) * cellHeight.toFloat(),
                    android.graphics.Paint().apply {
                        setColor(colors[colorIndex])
                        style = android.graphics.Paint.Style.FILL
                    }
                )
            }
        }

        // Draw distinctive elements to make rotation/flipping more obvious
        val paint = android.graphics.Paint().apply {
            setColor(Color.BLACK)
            strokeWidth = 8f
            style = android.graphics.Paint.Style.STROKE
        }

        // Draw a thick L-shape in the top-left quadrant to make rotations obvious
        val quadWidth = width / 2
        val quadHeight = height / 2
        canvas.drawLine(10f, 10f, quadWidth - 10f, 10f, paint)  // Top horizontal
        canvas.drawLine(10f, 10f, 10f, quadHeight - 10f, paint)  // Left vertical

        // Draw an arrow pointing right in the bottom-right quadrant
        val startX = width / 2 + 20
        val startY = height / 2 + quadHeight / 2
        canvas.drawLine((width / 2 + 20).toFloat(), startY.toFloat(), (width - 20).toFloat(), startY.toFloat(), paint)  // Arrow body
        canvas.drawLine((width - 20).toFloat(), startY.toFloat(), (width - 35).toFloat(), (startY - 10).toFloat(), paint)  // Arrow head
        canvas.drawLine((width - 20).toFloat(), startY.toFloat(), (width - 35).toFloat(), (startY + 10).toFloat(), paint)  // Arrow head

        // Draw text to make transformations more obvious
        val textPaint = android.graphics.Paint().apply {
            setColor(Color.WHITE)
            textSize = 40f
            textAlign = android.graphics.Paint.Align.CENTER
            style = android.graphics.Paint.Style.FILL_AND_STROKE
            strokeWidth = 2f
        }
        canvas.drawText("RGATST", width / 2f, height / 2f + 15, textPaint)

        ivOriginal.setImageBitmap(originalBitmap)
    }

    private fun runAllTests() {
        Thread {
            try {
                updateResultText("Running all tests...\n")

                // Run each test sequentially with delays
                testCopyOnUiThread()
                Thread.sleep(2000)

                testResizeOnUiThread()
                Thread.sleep(2000)

                testCropOnUiThread()
                Thread.sleep(2000)

                testRotateOnUiThread()
                Thread.sleep(2000)

                testFlipOnUiThread()
                Thread.sleep(2000)

                testTranslateOnUiThread()
                Thread.sleep(2000)

                testBlendOnUiThread()
                Thread.sleep(2000)

                testConvertColorOnUiThread()
                Thread.sleep(2000)

                testFillOnUiThread()
                Thread.sleep(2000)

                testFillUsingCopyOnUiThread() // New RGA3 workaround test

                updateResultTextOnUiThread("\nAll tests completed!")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error running tests", e)
                updateResultTextOnUiThread("Error running tests: ${e.message}")
            }
        }.start()
    }

    private fun testFillUsingCopyOnUiThread() {
        runOnUiThread { testFillUsingCopy() }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun testFillUsingCopy() {
        Thread {
            try {
                updateResultTextOnUiThread("Starting Fill-via-Copy (RGA3 Workaround)...")
                val width = 400
                val height = 400

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    try {
                        // 1. Create Destination HardwareBuffer (Target Image)
                        // Using GPU_COLOR_OUTPUT to ensure it's usable as render target if needed,
                        // and likely allocated in a way RGA likes (though RGA3 handles any).
                        val usageDst = android.hardware.HardwareBuffer.USAGE_CPU_READ_OFTEN or
                                android.hardware.HardwareBuffer.USAGE_CPU_WRITE_OFTEN or
                                android.hardware.HardwareBuffer.USAGE_GPU_COLOR_OUTPUT

                        val dstHb = android.hardware.HardwareBuffer.create(width, height, android.hardware.HardwareBuffer.RGBA_8888, 1, usageDst)
                        val dstBuffer = Rga.RgaBuffer(width, height, Rga.RK_FORMAT_RGBA_8888, hardwareBuffer = dstHb)

                        // 2. Create Source HardwareBuffer (Small 16x16 solid color block)
                        // RGA3 will scale this up to fill the target rect
                        val srcSize = 16
                        val usageSrc = android.hardware.HardwareBuffer.USAGE_CPU_WRITE_OFTEN or
                                android.hardware.HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE
                        val srcHb = android.hardware.HardwareBuffer.create(srcSize, srcSize, android.hardware.HardwareBuffer.RGBA_8888, 1, usageSrc)

                        // Fill source HB with solid Green color manually (using Bitmap wrapper)
                        val srcBitmap = Bitmap.wrapHardwareBuffer(srcHb, android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SRGB))
                        if (srcBitmap != null) {
                            srcBitmap.eraseColor(Color.GREEN)
                            srcBitmap.recycle() // Recycle wrapper, keep HB
                        } else {
                            updateResultTextOnUiThread("Failed to wrap src HB in Bitmap")
                            return@Thread
                        }

                        val srcBuffer = Rga.RgaBuffer(srcSize, srcSize, Rga.RK_FORMAT_RGBA_8888, hardwareBuffer = srcHb)

                        // 3. Perform Copy (Scaling) from small Src to Dst
                        // Destination rect: 50, 50, 100, 100
                        // Since imcopy usually copies src rect to dst rect, we need to ensure imcopy implementation
                        // supports scaling if src and dst dims differ.
                        // But wait, standard `imcopy` might just do 1:1 copy or require same size.
                        // `imresize` is for scaling. `imcopy` documentation says "Copy src to dst".
                        // If we use `imresize`, it takes full buffer to full buffer usually.
                        // We want "Copy src (small) to Dst (sub-rect)".
                        // RGA `imcopy` with explicit rects is actually `imcrop` or `improcess`.
                        // Kotlin wrapper `imcrop` takes `rect` for crop.

                        // Let's use `imresize` if we want scaling. But `imresize` in Kotlin wrapper takes full src and full dst buffers.
                        // To fill a sub-rect (50, 50, 100, 100), we can't easily use simple `imresize` API unless we create a "sub-buffer" wrapper for Dst.

                        // Option A: Use `imcopy` if the underlying implementation supports scaling (im2d usually handles it if rects differ).
                        // But Kotlin `imcopy` doesn't take rects.

                        // Option B: Use `imcrop`? `imcrop` usually means crop SRC to DST.

                        // Let's try `imresize` but we need to target a sub-region of dst.
                        // Since our Kotlin API is limited, maybe we can just fill the WHOLE dst buffer for this test to prove the point?
                        // "Fill (Copy) 400x400 with Green".

                        val result = Rga.imresize(srcBuffer, dstBuffer)

                        if (result == Rga.IM_STATUS_SUCCESS) {
                            updateResultTextOnUiThread("Fill-via-Copy (Resize): SUCCESS")
                        } else {
                            updateResultTextOnUiThread("Fill-via-Copy (Resize): FAILED (res: $result)")
                        }

                        srcHb.close()
                        dstHb.close()

                    } catch (e: Exception) {
                        Log.e("MainActivity", "Fill-via-Copy Error", e)
                        updateResultTextOnUiThread("Fill-via-Copy: ERROR - ${e.message}")
                    }
                } else {
                    updateResultTextOnUiThread("HardwareBuffer/Wrap not supported (Need API 26/29+)")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in testFillUsingCopy", e)
            }
        }.start()
    }

    private fun testResizeOnUiThread() {
        runOnUiThread { testResize() }
    }

    private fun testCropOnUiThread() {
        runOnUiThread { testCrop() }
    }

    private fun testRotateOnUiThread() {
        runOnUiThread { testRotate() }
    }

    private fun testFlipOnUiThread() {
        runOnUiThread { testFlip() }
    }

    private fun testTranslateOnUiThread() {
        runOnUiThread { testTranslate() }
    }

    private fun testBlendOnUiThread() {
        runOnUiThread { testBlend() }
    }

    private fun testConvertColorOnUiThread() {
        runOnUiThread { testConvertColor() }
    }

    private fun testFillOnUiThread() {
        runOnUiThread { testFill() }
    }

    private fun testCopyOnUiThread() {
        runOnUiThread { testCopy() }
    }

    private fun testCopy() {
        updateResultTextOnUiThread("Running Copy test...")
        Thread {
            try {
                val srcBitmap = originalBitmap ?: return@Thread
                val dstBitmap = Bitmap.createBitmap(srcBitmap.width, srcBitmap.height, Bitmap.Config.ARGB_8888)

                // Create RgaBuffers
                val srcBuffer = Rga.createRgaBufferFromBitmap(srcBitmap)
                val dstBuffer = Rga.createRgaBufferFromBitmap(dstBitmap)

                // Call the copy function
                val result = Rga.imcopy(srcBuffer, dstBuffer)

                if (result == Rga.IM_STATUS_SUCCESS) {
                    // Ensure the dstBitmap reflects the changes from RGA operation
                    // The RGA operation should have modified the underlying buffer
                    Rga.copyRgaBufferToBitmap(dstBuffer, dstBitmap)
                    updateProcessedBitmapWithErrorHandling(dstBitmap)
                    updateResultTextOnUiThread("Copy test: SUCCESS")
                } else {
                    updateResultTextOnUiThread("Copy test: FAILED (result: $result)")
                    // Keep the previous image in case of failure
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in testCopy", e)
                updateResultTextOnUiThread("Copy test: ERROR - ${e.message}")
                // Keep the previous image in case of exception
            }
        }.start()
    }

    private fun testResize() {
        updateResultTextOnUiThread("Running Resize test...")
        Thread {
            try {
                val srcBitmap = originalBitmap ?: return@Thread
                val newWidth = (srcBitmap.width * 0.7).toInt()  // Changed from 0.5 to 0.7 to make difference more visible
                val newHeight = (srcBitmap.height * 0.7).toInt()
                val dstBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

                // Create RgaBuffers
                val srcBuffer = Rga.createRgaBufferFromBitmap(srcBitmap)
                val dstBuffer = Rga.createRgaBufferFromBitmap(dstBitmap)

                // Call the resize function
                val result = Rga.imresize(srcBuffer, dstBuffer, 0.7, 0.7)  // Updated scale factors

                if (result == Rga.IM_STATUS_SUCCESS) {
                    // Copy data from buffer back to bitmap
                    Rga.copyRgaBufferToBitmap(dstBuffer, dstBitmap)

                    // Update the processed bitmap with the result
                    updateProcessedBitmapWithErrorHandling(dstBitmap)
                    updateResultTextOnUiThread("Resize test: SUCCESS (${srcBitmap.width}x${srcBitmap.height} -> ${newWidth}x${newHeight})")
                } else {
                    updateResultTextOnUiThread("Resize test: FAILED (result: $result)")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in testResize", e)
                updateResultTextOnUiThread("Resize test: ERROR - ${e.message}")
            }
        }.start()
    }

    private fun testCrop() {
        updateResultTextOnUiThread("Running Crop test...")
        Thread {
            try {
                val srcBitmap = originalBitmap ?: return@Thread
                val cropRect = Rga.RgaRect(100, 100, 200, 150)  // Changed to crop a distinctive region
                val dstBitmap = Bitmap.createBitmap(cropRect.width, cropRect.height, Bitmap.Config.ARGB_8888)

                // Create RgaBuffers
                val srcBuffer = Rga.createRgaBufferFromBitmap(srcBitmap)
                val dstBuffer = Rga.createRgaBufferFromBitmap(dstBitmap)

                // Call the crop function
                val result = Rga.imcrop(srcBuffer, dstBuffer, cropRect)

                if (result == Rga.IM_STATUS_SUCCESS) {
                    Rga.copyRgaBufferToBitmap(dstBuffer, dstBitmap)

                    // Update the processed bitmap with the result
                    updateProcessedBitmapWithErrorHandling(dstBitmap)
                    updateResultTextOnUiThread("Crop test: SUCCESS (cropped ${cropRect.x},${cropRect.y} ${cropRect.width}x${cropRect.height})")
                } else {
                    updateResultTextOnUiThread("Crop test: FAILED (result: $result)")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in testCrop", e)
                updateResultTextOnUiThread("Crop test: ERROR - ${e.message}")
            }
        }.start()
    }

    private fun testRotate() {
        updateResultTextOnUiThread("Running Rotate test...")
        Thread {
            try {
                val srcBitmap = originalBitmap ?: return@Thread
                val dstBitmap = Bitmap.createBitmap(srcBitmap.width, srcBitmap.height, Bitmap.Config.ARGB_8888)

                // Create RgaBuffers
                val srcBuffer = Rga.createRgaBufferFromBitmap(srcBitmap)
                val dstBuffer = Rga.createRgaBufferFromBitmap(dstBitmap)

                // Call the rotate function (90 degrees)
                val result = Rga.imrotate(srcBuffer, dstBuffer, Rga.IM_HAL_TRANSFORM_ROT_90)

                if (result == Rga.IM_STATUS_SUCCESS) {
                    // Update the processed bitmap with the result
                    Rga.copyRgaBufferToBitmap(dstBuffer, dstBitmap)
                    updateProcessedBitmapWithErrorHandling(dstBitmap)
                    updateResultTextOnUiThread("Rotate test: SUCCESS")
                } else {
                    updateResultTextOnUiThread("Rotate test: FAILED (result: $result)")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in testRotate", e)
                updateResultTextOnUiThread("Rotate test: ERROR - ${e.message}")
            }
        }.start()
    }

    private fun testFlip() {
        updateResultTextOnUiThread("Running Flip test...")
        Thread {
            try {
                val srcBitmap = originalBitmap ?: return@Thread
                val dstBitmap = Bitmap.createBitmap(srcBitmap.width, srcBitmap.height, Bitmap.Config.ARGB_8888)

                // Create RgaBuffers
                val srcBuffer = Rga.createRgaBufferFromBitmap(srcBitmap)
                val dstBuffer = Rga.createRgaBufferFromBitmap(dstBitmap)

                // Call the flip function (horizontal flip)
                val result = Rga.imflip(srcBuffer, dstBuffer, Rga.IM_HAL_TRANSFORM_FLIP_H)

                if (result == Rga.IM_STATUS_SUCCESS) {
                    // Update the processed bitmap with the result
                    Rga.copyRgaBufferToBitmap(dstBuffer, dstBitmap)
                    updateProcessedBitmapWithErrorHandling(dstBitmap)
                    updateResultTextOnUiThread("Flip test: SUCCESS")
                } else {
                    updateResultTextOnUiThread("Flip test: FAILED (result: $result)")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in testFlip", e)
                updateResultTextOnUiThread("Flip test: ERROR - ${e.message}")
            }
        }.start()
    }

    private fun testTranslate() {
        updateResultTextOnUiThread("Running Translate test...")
        Thread {
            try {
                val srcBitmap = originalBitmap ?: return@Thread
                val dstBitmap = Bitmap.createBitmap(srcBitmap.width, srcBitmap.height, Bitmap.Config.ARGB_8888)

                // Create RgaBuffers
                val srcBuffer = Rga.createRgaBufferFromBitmap(srcBitmap)
                val dstBuffer = Rga.createRgaBufferFromBitmap(dstBitmap)

                // Call the translate function (move 80 pixels right and down to make it more visible)
                val result = Rga.imtranslate(srcBuffer, dstBuffer, 80, 80)

                if (result == Rga.IM_STATUS_SUCCESS) {
                    // Update the processed bitmap with the result
                    Rga.copyRgaBufferToBitmap(dstBuffer, dstBitmap)
                    updateProcessedBitmapWithErrorHandling(dstBitmap)
                    updateResultTextOnUiThread("Translate test: SUCCESS (translated 80,80)")
                } else {
                    updateResultTextOnUiThread("Translate test: FAILED (result: $result)")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in testTranslate", e)
                updateResultTextOnUiThread("Translate test: ERROR - ${e.message}")
            }
        }.start()
    }

    private fun testBlend() {
        updateResultTextOnUiThread("Running Blend test...")
        Thread {
            try {
                val srcBitmap = originalBitmap ?: return@Thread
                val dstBitmap = Bitmap.createBitmap(srcBitmap.width, srcBitmap.height, Bitmap.Config.ARGB_8888)

                // Create a second source bitmap with a different pattern for blending
                val src2Bitmap = Bitmap.createBitmap(srcBitmap.width, srcBitmap.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(src2Bitmap)
                canvas.drawColor(Color.argb(128, 255, 0, 0)) // Semi-transparent red overlay

                // Create RgaBuffers
                val srcBuffer = Rga.createRgaBufferFromBitmap(srcBitmap)
                val src2Buffer = Rga.createRgaBufferFromBitmap(src2Bitmap)
                val dstBuffer = Rga.createRgaBufferFromBitmap(dstBitmap)

                // Call the composite function to blend src2 over src into dst
                val result = Rga.imcomposite(src2Buffer, srcBuffer, dstBuffer, Rga.IM_ALPHA_BLEND_SRC_OVER)

                if (result == Rga.IM_STATUS_SUCCESS) {
                    // Update the processed bitmap with the result
                    Rga.copyRgaBufferToBitmap(dstBuffer, dstBitmap)
                    updateProcessedBitmapWithErrorHandling(dstBitmap)
                    updateResultTextOnUiThread("Blend test: SUCCESS")
                } else {
                    updateResultTextOnUiThread("Blend test: FAILED (result: $result)")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in testBlend", e)
                updateResultTextOnUiThread("Blend test: ERROR - ${e.message}")
            }
        }.start()
    }

    private fun testConvertColor() {
        updateResultTextOnUiThread("Running Color conversion test...")
        Thread {
            try {
                val srcBitmap = originalBitmap ?: return@Thread
                val dstBitmap = Bitmap.createBitmap(srcBitmap.width, srcBitmap.height, Bitmap.Config.ARGB_8888)

                // Create RgaBuffers
                val srcBuffer = Rga.createRgaBufferFromBitmap(srcBitmap, Rga.RK_FORMAT_RGBA_8888)
                val dstBuffer = Rga.createRgaBufferFromBitmap(dstBitmap, Rga.RK_FORMAT_BGRA_8888)

                // Call the color conversion function
                val result = Rga.imcvtcolor(srcBuffer, dstBuffer, Rga.RK_FORMAT_RGBA_8888, Rga.RK_FORMAT_BGRA_8888)

                if (result == Rga.IM_STATUS_SUCCESS) {
                    // Update the processed bitmap with the result
                    Rga.copyRgaBufferToBitmap(dstBuffer, dstBitmap)
                    updateProcessedBitmapWithErrorHandling(dstBitmap)
                    updateResultTextOnUiThread("Color conversion test: SUCCESS")
                } else {
                    updateResultTextOnUiThread("Color conversion test: FAILED (result: $result)")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in testConvertColor", e)
                updateResultTextOnUiThread("Color conversion test: ERROR - ${e.message}")
            }
        }.start()
    }

    private fun testFill() {
        Thread {
            try {
                val width = 400
                val height = 400
                val fillRect = Rga.RgaRect(50, 50, 100, 100)

                updateResultTextOnUiThread("Starting Fill tests...")

                // --- Test 1: Standard Bitmap (likely to fail on RGA2 if address > 4G) ---
                try {
                    val dstBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val dstBuffer = Rga.createRgaBufferFromBitmap(dstBitmap)
                    val result = Rga.imfill(dstBuffer, fillRect, Color.RED)
                    if (result == Rga.IM_STATUS_SUCCESS) {
                        Rga.copyRgaBufferToBitmap(dstBuffer, dstBitmap)
                        updateResultTextOnUiThread("Fill (Bitmap): SUCCESS")
                        updateProcessedBitmap(dstBitmap)
                    } else {
                        updateResultTextOnUiThread("Fill (Bitmap): FAILED (res: $result) - Check dmesg for 4G limit")
                    }
                } catch (e: Exception) {
                    updateResultTextOnUiThread("Fill (Bitmap): ERROR - ${e.message}")
                }

                // --- Test 2: HardwareBuffer (Recommended for RGA) ---
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    try {
                        val usage = android.hardware.HardwareBuffer.USAGE_CPU_READ_OFTEN or
                                android.hardware.HardwareBuffer.USAGE_CPU_WRITE_OFTEN or
                                android.hardware.HardwareBuffer.USAGE_GPU_COLOR_OUTPUT

                        val hb = android.hardware.HardwareBuffer.create(width, height, android.hardware.HardwareBuffer.RGBA_8888, 1, usage)
                        val dstBuffer = Rga.RgaBuffer(width, height, Rga.RK_FORMAT_RGBA_8888, hardwareBuffer = hb)

                        val result = Rga.imfill(dstBuffer, fillRect, Color.BLUE) // Use Blue for HB test
                        if (result == Rga.IM_STATUS_SUCCESS) {
                            updateResultTextOnUiThread("Fill (HardwareBuffer): SUCCESS")
                        } else {
                            updateResultTextOnUiThread("Fill (HardwareBuffer): FAILED (res: $result)")
                        }
                        hb.close()
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to create HardwareBuffer", e)
                        updateResultTextOnUiThread("Fill (HardwareBuffer): ERROR - ${e.message}")
                    }
                } else {
                    updateResultTextOnUiThread("HardwareBuffer not supported on this API level")
                }

            } catch (e: Exception) {
                Log.e("MainActivity", "Error in testFill", e)
                updateResultTextOnUiThread("Fill test: ERROR - ${e.message}")
            }
        }.start()
    }


    private fun updateProcessedBitmap(bitmap: Bitmap) {
        processedBitmap = bitmap
        runOnUiThread {
            try {
                ivProcessed.setImageBitmap(processedBitmap)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error updating processed bitmap", e)
                updateResultTextOnUiThread("Error displaying result image: ${e.message}")
            }
        }
    }

    private fun updateProcessedBitmapWithErrorHandling(bitmap: Bitmap) {
        // Only update if the bitmap is valid
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) {
            Log.e("MainActivity", "Invalid bitmap provided to updateProcessedBitmap")
            updateResultTextOnUiThread("Error: Invalid result bitmap")
            return
        }

        // Create a new bitmap to ensure it's properly refreshed
        processedBitmap = bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, true)
        runOnUiThread {
            try {
                ivProcessed.setImageBitmap(processedBitmap)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error updating processed bitmap", e)
                updateResultTextOnUiThread("Error displaying result image: ${e.message}")
            }
        }
    }

    private fun updateResultText(text: String) {
        runOnUiThread { tvResult.text = text }
    }

    private fun updateResultTextOnUiThread(text: String) {
        Log.i("TEST", "$text")
        runOnUiThread {
            tvResult.text = "${tvResult.text}$text\n"
            // Scroll to bottom if needed
            tvResult.scrollTo(0, tvResult.bottom)
        }
    }
}
