package com.snowdango.amya

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicColorScheme


@Composable
fun AmyaTheme(
    seedColor: Color = Color(0xC5EDFF),
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = rememberDynamicColorScheme(seedColor = seedColor, isDark = darkTheme, isAmoled = false)

    MaterialTheme(
        colorScheme = colorScheme,
    ) {
        content()
    }

}