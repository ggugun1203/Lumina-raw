package com.example.model

enum class ExportFormat(val extension: String, val mimeType: String, val displayName: String) {
    JPG("jpg", "image/jpeg", "JPEG (Ultra HD)"),
    DNG("dng", "image/x-adobe-dng", "Adobe DNG (Raw Lossless)"),
    PNG("png", "image/png", "PNG (Lossless 24-bit)")
}

enum class ExportResolution(val label: String, val maxDimension: Int?) {
    ORIGINAL_MAX("Original / Maximum", null),
    UHD_4K("4K Ultra HD (3840px)", 3840),
    QHD_2K("2K QHD (2560px)", 2560),
    FHD_1080P("1080p Full HD (1920px)", 1920)
}

data class ExportConfig(
    val format: ExportFormat = ExportFormat.JPG,
    val quality: Int = 100, // 50 to 100%
    val resolution: ExportResolution = ExportResolution.ORIGINAL_MAX,
    val includeExif: Boolean = true,
    val embedColorProfile: Boolean = true
)
