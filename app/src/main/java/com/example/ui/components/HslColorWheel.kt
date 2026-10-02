package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HslColorAdjustment
import com.example.model.HslColorChannel
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioSurfaceVariant

private val channelColors = mapOf(
    HslColorChannel.RED to Color(0xFFFF453A),
    HslColorChannel.ORANGE to Color(0xFFFF9F0A),
    HslColorChannel.YELLOW to Color(0xFFFFD60A),
    HslColorChannel.GREEN to Color(0xFF30D158),
    HslColorChannel.AQUA to Color(0xFF64D2FF),
    HslColorChannel.BLUE to Color(0xFF0A84FF),
    HslColorChannel.PURPLE to Color(0xFFBF5AF2),
    HslColorChannel.MAGENTA to Color(0xFFFF375F)
)

@Composable
fun HslColorWheel(
    adjustments: Map<HslColorChannel, HslColorAdjustment>,
    onAdjustmentChange: (HslColorChannel, HslColorAdjustment) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedChannel by remember { mutableStateOf(HslColorChannel.RED) }
    val currentAdj = adjustments[selectedChannel] ?: HslColorAdjustment()
    val swatchColor = channelColors[selectedChannel] ?: Color.White

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkStudioSurfaceVariant)
            .padding(12.dp)
            .testTag("hsl_mixer_panel")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HSL Color Mixer",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF3F4F6)
                )
            )
            Text(
                text = selectedChannel.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = swatchColor
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 8 Swatch buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (channel in HslColorChannel.entries) {
                val color = channelColors[channel] ?: Color.Gray
                val isSelected = channel == selectedChannel
                val hasEdit = (adjustments[channel]?.let { it.hue != 0f || it.saturation != 0f || it.luminance != 0f } == true)

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else if (hasEdit) 2.dp else 1.dp,
                            color = if (isSelected) Color.White else if (hasEdit) Color.Yellow else DarkStudioBorder,
                            shape = CircleShape
                        )
                        .clickable { selectedChannel = channel }
                        .testTag("hsl_swatch_${channel.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasEdit) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sliders for selected channel
        AdjustmentSlider(
            name = "${selectedChannel.label} Hue",
            value = currentAdj.hue,
            onValueChange = { onAdjustmentChange(selectedChannel, currentAdj.copy(hue = it)) },
            valueRange = -100f..100f,
            accentColor = swatchColor,
            testTag = "hsl_hue_slider"
        )

        AdjustmentSlider(
            name = "${selectedChannel.label} Saturation",
            value = currentAdj.saturation,
            onValueChange = { onAdjustmentChange(selectedChannel, currentAdj.copy(saturation = it)) },
            valueRange = -100f..100f,
            accentColor = swatchColor,
            testTag = "hsl_sat_slider"
        )

        AdjustmentSlider(
            name = "${selectedChannel.label} Luminance",
            value = currentAdj.luminance,
            onValueChange = { onAdjustmentChange(selectedChannel, currentAdj.copy(luminance = it)) },
            valueRange = -100f..100f,
            accentColor = swatchColor,
            testTag = "hsl_lum_slider"
        )
    }
}
