package com.example.model

data class Preset(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val params: PhotoEditParams,
    val isCustom: Boolean = false
)

object PresetLibrary {
    val builtInPresets: List<Preset> = listOf(
        Preset(
            id = "preset_portra_400",
            name = "Portra 400 Film",
            category = "Film & Analog",
            description = "Warm creamy skin tones with gentle highlight rolloff and subtle grain",
            params = PhotoEditParams(
                exposure = 0.15f,
                contrast = -10f,
                highlights = -25f,
                shadows = 30f,
                whites = -15f,
                blacks = 15f,
                temperature = 18f,
                tint = 4f,
                vibrance = 15f,
                saturation = -8f,
                grain = 18f,
                vignette = -8f,
                colorGrading = ColorGrading(
                    shadowHue = 45f,
                    shadowSat = 14f,
                    highlightHue = 40f,
                    highlightSat = 12f
                ),
                hslAdjustments = mapOf(
                    HslColorChannel.ORANGE to HslColorAdjustment(hue = 5f, saturation = 10f, luminance = 8f),
                    HslColorChannel.RED to HslColorAdjustment(hue = 2f, saturation = -5f, luminance = 5f),
                    HslColorChannel.YELLOW to HslColorAdjustment(hue = -4f, saturation = -10f, luminance = 5f),
                    HslColorChannel.GREEN to HslColorAdjustment(hue = 15f, saturation = -20f, luminance = -5f)
                )
            )
        ),
        Preset(
            id = "preset_cinematic_teal",
            name = "Moody Teal & Orange",
            category = "Cinematic",
            description = "High contrast blockbuster look with cyan shadows and golden skin midtones",
            params = PhotoEditParams(
                exposure = 0.05f,
                contrast = 28f,
                highlights = -30f,
                shadows = -10f,
                whites = 15f,
                blacks = -20f,
                temperature = 8f,
                tint = -5f,
                vibrance = 25f,
                saturation = 5f,
                clarity = 18f,
                dehaze = 12f,
                vignette = -22f,
                colorGrading = ColorGrading(
                    shadowHue = 195f,
                    shadowSat = 35f,
                    highlightHue = 35f,
                    highlightSat = 28f,
                    balance = -10f
                ),
                hslAdjustments = mapOf(
                    HslColorChannel.AQUA to HslColorAdjustment(hue = 10f, saturation = 30f, luminance = -10f),
                    HslColorChannel.BLUE to HslColorAdjustment(hue = -15f, saturation = 25f, luminance = -15f),
                    HslColorChannel.ORANGE to HslColorAdjustment(hue = -5f, saturation = 20f, luminance = 12f)
                )
            )
        ),
        Preset(
            id = "preset_golden_hour",
            name = "Golden Hour Glow",
            category = "Warm & Natural",
            description = "Sun-drenched warmth, soft lifted blacks and golden amber highlights",
            params = PhotoEditParams(
                exposure = 0.25f,
                contrast = -5f,
                highlights = -40f,
                shadows = 35f,
                whites = 10f,
                blacks = 20f,
                temperature = 35f,
                tint = 8f,
                vibrance = 20f,
                saturation = 10f,
                clarity = -8f,
                vignette = -12f,
                colorGrading = ColorGrading(
                    shadowHue = 32f,
                    shadowSat = 20f,
                    highlightHue = 48f,
                    highlightSat = 30f
                ),
                hslAdjustments = mapOf(
                    HslColorChannel.YELLOW to HslColorAdjustment(hue = -10f, saturation = 25f, luminance = 15f),
                    HslColorChannel.ORANGE to HslColorAdjustment(hue = 5f, saturation = 15f, luminance = 10f)
                )
            )
        ),
        Preset(
            id = "preset_film_noir",
            name = "Film Noir B&W",
            category = "Monochrome",
            description = "Deep dramatic shadows, punchy midtone contrast, and fine silver grain",
            params = PhotoEditParams(
                exposure = -0.1f,
                contrast = 45f,
                highlights = 20f,
                shadows = -30f,
                whites = 25f,
                blacks = -35f,
                temperature = 0f,
                tint = 0f,
                vibrance = -100f,
                saturation = -100f,
                clarity = 25f,
                dehaze = 15f,
                vignette = -30f,
                grain = 30f,
                sharpen = 20f
            )
        ),
        Preset(
            id = "preset_pastel_dream",
            name = "Aesthetic Pastel",
            category = "Lifestyle & Editorial",
            description = "Soft desaturated tones, bright airy highlights, and clean modern feel",
            params = PhotoEditParams(
                exposure = 0.4f,
                contrast = -18f,
                highlights = -50f,
                shadows = 40f,
                whites = -10f,
                blacks = 25f,
                temperature = -6f,
                tint = 12f,
                vibrance = 10f,
                saturation = -15f,
                clarity = -12f,
                vignette = 5f,
                colorGrading = ColorGrading(
                    shadowHue = 270f,
                    shadowSat = 10f,
                    highlightHue = 55f,
                    highlightSat = 12f
                )
            )
        ),
        Preset(
            id = "preset_nordic_clean",
            name = "Nordic Minimalist",
            category = "Architecture & Nature",
            description = "Cool crisp blues, clean desaturated greens, and pure architectural whites",
            params = PhotoEditParams(
                exposure = 0.1f,
                contrast = 15f,
                highlights = -20f,
                shadows = 15f,
                whites = 20f,
                blacks = -10f,
                temperature = -22f,
                tint = -4f,
                vibrance = -10f,
                saturation = -12f,
                clarity = 15f,
                dehaze = 10f,
                hslAdjustments = mapOf(
                    HslColorChannel.BLUE to HslColorAdjustment(hue = 5f, saturation = -10f, luminance = 10f),
                    HslColorChannel.GREEN to HslColorAdjustment(hue = 25f, saturation = -35f, luminance = -10f),
                    HslColorChannel.YELLOW to HslColorAdjustment(hue = 15f, saturation = -30f, luminance = -5f)
                )
            )
        ),
        Preset(
            id = "preset_cyberpunk",
            name = "Cyberpunk Neon",
            category = "Night & Urban",
            description = "Saturated magenta and neon blue night grade with punchy contrast",
            params = PhotoEditParams(
                exposure = -0.2f,
                contrast = 35f,
                highlights = 15f,
                shadows = -25f,
                whites = 20f,
                blacks = -30f,
                temperature = -15f,
                tint = 28f,
                vibrance = 45f,
                saturation = 20f,
                clarity = 25f,
                dehaze = 20f,
                vignette = -25f,
                colorGrading = ColorGrading(
                    shadowHue = 220f,
                    shadowSat = 40f,
                    highlightHue = 310f,
                    highlightSat = 45f
                ),
                hslAdjustments = mapOf(
                    HslColorChannel.PURPLE to HslColorAdjustment(hue = -10f, saturation = 50f, luminance = 15f),
                    HslColorChannel.MAGENTA to HslColorAdjustment(hue = 10f, saturation = 45f, luminance = 15f),
                    HslColorChannel.AQUA to HslColorAdjustment(hue = 5f, saturation = 40f, luminance = 10f)
                )
            )
        )
    )
}
