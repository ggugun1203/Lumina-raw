package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.model.ColorGrading
import com.example.model.HslColorAdjustment
import com.example.model.HslColorChannel
import com.example.model.PhotoEditParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

data class AiToneResult(
    val styleName: String,
    val description: String,
    val params: PhotoEditParams,
    val dominantColors: List<Int> = emptyList(),
    val isFromGemini: Boolean = false
)

object GeminiToneMatcher {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Resolves active API key: User custom key has priority, otherwise BuildConfig.GEMINI_API_KEY.
     */
    fun getEffectiveApiKey(userKey: String?): String {
        if (!userKey.isNullOrBlank()) return userKey.trim()
        val buildKey = BuildConfig.GEMINI_API_KEY
        return if (buildKey != "MY_GEMINI_API_KEY" && buildKey.isNotBlank()) buildKey else ""
    }

    /**
     * Analyzes reference image tone using Gemini AI Vision, or falls back to
     * offline algorithmic tone matching.
     */
    suspend fun analyzeAndMatchTone(
        referenceBitmap: Bitmap,
        userApiKey: String?
    ): Result<AiToneResult> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey(userApiKey)

        if (apiKey.isBlank()) {
            // Algorithmic local tone transfer
            val localResult = extractLocalToneProfile(referenceBitmap)
            return@withContext Result.success(localResult)
        }

