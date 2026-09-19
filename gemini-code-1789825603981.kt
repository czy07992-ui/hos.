package com.example.flydigicooler.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flydigicooler.ui.components.GlassCard
import com.example.flydigicooler.ui.components.GlowBackground
import com.example.flydigicooler.ui.components.RotaryCoolerDial
import com.example.flydigicooler.ui.theme.CoolerTheme

@Composable
fun CoolerControlScreen(
    viewModel: CoolerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    CoolerControlContent(
        uiState = state,
        onModeSelected = { mode -> mode?.let { viewModel.setMode(it) } },
        onRgbToggled = { viewModel.toggleRgb(it) },
        modifier = modifier
    )
}

@Composable
fun CoolerControlContent(
    uiState: CoolerUiState,
    onModeSelected: (CoolerMode?) -> Unit,
    onRgbToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CoolerTheme.colors

    GlowBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 顶部磨砂连接卡
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.dialButtonBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BluetoothConnected,
                            contentDescription = null,
                            tint = if (uiState.isConnected) colors.accentPrimary else colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = uiState.deviceName,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (uiState.isConnected) "蓝牙已连接 · 握手正常" else uiState.statusText,
                            color = if (uiState.isConnected) colors.accentPrimary else colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 旋转控制轮盘
            RotaryCoolerDial(
                temperature = uiState.currentTemperature,
                fanRpm = uiState.fanSpeedRpm,
                currentMode = uiState.currentMode,
                onModeChanged = onModeSelected
            )

            Spacer(modifier = Modifier.height(44.dp))

            // RGB 氛围灯卡片
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.isRgbEnabled) {
                                        Brush.sweepGradient(
                                            listOf(
                                                Color(0xFFFF0055),
                                                Color(0xFFFFEE00),
                                                Color(0xFF00FF66),
                                                Color(0xFF00E5FF),
                                                Color(0xFF7000FF),
                                                Color(0xFFFF0055)
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(listOf(Color(0xFF707786), Color(0xFF707786)))
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "RGB",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "RGB 氛围灯效",
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (uiState.isRgbEnabled) "炫彩跑马灯已激活" else "灯效已关闭",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Switch(
                        checked = uiState.isRgbEnabled,
                        onCheckedChange = onRgbToggled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.accentPrimary,
                            uncheckedTrackColor = colors.switchTrackUnchecked,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}