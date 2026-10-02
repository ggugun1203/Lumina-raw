package com.example.model

import androidx.compose.ui.graphics.Color

enum class CollageLayoutType(val title: String, val photoCount: Int, val description: String) {
    MAGAZINE_DUO("Magazine Split", 2, "Editorial duo layout with sleek aesthetic margins"),
    FILM_STRIP_TRIO("Film Strip 35mm", 3, "Classic cinematic 3-frame analog film strip"),
    POLAROID_DUO("Polaroid Frames", 2, "Vintage instant photo prints with paper border"),
    MODERN_QUAD("Modern 2×2 Grid", 4, "Symmetrical gallery grid with balanced spacing"),
    ASYMMETRIC_HERO("Editorial Hero", 3, "1 dominant hero frame with 2 stacked side details"),
    STORY_PANEL("Story Triptych", 3, "Three horizontal cinematic widescreen panels"),
    MOODBOARD_FOUR("Art Moodboard", 4, "Curated pinboard aesthetic with mixed proportions")
}

data class CollageAestheticTheme(
    val id: String,
    val name: String,
    val backgroundColor: Color,
    val textColor: Color,
    val borderColor: Color
) {
    companion object {
        val themes: List<CollageAestheticTheme> = listOf(
            CollageAestheticTheme("paper_white", "Paper White", Color(0xFFF9F9FB), Color(0xFF1B1D22), Color(0xFFE5E7EB)),
            CollageAestheticTheme("studio_noir", "Studio Noir", Color(0xFF0F1115), Color(0xFFF3F4F6), Color(0xFF272A34)),
            CollageAestheticTheme("warm_sand", "Warm Sand", Color(0xFFF4ECE1), Color(0xFF2C241B), Color(0xFFE2D6C5)),
            CollageAestheticTheme("terracotta", "Terracotta", Color(0xFF3D211A), Color(0xFFF8EDE8), Color(0xFF5A352D)),
            CollageAestheticTheme("sage_olive", "Sage Olive", Color(0xFFE8EDE5), Color(0xFF1E281C), Color(0xFFCFD9CA)),
            CollageAestheticTheme("slate_cool", "Slate Navy", Color(0xFF1B222D), Color(0xFFE2E8F0), Color(0xFF2C384A))
        )
    }
}

data class CollageState(
    val layoutType: CollageLayoutType = CollageLayoutType.MAGAZINE_DUO,
    val photos: List<String?> = List(4) { null }, // null means placeholder or sample
    val aspectRatio: CropAspectRatio = CropAspectRatio.PORTRAIT_4_5,
    val borderPadding: Float = 12f, // dp
    val cornerRadius: Float = 8f,   // dp
    val selectedTheme: CollageAestheticTheme = CollageAestheticTheme.themes[1], // Studio Noir
    val caption: String = "LUMINA COLLECTION // VOL. 01"
)
