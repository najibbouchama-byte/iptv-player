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
                            if (item.remainingMinutes > 0) {
                                Text(
                                    text = "${item.remainingMinutes} min restantes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            if (topTen.isNotEmpty()) {
                item { SectionHeader("Tendances", onSeeAll = onNavigateMovies) }
                item {
                    val rowState = rememberLazyListState()
                    LazyRow(
                        state = rowState,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(topTen, key = { _, movie -> movie.id }) { index, movie ->
                            TopTenCard(
                                rank = index + 1,
                                movie = movie,
                                onClick = { channelForMovie(movie)?.let(onChannelClick) },
                                modifier = Modifier.rowFocusScale(index, rowState)
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            if (newReleases.isNotEmpty()) {
                item { SectionHeader("Nouveautés") }
                item {
                    MovieRowContent(movies = newReleases, onChannelClick = { channelForMovie(it)?.let(onChannelClick) })
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            categoryRows.forEach { row ->
                item { SectionHeader(row.title) }
                item {
                    MovieRowContent(movies = row.movies, onChannelClick = { channelForMovie(it)?.let(onChannelClick) })
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun TopBrandBar(profileName: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.logo_infinity),
                contentDescription = "Infinity Player",
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "INFINITY PLAYER",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { /* Pas encore de notifications à afficher */ }) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profileName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun HeroCarousel(
    movies: List<Movie>,
    myListIds: Set<String>,
    onWatch: (Movie) -> Unit,
    onToggleMyList: (Movie) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { movies.size })

    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val movie = movies[page]
            val inMyList = myListIds.contains(movie.id)
            val year = Regex("\\((\\d{4})\\)").find(movie.name)?.groupValues?.get(1)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                if (!movie.posterUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = movie.posterUrl,
                        contentDescription = movie.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.5f to Color.Black.copy(alpha = 0.55f),
                                    1.0f to Color.Black.copy(alpha = 0.9f)
                                )
                            )
                        )
                )
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brush.horizontalGradient(listOf(BrandViolet, BrandPink)))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text("À LA UNE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = movie.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (year != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(year, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        Button(
                            onClick = { onWatch(movie) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Regarder", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedButton(
                            onClick = { onToggleMyList(movie) },
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(
                                imageVector = if (inMyList) Icons.Filled.Check else Icons.Filled.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (inMyList) "Dans ma liste" else "Ma liste", color = Color.White)
                        }
                    }
                }
            }
        }

        if (movies.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                movies.indices.forEach { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (selected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAccessRow(
    onLiveTv: () -> Unit,
    onMovies: () -> Unit,
    onSeries: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickAccessCard(
            title = "Live TV",
            subtitle = "Chaînes en direct",
            icon = Icons.Filled.Tv,
            color = BrandTeal,
            onClick = onLiveTv,
            modifier = Modifier.weight(1f)
        )
        QuickAccessCard(
            title = "Films",
            subtitle = "Des milliers de films",
            icon = Icons.Filled.Movie,
            color = BrandViolet,
            onClick = onMovies,
            modifier = Modifier.weight(1f)
        )
        QuickAccessCard(
            title = "Séries",
            subtitle = "Vos séries préférées",
            icon = Icons.Filled.Theaters,
            color = BrandPink,
            onClick = onSeries,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickAccessCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Text(
            subtitle,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (onSeeAll != null) {
            Text(
                text = "Voir tout",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onSeeAll)
            )
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun TopTenCard(
    rank: Int,
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.width(120.dp)) {
        PosterCard(
            title = movie.name,
            posterUrl = movie.posterUrl,
            showPoster = true,
            onClick = onClick
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(text = "$rank", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun <T> SearchResultsGrid(
    items: List<T>,
    posterUrlOf: (T) -> String?,
    titleOf: (T) -> String,
    onClick: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.height(((items.size / 3 + 1) * 200).dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(items) { item ->
            PosterCard(
                title = titleOf(item),
                posterUrl = posterUrlOf(item),
                showPoster = true,
                onClick = { onClick(item) }
            )
        }
    }
}

@Composable
private fun MovieRowContent(
    movies: List<Movie>,
    onChannelClick: (Movie) -> Unit
) {
    val rowState = rememberLazyListState()
    LazyRow(
        state = rowState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(movies, key = { _, movie -> movie.id }) { index, movie ->
            PosterCard(
                title = movie.name,
                posterUrl = movie.posterUrl,
                showPoster = true,
                onClick = { onChannelClick(movie) },
                modifier = Modifier.width(120.dp).rowFocusScale(index, rowState)
            )
        }
    }
}
