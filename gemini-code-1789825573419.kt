package com.example.flydigicooler.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.flydigicooler.ui.theme.CoolerTheme

@Composable
fun GlowBackground(
    content: @Composable BoxScope.() -> Unit
) {
    val colors = CoolerTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = (-20).dp)
                .size(260.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(colors.glowColor1, Color.Transparent)
                    )
                )
                .blur(90.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 70.dp, y = 150.dp)
                .size(300.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(colors.glowColor2, Color.Transparent)
                    )
                )
                .blur(100.dp)
        )

        content()
    }
}