package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FlashblockGauge(
  subBlockIndex: Int,
  latencyMs: Long,
  l1DataFeeGwei: Double,
  l2ExecFeeGwei: Double,
  modifier: Modifier = Modifier
) {
  val pulseAnim = remember { Animatable(0.4f) }

  LaunchedEffect(Unit) {
    pulseAnim.animateTo(
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(200, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
      )
    )
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(DarkSurface)
      .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
      .padding(14.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .background(BaseCyan.copy(alpha = pulseAnim.value), CircleShape)
        )
        Text(
          text = "Flashblocks Stream",
          color = TextPrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )
      }

      Box(
        modifier = Modifier
          .background(AccentGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 3.dp)
      ) {
        Text(
          text = "${latencyMs}ms Finality",
          color = AccentGreen,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 10 sub-block indicators (200ms each = 2000ms block)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      val activeSegment = subBlockIndex % 10
      for (i in 0 until 10) {
        val isActive = i <= activeSegment
        val isHead = i == activeSegment

        Box(
          modifier = Modifier
            .weight(1f)
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(
              when {
                isHead -> BaseCyan
                isActive -> BaseBlue
                else -> DarkSurfaceElevated
              }
            )
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Gas & Fee Breakdown: L1 data fee vs L2 exec fee
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          text = "L2 EXECUTION FEE",
          color = TextSecondary,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = String.format("%.3f Gwei (<$0.001)", l2ExecFeeGwei),
          color = TextPrimary,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Medium
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "L1 DATA POST (BLOBS)",
          color = TextSecondary,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = String.format("%.4f Gwei (EIP-4844)", l1DataFeeGwei),
          color = Color(0xFFA5B4FC),
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}
