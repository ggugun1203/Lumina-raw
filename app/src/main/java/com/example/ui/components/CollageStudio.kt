package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engine.DngExporter
import com.example.engine.SamplePhotos
import com.example.model.CollageAestheticTheme
import com.example.model.CollageLayoutType
import com.example.model.CropAspectRatio
import com.example.model.ExportConfig
import com.example.model.ExportFormat
import com.example.model.ExportResolution
import com.example.ui.theme.DarkStudioBg
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioSurfaceVariant
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CollageStudio(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedLayout by remember { mutableStateOf(CollageLayoutType.MAGAZINE_DUO) }
    var selectedTheme by remember { mutableStateOf(CollageAestheticTheme.themes[0]) } // Paper White
    var selectedAspectRatio by remember { mutableStateOf(CropAspectRatio.PORTRAIT_4_5) }
    var borderPadding by remember { mutableStateOf(10f) }
    var cornerRadius by remember { mutableStateOf(8f) }

    // Slots bitmaps
    val slotBitmaps = remember {
        mutableStateListOf<Bitmap?>(
            SamplePhotos.items[0].generator(400, 400),
            SamplePhotos.items[1].generator(400, 400),
            SamplePhotos.items[2].generator(400, 400),
            SamplePhotos.items[3].generator(400, 400)
        )
    }

    var activeSlotForPicker by remember { mutableStateOf<Int?>(null) }
    var isExporting by remember { mutableStateOf(false) }
    var exportSuccessMsg by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val slot = activeSlotForPicker
        if (uri != null && slot != null && slot in 0 until slotBitmaps.size) {
            try {
                @Suppress("DEPRECATION")
                val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                slotBitmaps[slot] = bmp
            } catch (e: Exception) {
                // Ignore load error
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .background(DarkStudioBg)
                .testTag("collage_studio_dialog"),
            color = DarkStudioBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Aesthetic Collage Studio",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF3F4F6)
                                )
                            )
                            Text(
                                text = "Editorial, polaroid & film strip templates",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = Color(0xFF9EA6B8)
                                )
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF9EA6B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Collage Canvas Preview (Center)
                val canvasRatio = selectedAspectRatio.ratio ?: (4f / 5f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(canvasRatio)
                            .fillMaxHeight(0.95f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(selectedTheme.backgroundColor)
                            .border(1.dp, selectedTheme.borderColor, RoundedCornerShape(12.dp))
                            .padding(borderPadding.dp)
                            .testTag("collage_preview_canvas")
                    ) {
                        RenderCollageLayout(
                            layoutType = selectedLayout,
                            bitmaps = slotBitmaps,
                            cornerRadius = cornerRadius,
                            spacing = borderPadding,
                            theme = selectedTheme,
                            onSlotClick = { slotIdx ->
                                activeSlotForPicker = slotIdx
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }
                }

                if (exportSuccessMsg != null) {
                    Text(
                        text = exportSuccessMsg ?: "",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Controls in Scrollable Column
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Template Picker
                    Text(
                        text = "TEMPLATES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CollageLayoutType.entries) { layout ->
                            val isSelected = layout == selectedLayout
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) GoldAccent else DarkStudioSurfaceVariant)
                                    .border(1.dp, if (isSelected) GoldAccent else DarkStudioBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedLayout = layout }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("template_${layout.name.lowercase()}")
                            ) {
                                Text(
                                    text = layout.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color(0xFF14171F) else Color(0xFFE2E8F0)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Aesthetic Themes & Ratio Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Themes
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CollageAestheticTheme.themes.forEach { th ->
                                val isSelected = th.id == selectedTheme.id
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(th.backgroundColor)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) GoldAccent else DarkStudioBorder,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedTheme = th }
                                )
                            }
                        }

                        // Aspect Ratio Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                CropAspectRatio.PORTRAIT_4_5,
                                CropAspectRatio.SQUARE,
                                CropAspectRatio.STORY_9_16
                            ).forEach { ratio ->
                                val isSelected = ratio == selectedAspectRatio
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) GoldAccent else DarkStudioSurfaceVariant)
                                        .clickable { selectedAspectRatio = ratio }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = ratio.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(0xFF14171F) else Color(0xFF8B93A7)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sliders Row: Padding & Rounding
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gutter: ${borderPadding.toInt()}dp",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color(0xFF8B93A7))
                            )
                            Slider(
                                value = borderPadding,
                                onValueChange = { borderPadding = it },
                                valueRange = 0f..24f,
                                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Radius: ${cornerRadius.toInt()}dp",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = Color(0xFF8B93A7))
                            )
                            Slider(
                                value = cornerRadius,
                                onValueChange = { cornerRadius = it },
                                valueRange = 0f..24f,
                                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Export Collage Button
                    Button(
                        onClick = {
                            isExporting = true
                            exportSuccessMsg = null
                            scope.launch {
                                val collageBmp = renderCollageToBitmap(
                                    layoutType = selectedLayout,
                                    bitmaps = slotBitmaps,
                                    theme = selectedTheme,
                                    aspectRatio = selectedAspectRatio,
                                    borderPadding = borderPadding,
                                    cornerRadius = cornerRadius
                                )
                                val config = ExportConfig(
                                    format = ExportFormat.JPG,
                                    quality = 100,
                                    resolution = ExportResolution.ORIGINAL_MAX
                                )
                                val res = DngExporter.exportImage(
                                    context,
                                    collageBmp,
                                    config,
                                    "COLLAGE_${selectedLayout.name.lowercase()}"
                                )
                                res.onSuccess {
                                    exportSuccessMsg = "Collage saved in maximum quality to Gallery!"
                                    isExporting = false
                                }.onFailure {
                                    exportSuccessMsg = "Export failed: ${it.message}"
                                    isExporting = false
                                }
                            }
                        },
                        enabled = !isExporting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("export_collage_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = Color(0xFF14171F)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF14171F), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Collage (Max Quality 100% JPG)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderCollageLayout(
    layoutType: CollageLayoutType,
    bitmaps: List<Bitmap?>,
    cornerRadius: Float,
    spacing: Float,
    theme: CollageAestheticTheme,
    onSlotClick: (Int) -> Unit
) {
    val b0 = bitmaps.getOrNull(0)
    val b1 = bitmaps.getOrNull(1)
    val b2 = bitmaps.getOrNull(2)
    val b3 = bitmaps.getOrNull(3)

    when (layoutType) {
        CollageLayoutType.MAGAZINE_DUO -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing.dp)
            ) {
                CollageSlot(bitmap = b0, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxWidth(), onClick = { onSlotClick(0) })
                CollageSlot(bitmap = b1, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxWidth(), onClick = { onSlotClick(1) })
                Text(
                    text = "LUMINA // EDITORIAL DUO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor.copy(alpha = 0.8f),
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
        CollageLayoutType.FILM_STRIP_TRIO -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing.dp)
            ) {
                CollageSlot(bitmap = b0, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxWidth(), onClick = { onSlotClick(0) })
                CollageSlot(bitmap = b1, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxWidth(), onClick = { onSlotClick(1) })
                CollageSlot(bitmap = b2, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxWidth(), onClick = { onSlotClick(2) })
                Text(
                    text = "▶ 35MM ANALOG NEGATIVE 400",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor.copy(alpha = 0.7f),
                        letterSpacing = 1.5.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
        CollageLayoutType.POLAROID_DUO -> {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(spacing.dp)
            ) {
                PolaroidCard(bitmap = b0, cornerRadius = cornerRadius, caption = "FRAME 01", theme = theme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onSlotClick(0) })
                PolaroidCard(bitmap = b1, cornerRadius = cornerRadius, caption = "FRAME 02", theme = theme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onSlotClick(1) })
            }
        }
        CollageLayoutType.MODERN_QUAD -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.dp)
                ) {
                    CollageSlot(bitmap = b0, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onSlotClick(0) })
                    CollageSlot(bitmap = b1, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onSlotClick(1) })
                }
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.dp)
                ) {
                    CollageSlot(bitmap = b2, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onSlotClick(2) })
                    CollageSlot(bitmap = b3, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onSlotClick(3) })
                }
            }
        }
        CollageLayoutType.ASYMMETRIC_HERO -> {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(spacing.dp)
            ) {
                CollageSlot(bitmap = b0, cornerRadius = cornerRadius, modifier = Modifier.weight(1.3f).fillMaxHeight(), onClick = { onSlotClick(0) })
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(spacing.dp)
                ) {
                    CollageSlot(bitmap = b1, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxWidth(), onClick = { onSlotClick(1) })
                    CollageSlot(bitmap = b2, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxWidth(), onClick = { onSlotClick(2) })
                }
            }
        }
        CollageLayoutType.STORY_PANEL -> {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(spacing.dp)
            ) {
                CollageSlot(bitmap = b0, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onSlotClick(0) })
                CollageSlot(bitmap = b1, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onSlotClick(1) })
                CollageSlot(bitmap = b2, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onSlotClick(2) })
            }
        }
        CollageLayoutType.MOODBOARD_FOUR -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1.2f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.dp)
                ) {
                    CollageSlot(bitmap = b0, cornerRadius = cornerRadius, modifier = Modifier.weight(1.5f).fillMaxHeight(), onClick = { onSlotClick(0) })
                    CollageSlot(bitmap = b1, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onSlotClick(1) })
                }
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.dp)
                ) {
                    CollageSlot(bitmap = b2, cornerRadius = cornerRadius, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onSlotClick(2) })
                    CollageSlot(bitmap = b3, cornerRadius = cornerRadius, modifier = Modifier.weight(1.5f).fillMaxHeight(), onClick = { onSlotClick(3) })
                }
            }
        }
    }
}

