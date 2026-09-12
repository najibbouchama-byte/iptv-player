package com.iptvplayer.app.ui.movies

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.ui.common.CategorySelectorButton
import com.iptvplayer.app.ui.common.PosterCard
import com.iptvplayer.app.ui.common.gridFocusScale

@Composable
fun MoviesScreen(
    viewModel: MoviesViewModel = hiltViewModel(),
    onChannelClick: (Channel) -> Unit
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val movies by viewModel.movies.collectAsState()
    val myListIds by viewModel.myListIds.collectAsState()

    LaunchedEffect(categories) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            viewModel.selectCategory(categories.first())
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Films", style = MaterialTheme.typography.titleLarge)
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
        } else if (movies.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun film dans cette catégorie", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(movies, key = { it.id }) { movie ->
                    val inMyList = myListIds.contains(movie.id)
                    PosterCard(
                        title = movie.name,
                        posterUrl = movie.posterUrl,
                        showPoster = true,
                        onClick = {
                            val url = viewModel.streamUrlFor(movie) ?: return@PosterCard
                            onChannelClick(
                                Channel(
                                    id = movie.id,
                                    name = movie.name,
                                    logoUrl = movie.posterUrl,
                                    streamUrl = url,
                                    category = "Films",
                                    epgChannelId = null
                                )
                            )
                        },
                        onLongClick = {
                            viewModel.toggleMyList(movie)
                            val message = if (inMyList) "Retiré de ma liste" else "Ajouté à ma liste"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
