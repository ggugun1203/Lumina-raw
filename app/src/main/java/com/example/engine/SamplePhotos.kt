package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import com.example.model.ExifData

data class SamplePhotoItem(
    val id: String,
    val title: String,
    val category: String,
    val exif: ExifData,
    val generator: (width: Int, height: Int) -> Bitmap
)

object SamplePhotos {

    val items: List<SamplePhotoItem> = listOf(
        SamplePhotoItem(
            id = "sample_mountain_sunset",
            title = "Alpine Sunset RAW",
            category = "Landscape",
            exif = ExifData(
                cameraMake = "Sony",
                cameraModel = "ILCE-7RM5",
                lensModel = "FE 16-35mm F2.8 GM",
                iso = "100",
                shutterSpeed = "1/125s",
                aperture = "ƒ/8.0",
                focalLength = "24mm",
                dimensions = "3840 × 2560",
                megapixels = "9.8 MP",
                isRaw = true
            ),
            generator = { w, h -> generateMountainSunset(w, h) }
        ),
        SamplePhotoItem(
            id = "sample_golden_portrait",
            title = "Golden Hour Portrait",
            category = "Portrait",
            exif = ExifData(
                cameraMake = "Canon",
                cameraModel = "EOS R5",
                lensModel = "RF 85mm F1.2 L USM",
                iso = "200",
                shutterSpeed = "1/800s",
                aperture = "ƒ/1.4",
                focalLength = "85mm",
                dimensions = "3840 × 2560",
                megapixels = "9.8 MP",
                isRaw = true
            ),
            generator = { w, h -> generateGoldenPortrait(w, h) }
        ),
        SamplePhotoItem(
            id = "sample_cyberpunk_neon",
            title = "Tokyo Neon Alley",
            category = "Urban Night",
            exif = ExifData(
                cameraMake = "Fujifilm",
                cameraModel = "X-T5",
                lensModel = "XF 33mm F1.4 R LM WR",
                iso = "1600",
                shutterSpeed = "1/60s",
                aperture = "ƒ/1.4",
                focalLength = "33mm",
                dimensions = "3840 × 2560",
                megapixels = "9.8 MP",
                isRaw = true
            ),
            generator = { w, h -> generateNeonCity(w, h) }
        ),
        SamplePhotoItem(
            id = "sample_emerald_forest",
            title = "Misty Emerald Pines",
            category = "Nature",
            exif = ExifData(
                cameraMake = "Nikon",
                cameraModel = "Z8",
                lensModel = "NIKKOR Z 24-120mm f/4 S",
                iso = "400",
                shutterSpeed = "1/200s",
                aperture = "ƒ/5.6",
                focalLength = "70mm",
                dimensions = "3840 × 2560",
                megapixels = "9.8 MP",
                isRaw = true
            ),
            generator = { w, h -> generateEmeraldForest(w, h) }
        )
    )

