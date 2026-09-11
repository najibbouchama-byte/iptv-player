package com.iptvplayer.app.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iptvplayer.app.data.model.XtreamCategory

/**
 * Bouton "Catégorie" façon TiviMate : ouvre un panneau qui glisse depuis
 * le bas avec la liste complète, au lieu d'une rangée de chips qui déborde.
 * Réutilisé par Films, Séries (et potentiellement Live TV plus tard).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectorButton(
    categories: List<XtreamCategory>,
    selectedCategory: XtreamCategory?,
    placeholder: String,
    onSelect: (XtreamCategory) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    OutlinedButton(
        onClick = { showSheet = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Filled.List, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = selectedCategory?.name ?: placeholder,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(categories, key = { it.id }) { category ->
                    val isSelected = selectedCategory?.id == category.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(category)
                                showSheet = false
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}
