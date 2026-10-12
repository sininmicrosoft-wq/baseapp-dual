package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NetworkChain
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.OpRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ChainSelectorBar(
  selectedChain: NetworkChain,
  onSelectChain: (NetworkChain) -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Row(
    modifier = modifier
      .horizontalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    NetworkChain.allChains.forEach { chain ->
      val isSelected = chain.id == selectedChain.id
      val isOp = chain.id.startsWith("op")

      val bg = if (isSelected) {
        if (isOp) OpRed.copy(alpha = 0.18f) else BaseBlue.copy(alpha = 0.22f)
      } else DarkSurface

      val borderColor = if (isSelected) {
        if (isOp) OpRed else BaseBlue
      } else DarkSurfaceBorder

      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(bg)
          .border(1.dp, borderColor, RoundedCornerShape(20.dp))
          .clickable { onSelectChain(chain) }
          .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Chain emblem dot
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(if (isOp) OpRed else if (chain.id == "vibenet") Color(0xFFEAB308) else BaseBlue, CircleShape)
        )

        Text(
          text = chain.name,
          color = if (isSelected) TextPrimary else TextSecondary,
          fontSize = 12.sp,
          fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )

        if (chain.isTestnet) {
          Box(
            modifier = Modifier
              .background(DarkSurfaceElevated, RoundedCornerShape(4.dp))
              .padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Text(
              text = if (chain.id == "vibenet") "SANDBOX" else "TEST",
              color = Color(0xFFA5B4FC),
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