    private fun generateMountainSunset(w: Int, h: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Sky gradient: Dusk indigo to fiery orange and golden horizon
        paint.shader = LinearGradient(
            0f, 0f, 0f, h * 0.65f,
            intArrayOf(
                AndroidColor.rgb(24, 28, 62),
                AndroidColor.rgb(85, 45, 95),
                AndroidColor.rgb(220, 95, 50),
                AndroidColor.rgb(255, 180, 70),
                AndroidColor.rgb(255, 230, 140)
            ),
            floatArrayOf(0f, 0.35f, 0.7f, 0.9f, 1.0f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h * 0.65f, paint)

        // Sun disc with radiant glow
        val sunX = w * 0.68f
        val sunY = h * 0.52f
        val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                sunX, sunY, w * 0.28f,
                intArrayOf(
                    AndroidColor.argb(255, 255, 250, 220),
                    AndroidColor.argb(200, 255, 190, 70),
                    AndroidColor.argb(80, 240, 110, 40),
                    AndroidColor.TRANSPARENT
                ),
                floatArrayOf(0f, 0.2f, 0.6f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(sunX, sunY, w * 0.28f, sunPaint)

        // Far mountain silhouettes (Purplish haze)
        paint.shader = null
        paint.color = AndroidColor.rgb(90, 50, 75)
        val farMountains = Path().apply {
            moveTo(0f, h * 0.62f)
            lineTo(w * 0.2f, h * 0.45f)
            lineTo(w * 0.45f, h * 0.52f)
            lineTo(w * 0.7f, h * 0.42f)
            lineTo(w * 0.85f, h * 0.48f)
            lineTo(w.toFloat(), h * 0.40f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        canvas.drawPath(farMountains, paint)

        // Mid mountain range (Deep dusky blue with sharp peaks)
        paint.color = AndroidColor.rgb(38, 30, 52)
        val midMountains = Path().apply {
            moveTo(0f, h * 0.58f)
            lineTo(w * 0.15f, h * 0.52f)
            lineTo(w * 0.35f, h * 0.60f)
            lineTo(w * 0.55f, h * 0.48f)
            lineTo(w * 0.78f, h * 0.58f)
            lineTo(w.toFloat(), h * 0.50f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        canvas.drawPath(midMountains, paint)

        // Foreground alpine pine forest and reflective lake
        val lakePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, h * 0.7f, 0f, h.toFloat(),
                intArrayOf(AndroidColor.rgb(40, 42, 60), AndroidColor.rgb(20, 22, 32), AndroidColor.rgb(10, 12, 18)),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, h * 0.7f, w.toFloat(), h.toFloat(), lakePaint)

        // Dark pine silhouettes in foreground
        val treePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(12, 16, 22)
        }
        drawForestSlices(canvas, treePaint, w, h)

        return bitmap
    }

    private fun generateGoldenPortrait(w: Int, h: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Warm background with dreamy golden bokeh circles
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w * 0.7f, h * 0.3f, w * 0.8f,
                intArrayOf(
                    AndroidColor.rgb(255, 215, 140),
                    AndroidColor.rgb(225, 150, 75),
                    AndroidColor.rgb(140, 80, 40),
                    AndroidColor.rgb(65, 35, 20)
                ),
                floatArrayOf(0f, 0.4f, 0.75f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Soft bokeh orbs
        val bokehPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val orbs = listOf(
            Triple(w * 0.2f, h * 0.25f, w * 0.12f),
            Triple(w * 0.85f, h * 0.18f, w * 0.16f),
            Triple(w * 0.15f, h * 0.65f, w * 0.18f),
            Triple(w * 0.8f, h * 0.75f, w * 0.22f),
            Triple(w * 0.5f, h * 0.15f, w * 0.09f)
        )
        for ((ox, oy, or) in orbs) {
            bokehPaint.shader = RadialGradient(
                ox, oy, or,
                intArrayOf(AndroidColor.argb(120, 255, 240, 190), AndroidColor.argb(30, 255, 200, 100), AndroidColor.TRANSPARENT),
                null, Shader.TileMode.CLAMP
            )
            canvas.drawCircle(ox, oy, or, bokehPaint)
        }

        // Elegant portrait silhouette with warm golden rim lighting
        val headX = w * 0.46f
        val headY = h * 0.42f
        val headR = w * 0.18f

        val rimLight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(255, 220, 150)
        }
        canvas.drawCircle(headX + 4f, headY, headR + 6f, rimLight)

        val subjectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                headX - headR, headY, headX + headR, headY,
                intArrayOf(AndroidColor.rgb(35, 22, 16), AndroidColor.rgb(75, 45, 30)),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(headX, headY, headR, subjectPaint)

        // Shoulders / torso
        val torsoPath = Path().apply {
            moveTo(headX - w * 0.35f, h.toFloat())
            quadTo(headX - w * 0.25f, headY + headR * 1.3f, headX - headR * 0.6f, headY + headR * 0.9f)
            lineTo(headX + headR * 0.6f, headY + headR * 0.9f)
            quadTo(headX + w * 0.25f, headY + headR * 1.3f, headX + w * 0.35f, h.toFloat())
            close()
        }
        canvas.drawPath(torsoPath, subjectPaint)

        return bitmap
    }

    private fun generateNeonCity(w: Int, h: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Midnight cyan / blue atmospheric gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(
                    AndroidColor.rgb(8, 12, 22),
                    AndroidColor.rgb(12, 18, 32),
                    AndroidColor.rgb(20, 15, 35),
                    AndroidColor.rgb(10, 8, 16)
                ),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Skyscraper silhouettes with illuminated neon windows
        val buildingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(16, 22, 38)
        }
        val bWidths = listOf(w * 0.15f, w * 0.18f, w * 0.22f, w * 0.16f, w * 0.2f, w * 0.18f)
        var curX = 0f
        val heights = listOf(h * 0.45f, h * 0.35f, h * 0.55f, h * 0.4f, h * 0.5f, h * 0.38f)
        for (i in bWidths.indices) {
            val bw = bWidths[i]
            val bh = heights[i]
            canvas.drawRect(curX, h - bh, curX + bw, h.toFloat(), buildingPaint)

            // Neon signage on buildings
            val signPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (i % 2 == 0) AndroidColor.rgb(255, 45, 120) else AndroidColor.rgb(0, 240, 255)
            }
            canvas.drawRect(curX + bw * 0.25f, h - bh + 30f, curX + bw * 0.75f, h - bh + 70f, signPaint)
            curX += bw - 8f
        }

        // Wet asphalt ground with neon reflections
        val wetGround = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, h * 0.8f, 0f, h.toFloat(),
                intArrayOf(AndroidColor.rgb(18, 15, 28), AndroidColor.rgb(6, 8, 14)),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, h * 0.8f, w.toFloat(), h.toFloat(), wetGround)

        // Neon reflection streaks
        val streakPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        streakPaint.shader = LinearGradient(
            0f, h * 0.82f, 0f, h.toFloat(),
            intArrayOf(AndroidColor.argb(160, 255, 40, 130), AndroidColor.TRANSPARENT),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(w * 0.2f, h * 0.82f, w * 0.35f, h.toFloat(), streakPaint)

        streakPaint.shader = LinearGradient(
            0f, h * 0.82f, 0f, h.toFloat(),
            intArrayOf(AndroidColor.argb(160, 0, 230, 255), AndroidColor.TRANSPARENT),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(w * 0.6f, h * 0.82f, w * 0.78f, h.toFloat(), streakPaint)

        return bitmap
    }

    private fun generateEmeraldForest(w: Int, h: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Misty morning emerald gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(
                    AndroidColor.rgb(190, 215, 205),
                    AndroidColor.rgb(115, 160, 140),
                    AndroidColor.rgb(55, 95, 75),
                    AndroidColor.rgb(25, 55, 40),
                    AndroidColor.rgb(15, 35, 25)
                ),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Tree trunks and layers of foliage
        val treePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(20, 40, 30)
        }

        // Multiple tree trunks
        val trunkX = listOf(w * 0.12f, w * 0.28f, w * 0.48f, w * 0.72f, w * 0.88f)
        for (tx in trunkX) {
            canvas.drawRect(tx - 16f, h * 0.25f, tx + 16f, h.toFloat(), treePaint)
        }

        // Misty fog overlay band
        val fogPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, h * 0.35f, 0f, h * 0.65f,
                intArrayOf(AndroidColor.TRANSPARENT, AndroidColor.argb(140, 220, 235, 230), AndroidColor.TRANSPARENT),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, h * 0.35f, w.toFloat(), h * 0.65f, fogPaint)

        return bitmap
    }

    private fun drawForestSlices(canvas: Canvas, paint: Paint, w: Int, h: Int) {
        val baseH = h * 0.72f
        var x = 0f
        while (x < w) {
            val treeW = (18..35).random().toFloat()
            val treeH = (40..100).random().toFloat()
            val path = Path().apply {
                moveTo(x + treeW / 2f, baseH - treeH)
                lineTo(x + treeW, baseH)
                lineTo(x, baseH)
                close()
            }
            canvas.drawPath(path, paint)
            x += treeW * 0.7f
        }
    }
}