@Composable
private fun CollageSlot(
    bitmap: Bitmap?,
    cornerRadius: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(DarkStudioSurfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Collage slot",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = "Add Photo",
                tint = Color(0xFF6A7385),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PolaroidCard(
    bitmap: Bitmap?,
    cornerRadius: Float,
    caption: String,
    theme: CollageAestheticTheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(Color.White)
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(maxOf(0f, cornerRadius - 4f).dp))
                .background(Color(0xFFE5E7EB)),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2D35),
                letterSpacing = 1.sp
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * High-resolution canvas rendering of the aesthetic collage.
 */
private suspend fun renderCollageToBitmap(
    layoutType: CollageLayoutType,
    bitmaps: List<Bitmap?>,
    theme: CollageAestheticTheme,
    aspectRatio: CropAspectRatio,
    borderPadding: Float,
    cornerRadius: Float
): Bitmap = withContext(Dispatchers.Default) {
    val targetW = 2160
    val ratio = aspectRatio.ratio ?: (4f / 5f)
    val targetH = (targetW / ratio).toInt()

    val out = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)
    val bgPaint = Paint().apply { color = theme.backgroundColor.toArgb() }
    canvas.drawRect(0f, 0f, targetW.toFloat(), targetH.toFloat(), bgPaint)

    val padPx = (borderPadding / 400f) * targetW
    val radiusPx = (cornerRadius / 400f) * targetW
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val contentRect = RectF(padPx, padPx, targetW - padPx, targetH - padPx)

    fun drawImageInRect(bmp: Bitmap?, rect: RectF) {
        if (bmp == null) return
        val srcRect = Rect(0, 0, bmp.width, bmp.height)
        canvas.save()
        val clipPath = android.graphics.Path().apply {
            addRoundRect(rect, radiusPx, radiusPx, android.graphics.Path.Direction.CW)
        }
        canvas.clipPath(clipPath)
        canvas.drawBitmap(bmp, srcRect, rect, paint)
        canvas.restore()
    }

    when (layoutType) {
        CollageLayoutType.MAGAZINE_DUO -> {
            val halfH = (contentRect.height() - padPx) / 2f
            drawImageInRect(bitmaps.getOrNull(0), RectF(contentRect.left, contentRect.top, contentRect.right, contentRect.top + halfH))
            drawImageInRect(bitmaps.getOrNull(1), RectF(contentRect.left, contentRect.top + halfH + padPx, contentRect.right, contentRect.bottom))
        }
        CollageLayoutType.MODERN_QUAD -> {
            val halfW = (contentRect.width() - padPx) / 2f
            val halfH = (contentRect.height() - padPx) / 2f
            drawImageInRect(bitmaps.getOrNull(0), RectF(contentRect.left, contentRect.top, contentRect.left + halfW, contentRect.top + halfH))
            drawImageInRect(bitmaps.getOrNull(1), RectF(contentRect.left + halfW + padPx, contentRect.top, contentRect.right, contentRect.top + halfH))
            drawImageInRect(bitmaps.getOrNull(2), RectF(contentRect.left, contentRect.top + halfH + padPx, contentRect.left + halfW, contentRect.bottom))
            drawImageInRect(bitmaps.getOrNull(3), RectF(contentRect.left + halfW + padPx, contentRect.top + halfH + padPx, contentRect.right, contentRect.bottom))
        }
        else -> {
            // General multi-split
            val halfW = (contentRect.width() - padPx) / 2f
            drawImageInRect(bitmaps.getOrNull(0), RectF(contentRect.left, contentRect.top, contentRect.left + halfW, contentRect.bottom))
            drawImageInRect(bitmaps.getOrNull(1), RectF(contentRect.left + halfW + padPx, contentRect.top, contentRect.right, contentRect.bottom))
        }
    }

    out
}
