package com.iptvplayer.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.iptvplayer.app.R
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.Movie
import com.iptvplayer.app.data.model.Series
import com.iptvplayer.app.ui.common.PosterCard
import com.iptvplayer.app.ui.common.rowFocusScale

private val BrandTeal = Color(0xFF22D3EE)
private val BrandViolet = Color(0xFF8B5CF6)
private val BrandPink = Color(0xFFEC4899)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onChannelClick: (Channel) -> Unit,
    onSeriesClick: (Series) -> Unit = {},
    onNavigateLiveTv: () -> Unit = {},
    onNavigateMovies: () -> Unit = {},
    onNavigateSeries: () -> Unit = {}
) {
    val continueWatching by viewModel.continueWatching.collectAsState()
    val heroMovies by viewModel.heroMovies.collectAsState()
    val topTen by viewModel.topTen.collectAsState()
    val newReleases by viewModel.newReleases.collectAsState()
    val categoryRows by viewModel.categoryRows.collectAsState()
    val isLoading by viewModel.isLoadingDiscovery.collectAsState()
    val myListIds by viewModel.myListIds.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val isIndexing by viewModel.isIndexing.collectAsState()
    val searchMovies by viewModel.searchMovies.collectAsState()
    val searchSeries by viewModel.searchSeries.collectAsState()
    val isSearching = searchQuery.isNotBlank()

    fun channelForMovie(movie: Movie): Channel? {
        val url = viewModel.streamUrlFor(movie) ?: return null
        return Channel(
            id = movie.id,
            name = movie.name,
            logoUrl = movie.posterUrl,
            streamUrl = url,
            category = "Films",
            epgChannelId = null
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            TopBrandBar(profileName = viewModel.profileName)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!isSearching) {
            if (heroMovies.isNotEmpty()) {
                item {
                    HeroCarousel(
                        movies = heroMovies,
                        myListIds = myListIds,
                        onWatch = { movie -> channelForMovie(movie)?.let(onChannelClick) },
                        onToggleMyList = { viewModel.toggleMyList(it) }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = {
                    Text(
                        "Rechercher un film ou une série…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Effacer",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (isSearching) {
            if (isIndexing) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Indexation du catalogue…", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            } else if (searchMovies.isEmpty() && searchSeries.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Aucun résultat pour « $searchQuery »", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            } else {
                if (searchMovies.isNotEmpty()) {
                    item { SectionHeader("Films (${searchMovies.size})") }
                    item {
                        SearchResultsGrid(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            items = searchMovies,
                            posterUrlOf = { it.posterUrl },
                            titleOf = { it.name },
                            onClick = { movie -> channelForMovie(movie)?.let(onChannelClick) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
                if (searchSeries.isNotEmpty()) {
                    item { SectionHeader("Séries (${searchSeries.size})") }
                    item {
                        SearchResultsGrid(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            items = searchSeries,
                            posterUrlOf = { it.posterUrl },
                            titleOf = { it.name },
                            onClick = { onSeriesClick(it) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            return@LazyColumn
        }

        item {
            QuickAccessRow(
                onLiveTv = onNavigateLiveTv,
                onMovies = onNavigateMovies,
                onSeries = onNavigateSeries
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (continueWatching.isNotEmpty()) {
            item { SectionHeader("Reprendre la lecture") }
            item {
                val rowState = rememberLazyListState()
                LazyRow(
                    state = rowState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(continueWatching, key = { _, item -> item.channel.id }) { index, item ->
                        Column(modifier = Modifier.width(120.dp).rowFocusScale(index, rowState)) {
                            PosterCard(
                                title = item.channel.name,
                                posterUrl = item.channel.logoUrl,
                                showPoster = true,
                                progressFraction = item.progress,
                                onClick = { onChannelClick(item.channel) }
                            )
