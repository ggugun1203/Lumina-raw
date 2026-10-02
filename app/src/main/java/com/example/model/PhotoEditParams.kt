package com.example.model

data class HslColorAdjustment(
    val hue: Float = 0f,        // -100f to +100f
    val saturation: Float = 0f, // -100f to +100f
    val luminance: Float = 0f   // -100f to +100f
)

enum class HslColorChannel(val label: String, val centerHue: Float) {
    RED("Red", 0f),
    ORANGE("Orange", 30f),
    YELLOW("Yellow", 60f),
    GREEN("Green", 120f),
    AQUA("Aqua", 180f),
    BLUE("Blue", 240f),
    PURPLE("Purple", 280f),
    MAGENTA("Magenta", 320f)
}

data class ColorGrading(
    val shadowHue: Float = 0f,       // 0 to 360
    val shadowSat: Float = 0f,       // 0 to 100
    val highlightHue: Float = 0f,    // 0 to 360
    val highlightSat: Float = 0f,    // 0 to 100
    val midtoneHue: Float = 0f,      // 0 to 360
    val midtoneSat: Float = 0f,      // 0 to 100
    val balance: Float = 0f          // -100 to +100
)

data class ToneCurveParams(
    val highlights: Float = 0f, // -100 to 100
    val lights: Float = 0f,     // -100 to 100
    val darks: Float = 0f,      // -100 to 100
    val shadows: Float = 0f     // -100 to 100
)

data class CropTransform(
    val rotationDegrees: Int = 0, // 0, 90, 180, 270
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val aspectRatio: CropAspectRatio = CropAspectRatio.ORIGINAL
)

enum class CropAspectRatio(val label: String, val ratio: Float?) {
    ORIGINAL("Original", null),
    FREE("Free", null),
    SQUARE("1:1", 1.0f),
    PORTRAIT_4_5("4:5", 0.8f),
    STORY_9_16("9:16", 9f / 16f),
    CINEMA_16_9("16:9", 16f / 9f),
    CLASSIC_3_2("3:2", 1.5f),
    PORTRAIT_2_3("2:3", 2f / 3f)
}

data class PhotoEditParams(
    // Light
    val exposure: Float = 0f,    // -2.0f to +2.0f EV
    val contrast: Float = 0f,    // -100f to +100f
    val highlights: Float = 0f,  // -100f to +100f
    val shadows: Float = 0f,     // -100f to +100f
    val whites: Float = 0f,      // -100f to +100f
    val blacks: Float = 0f,      // -100f to +100f

    // Color
    val temperature: Float = 0f, // -100f (cool blue) to +100f (warm amber)
    val tint: Float = 0f,        // -100f (green) to +100f (magenta)
    val vibrance: Float = 0f,    // -100f to +100f
    val saturation: Float = 0f,  // -100f to +100f

    // Effects & Detail
    val clarity: Float = 0f,     // -100f to +100f
    val dehaze: Float = 0f,      // -100f to +100f
    val vignette: Float = 0f,    // -100f (dark) to +100f (white)
    val grain: Float = 0f,       // 0f to 100f
    val sharpen: Float = 0f,     // 0f to 100f

    // Tone Curve
    val toneCurve: ToneCurveParams = ToneCurveParams(),

    // Color Grading
    val colorGrading: ColorGrading = ColorGrading(),

    // HSL 8 Channels
    val hslAdjustments: Map<HslColorChannel, HslColorAdjustment> = HslColorChannel.entries.associateWith {
        HslColorAdjustment()
    },

    // Transform
    val transform: CropTransform = CropTransform()
) {
    fun isModified(): Boolean {
        return exposure != 0f || contrast != 0f || highlights != 0f || shadows != 0f ||
                whites != 0f || blacks != 0f || temperature != 0f || tint != 0f ||
                vibrance != 0f || saturation != 0f || clarity != 0f || dehaze != 0f ||
                vignette != 0f || grain != 0f || sharpen != 0f ||
                toneCurve != ToneCurveParams() ||
                colorGrading != ColorGrading() ||
                hslAdjustments.values.any { it.hue != 0f || it.saturation != 0f || it.luminance != 0f } ||
                transform != CropTransform()
    }
}
