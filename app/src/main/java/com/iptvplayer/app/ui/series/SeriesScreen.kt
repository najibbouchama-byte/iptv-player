package com.iptvplayer.app.ui.series

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.ui.common.CategorySelectorButton
import com.iptvplayer.app.ui.common.PosterCard

@Composable
fun SeriesScreen(
    viewModel: SeriesViewModel = hiltViewModel(),
    onPlayEpisodes: (List<Channel>, Int) -> Unit
) {
    var selectedSeriesId by rememberSaveable { mutableStateOf<String?>(null) }

    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val seriesList by viewModel.series.collectAsState()

    val selectedSeries = seriesList.find { it.id == selectedSeriesId }

    if (selectedSeries != null) {
        SeriesDetailScreen(
            series = selectedSeries,
            onBack = { selectedSeriesId = null },
            onPlayEpisodes = onPlayEpisodes
        )
        return
    }

    LaunchedEffect(categories) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            viewModel.selectCategory(categories.first())
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Séries", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        if (categories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        CategorySelectorButton(
            categories = categories,
            selectedCategory = selectedCategory,
            placeholder = "Catégories",
            onSelect = { viewModel.selectCategory(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (seriesList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucune série dans cette catégorie", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(seriesList, key = { it.id }) { series ->
                    PosterCard(
                        title = series.name,
                        posterUrl = series.posterUrl,
                        showPoster = true,
                        onClick = { selectedSeriesId = series.id }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
