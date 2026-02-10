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
    private lateinit var btnTestRescale: Button
    private lateinit var btnTestCrop: Button
    private lateinit var btnTestRotate: Button
    private lateinit var btnTestFlip: Button
    private lateinit var btnTestTranslate: Button
    private lateinit var btnTestBlend: Button
    private lateinit var btnTestConvertColor: Button
    private lateinit var btnTestYuvToBitmap: Button
    private lateinit var btnTestJob: Button
    private lateinit var btnTestCopyTask: Button
    private lateinit var btnTestResizeTask: Button
    private lateinit var btnTestCropTask: Button
    private lateinit var btnTestRotateTask: Button
    private lateinit var btnTestFlipTask: Button
    private lateinit var btnTestTranslateTask: Button
    private lateinit var btnTestBlendTask: Button
    private lateinit var btnTestConvertColorTask: Button

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

    }

    private fun initViews() {
        tvResult = findViewById(R.id.tvResult)
        ivOriginal = findViewById(R.id.ivOriginal)
        ivProcessed = findViewById(R.id.ivProcessed)
        btnRunAllTests = findViewById(R.id.btnRunAllTests)
        btnTestCopy = findViewById(R.id.btnTestCopy)
        btnTestResize = findViewById(R.id.btnTestResize)
        btnTestRescale = findViewById(R.id.btnTestRescale)
        btnTestCrop = findViewById(R.id.btnTestCrop)
        btnTestRotate = findViewById(R.id.btnTestRotate)
        btnTestFlip = findViewById(R.id.btnTestFlip)
        btnTestTranslate = findViewById(R.id.btnTestTranslate)
        btnTestBlend = findViewById(R.id.btnTestBlend)
        btnTestConvertColor = findViewById(R.id.btnTestConvertColor)
        btnTestYuvToBitmap = findViewById(R.id.btnTestYuvToBitmap)
        btnTestJob = findViewById(R.id.btnTestJob)
        btnTestCopyTask = findViewById(R.id.btnTestCopyTask)
        btnTestResizeTask = findViewById(R.id.btnTestResizeTask)
        btnTestCropTask = findViewById(R.id.btnTestCropTask)
        btnTestRotateTask = findViewById(R.id.btnTestRotateTask)
        btnTestFlipTask = findViewById(R.id.btnTestFlipTask)
        btnTestTranslateTask = findViewById(R.id.btnTestTranslateTask)
        btnTestBlendTask = findViewById(R.id.btnTestBlendTask)
        btnTestConvertColorTask = findViewById(R.id.btnTestConvertColorTask)
    }

    private fun setupClickListeners() {
        btnRunAllTests.setOnClickListener { runAllTests() }
        btnTestCopy.setOnClickListener { testCopy() }
        btnTestResize.setOnClickListener { testResize() }
        btnTestRescale.setOnClickListener { testRescale() }
        btnTestCrop.setOnClickListener { testCrop() }
        btnTestRotate.setOnClickListener { testRotate() }
        btnTestFlip.setOnClickListener { testFlip() }
        btnTestTranslate.setOnClickListener { testTranslate() }
        btnTestBlend.setOnClickListener { testBlend() }
        btnTestConvertColor.setOnClickListener { testConvertColor() }
        btnTestYuvToBitmap.setOnClickListener { testYuvToBitmap() }
        btnTestJob.setOnClickListener { testJob() }
        btnTestCopyTask.setOnClickListener { testCopyTask() }
        btnTestResizeTask.setOnClickListener { testResizeTask() }
        btnTestCropTask.setOnClickListener { testCropTask() }
        btnTestRotateTask.setOnClickListener { testRotateTask() }
        btnTestFlipTask.setOnClickListener { testFlipTask() }
        btnTestTranslateTask.setOnClickListener { testTranslateTask() }
        btnTestBlendTask.setOnClickListener { testBlendTask() }
        btnTestConvertColorTask.setOnClickListener { testConvertColorTask() }
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

                // Run original tests
                testCopyOnUiThread()
                Thread.sleep(1000)
                testResizeOnUiThread()
                Thread.sleep(1000)
                testCropOnUiThread()
                Thread.sleep(1000)
                testRotateOnUiThread()
                Thread.sleep(1000)
                testFlipOnUiThread()
                Thread.sleep(1000)
                testTranslateOnUiThread()
                Thread.sleep(1000)
                testBlendOnUiThread()
                Thread.sleep(1000)
                testConvertColorOnUiThread()
                Thread.sleep(1000)
                testYuvToBitmapOnUiThread()
                Thread.sleep(1000)

                // Run new Multi-task Job test
                testJobOnUiThread()
                Thread.sleep(1500)

                // Run individual Task tests
                updateResultTextOnUiThread("\n--- Running Individual Task Tests ---")
                testCopyTaskOnUiThread()
                Thread.sleep(1000)
                testResizeTaskOnUiThread()
                Thread.sleep(1000)
                testCropTaskOnUiThread()
                Thread.sleep(1000)
                testRotateTaskOnUiThread()
                Thread.sleep(1000)
                testFlipTaskOnUiThread()
                Thread.sleep(1000)
                testTranslateTaskOnUiThread()
                Thread.sleep(1000)
                testBlendTaskOnUiThread()
                Thread.sleep(1000)
                testConvertColorTaskOnUiThread()
                Thread.sleep(1000)

                updateResultTextOnUiThread("\nAll tests completed!")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error running tests", e)
                updateResultTextOnUiThread("Error running tests: ${e.message}")
            }
        }.start()
    }

    private fun testCopyTaskOnUiThread() {
        runOnUiThread { testCopyTask() }
    }

    private fun testResizeTaskOnUiThread() {
        runOnUiThread { testResizeTask() }
    }

    private fun testCropTaskOnUiThread() {
        runOnUiThread { testCropTask() }
    }

    private fun testRotateTaskOnUiThread() {
        runOnUiThread { testRotateTask() }
    }

    private fun testFlipTaskOnUiThread() {
        runOnUiThread { testFlipTask() }
    }

    private fun testTranslateTaskOnUiThread() {
        runOnUiThread { testTranslateTask() }
    }

    private fun testBlendTaskOnUiThread() {
        runOnUiThread { testBlendTask() }
    }

    private fun testConvertColorTaskOnUiThread() {
        runOnUiThread { testConvertColorTask() }
    }

    private fun testJobOnUiThread() {
        runOnUiThread { testJob() }
    }

    private fun testResizeOnUiThread() {
        runOnUiThread { testResize() }
    }

    private fun testRescaleOnUiThread() {
        runOnUiThread { testRescale() }
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

    private fun testYuvToBitmapOnUiThread() {
        runOnUiThread { testYuvToBitmap() }
    }

    private fun testYuvToBitmap() {
        updateResultTextOnUiThread("Running YUV to Bitmap test...")
        Thread {
            try {
                val width = 400
                val height = 400
                
                // 1. Create NV21 (YCrCb_420_SP) data
                // Y size = width * height, UV size = width * height / 2
                val nv21Size = width * height * 3 / 2
                val nv21Data = ByteArray(nv21Size)
                
                // Fill Y with a horizontal gradient
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        nv21Data[y * width + x] = ((x * 255) / width).toByte()
                    }
                }
                
                // Fill UV with a vertical gradient (for simplicity)
                val uvOffset = width * height
                for (i in 0 until (width * height / 2)) {
                    nv21Data[uvOffset + i] = ((i * 2 / width * 255) / height).toByte()
                }

                // 2. Create Destination Bitmap
                val dstBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                // 3. Create RgaBuffers
                val srcBuffer = Rga.createRgaBufferFromNv21(nv21Data, width, height)
                val dstBuffer = Rga.createRgaBufferFromBitmap(dstBitmap)

                // 4. Call the color conversion function
                // Note: imcvtcolor also supports CSC (Color Space Conversion)
                val result = Rga.imcvtcolor(srcBuffer, dstBuffer, Rga.RK_FORMAT_YCrCb_420_SP, Rga.RK_FORMAT_RGBA_8888)

                if (result == Rga.IM_STATUS_SUCCESS) {
                    // Update the processed bitmap with the result
                    Rga.copyRgaBufferToBitmap(dstBuffer, dstBitmap)
                    updateProcessedBitmapWithErrorHandling(dstBitmap)
                    updateResultTextOnUiThread("YUV to Bitmap test: SUCCESS")
                } else {
                    updateResultTextOnUiThread("YUV to Bitmap test: FAILED (result: $result)")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in testYuvToBitmap", e)
                updateResultTextOnUiThread("YUV to Bitmap test: ERROR - ${e.message}")
            }
        }.start()
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

    private fun testRescale() {
        updateResultTextOnUiThread("Running Rescale test...")
        Thread {
            try {
                val srcBitmap = originalBitmap ?: return@Thread
                val fx = 0.5
                val fy = 0.5
                val newWidth = (srcBitmap.width * fx).toInt()
                val newHeight = (srcBitmap.height * fy).toInt()
                val dstBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

                // Create RgaBuffers
                val srcBuffer = Rga.createRgaBufferFromBitmap(srcBitmap)
                val dstBuffer = Rga.createRgaBufferFromBitmap(dstBitmap)

                // Call the rescale function
                val result = Rga.imrescale(srcBuffer, dstBuffer, fx, fy)

                if (result == Rga.IM_STATUS_SUCCESS) {
                    // Copy data from buffer back to bitmap
                    Rga.copyRgaBufferToBitmap(dstBuffer, dstBitmap)

                    // Update the processed bitmap with the result
                    updateProcessedBitmapWithErrorHandling(dstBitmap)
                    updateResultTextOnUiThread("Rescale test: SUCCESS (${srcBitmap.width}x${srcBitmap.height} -> ${newWidth}x${newHeight})")
                } else {
                    updateResultTextOnUiThread("Rescale test: FAILED (result: $result)")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in testRescale", e)
                updateResultTextOnUiThread("Rescale test: ERROR - ${e.message}")
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

    private fun testJob() {
        updateResultTextOnUiThread("Running Job/Task test...")
        Thread {
            try {
                val srcBitmap = originalBitmap ?: return@Thread
                val dstBitmap = Bitmap.createBitmap(srcBitmap.width, srcBitmap.height, Bitmap.Config.ARGB_8888)

                // Create RgaBuffers
                val srcBuffer = Rga.createRgaBufferFromBitmap(srcBitmap)
                val dstBuffer = Rga.createRgaBufferFromBitmap(dstBitmap)

                // 1. Begin Job
                val jobHandle = Rga.imbeginJob()
                if (jobHandle <= 0L) {
                    updateResultTextOnUiThread("Job test: FAILED to begin job")
                    return@Thread
                }

                // 2. Add tasks (Rotate then Flip)
                // Note: We use dstBuffer as intermediate for demonstration
                Rga.imrotateTask(jobHandle, srcBuffer, dstBuffer, Rga.IM_HAL_TRANSFORM_ROT_90)
                Rga.imflipTask(jobHandle, dstBuffer, dstBuffer, Rga.IM_HAL_TRANSFORM_FLIP_V)

                // 3. End Job
                val result = Rga.imendJob(jobHandle)

                if (result == Rga.IM_STATUS_SUCCESS) {
                    Rga.copyRgaBufferToBitmap(dstBuffer, dstBitmap)
                    updateProcessedBitmapWithErrorHandling(dstBitmap)
                    updateResultTextOnUiThread("Job test: SUCCESS (Rotate 90 + Flip V)")
                } else {
                    updateResultTextOnUiThread("Job test: FAILED (result: $result)")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in testJob", e)
                updateResultTextOnUiThread("Job test: ERROR - ${e.message}")
            }
        }.start()
    }

    private fun testCopyTask() {
        executeSingleTaskTest("CopyTask") { handle, src, dst ->
            Rga.imcopyTask(handle, src, dst)
        }
    }

    private fun testResizeTask() {
        executeSingleTaskTest("ResizeTask") { handle, src, dst ->
            Rga.imresizeTask(handle, src, dst, 0.5, 0.5)
        }
    }

    private fun testCropTask() {
        executeSingleTaskTest("CropTask") { handle, src, dst ->
            val cropRect = Rga.RgaRect(50, 50, 200, 200)
            Rga.imcropTask(handle, src, dst, cropRect)
        }
    }

    private fun testRotateTask() {
        executeSingleTaskTest("RotateTask") { handle, src, dst ->
            Rga.imrotateTask(handle, src, dst, Rga.IM_HAL_TRANSFORM_ROT_180)
        }
    }

    private fun testFlipTask() {
        executeSingleTaskTest("FlipTask") { handle, src, dst ->
            Rga.imflipTask(handle, src, dst, Rga.IM_HAL_TRANSFORM_FLIP_V)
        }
    }

    private fun testTranslateTask() {
        executeSingleTaskTest("TranslateTask") { handle, src, dst ->
            Rga.imtranslateTask(handle, src, dst, 50, 50)
        }
    }

    private fun testBlendTask() {
        updateResultTextOnUiThread("Running BlendTask test...")
        Thread {
            try {
                val srcBitmap = originalBitmap ?: return@Thread
                val overlay = Bitmap.createBitmap(srcBitmap.width, srcBitmap.height, Bitmap.Config.ARGB_8888)
                Canvas(overlay).drawColor(Color.argb(100, 0, 255, 0))

                val dstBitmap = Bitmap.createBitmap(srcBitmap.width, srcBitmap.height, Bitmap.Config.ARGB_8888)

                val srcBuf = Rga.createRgaBufferFromBitmap(srcBitmap)
                val overlayBuf = Rga.createRgaBufferFromBitmap(overlay)
                val dstBuf = Rga.createRgaBufferFromBitmap(dstBitmap)

                val handle = Rga.imbeginJob()
                Rga.imcompositeTask(handle, overlayBuf, srcBuf, dstBuf, Rga.IM_ALPHA_BLEND_SRC_OVER)
                val result = Rga.imendJob(handle)

                if (result == Rga.IM_STATUS_SUCCESS) {
                    Rga.copyRgaBufferToBitmap(dstBuf, dstBitmap)
                    updateProcessedBitmapWithErrorHandling(dstBitmap)
                    updateResultTextOnUiThread("BlendTask test: SUCCESS")
                } else {
                    updateResultTextOnUiThread("BlendTask test: FAILED ($result)")
                }
            } catch (e: Exception) {
                updateResultTextOnUiThread("BlendTask test: ERROR ${e.message}")
            }
        }.start()
    }

    private fun testConvertColorTask() {
        executeSingleTaskTest("ConvertColorTask") { handle, src, dst ->
            Rga.imcvtcolorTask(handle, src, dst, Rga.RK_FORMAT_RGBA_8888, Rga.RK_FORMAT_BGRA_8888)
        }
    }

    /**
     * Helper to execute a single task within a job.
     */
    private fun executeSingleTaskTest(name: String, taskAdder: (Long, Rga.RgaBuffer, Rga.RgaBuffer) -> Int) {
        updateResultTextOnUiThread("Running $name test...")
        Thread {
            try {
                val srcBitmap = originalBitmap ?: return@Thread
                // Most tasks here use same size dst for simplicity, except resize/crop if we wanted but let's keep it simple
                val dstWidth = if (name == "ResizeTask") (srcBitmap.width * 0.5).toInt() else if (name == "CropTask") 200 else srcBitmap.width
                val dstHeight = if (name == "ResizeTask") (srcBitmap.height * 0.5).toInt() else if (name == "CropTask") 200 else srcBitmap.height
                
                val dstBitmap = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888)

                val srcBuf = Rga.createRgaBufferFromBitmap(srcBitmap)
                val dstBuf = Rga.createRgaBufferFromBitmap(dstBitmap)

                val handle = Rga.imbeginJob()
                if (handle <= 0) {
                    updateResultTextOnUiThread("$name: Failed to begin job")
                    return@Thread
                }

                taskAdder(handle, srcBuf, dstBuf)

                val result = Rga.imendJob(handle)

                if (result == Rga.IM_STATUS_SUCCESS) {
                    Rga.copyRgaBufferToBitmap(dstBuf, dstBitmap)
                    updateProcessedBitmapWithErrorHandling(dstBitmap)
                    updateResultTextOnUiThread("$name: SUCCESS")
                } else {
                    updateResultTextOnUiThread("$name: FAILED ($result)")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in $name", e)
                updateResultTextOnUiThread("$name: ERROR ${e.message}")
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
