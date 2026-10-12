package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = BaseBlue,
  onPrimary = Color.White,
  primaryContainer = BaseBlueDark,
  onPrimaryContainer = Color(0xFFD6E4FF),
  secondary = BaseCyan,
  onSecondary = Color(0xFF00363F),
  secondaryContainer = Color(0xFF004F5C),
  onSecondaryContainer = Color(0xFFA6EEFF),
  tertiary = OpRed,
  onTertiary = Color.White,
  tertiaryContainer = OpRedDark,
  onTertiaryContainer = Color(0xFFFFDAD6),
  background = DarkBg,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = TextSecondary,
  outline = DarkSurfaceBorder,
  outlineVariant = Color(0xFF1E2842),
  error = Color(0xFFFF5449),
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = BaseBlue,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFD6E4FF),
  onPrimaryContainer = BaseBlueDark,
  secondary = BaseBlueLight,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFD4E3FF),
  onSecondaryContainer = Color(0xFF001B3F),
  tertiary = OpRed,
  onTertiary = Color.White,
  background = Color(0xFFF8FAFC),
  onBackground = Color(0xFF0F172A),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF0F172A),
  surfaceVariant = Color(0xFFE2E8F0),
  onSurfaceVariant = Color(0xFF475569),
  outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek crypto dark theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
