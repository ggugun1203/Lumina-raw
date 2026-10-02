package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Matrix
import android.graphics.Paint
import com.example.model.CropAspectRatio
import com.example.model.CropTransform
import com.example.model.HslColorChannel
import com.example.model.PhotoEditParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class HistogramData(
    val redBins: IntArray = IntArray(256),
    val greenBins: IntArray = IntArray(256),
    val blueBins: IntArray = IntArray(256),
    val lumBins: IntArray = IntArray(256),
    val maxBinCount: Int = 1
)

object ImageProcessor {

    /**
     * Compute 256-bin RGB and Luminance histograms.
     */
    suspend fun computeHistogram(bitmap: Bitmap): HistogramData = withContext(Dispatchers.Default) {
        val width = bitmap.width
        val height = bitmap.height
        val totalPixels = width * height
        if (totalPixels <= 0) return@withContext HistogramData()

        val rBins = IntArray(256)
        val gBins = IntArray(256)
        val bBins = IntArray(256)
        val lBins = IntArray(256)

        // Sample up to 100,000 pixels for fast real-time calculation
        val step = max(1, totalPixels / 100000)
        val pixels = IntArray(totalPixels)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var maxVal = 1
        var i = 0
        while (i < totalPixels) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val lum = (0.299f * r + 0.587f * g + 0.114f * b).toInt().coerceIn(0, 255)

            rBins[r]++
            gBins[g]++
            bBins[b]++
            lBins[lum]++

            if (rBins[r] > maxVal) maxVal = rBins[r]
            if (gBins[g] > maxVal) maxVal = gBins[g]
            if (bBins[b] > maxVal) maxVal = bBins[b]
            if (lBins[lum] > maxVal) maxVal = lBins[lum]

            i += step
        }

