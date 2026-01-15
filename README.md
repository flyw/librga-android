# Rockchip RGA (Raster Graphic Acceleration) for Android

This project is an **Android build package** and Kotlin wrapper for Rockchip's **librga** (im2d API). It provides hardware-accelerated 2D graphics operations optimized for Rockchip SoCs, including pre-compiled native binaries and an easy-to-use JNI interface.

## Features

- **Pre-compiled Binaries**: Includes optimized `librga.so` and `librga.a` for `arm64-v8a` and `armeabi-v7a`.
- **Hardware Acceleration**: Full support for Rockchip's RGA hardware engine.
- **Kotlin Wrapper**: Clean, idiomatic Kotlin API for image processing.
- **Comprehensive Operations**: Support for resize, crop, rotate, flip, blend, and color space conversion (NV21/RGBA/etc.).

## Project Structure

- `librga/`: The core Android Library module.
- `librga/src/main/jniLibs/`: Pre-compiled Rockchip RGA native libraries.

## Native Build & Packaging

This library uses a hybrid approach for native dependencies to ensure both performance and ease of use:

- **Pre-compiled Binaries**: Rockchip's proprietary `librga.so` and `librga.a` are stored in `jniLibs`. These are provided by the SoC vendor and do not have public source code.
- **Source-built JNI**: The `librga_jni.so` (the actual wrapper) is compiled from `src/main/cpp/librga_jni.cpp` during the Gradle build process. This ensures the wrapper is always in sync with the Kotlin API.
- **Automatic Bundling**: When building the project, the Android Gradle Plugin automatically bundles the vendor libraries, the compiled JNI wrapper, and the required `libc++_shared.so` into the final AAR.

**Supported ABIs:** `arm64-v8a`, `armeabi-v7a`.

## Documentation & Source

For the latest RGA documentation, hardware specifications, and the underlying native implementation, please refer to the official Rockchip repository:

