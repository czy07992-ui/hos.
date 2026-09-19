package com.example.flydigicooler.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flydigicooler.ui.CoolerMode
import com.example.flydigicooler.ui.theme.CoolerTheme
import kotlin.math.atan2
import kotlin.math.roundToInt

data class DialOption(
    val id: Int,
    val mode: CoolerMode?,
    val label: String,
    val icon: ImageVector,
    val targetAngle: Float
)

val DIAL_OPTIONS = listOf(
    DialOption(0, CoolerMode.SILENT, "静音", Icons.Default.VolumeMute, 0f),
    DialOption(1, CoolerMode.AUTO, "极智", Icons.Default.AutoAwesome, 90f),
    DialOption(2, CoolerMode.OVERCLOCK, "极寒", Icons.Default.AcUnit, 180f),
    DialOption(3, null, "关闭", Icons.Default.PowerSettingsNew, 270f)
)

@Composable
fun RotaryCoolerDial(
    temperature: Float,
    fanRpm: Int,
    currentMode: CoolerMode?,
    onModeChanged: (CoolerMode?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CoolerTheme.colors
    val view = LocalView.current
    var rotationAngle by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(currentMode) {
        if (!isDragging) {
            val target = DIAL_OPTIONS.firstOrNull { it.mode == currentMode }?.targetAngle ?: 0f
            rotationAngle = -target
        }
    }

    val animatedAngle by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dial_snap"
    )

    Box(
        modifier = modifier
            .size(310.dp)
            .pointerInput(Unit) {
                val center = Offset(size.width / 2f, size.height / 2f)
                var lastTouchAngle = 0f

                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        lastTouchAngle = Math.toDegrees(
                            atan2((offset.y - center.y).toDouble(), (offset.x - center.x).toDouble())
                        ).toFloat()
                    },
                    onDragEnd = {
                        isDragging = false
                        val normalized = (-rotationAngle) % 360f
                        val positiveAngle = if (normalized < 0) normalized + 360f else normalized
                        val nearestIndex = ((positiveAngle / 90f).roundToInt()) % 4
                        val snappedOption = DIAL_OPTIONS[nearestIndex]

                        rotationAngle = -snappedOption.targetAngle
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        onModeChanged(snappedOption.mode)
                    },
                    onDrag = { change, _ ->
                        val currentTouchAngle = Math.toDegrees(
                            atan2((change.position.y - center.y).toDouble(), (change.position.x - center.x).toDouble())
                        ).toFloat()

                        var delta = currentTouchAngle - lastTouchAngle
                        if (delta > 180f) delta -= 360f
                        if (delta < -180f) delta += 360f

                        rotationAngle += delta
                        lastTouchAngle = currentTouchAngle
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 顶部固定对准游标
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-4).dp)
                .size(width = 6.dp, height = 18.dp)
                .clip(CircleShape)
                .background(colors.accentPrimary)
        )

        // 旋转刻度线
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .rotate(if (isDragging) rotationAngle else animatedAngle)
        ) {
            val strokeWidth = 2.dp.toPx()
            drawCircle(
                color = colors.dialTrackColor,
                style = Stroke(width = strokeWidth)
            )

            for (i in 0 until 36) {
                val angleRad = Math.toRadians((i * 10).toDouble())
                val isMajorTick = i % 9 == 0
                val innerR = size.width / 2 - if (isMajorTick) 16.dp.toPx() else 10.dp.toPx()
                val outerR = size.width / 2 - 2.dp.toPx()

                val start = Offset(
                    x = (center.x + innerR * kotlin.math.cos(angleRad)).toFloat(),
                    y = (center.y + innerR * kotlin.math.sin(angleRad)).toFloat()
                )
                val end = Offset(
                    x = (center.x + outerR * kotlin.math.cos(angleRad)).toFloat(),
                    y = (center.y + outerR * kotlin.math.sin(angleRad)).toFloat()
                )

                drawLine(
                    color = if (isMajorTick) colors.dialTickMajor else colors.dialTickMinor,
                    start = start,
                    end = end,
                    strokeWidth = if (isMajorTick) 3.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // 4 个功能节点
        val currentDisplayAngle = if (isDragging) rotationAngle else animatedAngle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(currentDisplayAngle)
        ) {
            DIAL_OPTIONS.forEach { option ->
                val angleRad = Math.toRadians((option.targetAngle - 90).toDouble())
                val radius = 105.dp
                val isSelected = currentMode == option.mode

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(
                            x = radius * kotlin.math.cos(angleRad).toFloat(),
                            y = radius * kotlin.math.sin(angleRad).toFloat()
                        )
                        .rotate(-currentDisplayAngle),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(colors.dialButtonBg)
                                .border(1.dp, colors.dialButtonBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = option.label,
                                tint = if (isSelected) colors.accentPrimary else colors.textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = option.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) colors.textPrimary else colors.textSecondary
                        )
                    }
                }
            }
        }

        // 中心温控圆盘
        Box(
            modifier = Modifier
                .size(142.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x18000000),
                    spotColor = Color(0x18000000)
                )
                .clip(CircleShape)
                .background(colors.hubBackground)
                .border(
                    width = 1.5.dp,
                    brush = colors.hubBorderBrush,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${String.format("%.1f", temperature)}°C",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = if (temperature < 15f) colors.lowTempColor else colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$fanRpm RPM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
            }
        }
    }
}