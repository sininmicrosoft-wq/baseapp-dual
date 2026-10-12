package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CodeSnippetBox(
  title: String,
  language: String,
  code: String,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val horizScroll = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(DarkBg)
      .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
  ) {
    // Header Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF0F1422))
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .background(BaseBlue.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = language.uppercase(),
            color = BaseBlue,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Text(
          text = title,
          color = TextSecondary,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
      }

      IconButton(
        onClick = {
          val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
          cm.setPrimaryClip(ClipData.newPlainText(title, code))
          Toast.makeText(context, "Snippet copied to clipboard!", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.size(28.dp)
      ) {
        Icon(
          imageVector = Icons.Default.ContentCopy,
          contentDescription = "Copy code",
          tint = TextSecondary,
          modifier = Modifier.size(15.dp)
        )
      }
    }

    // Code Body
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(horizScroll)
        .padding(12.dp)
    ) {
      Text(
        text = code,
        color = TextPrimary,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        lineHeight = 16.sp
      )
    }
  }
}