        try {
            // Downscale reference bitmap for fast transmission
            val scaledRef = downscaleBitmap(referenceBitmap, 512)
            val base64Image = bitmapToBase64(scaledRef)

            val prompt = """
                You are a world-class Adobe Lightroom master and senior colorist.
                Analyze the attached reference image's color grade, color temperature, tint, exposure mood, contrast, highlights, shadows, whites, blacks, saturation, vibrance, HSL shifts for 8 color channels, and split toning.
                Extract exact numeric Lightroom slider adjustments so that a photographer can reproduce the visual color tone, atmosphere, and palette of this reference image onto their own target photo WITHOUT altering any objects, faces, or geometry in the target photo.
                
                Return ONLY valid JSON matching this exact structure:
                {
                  "styleName": "Creative name for this color tone",
                  "description": "Short explanation of the aesthetic look",
                  "exposure": 0.0,
                  "contrast": 0.0,
                  "highlights": 0.0,
                  "shadows": 0.0,
                  "whites": 0.0,
                  "blacks": 0.0,
                  "temperature": 0.0,
                  "tint": 0.0,
                  "vibrance": 0.0,
                  "saturation": 0.0,
                  "clarity": 0.0,
                  "dehaze": 0.0,
                  "vignette": 0.0,
                  "grain": 0.0,
                  "colorGrading": {
                    "shadowHue": 210.0, "shadowSat": 25.0,
                    "highlightHue": 38.0, "highlightSat": 20.0
                  },
                  "hsl": {
                    "red": {"hue": 0.0, "saturation": 0.0, "luminance": 0.0},
                    "orange": {"hue": 0.0, "saturation": 0.0, "luminance": 0.0},
                    "yellow": {"hue": 0.0, "saturation": 0.0, "luminance": 0.0},
                    "green": {"hue": 0.0, "saturation": 0.0, "luminance": 0.0},
                    "aqua": {"hue": 0.0, "saturation": 0.0, "luminance": 0.0},
                    "blue": {"hue": 0.0, "saturation": 0.0, "luminance": 0.0},
                    "purple": {"hue": 0.0, "saturation": 0.0, "luminance": 0.0},
                    "magenta": {"hue": 0.0, "saturation": 0.0, "luminance": 0.0}
                  }
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArr = org.json.JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArr = org.json.JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            put(JSONObject().apply {
                                put("inline_data", JSONObject().apply {
                                    put("mime_type", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", partsArr)
                    }
                    put(contentObj)
                }
                put("contents", contentsArr)
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder().url(url).post(requestBody).build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                // If API call fails (quota, invalid key, or network), fallback gracefully
                val localFallback = extractLocalToneProfile(referenceBitmap)
                return@withContext Result.success(localFallback)
            }

            val responseBody = response.body?.string() ?: ""
            val jsonRoot = JSONObject(responseBody)
            val candidates = jsonRoot.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val parts = firstCandidate?.optJSONObject("content")?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            val parsedParams = parseToneParamsFromJson(text)
            Result.success(parsedParams.copy(isFromGemini = true))
        } catch (e: Exception) {
            // Graceful fallback to local algorithmic matching
            val localResult = extractLocalToneProfile(referenceBitmap)
            Result.success(localResult)
        }
    }

    private fun parseToneParamsFromJson(jsonString: String): AiToneResult {
        val root = JSONObject(jsonString)
        val styleName = root.optString("styleName", "AI Matched Grade")
        val description = root.optString("description", "Color grade mapped from reference photo")

        val exposure = root.optDouble("exposure", 0.0).toFloat().coerceIn(-2.0f, 2.0f)
        val contrast = root.optDouble("contrast", 0.0).toFloat().coerceIn(-100f, 100f)
        val highlights = root.optDouble("highlights", 0.0).toFloat().coerceIn(-100f, 100f)
        val shadows = root.optDouble("shadows", 0.0).toFloat().coerceIn(-100f, 100f)
        val whites = root.optDouble("whites", 0.0).toFloat().coerceIn(-100f, 100f)
        val blacks = root.optDouble("blacks", 0.0).toFloat().coerceIn(-100f, 100f)

        val temperature = root.optDouble("temperature", 0.0).toFloat().coerceIn(-100f, 100f)
        val tint = root.optDouble("tint", 0.0).toFloat().coerceIn(-100f, 100f)
        val vibrance = root.optDouble("vibrance", 0.0).toFloat().coerceIn(-100f, 100f)
        val saturation = root.optDouble("saturation", 0.0).toFloat().coerceIn(-100f, 100f)

        val clarity = root.optDouble("clarity", 0.0).toFloat().coerceIn(-100f, 100f)
        val dehaze = root.optDouble("dehaze", 0.0).toFloat().coerceIn(-100f, 100f)
        val vignette = root.optDouble("vignette", 0.0).toFloat().coerceIn(-100f, 100f)
        val grain = root.optDouble("grain", 0.0).toFloat().coerceIn(0f, 100f)

        val cgObj = root.optJSONObject("colorGrading")
        val colorGrading = if (cgObj != null) {
            ColorGrading(
                shadowHue = cgObj.optDouble("shadowHue", 0.0).toFloat().coerceIn(0f, 360f),
                shadowSat = cgObj.optDouble("shadowSat", 0.0).toFloat().coerceIn(0f, 100f),
                highlightHue = cgObj.optDouble("highlightHue", 0.0).toFloat().coerceIn(0f, 360f),
                highlightSat = cgObj.optDouble("highlightSat", 0.0).toFloat().coerceIn(0f, 100f)
            )
        } else {
            ColorGrading()
        }

        val hslMap = mutableMapOf<HslColorChannel, HslColorAdjustment>()
        val hslObj = root.optJSONObject("hsl")
        if (hslObj != null) {
            for (channel in HslColorChannel.entries) {
                val key = channel.name.lowercase()
                val chObj = hslObj.optJSONObject(key)
                if (chObj != null) {
                    hslMap[channel] = HslColorAdjustment(
                        hue = chObj.optDouble("hue", 0.0).toFloat().coerceIn(-100f, 100f),
                        saturation = chObj.optDouble("saturation", 0.0).toFloat().coerceIn(-100f, 100f),
                        luminance = chObj.optDouble("luminance", 0.0).toFloat().coerceIn(-100f, 100f)
                    )
                }
            }
        }

        val params = PhotoEditParams(
            exposure = exposure,
            contrast = contrast,
            highlights = highlights,
            shadows = shadows,
            whites = whites,
            blacks = blacks,
            temperature = temperature,
            tint = tint,
            vibrance = vibrance,
            saturation = saturation,
            clarity = clarity,
            dehaze = dehaze,
            vignette = vignette,
            grain = grain,
            colorGrading = colorGrading,
            hslAdjustments = hslMap
        )

        return AiToneResult(styleName, description, params)
    }

    /**
     * Local algorithm to analyze reference photo's color moments (temperature, tint, contrast,
     * highlight/shadow casts) when offline or without Gemini key.
     */
    fun extractLocalToneProfile(bitmap: Bitmap): AiToneResult {
        val w = min(bitmap.width, 300)
        val h = min(bitmap.height, 300)
        val scaled = Bitmap.createScaledBitmap(bitmap, w, h, true)
        val total = w * h
        val pixels = IntArray(total)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)

        var totalR = 0.0
        var totalG = 0.0
        var totalB = 0.0
        var totalLum = 0.0

        var shadowR = 0.0
        var shadowB = 0.0
        var shadowCount = 0

        var highlightR = 0.0
        var highlightB = 0.0
        var highlightCount = 0

        val hsv = FloatArray(3)
        var totalSat = 0f

        for (p in pixels) {
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val lum = 0.299 * r + 0.587 * g + 0.114 * b

            totalR += r
            totalG += g
            totalB += b
            totalLum += lum

            android.graphics.Color.RGBToHSV(r, g, b, hsv)
            totalSat += hsv[1]

            if (lum < 80) {
                shadowR += r
                shadowB += b
                shadowCount++
            } else if (lum > 175) {
                highlightR += r
                highlightB += b
                highlightCount++
            }
        }

        val avgR = totalR / total
        val avgG = totalG / total
        val avgB = totalB / total
        val avgSat = totalSat / total

        // Temperature: warm (R > B) vs cool (B > R)
        val tempRaw = ((avgR - avgB) / 255.0) * 120.0
        val temp = tempRaw.toFloat().coerceIn(-60f, 60f)

        // Tint: magenta (R+B > 2G) vs green
        val tintRaw = (((avgR + avgB) / 2.0 - avgG) / 255.0) * 80.0
        val tint = tintRaw.toFloat().coerceIn(-40f, 40f)

        // Contrast and exposure estimate
        val exp = (((totalLum / total) - 128.0) / 255.0).toFloat().coerceIn(-0.6f, 0.6f)
        val sat = ((avgSat - 0.4f) * 80f).coerceIn(-40f, 40f)

        // Split toning from shadow vs highlight casts
        var shHue = 0f
        var shSat = 0f
        if (shadowCount > 50) {
            val sAvgR = shadowR / shadowCount
            val sAvgB = shadowB / shadowCount
            if (sAvgB > sAvgR) {
                shHue = 215f // cool blue shadows
                shSat = (((sAvgB - sAvgR) / 255.0) * 80.0).toFloat().coerceIn(5f, 40f)
            } else {
                shHue = 35f // warm shadows
                shSat = (((sAvgR - sAvgB) / 255.0) * 80.0).toFloat().coerceIn(5f, 40f)
            }
        }

        var hiHue = 0f
        var hiSat = 0f
        if (highlightCount > 50) {
            val hAvgR = highlightR / highlightCount
            val hAvgB = highlightB / highlightCount
            if (hAvgR > hAvgB) {
                hiHue = 42f // golden highlights
                hiSat = (((hAvgR - hAvgB) / 255.0) * 80.0).toFloat().coerceIn(5f, 45f)
            } else {
                hiHue = 195f // teal/cyan highlights
                hiSat = (((hAvgB - hAvgR) / 255.0) * 80.0).toFloat().coerceIn(5f, 45f)
            }
        }

        val params = PhotoEditParams(
            exposure = exp * 0.5f,
            contrast = 15f,
            highlights = -15f,
            shadows = 20f,
            temperature = temp,
            tint = tint,
            vibrance = 15f,
            saturation = sat,
            colorGrading = ColorGrading(
                shadowHue = shHue,
                shadowSat = shSat,
                highlightHue = hiHue,
                highlightSat = hiSat
            )
        )

        return AiToneResult(
            styleName = if (temp > 15f) "Golden Warm Grade" else if (temp < -15f) "Nordic Cool Tone" else "Harmonic Color Mood",
            description = "Analyzed color balance & split-toning mapped to Lightroom adjustments",
            params = params,
            isFromGemini = false
        )
    }

    private fun downscaleBitmap(bitmap: Bitmap, maxDim: Int): Bitmap {
        val currentMax = maxOf(bitmap.width, bitmap.height)
        if (currentMax <= maxDim) return bitmap
        val scale = maxDim.toFloat() / currentMax.toFloat()
        val targetW = (bitmap.width * scale).toInt()
        val targetH = (bitmap.height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }
}
