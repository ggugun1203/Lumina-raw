package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.HistogramData
import com.example.ui.theme.DarkStudioSurfaceVariant

enum class HistogramMode {
    RGB_ALL, RED, GREEN, BLUE, LUMINANCE
}

@Composable
fun HistogramView(
    histogramData: HistogramData,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(HistogramMode.RGB_ALL) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkStudioSurfaceVariant)
            .clickable {
                mode = when (mode) {
                    HistogramMode.RGB_ALL -> HistogramMode.LUMINANCE
                    HistogramMode.LUMINANCE -> HistogramMode.RED
                    HistogramMode.RED -> HistogramMode.GREEN
                    HistogramMode.GREEN -> HistogramMode.BLUE
                    HistogramMode.BLUE -> HistogramMode.RGB_ALL
                }
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("histogram_view")
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val maxCount = maxOf(histogramData.maxBinCount, 1).toFloat()

            fun drawChannel(bins: IntArray, color: Color, fillAlpha: Float = 0.25f) {
                val path = Path()
                path.moveTo(0f, h)
                for (i in 0 until 256) {
                    val x = (i / 255f) * w
                    val normalized = (bins[i] / maxCount).coerceIn(0f, 1f)
                    val y = h - (normalized * (h - 4f))
                    path.lineTo(x, y)
                }
                path.lineTo(w, h)
                path.close()
                drawPath(path, color.copy(alpha = fillAlpha))

                // Line stroke
                val strokePath = Path()
                for (i in 0 until 256) {
                    val x = (i / 255f) * w
                    val normalized = (bins[i] / maxCount).coerceIn(0f, 1f)
                    val y = h - (normalized * (h - 4f))
                    if (i == 0) strokePath.moveTo(x, y) else strokePath.lineTo(x, y)
                }
                drawPath(strokePath, color, style = Stroke(width = 1.5f, cap = StrokeCap.Round))
            }

            when (mode) {
                HistogramMode.RGB_ALL -> {
                    drawChannel(histogramData.redBins, Color(0xFFFF5252), fillAlpha = 0.2f)
                    drawChannel(histogramData.greenBins, Color(0xFF69F0AE), fillAlpha = 0.2f)
                    drawChannel(histogramData.blueBins, Color(0xFF448AFF), fillAlpha = 0.2f)
                }
                HistogramMode.LUMINANCE -> {
                    drawChannel(histogramData.lumBins, Color(0xFFE0E0E0), fillAlpha = 0.35f)
                }
                HistogramMode.RED -> {
                    drawChannel(histogramData.redBins, Color(0xFFFF5252), fillAlpha = 0.4f)
                }
                HistogramMode.GREEN -> {
                    drawChannel(histogramData.greenBins, Color(0xFF69F0AE), fillAlpha = 0.4f)
                }
                HistogramMode.BLUE -> {
                    drawChannel(histogramData.blueBins, Color(0xFF448AFF), fillAlpha = 0.4f)
                }
            }
        }

        // Mode label badge
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mode.name.replace("_", " "),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB0B7C3)
                )
            )
        }
    }
}
