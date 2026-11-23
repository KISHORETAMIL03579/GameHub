package com.gamehub.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gamehub.app.data.remote.model.LibraryGame
import com.gamehub.app.data.remote.model.StoreType
import com.gamehub.app.ui.theme.*
import com.gamehub.app.ui.viewmodel.GameHubViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: GameHubViewModel,
    initialTab: Int = 0,
    onGameSelected: (String) -> Unit = {}
) {
    val libraryData by viewModel.libraryData.collectAsState()
    val isLoading by viewModel.isLoadingLibrary.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf("All Games", "Steam", "Epic", "Duplicates")
    val pagerState = rememberPagerState(
        initialPage = initialTab.coerceIn(0, 3),
        pageCount = { tabs.size }
    )

    LaunchedEffect(initialTab) {
        if (pagerState.currentPage != initialTab && initialTab in 0..3) {
            pagerState.scrollToPage(initialTab)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Screen Header
        Text(
            text = "My Gaming Library",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
        )

        // Statistics Summary Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL GAMES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text("${libraryData?.totalGames ?: 0}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EST. VALUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text("₹${(libraryData?.estimatedTotalValueInr ?: 0.0).toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = DealGreen)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("HOURS PLAYED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text("${libraryData?.totalPlaytimeHours ?: 0.0}h", fontSize = 18.sp, fontWeight = FontWeight.Black, color = WarningAmber)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Store Tabs - ScrollableTabRow prevents text wrapping of long words like "Duplicates"
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = pagerState.currentPage == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            maxLines = 1,
                            softWrap = false,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val games = libraryData?.games ?: emptyList()

            // Horizontal Swipe between Sub-Tabs (All Games, Steam, Epic, Duplicates)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val filteredGames = when (page) {
                    1 -> games.filter { it.store == StoreType.STEAM }
                    2 -> games.filter { it.store == StoreType.EPIC }
                    3 -> {
                        val dupTitles = libraryData?.duplicates ?: emptyList()
                        games.filter { g -> dupTitles.any { d -> g.title.contains(d, ignoreCase = true) } }
                    }
                    else -> games
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    if (page == 3) {
                        if (filteredGames.isNotEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Warning, contentDescription = null, tint = WarningAmber)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Duplicate Purchases Detected across Steam & Epic!", fontSize = 12.sp, color = WarningAmber, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            item {
                                Text(
                                    text = "No duplicate purchases found in your library.",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        }
                    }

                    if (filteredGames.isEmpty() && page != 3) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Filled.SportsEsports,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No games found in this section",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Connect your store accounts in Settings to sync your real library.",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    items(filteredGames) { game ->
                        LibraryGameCard(
                            game = game,
                            onClick = { onGameSelected(game.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryGameCard(
    game: LibraryGame,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = game.coverImage,
                contentDescription = game.title,
                modifier = Modifier
                    .width(90.dp)
                    .height(55.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val storeColor = if (game.store == StoreType.STEAM) SteamAccent else EpicAccent
                    Text(
                        text = game.store.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = storeColor,
                        modifier = Modifier
                            .background(storeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )

                    if (game.playtimeHours > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${game.playtimeHours} hrs played",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "View Details",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
