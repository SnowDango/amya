package com.snowdango.amya.feature.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Badge
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.aboutlibraries.entity.Library
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun LicensesViewScreen(
    modifier: Modifier = Modifier,
    viewModel: LicensesViewModel = koinViewModel()
) {
    val libraries = viewModel.libraries.collectAsStateWithLifecycle()
    Box(
        modifier = modifier
            .padding(all = 16.dp)
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(libraries.value.distinctBy { it.uniqueId }) {
                LibraryCard(
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .fillMaxWidth(),
                    library = it,
                )
            }
        }
    }
}

@Composable
fun LibraryCard(
    modifier: Modifier = Modifier,
    library: Library,
) {
    Column(
        modifier = modifier
            .padding(all = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = 4.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = library.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .fillMaxWidth()
                    .weight(1f),
            )
            Text(
                text = library.artifactVersion.orEmpty(),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                modifier = Modifier
                    .wrapContentWidth()
            )
        }
        Text(
            text = library.developers.firstOrNull()?.name.orEmpty(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            library.licenses.forEach {
                Badge(
                    modifier = Modifier
                        .padding(end = 8.dp),
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = it.name,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(all = 2.dp)
                    )
                }
            }
        }
    }
}