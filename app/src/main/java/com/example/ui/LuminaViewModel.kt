package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PresetEntity
import com.example.data.UserPreferences
import com.example.engine.HistogramData
import com.example.engine.ImageProcessor
import com.example.engine.SamplePhotos
import com.example.model.CropAspectRatio
import com.example.model.ExifData
import com.example.model.HslColorAdjustment
import com.example.model.HslColorChannel
import com.example.model.PhotoEditParams
import com.example.model.Preset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

enum class EditToolTab(val title: String) {
    LIGHT("Light"),
    COLOR("Color"),
    HSL("HSL"),
    GRADING("Grading"),
    EFFECTS("Effects"),
    CURVE("Curve"),
    CROP("Crop"),
    PRESETS("Presets")
}

data class LuminaUiState(
    val currentParams: PhotoEditParams = PhotoEditParams(),
    val activeToolTab: EditToolTab = EditToolTab.LIGHT,
    val exifData: ExifData = ExifData(),
    val isComparingOriginal: Boolean = false,
    val showExifHud: Boolean = true,
    val showHistogram: Boolean = true,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val showAiToneDialog: Boolean = false,
    val showExportDialog: Boolean = false,
    val showCollageStudio: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val isProcessing: Boolean = false,
    val userApiKey: String = ""
)

class LuminaViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefs = UserPreferences(application)
    private val presetDao = AppDatabase.getDatabase(application).presetDao()

    private val _uiState = MutableStateFlow(
        LuminaUiState(userApiKey = userPrefs.customGeminiApiKey)
    )
    val uiState: StateFlow<LuminaUiState> = _uiState.asStateFlow()

    private var originalBitmap: Bitmap = SamplePhotos.items[0].generator(1200, 800)
    
    private val _renderedBitmap = MutableStateFlow<Bitmap>(originalBitmap)
    val renderedBitmap: StateFlow<Bitmap> = _renderedBitmap.asStateFlow()

    private val _histogram = MutableStateFlow(HistogramData())
    val histogram: StateFlow<HistogramData> = _histogram.asStateFlow()

    private val _customPresets = MutableStateFlow<List<Preset>>(emptyList())
    val customPresets: StateFlow<List<Preset>> = _customPresets.asStateFlow()

    // Undo / Redo history
    private val undoStack = mutableListOf<PhotoEditParams>()
    private val redoStack = mutableListOf<PhotoEditParams>()

    private var filterJob: Job? = null

    init {
        _uiState.update { it.copy(exifData = SamplePhotos.items[0].exif) }
        loadCustomPresets()
        updateRenderedImage(immediate = true)
    }

    private fun loadCustomPresets() {
        viewModelScope.launch {
            presetDao.getAllPresets().collect { entities ->
                val presets = entities.mapNotNull { entity ->
                    try {
                        val params = deserializeParams(entity.paramsJson)
                        Preset(
                            id = entity.id,
                            name = entity.name,
                            category = entity.category,
                            description = entity.description,
                            params = params,
                            isCustom = true
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                _customPresets.value = presets
            }
        }
    }

    fun selectToolTab(tab: EditToolTab) {
        _uiState.update { it.copy(activeToolTab = tab) }
    }

    fun toggleExifHud() {
        _uiState.update { it.copy(showExifHud = !it.showExifHud) }
    }

    fun toggleHistogram() {
        _uiState.update { it.copy(showHistogram = !it.showHistogram) }
    }

    fun setComparingOriginal(comparing: Boolean) {
        _uiState.update { it.copy(isComparingOriginal = comparing) }
    }

    fun setAiToneDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showAiToneDialog = visible) }
    }

    fun setExportDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showExportDialog = visible) }
    }

    fun setCollageStudioVisible(visible: Boolean) {
        _uiState.update { it.copy(showCollageStudio = visible) }
    }

    fun setSettingsDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = visible) }
    }

    fun saveUserApiKey(key: String) {
        userPrefs.customGeminiApiKey = key
        _uiState.update { it.copy(userApiKey = key) }
    }

    /**
     * Updates an edit parameter and queues real-time filter rendering.
     */
    fun updateParams(newParams: PhotoEditParams, pushUndo: Boolean = false) {
        val current = _uiState.value.currentParams
        if (pushUndo && current != newParams) {
            undoStack.add(current)
            redoStack.clear()
        }
        _uiState.update {
            it.copy(
                currentParams = newParams,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        updateRenderedImage(immediate = pushUndo)
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.size - 1)
            redoStack.add(_uiState.value.currentParams)
            _uiState.update {
                it.copy(
                    currentParams = previous,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty()
                )
            }
            updateRenderedImage(immediate = true)
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.size - 1)
            undoStack.add(_uiState.value.currentParams)
            _uiState.update {
                it.copy(
                    currentParams = next,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty()
                )
            }
            updateRenderedImage(immediate = true)
        }
    }

    fun resetAllAdjustments() {
        updateParams(PhotoEditParams(), pushUndo = true)
    }

    fun loadSamplePhoto(sampleId: String) {
        val sample = SamplePhotos.items.find { it.id == sampleId } ?: SamplePhotos.items[0]
        originalBitmap = sample.generator(1600, 1066)
        undoStack.clear()
        redoStack.clear()
        _uiState.update {
            it.copy(
                exifData = sample.exif,
                currentParams = PhotoEditParams(),
                canUndo = false,
                canRedo = false
            )
        }
        updateRenderedImage(immediate = true)
    }

    fun loadUserPhoto(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val src = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(src) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }

                originalBitmap = bmp
                undoStack.clear()
                redoStack.clear()

                val mp = (bmp.width * bmp.height / 1000000f)
                val exif = ExifData(
                    cameraMake = "Digital Camera",
                    cameraModel = "Raw Import",
                    lensModel = "Standard Lens",
                    dimensions = "${bmp.width} × ${bmp.height}",
                    megapixels = "%.1f MP".format(mp),
                    isRaw = true
                )

                _uiState.update {
                    it.copy(
                        exifData = exif,
                        currentParams = PhotoEditParams(),
                        canUndo = false,
                        canRedo = false
                    )
                }
                updateRenderedImage(immediate = true)
            } catch (e: Exception) {
                // Keep current bitmap
            }
        }
    }

    fun getOriginalBitmap(): Bitmap = originalBitmap

    fun savePreset(name: String, category: String) {
        viewModelScope.launch {
            val params = _uiState.value.currentParams
            val json = serializeParams(params)
            val entity = PresetEntity(
                id = "custom_" + System.currentTimeMillis(),
                name = name,
                category = category,
                description = "User custom adjustment preset",
                paramsJson = json
            )
            presetDao.insertPreset(entity)
        }
    }

    private fun updateRenderedImage(immediate: Boolean = false) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch(Dispatchers.Default) {
            if (!immediate) {
                delay(16) // ~60fps throttle for ultra smooth sliding
            }
            _uiState.update { it.copy(isProcessing = true) }
            val params = _uiState.value.currentParams
            val processed = ImageProcessor.applyAdjustments(originalBitmap, params)
            val hist = ImageProcessor.computeHistogram(processed)

            _renderedBitmap.value = processed
            _histogram.value = hist
            _uiState.update { it.copy(isProcessing = false) }
        }
    }

    private fun serializeParams(p: PhotoEditParams): String {
        return JSONObject().apply {
            put("exposure", p.exposure.toDouble())
            put("contrast", p.contrast.toDouble())
            put("highlights", p.highlights.toDouble())
            put("shadows", p.shadows.toDouble())
            put("whites", p.whites.toDouble())
            put("blacks", p.blacks.toDouble())
            put("temperature", p.temperature.toDouble())
            put("tint", p.tint.toDouble())
            put("vibrance", p.vibrance.toDouble())
            put("saturation", p.saturation.toDouble())
            put("clarity", p.clarity.toDouble())
            put("dehaze", p.dehaze.toDouble())
            put("vignette", p.vignette.toDouble())
            put("grain", p.grain.toDouble())
            put("sharpen", p.sharpen.toDouble())
        }.toString()
    }

    private fun deserializeParams(jsonStr: String): PhotoEditParams {
        val root = JSONObject(jsonStr)
        return PhotoEditParams(
            exposure = root.optDouble("exposure", 0.0).toFloat(),
            contrast = root.optDouble("contrast", 0.0).toFloat(),
            highlights = root.optDouble("highlights", 0.0).toFloat(),
            shadows = root.optDouble("shadows", 0.0).toFloat(),
            whites = root.optDouble("whites", 0.0).toFloat(),
            blacks = root.optDouble("blacks", 0.0).toFloat(),
            temperature = root.optDouble("temperature", 0.0).toFloat(),
            tint = root.optDouble("tint", 0.0).toFloat(),
            vibrance = root.optDouble("vibrance", 0.0).toFloat(),
            saturation = root.optDouble("saturation", 0.0).toFloat(),
            clarity = root.optDouble("clarity", 0.0).toFloat(),
            dehaze = root.optDouble("dehaze", 0.0).toFloat(),
            vignette = root.optDouble("vignette", 0.0).toFloat(),
            grain = root.optDouble("grain", 0.0).toFloat(),
            sharpen = root.optDouble("sharpen", 0.0).toFloat()
        )
    }
}
