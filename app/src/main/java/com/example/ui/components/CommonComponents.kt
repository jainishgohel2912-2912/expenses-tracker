package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

// A glassmorphism/gradient container card
@Composable
fun FinanceCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val bgColors = if (isDark) {
        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
    } else {
        listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))
    }

    val borderColor = if (isDark) {
        Color(0xFF334155).copy(alpha = 0.5f)
    } else {
        Color(0xFFCBD5E1).copy(alpha = 0.5f)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(bgColors))
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(20.dp),
        content = content
    )
}

// A beautiful progress bar with a gradient filled region
@Composable
fun FinanceProgressBar(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF10B981),
    trackColor: Color = Color(0xFFE2E8F0).copy(alpha = 0.2f)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "progressBar"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(color.copy(alpha = 0.7f), color)
                    )
                )
        )
    }
}

// Custom Pie Chart drawn using Compose Canvas
@Composable
fun SimplePieChart(
    data: List<Pair<String, Double>>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.second }
    if (total <= 0.0) {
        // Empty state chart
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(140.dp)) {
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    radius = size.minDimension / 2,
                    style = Stroke(width = 24.dp.toPx())
                )
            }
            Text(
                "No Data",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    var startAngle = -90f

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val chartSize = size.minDimension
            val strokeWidth = 24.dp.toPx()
            val radius = (chartSize - strokeWidth) / 2

            for (i in data.indices) {
                val value = data[i].second
                val sweepAngle = ((value / total) * 360f).toFloat()
                val color = colors[i % colors.size]

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(chartSize - strokeWidth, chartSize - strokeWidth),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                )

                startAngle += sweepAngle
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Total Spent",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Text(
                String.format("%.0f%%", 100f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Custom Bar Chart drawn using Canvas
@Composable
fun SimpleBarChart(
    data: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF3B82F6),
    isDark: Boolean = true
) {
    val maxVal = data.maxOrNull() ?: 1.0
    val maxDisplay = if (maxVal == 0.0) 1.0 else maxVal
    val textColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barCount = data.size
        
        val spacing = 20.dp.toPx()
        val totalSpacing = spacing * (barCount + 1)
        val barWidth = (width - totalSpacing) / barCount

        for (i in 0 until barCount) {
            val value = data[i]
            val barHeight = ((value / maxDisplay) * (height - 30.dp.toPx())).toFloat()
            
            val left = spacing + i * (barWidth + spacing)
            val top = height - 20.dp.toPx() - barHeight
            val right = left + barWidth
            val bottom = height - 20.dp.toPx()

            // Draw shadow or glow behind bar if desired
            drawRoundRect(
                color = barColor.copy(alpha = 0.1f),
                topLeft = Offset(left, top - 4.dp.toPx()),
                size = Size(barWidth, barHeight + 4.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Draw bar with gradient
            val gradient = Brush.verticalGradient(
                colors = listOf(barColor, barColor.copy(alpha = 0.5f))
            )
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
        }
    }
}

// A beautiful, stylized chips selector
@Composable
fun FilterChipGroup(
    items: List<String>,
    selectedItem: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem
            val bgColor = if (isSelected) {
                Color(0xFF3B82F6)
            } else {
                if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
            }
            val textColor = if (isSelected) {
                Color.White
            } else {
                if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .clickable { onSelected(item) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("chip_$item"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    ),
                    color = textColor
                )
            }
        }
    }
}
