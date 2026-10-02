package com.example.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SamplePhotos
import com.example.model.ColorGrading
import com.example.model.CropAspectRatio
import com.example.model.PhotoEditParams
import com.example.model.ToneCurveParams
import com.example.ui.components.AdjustmentSlider
import com.example.ui.components.AiToneTransferDialog
import com.example.ui.components.CollageStudio
import com.example.ui.components.ExifHud
import com.example.ui.components.ExportDialog
import com.example.ui.components.HistogramView
import com.example.ui.components.HslColorWheel
import com.example.ui.components.PresetBrowser
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkStudioBg
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioSurface
import com.example.ui.theme.DarkStudioSurfaceVariant
import com.example.ui.theme.GoldAccent

@Composable
fun LuminaMainScreen(
    viewModel: LuminaViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val renderedBitmap by viewModel.renderedBitmap.collectAsState()
    val histogramData by viewModel.histogram.collectAsState()
    val customPresets by viewModel.customPresets.collectAsState()

    var showSampleMenu by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.loadUserPhoto(uri)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkStudioBg)
            .testTag("lumina_main_screen"),
        topBar = {
            // Pro Header Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkStudioSurface)
                    .border(width = 0.5.dp, color = DarkStudioBorder)
                    .padding(top = 8.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo and Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showSampleMenu = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "RAW",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp,
                                    color = Color(0xFF14171F)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "LUMINA",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        letterSpacing = 1.sp,
                                        color = Color(0xFFF3F4F6)
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "STUDIO",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Light,
                                        fontSize = 13.sp,
                                        color = GoldAccent
                                    )
                                )
                            }
                            Text(
                                text = "Tap for samples / photos",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    color = Color(0xFF8B93A7)
                                )
                            )
                        }

                        // Sample Dropdown Menu
                        DropdownMenu(
                            expanded = showSampleMenu,
                            onDismissRequest = { showSampleMenu = false },
                            modifier = Modifier.background(DarkStudioSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Open Photo from Device", color = Color(0xFFE2E8F0)) },
                                leadingIcon = {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = GoldAccent)
                                },
                                onClick = {
                                    showSampleMenu = false
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            SamplePhotos.items.forEach { sample ->
                                DropdownMenuItem(
                                    text = { Text(sample.title, color = Color(0xFFE2E8F0)) },
                                    onClick = {
                                        showSampleMenu = false
                                        viewModel.loadSamplePhoto(sample.id)
                                    }
                                )
                            }
                        }
                    }

                    // Top Action Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        // Undo
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = uiState.canUndo,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo",
                                tint = if (uiState.canUndo) Color.White else Color(0xFF4A5162),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Redo
                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = uiState.canRedo,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo",
                                tint = if (uiState.canRedo) Color.White else Color(0xFF4A5162),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Compare Before/After
                        IconButton(
                            onClick = { viewModel.setComparingOriginal(!uiState.isComparingOriginal) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Compare original",
                                tint = if (uiState.isComparingOriginal) GoldAccent else Color(0xFF9EA6B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Histogram toggle
                        IconButton(
                            onClick = { viewModel.toggleHistogram() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Histogram",
                                tint = if (uiState.showHistogram) GoldAccent else Color(0xFF9EA6B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // EXIF HUD toggle
                        IconButton(
                            onClick = { viewModel.toggleExifHud() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "EXIF Info",
                                tint = if (uiState.showExifHud) GoldAccent else Color(0xFF9EA6B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Settings
                        IconButton(
                            onClick = { viewModel.setSettingsDialogVisible(true) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFF9EA6B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Bar: AI Tone Matcher, Collage Studio & Export
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // AI Tone Match Button
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF221F10))
                            .border(1.dp, GoldAccent, RoundedCornerShape(8.dp))
                            .clickable { viewModel.setAiToneDialogVisible(true) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("ai_tone_match_bar_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "AI Tone Match",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = GoldAccent
                                )
                            )
                        }
                    }

                    // Aesthetic Collage Studio Button
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkStudioSurfaceVariant)
                            .border(1.dp, DarkStudioBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.setCollageStudioVisible(true) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("collage_studio_bar_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Collage",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Color(0xFFF3F4F6)
                                )
                            )
                        }
                    }

                    // Export Button
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldAccent)
                            .clickable { viewModel.setExportDialogVisible(true) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("export_bar_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = Color(0xFF14171F),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Export",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF14171F)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Viewport & Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF07080A))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                viewModel.setComparingOriginal(true)
                                tryAwaitRelease()
                                viewModel.setComparingOriginal(false)
                            }
                        )
                    }
                    .testTag("image_viewport"),
                contentAlignment = Alignment.Center
            ) {
                val displayBitmap = if (uiState.isComparingOriginal) {
                    viewModel.getOriginalBitmap()
                } else {
                    renderedBitmap
                }

                Image(
                    bitmap = displayBitmap.asImageBitmap(),
                    contentDescription = "Photo Preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // Comparing "BEFORE" Badge
                if (uiState.isComparingOriginal) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xCC000000))
                            .border(1.dp, GoldAccent, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ORIGINAL (BEFORE)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = GoldAccent
                            )
                        )
                    }
                }

                // Processing Indicator
                if (uiState.isProcessing) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(24.dp)
                    ) {
                        CircularProgressIndicator(
                            color = GoldAccent,
                            strokeWidth = 2.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Overlays: Histogram & EXIF
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AnimatedVisibility(
                        visible = uiState.showHistogram,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        HistogramView(histogramData = histogramData)
                    }

                    AnimatedVisibility(
                        visible = uiState.showExifHud,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        ExifHud(exif = uiState.exifData)
                    }
                }
            }

            // Bottom Pro Tool Tabs Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkStudioSurface)
                    .border(width = 0.5.dp, color = DarkStudioBorder)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditToolTab.entries.forEach { tab ->
                    val isSelected = tab == uiState.activeToolTab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) GoldAccent else Color.Transparent)
                            .clickable { viewModel.selectToolTab(tab) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("tool_tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) Color(0xFF14171F) else Color(0xFF9EA6B8)
                            )
                        )
                    }
                }
            }

            // Bottom Control Deck (Tool Sliders & Panels)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(DarkStudioBg)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                ToolPanelContent(
                    tab = uiState.activeToolTab,
                    params = uiState.currentParams,
                    onParamsChange = { newParams -> viewModel.updateParams(newParams) },
                    onPresetApply = { preset -> viewModel.updateParams(preset.params, pushUndo = true) },
                    onSaveCustomPreset = { name, cat -> viewModel.savePreset(name, cat) },
                    customPresets = customPresets,
                    onResetAll = { viewModel.resetAllAdjustments() }
                )
            }
        }
    }

    // Dialogs
    if (uiState.showAiToneDialog) {
        AiToneTransferDialog(
            onDismiss = { viewModel.setAiToneDialogVisible(false) },
            onApplyTone = { newParams ->
                viewModel.updateParams(newParams, pushUndo = true)
            },
            userApiKey = uiState.userApiKey,
            onOpenSettings = {
                viewModel.setAiToneDialogVisible(false)
                viewModel.setSettingsDialogVisible(true)
            }
        )
    }

    if (uiState.showExportDialog) {
        ExportDialog(
            bitmap = renderedBitmap,
            onDismiss = { viewModel.setExportDialogVisible(false) }
        )
    }

    if (uiState.showCollageStudio) {
        CollageStudio(
            onDismiss = { viewModel.setCollageStudioVisible(false) }
        )
    }

    if (uiState.showSettingsDialog) {
        SettingsDialog(
            currentApiKey = uiState.userApiKey,
            onSaveApiKey = { viewModel.saveUserApiKey(it) },
            onDismiss = { viewModel.setSettingsDialogVisible(false) }
        )
    }
}

