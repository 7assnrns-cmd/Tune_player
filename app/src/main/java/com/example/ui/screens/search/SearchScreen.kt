package com.example.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.domain.model.PlaybackState
import com.example.domain.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassTheme

enum class SearchFilter(val label: String) {
    ALL("All"),
    SONGS("Songs"),
    ARTISTS("Artists"),
    ALBUMS("Albums"),
    YOUTUBE("YouTube Music")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    searchQuery: String,
    searchResults: List<Song>,
    searchHistory: List<String>,
    playbackState: PlaybackState,
    onQueryChange: (String) -> Unit,
    onSubmitSearch: (String) -> Unit,
    onDeleteSearch: (String) -> Unit,
    onClearSearchHistory: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var selectedFilter by remember { mutableStateOf(SearchFilter.ALL) }

    val filteredResults = remember(searchResults, selectedFilter, searchQuery) {
        when (selectedFilter) {
            SearchFilter.ALL -> searchResults
            SearchFilter.SONGS -> searchResults.filter { it.title.contains(searchQuery, ignoreCase = true) }
            SearchFilter.ARTISTS -> searchResults.filter { it.artist.contains(searchQuery, ignoreCase = true) }
            SearchFilter.ALBUMS -> searchResults.filter { it.album.contains(searchQuery, ignoreCase = true) }
            SearchFilter.YOUTUBE -> searchResults.filter { it.isRemote }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen")
    ) {
        // Glass Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("Search songs, artists, YouTube Music...", color = Color.White.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = GlassTheme.ActiveDockIcon)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(44.dp).testTag("clear_search_btn")
                    ) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear search", tint = Color.White.copy(alpha = 0.7f))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = GlassTheme.ActiveDockIcon,
                unfocusedIndicatorColor = GlassTheme.BorderGlass
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                    onSubmitSearch(searchQuery)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_text_input")
        )

        // Filter Chips Row
        if (searchQuery.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchFilter.values().forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label, color = if (selectedFilter == filter) Color.Black else Color.White) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GlassTheme.ActiveDockIcon,
                            containerColor = Color.White.copy(alpha = 0.08f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == filter,
                            borderColor = GlassTheme.BorderGlass
                        )
                    )
                }
            }
        }

        // Search History (when search query is blank)
        if (searchQuery.isBlank()) {
            if (searchHistory.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Searches",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    TextButton(onClick = onClearSearchHistory) {
                        Text("Clear All", color = GlassTheme.ActiveDockIcon)
                    }
                }

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    searchHistory.forEach { query ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, GlassTheme.BorderGlass, RoundedCornerShape(20.dp))
                                .clickable {
                                    onQueryChange(query)
                                    onSubmitSearch(query)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = GlassTheme.ActiveDockIcon
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = query,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Filled.Clear,
                                    contentDescription = "Remove query",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onDeleteSearch(query) },
                                    tint = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Search across songs, artists, albums & YouTube Music",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.60f)
                        )
                    }
                }
            }
        } else {
            // Results List
            if (filteredResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No results found for \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.70f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    item {
                        Text(
                            text = "${filteredResults.size} results found",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.60f),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }

                    items(filteredResults, key = { it.id }) { song ->
                        SongListItem(
                            song = song,
                            isCurrentSong = song.id == playbackState.currentSong?.id,
                            isPlaying = playbackState.isPlaying && song.id == playbackState.currentSong?.id,
                            onClick = {
                                onSubmitSearch(searchQuery)
                                onSongClick(song, filteredResults)
                            },
                            onToggleFavorite = onToggleFavorite,
                            onAddToPlaylist = onAddToPlaylist,
                            onAddToQueue = onAddToQueue,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
