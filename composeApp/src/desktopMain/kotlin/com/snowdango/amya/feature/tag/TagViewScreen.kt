package com.snowdango.amya.feature.tag

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TagViewScreen(
    tagId: Long,
    subTagId: Long?,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(text = "Tag View $tagId, SubTag $subTagId")
    }
}