@Composable
private fun ToolPanelContent(
    tab: EditToolTab,
    params: PhotoEditParams,
    onParamsChange: (PhotoEditParams) -> Unit,
    onPresetApply: (com.example.model.Preset) -> Unit,
    onSaveCustomPreset: (String, String) -> Unit,
    customPresets: List<com.example.model.Preset>,
    onResetAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        when (tab) {
            EditToolTab.LIGHT -> {
                AdjustmentSlider(
                    name = "Exposure",
                    value = params.exposure,
                    onValueChange = { onParamsChange(params.copy(exposure = it)) },
                    valueRange = -2.0f..2.0f,
                    step = 0.05f,
                    displayFormatter = { "%+.2f EV".format(it) }
                )
                AdjustmentSlider(
                    name = "Contrast",
                    value = params.contrast,
                    onValueChange = { onParamsChange(params.copy(contrast = it)) },
                    valueRange = -100f..100f,
                    step = 1f,
                    displayFormatter = { "%+.0f".format(it) }
                )
                AdjustmentSlider(
                    name = "Highlights",
                    value = params.highlights,
                    onValueChange = { onParamsChange(params.copy(highlights = it)) },
                    valueRange = -100f..100f,
                    step = 1f,
                    displayFormatter = { "%+.0f".format(it) }
                )
                AdjustmentSlider(
                    name = "Shadows",
                    value = params.shadows,
                    onValueChange = { onParamsChange(params.copy(shadows = it)) },
                    valueRange = -100f..100f,
                    step = 1f,
                    displayFormatter = { "%+.0f".format(it) }
                )
                AdjustmentSlider(
                    name = "Whites",
                    value = params.whites,
                    onValueChange = { onParamsChange(params.copy(whites = it)) },
                    valueRange = -100f..100f,
                    step = 1f,
                    displayFormatter = { "%+.0f".format(it) }
                )
                AdjustmentSlider(
                    name = "Blacks",
                    value = params.blacks,
                    onValueChange = { onParamsChange(params.copy(blacks = it)) },
                    valueRange = -100f..100f,
                    step = 1f,
                    displayFormatter = { "%+.0f".format(it) }
                )
            }

            EditToolTab.COLOR -> {
                AdjustmentSlider(
                    name = "Temperature",
                    value = params.temperature,
                    onValueChange = { onParamsChange(params.copy(temperature = it)) },
                    valueRange = -100f..100f,
                    step = 1f,
                    displayFormatter = { if (it > 0) "Warm +%.0f".format(it) else if (it < 0) "Cool %.0f".format(it) else "0" }
                )
                AdjustmentSlider(
                    name = "Tint",
                    value = params.tint,
                    onValueChange = { onParamsChange(params.copy(tint = it)) },
                    valueRange = -100f..100f,
                    step = 1f,
                    displayFormatter = { if (it > 0) "Magenta +%.0f".format(it) else if (it < 0) "Green %.0f".format(it) else "0" }
                )
                AdjustmentSlider(
                    name = "Vibrance",
                    value = params.vibrance,
                    onValueChange = { onParamsChange(params.copy(vibrance = it)) },
                    valueRange = -100f..100f,
                    step = 1f,
                    displayFormatter = { "%+.0f".format(it) }
                )
                AdjustmentSlider(
                    name = "Saturation",
                    value = params.saturation,
                    onValueChange = { onParamsChange(params.copy(saturation = it)) },
                    valueRange = -100f..100f,
                    step = 1f,
                    displayFormatter = { "%+.0f".format(it) }
                )
            }

            EditToolTab.HSL -> {
                HslColorWheel(
                    adjustments = params.hslAdjustments,
                    onAdjustmentChange = { channel, adj ->
                        val newMap = params.hslAdjustments.toMutableMap()
                        newMap[channel] = adj
                        onParamsChange(params.copy(hslAdjustments = newMap))
                    }
                )
            }

            EditToolTab.GRADING -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkStudioSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Split Toning & Color Grading",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF3F4F6)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AdjustmentSlider(
                        name = "Shadows Hue",
                        value = params.colorGrading.shadowHue,
                        onValueChange = { onParamsChange(params.copy(colorGrading = params.colorGrading.copy(shadowHue = it))) },
                        valueRange = 0f..360f,
                        step = 2f,
                        displayFormatter = { "%.0f°".format(it) }
                    )
                    AdjustmentSlider(
                        name = "Shadows Saturation",
                        value = params.colorGrading.shadowSat,
                        onValueChange = { onParamsChange(params.copy(colorGrading = params.colorGrading.copy(shadowSat = it))) },
                        valueRange = 0f..100f,
                        step = 1f,
                        displayFormatter = { "%.0f".format(it) }
                    )
                    AdjustmentSlider(
                        name = "Highlights Hue",
                        value = params.colorGrading.highlightHue,
                        onValueChange = { onParamsChange(params.copy(colorGrading = params.colorGrading.copy(highlightHue = it))) },
                        valueRange = 0f..360f,
                        step = 2f,
                        displayFormatter = { "%.0f°".format(it) }
                    )
                    AdjustmentSlider(
                        name = "Highlights Saturation",
                        value = params.colorGrading.highlightSat,
                        onValueChange = { onParamsChange(params.copy(colorGrading = params.colorGrading.copy(highlightSat = it))) },
                        valueRange = 0f..100f,
                        step = 1f,
                        displayFormatter = { "%.0f".format(it) }
                    )
                }
            }

            EditToolTab.EFFECTS -> {
                AdjustmentSlider(
                    name = "Clarity",
                    value = params.clarity,
                    onValueChange = { onParamsChange(params.copy(clarity = it)) },
                    valueRange = -100f..100f,
                    step = 1f,
                    displayFormatter = { "%+.0f".format(it) }
                )
                AdjustmentSlider(
                    name = "Dehaze",
                    value = params.dehaze,
                    onValueChange = { onParamsChange(params.copy(dehaze = it)) },
                    valueRange = -100f..100f,
                    step = 1f,
                    displayFormatter = { "%+.0f".format(it) }
                )
                AdjustmentSlider(
                    name = "Vignette",
                    value = params.vignette,
                    onValueChange = { onParamsChange(params.copy(vignette = it)) },
                    valueRange = -100f..100f,
                    step = 1f,
                    displayFormatter = { "%+.0f".format(it) }
                )
                AdjustmentSlider(
                    name = "Film Grain",
                    value = params.grain,
                    onValueChange = { onParamsChange(params.copy(grain = it)) },
                    valueRange = 0f..100f,
                    step = 1f,
                    displayFormatter = { "%.0f".format(it) }
                )
                AdjustmentSlider(
                    name = "Sharpen",
                    value = params.sharpen,
                    onValueChange = { onParamsChange(params.copy(sharpen = it)) },
                    valueRange = 0f..100f,
                    step = 1f,
                    displayFormatter = { "%.0f".format(it) }
                )
            }

            EditToolTab.CURVE -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkStudioSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Parametric Tone Curve",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF3F4F6)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AdjustmentSlider(
                        name = "Curve Highlights",
                        value = params.toneCurve.highlights,
                        onValueChange = { onParamsChange(params.copy(toneCurve = params.toneCurve.copy(highlights = it))) },
                        valueRange = -100f..100f,
                        step = 1f
                    )
                    AdjustmentSlider(
                        name = "Curve Lights",
                        value = params.toneCurve.lights,
                        onValueChange = { onParamsChange(params.copy(toneCurve = params.toneCurve.copy(lights = it))) },
                        valueRange = -100f..100f,
                        step = 1f
                    )
                    AdjustmentSlider(
                        name = "Curve Darks",
                        value = params.toneCurve.darks,
                        onValueChange = { onParamsChange(params.copy(toneCurve = params.toneCurve.copy(darks = it))) },
                        valueRange = -100f..100f,
                        step = 1f
                    )
                    AdjustmentSlider(
                        name = "Curve Shadows",
                        value = params.toneCurve.shadows,
                        onValueChange = { onParamsChange(params.copy(toneCurve = params.toneCurve.copy(shadows = it))) },
                        valueRange = -100f..100f,
                        step = 1f
                    )
                }
            }

            EditToolTab.CROP -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkStudioSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Aspect Ratio & Geometry",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF3F4F6)
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Ratio presets
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CropAspectRatio.entries.forEach { ratio ->
                            val isSelected = params.transform.aspectRatio == ratio
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) GoldAccent else DarkStudioBg)
                                    .border(1.dp, if (isSelected) GoldAccent else DarkStudioBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        onParamsChange(params.copy(transform = params.transform.copy(aspectRatio = ratio)))
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = ratio.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF14171F) else Color(0xFFE2E8F0)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rotate & Flip buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val nextRot = (params.transform.rotationDegrees + 90) % 360
                                onParamsChange(params.copy(transform = params.transform.copy(rotationDegrees = nextRot)))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF3F4F6)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkStudioBorder)
                        ) {
                            Icon(Icons.Default.Rotate90DegreesCw, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rotate 90°", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                onParamsChange(params.copy(transform = params.transform.copy(flipHorizontal = !params.transform.flipHorizontal)))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF3F4F6)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkStudioBorder)
                        ) {
                            Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Flip H", fontSize = 11.sp)
                        }
                    }
                }
            }

            EditToolTab.PRESETS -> {
                PresetBrowser(
                    currentParams = params,
                    onApplyPreset = onPresetApply,
                    onSaveCustomPreset = onSaveCustomPreset,
                    customPresets = customPresets
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Reset All Button
        if (params.isModified()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onResetAll() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Reset All Adjustments to Original",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
