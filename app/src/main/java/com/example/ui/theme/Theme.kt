package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = EduBlueDarkTheme,
    onPrimary = Color(0xFF00227B),
    primaryContainer = EduBlueDark,
    onPrimaryContainer = EduBlueLight,
    secondary = EduAmberDarkTheme,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = EduAmberDark,
    onSecondaryContainer = EduAmberLight,
    tertiary = EduGreenDarkTheme,
    background = EduBackgroundDark,
    surface = EduSurfaceDark,
    surfaceVariant = EduSurfaceVariantDark,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EduBluePrimary,
    onPrimary = Color.White,
    primaryContainer = EduBlueLight,
    onPrimaryContainer = EduBlueDark,
    secondary = EduAmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = EduAmberLight,
    onSecondaryContainer = EduAmberDark,
    tertiary = EduGreenSuccess,
    background = EduBackgroundLight,
    surface = EduSurfaceLight,
    surfaceVariant = EduSurfaceVariant,
    onBackground = EduTextPrimary,
    onSurface = EduTextPrimary,
    onSurfaceVariant = EduTextSecondary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