        HistogramData(rBins, gBins, bBins, lBins, maxVal)
    }

    /**
     * Applies full Lightroom adjustments pipeline onto source bitmap.
     */
    suspend fun applyAdjustments(
        source: Bitmap,
        params: PhotoEditParams,
        isExport: Boolean = false
    ): Bitmap = withContext(Dispatchers.Default) {
        // First apply geometry (Crop, Rotate, Flip)
        val transformed = applyTransform(source, params.transform)

        if (!params.isModified()) {
            return@withContext transformed
        }

        val width = transformed.width
        val height = transformed.height
        val totalPixels = width * height
        val pixels = IntArray(totalPixels)
        transformed.getPixels(pixels, 0, width, 0, 0, width, height)

        // Precompute parameters
        val expFactor = 2.0f.pow(params.exposure)
        val contrastFactor = (100f + params.contrast) / 100f
        val tempShift = params.temperature / 100f // -1.0 to 1.0
        val tintShift = params.tint / 100f       // -1.0 to 1.0

        val highlightsShift = params.highlights / 100f
        val shadowsShift = params.shadows / 100f
        val whitesShift = params.whites / 100f
        val blacksShift = params.blacks / 100f

        val vibranceFactor = params.vibrance / 100f
        val saturationFactor = (100f + params.saturation) / 100f

        val clarityFactor = params.clarity / 100f
        val dehazeFactor = params.dehaze / 100f
        val vignetteFactor = params.vignette / 100f
        val grainAmount = params.grain / 100f

        // Split toning precomputations
        val hasSplitTone = params.colorGrading.shadowSat > 0f || params.colorGrading.highlightSat > 0f
        val shadowRgb = if (hasSplitTone) hueSatToRgb(params.colorGrading.shadowHue, params.colorGrading.shadowSat / 100f) else null
        val highlightRgb = if (hasSplitTone) hueSatToRgb(params.colorGrading.highlightHue, params.colorGrading.highlightSat / 100f) else null

        // HSL adjustments map
        val hasHsl = params.hslAdjustments.values.any { it.hue != 0f || it.saturation != 0f || it.luminance != 0f }

        // Center for vignette
        val centerX = width / 2.0f
        val centerY = height / 2.0f
        val maxDistSq = (centerX * centerX + centerY * centerY)

        val hsv = FloatArray(3)

        for (idx in 0 until totalPixels) {
            val color = pixels[idx]
            var r = ((color shr 16) and 0xFF) / 255.0f
            var g = ((color shr 8) and 0xFF) / 255.0f
            var b = (color and 0xFF) / 255.0f

            // 1. Exposure
            r *= expFactor
            g *= expFactor
            b *= expFactor

            // 2. White Balance (Temperature & Tint)
            if (tempShift != 0f) {
                if (tempShift > 0f) {
                    r *= (1f + tempShift * 0.4f)
                    b *= (1f - tempShift * 0.3f)
                } else {
                    val cool = -tempShift
                    r *= (1f - cool * 0.3f)
                    b *= (1f + cool * 0.4f)
                }
            }
            if (tintShift != 0f) {
                if (tintShift > 0f) {
                    // Magenta
                    r *= (1f + tintShift * 0.15f)
                    g *= (1f - tintShift * 0.2f)
                    b *= (1f + tintShift * 0.15f)
                } else {
                    // Green
                    val grn = -tintShift
                    g *= (1f + grn * 0.25f)
                }
            }

            // 3. Contrast & Tone Curve (Highlights / Shadows / Whites / Blacks)
            var lum = 0.299f * r + 0.587f * g + 0.114f * b

            // Highlights & Shadows
            if (highlightsShift != 0f && lum > 0.5f) {
                val weight = (lum - 0.5f) * 2f
                val factor = 1f + highlightsShift * weight * 0.5f
                r *= factor
                g *= factor
                b *= factor
            }
            if (shadowsShift != 0f && lum < 0.5f) {
                val weight = (0.5f - lum) * 2f
                val boost = shadowsShift * weight * 0.4f
                r += boost
                g += boost
                b += boost
            }

            // Whites & Blacks
            if (whitesShift != 0f && lum > 0.7f) {
                val weight = (lum - 0.7f) / 0.3f
                val factor = 1f + whitesShift * weight * 0.3f
                r *= factor
                g *= factor
                b *= factor
            }
            if (blacksShift != 0f && lum < 0.3f) {
                val weight = (0.3f - lum) / 0.3f
                val offset = blacksShift * weight * 0.2f
                r += offset
                g += offset
                b += offset
            }

            // Contrast
            if (contrastFactor != 1.0f) {
                r = ((r - 0.5f) * contrastFactor) + 0.5f
                g = ((g - 0.5f) * contrastFactor) + 0.5f
                b = ((b - 0.5f) * contrastFactor) + 0.5f
            }

            // Dehaze & Clarity approximation
            if (dehazeFactor != 0f) {
                val dehazeAmount = dehazeFactor * 0.3f
                r = (r - dehazeAmount * 0.2f) * (1f + dehazeAmount)
                g = (g - dehazeAmount * 0.2f) * (1f + dehazeAmount)
                b = (b - dehazeAmount * 0.2f) * (1f + dehazeAmount)
            }
            if (clarityFactor != 0f) {
                // Midtone local contrast pop
                val midWeight = 1.0f - (2.0f * lum - 1.0f).let { it * it }
                val pop = clarityFactor * midWeight * 0.2f
                r += (r - lum) * pop
                g += (g - lum) * pop
                b += (b - lum) * pop
            }

            // Clamp intermediate
            r = r.coerceIn(0f, 1f)
            g = g.coerceIn(0f, 1f)
            b = b.coerceIn(0f, 1f)

            // 4. HSL Adjustments & Vibrance/Saturation
            AndroidColor.RGBToHSV((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt(), hsv)
            var hue = hsv[0]
            var sat = hsv[1]
            var valLum = hsv[2]

            // Apply 8-channel HSL
            if (hasHsl) {
                for (channel in HslColorChannel.entries) {
                    val adj = params.hslAdjustments[channel] ?: continue
                    if (adj.hue == 0f && adj.saturation == 0f && adj.luminance == 0f) continue

                    val diff = colorHueDistance(hue, channel.centerHue)
                    if (diff < 35f) {
                        val influence = 1.0f - (diff / 35f)
                        hue = (hue + adj.hue * influence * 0.4f + 360f) % 360f
                        sat = (sat + (adj.saturation / 100f) * influence * 0.5f).coerceIn(0f, 1f)
                        valLum = (valLum + (adj.luminance / 100f) * influence * 0.4f).coerceIn(0f, 1f)
                    }
                }
            }

            // Vibrance (smart boost on less saturated pixels)
            if (vibranceFactor != 0f) {
                val boost = (1.0f - sat) * vibranceFactor * 0.6f
                sat = (sat + boost).coerceIn(0f, 1f)
            }

            // Saturation
            if (saturationFactor != 1.0f) {
                sat = (sat * saturationFactor).coerceIn(0f, 1f)
            }

            hsv[0] = hue
            hsv[1] = sat
            hsv[2] = valLum

            val hsvRgb = AndroidColor.HSVToColor(hsv)
            r = ((hsvRgb shr 16) and 0xFF) / 255.0f
            g = ((hsvRgb shr 8) and 0xFF) / 255.0f
            b = (hsvRgb and 0xFF) / 255.0f

            // 5. Split Toning / Color Grading
            if (hasSplitTone) {
                lum = 0.299f * r + 0.587f * g + 0.114f * b
                if (shadowRgb != null && lum < 0.6f) {
                    val shadowWeight = (1.0f - lum / 0.6f) * (params.colorGrading.shadowSat / 100f) * 0.4f
                    r = (r * (1f - shadowWeight) + shadowRgb[0] * shadowWeight).coerceIn(0f, 1f)
                    g = (g * (1f - shadowWeight) + shadowRgb[1] * shadowWeight).coerceIn(0f, 1f)
                    b = (b * (1f - shadowWeight) + shadowRgb[2] * shadowWeight).coerceIn(0f, 1f)
                }
                if (highlightRgb != null && lum > 0.4f) {
                    val highlightWeight = ((lum - 0.4f) / 0.6f) * (params.colorGrading.highlightSat / 100f) * 0.35f
                    r = (r * (1f - highlightWeight) + highlightRgb[0] * highlightWeight).coerceIn(0f, 1f)
                    g = (g * (1f - highlightWeight) + highlightRgb[1] * highlightWeight).coerceIn(0f, 1f)
                    b = (b * (1f - highlightWeight) + highlightRgb[2] * highlightWeight).coerceIn(0f, 1f)
                }
            }

            // 6. Vignette
            if (vignetteFactor != 0f) {
                val px = idx % width
                val py = idx / width
                val dx = px - centerX
                val dy = py - centerY
                val distSq = dx * dx + dy * dy
                val normDist = (distSq / maxDistSq).coerceIn(0f, 1f)
                if (normDist > 0.25f) {
                    val vigInfluence = ((normDist - 0.25f) / 0.75f).pow(1.5f)
                    val vigMul = 1.0f + (vignetteFactor * vigInfluence * 0.7f)
                    r = (r * vigMul).coerceIn(0f, 1f)
                    g = (g * vigMul).coerceIn(0f, 1f)
                    b = (b * vigMul).coerceIn(0f, 1f)
                }
            }

            // 7. Grain
            if (grainAmount > 0f) {
                // Pseudo-random film grain
                val noise = ((Math.sin((idx * 12.9898 + idx * 78.233)) * 43758.5453).let { it - it.toInt() } - 0.5f) * grainAmount * 0.18f
                r = (r + noise.toFloat()).coerceIn(0f, 1f)
                g = (g + noise.toFloat()).coerceIn(0f, 1f)
                b = (b + noise.toFloat()).coerceIn(0f, 1f)
            }

            val ir = (r * 255).toInt().coerceIn(0, 255)
            val ig = (g * 255).toInt().coerceIn(0, 255)
            val ib = (b * 255).toInt().coerceIn(0, 255)
            pixels[idx] = (0xFF shl 24) or (ir shl 16) or (ig shl 8) or ib
        }

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        resultBitmap
    }

    /**
     * Applies geometric transform: rotation, flips, aspect ratio cropping.
     */
    fun applyTransform(source: Bitmap, transform: CropTransform): Bitmap {
        var bmp = source
        val matrix = Matrix()
        var needMatrix = false

        if (transform.rotationDegrees != 0) {
            matrix.postRotate(transform.rotationDegrees.toFloat())
            needMatrix = true
        }
        if (transform.flipHorizontal || transform.flipVertical) {
            val sx = if (transform.flipHorizontal) -1f else 1f
            val sy = if (transform.flipVertical) -1f else 1f
            matrix.postScale(sx, sy)
            needMatrix = true
        }

        if (needMatrix) {
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        }

        // Aspect ratio crop
        val targetRatio = transform.aspectRatio.ratio
        if (targetRatio != null && targetRatio > 0f) {
            val currentRatio = bmp.width.toFloat() / bmp.height.toFloat()
            if (kotlin.math.abs(currentRatio - targetRatio) > 0.02f) {
                val cropWidth: Int
                val cropHeight: Int
                if (currentRatio > targetRatio) {
                    // Current is wider than target -> crop width
                    cropHeight = bmp.height
                    cropWidth = (cropHeight * targetRatio).toInt().coerceIn(1, bmp.width)
                } else {
                    // Current is taller than target -> crop height
                    cropWidth = bmp.width
                    cropHeight = (cropWidth / targetRatio).toInt().coerceIn(1, bmp.height)
                }
                val startX = (bmp.width - cropWidth) / 2
                val startY = (bmp.height - cropHeight) / 2
                bmp = Bitmap.createBitmap(bmp, startX, startY, cropWidth, cropHeight)
            }
        }

        return bmp
    }

    private fun colorHueDistance(h1: Float, h2: Float): Float {
        val diff = kotlin.math.abs(h1 - h2) % 360f
        return if (diff > 180f) 360f - diff else diff
    }

    private fun hueSatToRgb(hue: Float, sat: Float): FloatArray {
        val hsv = floatArrayOf(hue, sat, 1.0f)
        val color = AndroidColor.HSVToColor(hsv)
        return floatArrayOf(
            ((color shr 16) and 0xFF) / 255.0f,
            ((color shr 8) and 0xFF) / 255.0f,
            (color and 0xFF) / 255.0f
        )
    }
}
