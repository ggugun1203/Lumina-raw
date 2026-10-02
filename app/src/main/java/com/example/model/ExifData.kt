package com.example.model

data class ExifData(
    val cameraMake: String = "Sony",
    val cameraModel: String = "ILCE-7RM5",
    val lensModel: String = "FE 24-70mm F2.8 GM II",
    val iso: String = "100",
    val shutterSpeed: String = "1/250s",
    val aperture: String = "ƒ/2.8",
    val focalLength: String = "35mm",
    val dateTime: String = "2026:10:01 16:42:00",
    val dimensions: String = "6000 × 4000",
    val megapixels: String = "24.0 MP",
    val colorSpace: String = "sRGB / DNG 1.4",
    val isRaw: Boolean = true
)
