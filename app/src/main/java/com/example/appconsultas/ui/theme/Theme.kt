package com.example.appconsultas.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Mapeamento do Tema Escuro (dark_green) ---
private val DarkColorScheme = darkColorScheme(
    primary = dark_green_button_bg,       // Cor de Botões, FABs
    onPrimary = dark_green_selected_fg,   // Texto em cima dos Botões

    secondary = dark_green_selected_bg,   // Cor de Seleção
    onSecondary = dark_green_selected_fg, // Texto em cima da Seleção

    tertiary = dark_green_selected_bg,    // Cor de Destaque
    onTertiary = dark_green_selected_fg,  // Texto em cima do Destaque

    background = dark_green_bg,           // Fundo da App
    onBackground = dark_green_fg,         // Texto no Fundo da App

    surface = dark_green_alt_bg,          // Fundo de Cartões, Menus
    onSurface = dark_green_fg,            // Texto em cima de Cartões

    surfaceVariant = dark_green_entry_bg, // Fundo de Campos de Texto
    onSurfaceVariant = dark_green_fg      // Texto em Campos de Texto
)

// --- Mapeamento do Tema Claro (light_green) ---
private val LightColorScheme = lightColorScheme(
    primary = light_green_selected_bg,    // Cor de Botões, FABs
    onPrimary = light_green_selected_fg,  // Texto em cima dos Botões

    secondary = light_green_selected_bg,  // Cor de Seleção
    onSecondary = light_green_selected_fg,// Texto em cima da Seleção

    tertiary = light_green_selected_bg,   // Cor de Destaque
    onTertiary = light_green_selected_fg, // Texto em cima do Destaque

    background = light_green_bg,          // Fundo da App
    onBackground = light_green_fg,        // Texto no Fundo da App

    surface = light_green_alt_bg,         // Fundo de Cartões, Menus
    onSurface = light_green_fg,           // Texto em cima de Cartões

    surfaceVariant = light_green_entry_bg,// Fundo de Campos de Texto
    onSurfaceVariant = light_green_fg     // Texto em Campos de Texto
)

@Composable
fun AppConsultasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // IMPORTANTE: Mudei isto para 'false' para FORÇAR o seu tema
    // em vez de usar as cores do wallpaper do telemóvel.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme // Usa o seu 'dark_green'
        else -> LightColorScheme // Usa o seu 'light_green'
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}