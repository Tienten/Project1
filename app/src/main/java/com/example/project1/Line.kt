package com.example.project1
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

data class Line(
    val start: Offset,
    val end: Offset,
    val color: Color,
    val strokeWidth: Float
)

data class LineDTO(
    val startX: Float = 0f,
    val startY: Float = 0f,
    val endX: Float = 0f,
    val endY: Float = 0f,
    val color: Long = 0xFF000000,
    val strokeWidth: Float = 10f
)

fun Line.toDTO(): LineDTO = LineDTO(
    startX = start.x,
    startY = start.y,
    endX = end.x,
    endY = end.y,
    color = color.toArgb().toLong(),
    strokeWidth = strokeWidth
)

fun LineDTO.toLine(): Line = Line(
    start = Offset(startX, startY),
    end = Offset(endX, endY),
    color = Color(color.toInt()),
    strokeWidth = strokeWidth
)
