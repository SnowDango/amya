package com.snowdango.amya.feature.all

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AllViewScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(text = "All View")
    }
}