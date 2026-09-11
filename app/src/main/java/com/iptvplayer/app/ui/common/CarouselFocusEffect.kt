package com.iptvplayer.app.ui.common

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.abs

private const val MIN_FOCUS_SCALE = 0.86f
private const val MAX_FOCUS_SCALE = 1.1f

/**
 * Fait "vivre" une LazyRow horizontale : la carte la plus proche du centre
 * de l'écran grossit légèrement pendant le défilement, comme un carrousel
 * Apple TV / Netflix. À poser sur chaque item, avec son index et le
 * LazyListState de la LazyRow.
 */
fun Modifier.rowFocusScale(index: Int, listState: LazyListState): Modifier = composed {
    val scale by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val item = info.visibleItemsInfo.firstOrNull { it.index == index }
                ?: return@derivedStateOf MIN_FOCUS_SCALE
            val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
            val itemCenter = item.offset + item.size / 2f
            val halfViewport = ((info.viewportEndOffset - info.viewportStartOffset) / 2f).coerceAtLeast(1f)
            val fraction = 1f - (abs(itemCenter - viewportCenter) / halfViewport).coerceIn(0f, 1f)
            MIN_FOCUS_SCALE + (MAX_FOCUS_SCALE - MIN_FOCUS_SCALE) * fraction
        }
    }
    graphicsLayer {
        scaleX = scale
        scaleY = scale
        alpha = (0.65f + 0.35f * ((scale - MIN_FOCUS_SCALE) / (MAX_FOCUS_SCALE - MIN_FOCUS_SCALE))).coerceIn(0f, 1f)
    }
}

/**
 * Même effet mais pour une grille verticale (Films / Séries) : la carte
 * la plus proche du centre vertical de l'écran ressort davantage.
 */
fun Modifier.gridFocusScale(index: Int, gridState: LazyGridState): Modifier = composed {
    val scale by remember {
        derivedStateOf {
            val info = gridState.layoutInfo
            val item = info.visibleItemsInfo.firstOrNull { it.index == index }
                ?: return@derivedStateOf MIN_FOCUS_SCALE
            val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
            val itemCenter = item.offset.y + item.size.height / 2f
            val halfViewport = ((info.viewportEndOffset - info.viewportStartOffset) / 2f).coerceAtLeast(1f)
            val fraction = 1f - (abs(itemCenter - viewportCenter) / halfViewport).coerceIn(0f, 1f)
            MIN_FOCUS_SCALE + (MAX_FOCUS_SCALE - MIN_FOCUS_SCALE) * fraction
        }
    }
    graphicsLayer {
        scaleX = scale
        scaleY = scale
        alpha = (0.65f + 0.35f * ((scale - MIN_FOCUS_SCALE) / (MAX_FOCUS_SCALE - MIN_FOCUS_SCALE))).coerceIn(0f, 1f)
    }
}
