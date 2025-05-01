package com.snowdango.amya


import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun App() {
    AmyaTheme {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            SideNavigation (
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    text = "Side Navigation",
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Scaffold(
                modifier = Modifier.fillMaxSize()
                    .weight(1f),
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(all = 16.dp)
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(16.dp),
                        ),
                ){

                }
            }
        }
    }
}


@Composable
fun SideNavigation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val width = with(LocalDensity.current) { 200.toDp() }
    Column(
        modifier = modifier
            .padding(
                top = 32.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
            )
            .fillMaxHeight()
            .width(width)
    ) {
        content()
    }
}