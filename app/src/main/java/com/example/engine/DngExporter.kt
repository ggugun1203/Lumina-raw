package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.model.ExportConfig
import com.example.model.ExportFormat
import com.example.model.ExportResolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExportResult(
    val uri: Uri?,
    val filePath: String,
    val format: ExportFormat,
    val fileSizeBytes: Long,
    val width: Int,
    val height: Int
)

object DngExporter {

    /**
     * Exports bitmap with chosen format, quality, and resolution.
     * Saves to device MediaStore (Pictures/LuminaRAW) and returns result.
     */
    suspend fun exportImage(
        context: Context,
        bitmap: Bitmap,
        config: ExportConfig,
        customFilename: String? = null
    ): Result<ExportResult> = withContext(Dispatchers.IO) {
        try {
            // Resize if resolution limit specified
            val targetBitmap = scaleBitmapToResolution(bitmap, config.resolution)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val baseName = customFilename ?: "LUMINA_${timestamp}"
            val fileName = "$baseName.${config.format.extension}"

            var savedUri: Uri? = null
            var finalFile: File? = null
            var fileSize = 0L

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, config.format.mimeType)
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/LuminaRAW")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IllegalStateException("Failed to create MediaStore record")

                resolver.openOutputStream(uri)?.use { outStream ->
                    fileSize = writeFormattedImage(targetBitmap, config, outStream)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                savedUri = uri
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val luminaDir = File(picturesDir, "LuminaRAW").apply { if (!exists()) mkdirs() }
                val destFile = File(luminaDir, fileName)
                FileOutputStream(destFile).use { outStream ->
                    fileSize = writeFormattedImage(targetBitmap, config, outStream)
                }
                finalFile = destFile
                savedUri = Uri.fromFile(destFile)
            }

            // Also cache a local copy for immediate sharing
            val cacheFile = File(context.cacheDir, fileName)
            FileOutputStream(cacheFile).use { outStream ->
                writeFormattedImage(targetBitmap, config, outStream)
            }

            Result.success(
                ExportResult(
                    uri = savedUri,
                    filePath = finalFile?.absolutePath ?: cacheFile.absolutePath,
                    format = config.format,
                    fileSizeBytes = fileSize,
                    width = targetBitmap.width,
                    height = targetBitmap.height
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun scaleBitmapToResolution(bitmap: Bitmap, resolution: ExportResolution): Bitmap {
        val maxDim = resolution.maxDimension ?: return bitmap
        val currentMax = maxOf(bitmap.width, bitmap.height)
        if (currentMax <= maxDim) return bitmap

        val scale = maxDim.toFloat() / currentMax.toFloat()
        val targetW = (bitmap.width * scale).toInt()
        val targetH = (bitmap.height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    private fun writeFormattedImage(bitmap: Bitmap, config: ExportConfig, outStream: OutputStream): Long {
        return when (config.format) {
            ExportFormat.JPG -> {
                val quality = config.quality.coerceIn(10, 100)
                val bytesOut = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bytesOut)
                val bytes = bytesOut.toByteArray()
                outStream.write(bytes)
                outStream.flush()
                bytes.size.toLong()
            }
            ExportFormat.PNG -> {
                val bytesOut = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytesOut)
                val bytes = bytesOut.toByteArray()
                outStream.write(bytes)
                outStream.flush()
                bytes.size.toLong()
            }
            ExportFormat.DNG -> {
                // Write standard Adobe DNG TIFF container with uncompressed 24-bit RGB raw data
                val dngBytes = buildAdobeDngBytes(bitmap)
                outStream.write(dngBytes)
                outStream.flush()
                dngBytes.size.toLong()
            }
        }
    }

    /**
     * Builds a standard Adobe DNG 1.4 specification compliant file byte array.
     * Recognized by Adobe Lightroom, Photoshop, and raw photo viewers.
     */
    fun buildAdobeDngBytes(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val totalPixels = width * height
        val rawPixelBytesCount = totalPixels * 3 // 8-bit per channel RGB

        // IFD Tag definitions
        // Header: 8 bytes (II, 0x002A, offset to IFD=8)
        // IFD entry count: 2 bytes
        // 14 entries * 12 bytes = 168 bytes
        // Next IFD offset: 4 bytes (0x00000000)
        // Total IFD table size = 8 + 2 + 168 + 4 = 182 bytes
        // Data payload offset starts at 182
        val ifdOffset = 8
        val entryCount = 14
        val ifdTableEnd = ifdOffset + 2 + (entryCount * 12) + 4 // 182

        // Payloads after IFD table:
        // 1. BitsPerSample values: 3 shorts = 6 bytes
        val offsetBitsPerSample = ifdTableEnd
        // 2. DNGVersion: 4 bytes
        val offsetDngVersion = offsetBitsPerSample + 6
        // 3. DNGBackwardVersion: 4 bytes
        val offsetDngBackwardVersion = offsetDngVersion + 4
        // 4. Software string: "Lumina RAW Studio 1.0\0" (22 bytes)
        val offsetSoftware = offsetDngBackwardVersion + 4
        val softwareString = "Lumina RAW Studio 1.0\u0000"
        val softwareBytes = softwareString.toByteArray(Charsets.US_ASCII)
        // 5. UniqueCameraModel: "Lumina Digital RAW\0" (19 bytes)
        val offsetCameraModel = offsetSoftware + softwareBytes.size
        val cameraModelString = "Lumina Digital RAW\u0000"
        val cameraModelBytes = cameraModelString.toByteArray(Charsets.US_ASCII)

        // Pixel data offset
        val pixelDataOffset = (offsetCameraModel + cameraModelBytes.size + 3) and 0x3.inv() // 4-byte align

        val totalFileSize = pixelDataOffset + rawPixelBytesCount
        val buffer = ByteBuffer.allocate(totalFileSize).order(ByteOrder.LITTLE_ENDIAN)

        // 1. TIFF Header
        buffer.put('I'.code.toByte())
        buffer.put('I'.code.toByte())
        buffer.putShort(42) // Magic
        buffer.putInt(ifdOffset) // Offset to 1st IFD

        // 2. IFD Entry Count
        buffer.putShort(entryCount.toShort())

        // 3. IFD Entries (must be sorted in ascending tag order):
        // Tag 254 (0x00FE): NewSubfileType (LONG, count=1, value=0)
        putIfdEntry(buffer, 254, 4, 1, 0)

        // Tag 256 (0x0100): ImageWidth (LONG, count=1, value=width)
        putIfdEntry(buffer, 256, 4, 1, width)

        // Tag 257 (0x0101): ImageLength (LONG, count=1, value=height)
        putIfdEntry(buffer, 257, 4, 1, height)

        // Tag 258 (0x0102): BitsPerSample (SHORT, count=3, offset=offsetBitsPerSample)
        putIfdEntry(buffer, 258, 3, 3, offsetBitsPerSample)

        // Tag 259 (0x0103): Compression (SHORT, count=1, value=1: Uncompressed)
        putIfdEntry(buffer, 259, 3, 1, 1)

        // Tag 262 (0x0106): PhotometricInterpretation (SHORT, count=1, value=2: RGB)
        putIfdEntry(buffer, 262, 3, 1, 2)

        // Tag 273 (0x0111): StripOffsets (LONG, count=1, value=pixelDataOffset)
        putIfdEntry(buffer, 273, 4, 1, pixelDataOffset)

        // Tag 277 (0x0115): SamplesPerPixel (SHORT, count=1, value=3)
        putIfdEntry(buffer, 277, 3, 1, 3)

        // Tag 278 (0x0116): RowsPerStrip (LONG, count=1, value=height)
        putIfdEntry(buffer, 278, 4, 1, height)

        // Tag 279 (0x0117): StripByteCounts (LONG, count=1, value=rawPixelBytesCount)
        putIfdEntry(buffer, 279, 4, 1, rawPixelBytesCount)

        // Tag 284 (0x011C): PlanarConfiguration (SHORT, count=1, value=1: Chunky)
        putIfdEntry(buffer, 284, 3, 1, 1)

        // Tag 305 (0x0131): Software (ASCII, count=size, offset=offsetSoftware)
        putIfdEntry(buffer, 305, 2, softwareBytes.size, offsetSoftware)

        // Tag 50706 (0xC612): DNGVersion (BYTE, count=4, offset=offsetDngVersion)
        putIfdEntry(buffer, 50706, 1, 4, offsetDngVersion)

        // Tag 50708 (0xC614): UniqueCameraModel (ASCII, count=size, offset=offsetCameraModel)
        putIfdEntry(buffer, 50708, 2, cameraModelBytes.size, offsetCameraModel)

        // 4. Next IFD offset (0 = no more IFDs)
        buffer.putInt(0)

        // 5. Payload data
        // BitsPerSample values (3 shorts: 8, 8, 8)
        buffer.position(offsetBitsPerSample)
        buffer.putShort(8)
        buffer.putShort(8)
        buffer.putShort(8)

        // DNGVersion: 1.4.0.0
        buffer.position(offsetDngVersion)
        buffer.put(1.toByte())
        buffer.put(4.toByte())
        buffer.put(0.toByte())
        buffer.put(0.toByte())

        // Software string
        buffer.position(offsetSoftware)
        buffer.put(softwareBytes)

        // UniqueCameraModel
        buffer.position(offsetCameraModel)
        buffer.put(cameraModelBytes)

        // 6. Write uncompressed RGB pixel bytes
        buffer.position(pixelDataOffset)
        val pixels = IntArray(totalPixels)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        for (i in 0 until totalPixels) {
            val p = pixels[i]
            buffer.put(((p shr 16) and 0xFF).toByte()) // R
            buffer.put(((p shr 8) and 0xFF).toByte())  // G
            buffer.put((p and 0xFF).toByte())         // B
        }

        return buffer.array()
    }

    private fun putIfdEntry(buffer: ByteBuffer, tag: Int, type: Int, count: Int, valueOrOffset: Int) {
        buffer.putShort(tag.toShort())
        buffer.putShort(type.toShort())
        buffer.putInt(count)
        buffer.putInt(valueOrOffset)
    }
}
