package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BaseBlue
import kotlin.random.Random

/**
 * Procedural high-contrast QR visualizer generating deterministic QR patterns
 * based on the input text payload with finder patterns and central Base emblem.
 */
@Composable
fun BaseQrCodeView(
  payload: String,
  modifier: Modifier = Modifier,
  sizeDp: Dp = 180.dp,
  primaryColor: Color = Color(0xFF0F172A),
  accentColor: Color = BaseBlue
) {
  val grid = remember(payload) {
    val seed = payload.hashCode().toLong()
    val rng = Random(seed)
    val matrixSize = 21 // Standard Version 1 QR matrix size
    val data = Array(matrixSize) { BooleanArray(matrixSize) }

    for (r in 0 until matrixSize) {
      for (c in 0 until matrixSize) {
        // Reserved corner finder patterns (7x7)
        val isTopLeft = r < 7 && c < 7
        val isTopRight = r < 7 && c >= matrixSize - 7
        val isBottomLeft = r >= matrixSize - 7 && c < 7
        val isCenter = r in 9..11 && c in 9..11

        if (isTopLeft || isTopRight || isBottomLeft || isCenter) {
          data[r][c] = false
        } else {
          data[r][c] = rng.nextBoolean()
        }
      }
    }
    data
  }

  Box(
    modifier = modifier
      .size(sizeDp)
      .background(Color.White, RoundedCornerShape(16.dp))
      .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp)),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(sizeDp - 28.dp)) {
      val matrixSize = 21
      val cellSize = size.width / matrixSize

      // Draw modules
      for (r in 0 until matrixSize) {
        for (c in 0 until matrixSize) {
          if (grid[r][c]) {
            drawRoundRect(
              color = primaryColor,
              topLeft = Offset(c * cellSize + 0.5f, r * cellSize + 0.5f),
              size = Size(cellSize - 1f, cellSize - 1f),
              cornerRadius = CornerRadius(2f, 2f)
            )
          }
        }
      }

      // Draw 3 Finder Patterns
      val drawFinder = { ox: Float, oy: Float ->
        // Outer 7x7
        drawRoundRect(
          color = primaryColor,
          topLeft = Offset(ox, oy),
          size = Size(7 * cellSize, 7 * cellSize),
          cornerRadius = CornerRadius(6f, 6f)
        )
        // White inner 5x5
        drawRoundRect(
          color = Color.White,
          topLeft = Offset(ox + cellSize, oy + cellSize),
          size = Size(5 * cellSize, 5 * cellSize),
          cornerRadius = CornerRadius(4f, 4f)
        )
        // Solid center 3x3
        drawRoundRect(
          color = accentColor,
          topLeft = Offset(ox + 2 * cellSize, oy + 2 * cellSize),
          size = Size(3 * cellSize, 3 * cellSize),
          cornerRadius = CornerRadius(3f, 3f)
        )
      }

      drawFinder(0f, 0f)
      drawFinder((matrixSize - 7) * cellSize, 0f)
      drawFinder(0f, (matrixSize - 7) * cellSize)

      // Center Base Blue Badge
      val centerPx = size.width / 2f
      val badgeRadius = 2.2f * cellSize
      drawCircle(
        color = Color.White,
        radius = badgeRadius,
        center = Offset(centerPx, centerPx)
      )
      drawCircle(
        color = BaseBlue,
        radius = badgeRadius - 3f,
        center = Offset(centerPx, centerPx)
      )
      drawCircle(
        color = Color.White,
        radius = badgeRadius - 7f,
        center = Offset(centerPx + 2f, centerPx - 2f)
      )
    }
  }
}