👉 **[Rockchip librga (GitHub)](https://github.com/airockchip/librga)**

This Android wrapper is built based on the `im2d` API provided by the official repository above.

## Supported Operations

- Copy
- Resize
- Crop
- Rotate
- Flip
- Translate
- Blend
- Composite
- Color Format Conversion
- Fill

## API Reference

### Constants

```kotlin
// Status codes
const val IM_STATUS_SUCCESS = 1

// Rotation transforms
const val IM_HAL_TRANSFORM_ROT_90     = 1 shl 0
const val IM_HAL_TRANSFORM_ROT_180    = 1 shl 1
const val IM_HAL_TRANSFORM_ROT_270    = 1 shl 2
const val IM_HAL_TRANSFORM_FLIP_H     = 1 shl 3  // Horizontal flip
const val IM_HAL_TRANSFORM_FLIP_V     = 1 shl 4  // Vertical flip
const val IM_HAL_TRANSFORM_FLIP_H_V   = 1 shl 5  // Horizontal and vertical flip

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

// Common pixel formats
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
```

### Data Classes

#### RgaBuffer
Represents an image buffer for RGA operations.

```kotlin
data class RgaBuffer(
    val width: Int,
    val height: Int,
    val format: Int,
    val wstride: Int = width,
    val hstride: Int = height,
    val fd: Int = -1,           // File descriptor (for buffer sharing)
    val handle: Int = 0,        // Buffer handle
    val ptr: ByteBuffer? = null, // Direct ByteBuffer
    val hardwareBuffer: Any? = null // Android HardwareBuffer (API 26+)
)
```

#### RgaRect
Defines a rectangular region for cropping and filling operations.

```kotlin
data class RgaRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)
```

### Core RGA Operations

#### Copy
Copies source image to destination.

```kotlin
external fun imcopy(src: RgaBuffer, dst: RgaBuffer): Int
```

**Example:**
```kotlin
val srcBuffer = Rga.createRgaBufferFromBitmap(sourceBitmap)
val dstBuffer = Rga.createRgaBufferFromBitmap(destinationBitmap)
val result = Rga.imcopy(srcBuffer, dstBuffer)
if (result == Rga.IM_STATUS_SUCCESS) {
    // Copy successful
}
```

#### Resize
Resizes source image to destination dimensions.

```kotlin
external fun imresize(src: RgaBuffer, dst: RgaBuffer, fx: Double = 0.0, fy: Double = 0.0): Int
```

**Example:**
```kotlin
val srcBuffer = Rga.createRgaBufferFromBitmap(sourceBitmap)
val dstBuffer = Rga.createRgaBufferFromBitmap(destinationBitmap)
val result = Rga.imresize(srcBuffer, dstBuffer, 0.5, 0.5) // Scale to 50%
```

#### Crop
Crops source image to destination using a rectangular region.

```kotlin
external fun imcrop(src: RgaBuffer, dst: RgaBuffer, rect: RgaRect): Int
```

**Example:**
```kotlin
val cropRect = Rga.RgaRect(10, 10, 100, 100) // x, y, width, height
val srcBuffer = Rga.createRgaBufferFromBitmap(sourceBitmap)
val dstBuffer = Rga.createRgaBufferFromBitmap(destinationBitmap)
val result = Rga.imcrop(srcBuffer, dstBuffer, cropRect)
```

#### Rotate
Rotates source image to destination.

```kotlin
external fun imrotate(src: RgaBuffer, dst: RgaBuffer, rotation: Int): Int
```

**Example:**
```kotlin
val srcBuffer = Rga.createRgaBufferFromBitmap(sourceBitmap)
val dstBuffer = Rga.createRgaBufferFromBitmap(destinationBitmap)
val result = Rga.imrotate(srcBuffer, dstBuffer, Rga.IM_HAL_TRANSFORM_ROT_90) // 90° rotation
```

#### Flip
Flips source image to destination.

```kotlin
external fun imflip(src: RgaBuffer, dst: RgaBuffer, mode: Int): Int
```

**Example:**
```kotlin
val srcBuffer = Rga.createRgaBufferFromBitmap(sourceBitmap)
val dstBuffer = Rga.createRgaBufferFromBitmap(destinationBitmap)
val result = Rga.imflip(srcBuffer, dstBuffer, Rga.IM_HAL_TRANSFORM_FLIP_H) // Horizontal flip
```

#### Translate
Translates (moves) source image to destination.

```kotlin
external fun imtranslate(src: RgaBuffer, dst: RgaBuffer, x: Int, y: Int): Int
```

**Example:**
```kotlin
val srcBuffer = Rga.createRgaBufferFromBitmap(sourceBitmap)
val dstBuffer = Rga.createRgaBufferFromBitmap(destinationBitmap)
val result = Rga.imtranslate(srcBuffer, dstBuffer, 50, 50) // Move 50px right and down
```

#### Blend
Blends source image over destination.

```kotlin
external fun imblend(src: RgaBuffer, dst: RgaBuffer, mode: Int = IM_ALPHA_BLEND_SRC_OVER): Int
```

**Example:**
```kotlin
val srcBuffer = Rga.createRgaBufferFromBitmap(foregroundBitmap)
val dstBuffer = Rga.createRgaBufferFromBitmap(backgroundBitmap)
val result = Rga.imblend(srcBuffer, dstBuffer, Rga.IM_ALPHA_BLEND_SRC_OVER)
```

#### Composite
Composites two source images to destination.

```kotlin
external fun imcomposite(srcA: RgaBuffer, srcB: RgaBuffer, dst: RgaBuffer, mode: Int = IM_ALPHA_BLEND_SRC_OVER): Int
```

**Example:**
```kotlin
val srcABuffer = Rga.createRgaBufferFromBitmap(foregroundBitmap)
val srcBBuffer = Rga.createRgaBufferFromBitmap(backgroundBitmap)
val dstBuffer = Rga.createRgaBufferFromBitmap(resultBitmap)
val result = Rga.imcomposite(srcABuffer, srcBBuffer, dstBuffer, Rga.IM_ALPHA_BLEND_SRC_OVER)
```

#### Color Format Conversion
Converts color format from source to destination.

```kotlin
external fun imcvtcolor(src: RgaBuffer, dst: RgaBuffer, sfmt: Int, dfmt: Int): Int
```

**Example:**
```kotlin
val srcBuffer = Rga.createRgaBufferFromBitmap(sourceBitmap, Rga.RK_FORMAT_RGBA_8888)
val dstBuffer = Rga.createRgaBufferFromBitmap(destinationBitmap, Rga.RK_FORMAT_BGRA_8888)
val result = Rga.imcvtcolor(srcBuffer, dstBuffer, Rga.RK_FORMAT_RGBA_8888, Rga.RK_FORMAT_BGRA_8888)
```

#### Fill
Fills destination buffer with a color in a specified rectangle.

```kotlin
external fun imfill(dst: RgaBuffer, rect: RgaRect, color: Int): Int
```

**Example:**
```kotlin
val fillRect = Rga.RgaRect(50, 50, 100, 100)
val dstBuffer = Rga.createRgaBufferFromBitmap(destinationBitmap)
val result = Rga.imfill(dstBuffer, fillRect, Color.RED)
```

### Helper Methods

#### Creating RGA Buffers from Android Bitmap

```kotlin
fun createRgaBufferFromBitmap(bitmap: android.graphics.Bitmap, format: Int = Rga.RK_FORMAT_RGBA_8888): RgaBuffer
```

**Example:**
```kotlin
val buffer = Rga.createRgaBufferFromBitmap(bitmap)
```

#### Copying RGA Buffer to Android Bitmap

```kotlin
fun copyRgaBufferToBitmap(srcBuffer: RgaBuffer, dstBitmap: android.graphics.Bitmap)
```

**Example:**
```kotlin
Rga.copyRgaBufferToBitmap(buffer, bitmap)
```

#### Converting Bitmap to ByteBuffer

```kotlin
fun bitmapToByteBuffer(bitmap: android.graphics.Bitmap): java.nio.ByteBuffer
```

**Example:**
```kotlin
val byteBuffer = Rga.bitmapToByteBuffer(bitmap)
```

#### Creating RGA Buffers from File Descriptor

```kotlin
fun createBufferFromFd(fd: Int, width: Int, height: Int, format: Int, wstride: Int = width, hstride: Int = height): RgaBuffer
```

**Example:**
```kotlin
val buffer = Rga.createBufferFromFd(fd, width, height, Rga.RK_FORMAT_RGBA_8888)
```

#### Creating RGA Buffers from ByteBuffer

```kotlin
fun createBufferFromByteBuffer(buffer: java.nio.ByteBuffer, width: Int, height: Int, format: Int, wstride: Int = width, hstride: Int = height): RgaBuffer
```

**Example:**
```kotlin
val buffer = Rga.createBufferFromByteBuffer(byteBuffer, width, height, Rga.RK_FORMAT_RGBA_8888)
```

### NV21 Data Processing

#### Creating RGA Buffers from NV21 Data

```kotlin
fun createRgaBufferFromNv21(nv21Data: ByteArray, width: Int, height: Int, format: Int = Rga.RK_FORMAT_YCrCb_420_SP): RgaBuffer
```

**Example:**
```kotlin
val buffer = Rga.createRgaBufferFromNv21(nv21ByteArray, width, height)
```

#### Converting NV21 Data to Bitmap

```kotlin
fun nv21ToBitmap(nv21Data: ByteArray, width: Int, height: Int): android.graphics.Bitmap
```

**Example:**
```kotlin
val bitmap = Rga.nv21ToBitmap(nv21ByteArray, width, height)
```

#### Converting Bitmap to NV21 Data

```kotlin
fun bitmapToNv21(bitmap: android.graphics.Bitmap): ByteArray
```

**Example:**
```kotlin
val nv21Data = Rga.bitmapToNv21(bitmap)
```

## Re-packaging Instructions

To re-package this library as a standalone project, follow these steps:

### Files to Copy

1. **Root Directory:**
    - `build.gradle`
    - `gradle.properties`
    - `gradlew`
    - `gradlew.bat`
    - `settings.gradle`
    - `.gitignore`

2. **Gradle Wrapper Directory:**
    - `gradle/` (entire directory)

3. **Library Source Directory:**
    - `librga/` (entire directory)

### Step-by-Step Repackaging Process

1. **Create a new directory for your re-packaged project:**
   ```bash
   mkdir your-new-project-name
   cd your-new-project-name
   ```

2. **Copy the required files from the original project:**
   ```bash
   # Copy root files
   cp /path/to/original/project/build.gradle .
   cp /path/to/original/project/gradle.properties .
   cp /path/to/original/project/gradlew .
   cp /path/to/original/project/gradlew.bat .
   cp /path/to/original/project/settings.gradle .
   cp /path/to/original/project/.gitignore .

   # Copy gradle wrapper directory
   cp -r /path/to/original/project/gradle/ .

   # Copy library directory
   cp -r /path/to/original/project/librga/ .
   ```

3. **Update settings.gradle (if needed):**
   Make sure the module is properly included:
   ```gradle
   include ':librga'
   rootProject.name = "YourNewProjectName"
   ```

4. **Update build.gradle (module level - librga/build.gradle):**
   Adjust the library configuration as needed for your use case:
   ```gradle
   android {
       // Update namespace, version, etc. as needed
       namespace 'com.yourcompany.yourlibrary'
       compileSdk 34

       defaultConfig {
           minSdk 21
           targetSdk 34
           versionCode 1
           versionName "1.0"
       }
       
       // Other configurations...
   }
   ```

5. **Update package names in source files (optional):**
   If you want to change the package name, update:
    - `librga/src/main/kotlin/com/rockchip/librga/Rga.kt`
    - `librga/src/main/kotlin/com/rockchip/librga/TestActivity.kt`
    - `librga/src/main/AndroidManifest.xml`
    - `librga/src/main/res/values/strings.xml`

6. **Build the project:**
   ```bash
   ./gradlew build
   ```

7. **Test the library:**
   Create a sample app to verify that the re-packaged library works correctly.

## Usage in Your Project

1. **Add the library to your project:**
   Place the `librga` directory in your project root and include it in your `settings.gradle`:
   ```gradle
   include ':librga'
   ```

2. **Add dependency in your app's build.gradle:**
   ```gradle
   dependencies {
       implementation project(':librga')
   }
   ```

3. **Initialize and use the RGA library:**
   ```kotlin
   // The RGA library is automatically initialized when the object is accessed
   val result = Rga.imcopy(srcBuffer, dstBuffer)
   ```

## License

This project is licensed under the Apache 2.0 License - see the LICENSE file for details.
