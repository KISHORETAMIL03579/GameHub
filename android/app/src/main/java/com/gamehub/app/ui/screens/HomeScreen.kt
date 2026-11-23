package com.gamehub.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gamehub.app.data.local.entity.WishlistEntity
import com.gamehub.app.data.remote.model.*
import com.gamehub.app.ui.theme.*
import com.gamehub.app.ui.viewmodel.GameHubViewModel
import com.gamehub.app.ui.viewmodel.SearchState
import com.gamehub.app.ui.viewmodel.StoreFilter

@Composable
fun HomeScreen(
    viewModel: GameHubViewModel,
    onGameSelected: (String, String?, String?, StoreType?, String?, List<StorePriceInfo>) -> Unit,
    onNavigateToLibrary: ((Int) -> Unit)? = null,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAllDeals: () -> Unit = {},
    onNavigateToRecentlyAdded: () -> Unit = {},
    onNavigateToExplore: () -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val selectedFilter by viewModel.selectedStoreFilter.collectAsState()
    val dashboardData by viewModel.dashboardData.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()
    val unreadNotifs by viewModel.unreadNotificationCount.collectAsState()

    val isSearchingMode = searchQuery.isNotBlank() || searchState !is SearchState.Idle

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // --- 1. Compact Top Header (Height: 48dp, Logo: 34dp) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF6C5CE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SportsEsports,
                        contentDescription = "GameHub Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "GAMEHUB",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "YOUR GAMING UNIVERSE",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateToNotifications,
                    modifier = Modifier.size(38.dp)
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotifs > 0) {
                                Badge(
                                    containerColor = Color(0xFF6C5CE7),
                                    contentColor = Color.White
                                ) {
                                    Text("$unreadNotifs", fontSize = 9.sp)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- 2. Search Bar (Compact Height: 48dp) ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search any PC game...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { viewModel.searchImmediately(searchQuery) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(onClick = { /* Filter */ }) {
                            Icon(Icons.Filled.Tune, contentDescription = "Filter", tint = TextMuted, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.searchImmediately(searchQuery) }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = DividerColor.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // --- Conditional Content: Search Results Mode VS Dashboard Mode ---
        if (isSearchingMode) {
            SearchContent(
                searchState = searchState,
                selectedFilter = selectedFilter,
                onFilterSelected = { viewModel.setStoreFilter(it) },
                wishlist = wishlist,
                onToggleWishlist = { viewModel.toggleWishlist(it) },
                onGameSelected = onGameSelected,
                onRetry = { viewModel.searchImmediately(searchQuery) }
            )
        } else {
            DashboardContent(
                dashboardData = dashboardData,
                onGameSelected = onGameSelected,
                onNavigateToLibrary = onNavigateToLibrary,
                onNavigateToAllDeals = onNavigateToAllDeals,
                onNavigateToRecentlyAdded = onNavigateToRecentlyAdded,
                onNavigateToExplore = onNavigateToExplore
            )
        }
    }
}

@Composable
fun DashboardContent(
    dashboardData: DashboardResponse?,
    onGameSelected: (String, String?, String?, StoreType?, String?, List<StorePriceInfo>) -> Unit,
    onNavigateToLibrary: ((Int) -> Unit)?,
    onNavigateToAllDeals: () -> Unit = {},
    onNavigateToRecentlyAdded: () -> Unit = {},
    onNavigateToExplore: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // --- Section: Your Gaming Universe ---
        Text(
            text = "Your Gaming Universe",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Steam Library Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToLibrary?.invoke(1) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16243A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.SportsEsports,
                            contentDescription = null,
                            tint = SteamAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STEAM LIBRARY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SteamAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${dashboardData?.steamCount ?: 0}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Games owned",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            // Epic Library Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToLibrary?.invoke(2) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF28201A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.SportsEsports,
                            contentDescription = null,
                            tint = Color(0xFFE5A93C),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EPIC LIBRARY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5A93C)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${dashboardData?.epicCount ?: 0}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Games owned",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Section: Featured Game ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Featured game",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(
                onClick = onNavigateToExplore,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Explore all",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9D84FF)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val featured = dashboardData?.featuredGame
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable {
                    if (featured != null) {
                        onGameSelected(
                            featured.gameId,
                            featured.title,
                            featured.bannerImage,
                            featured.cheapestStore,
                            featured.formattedLowestPrice,
                            featured.stores
                        )
                    }
                },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                AsyncImage(
                    model = featured?.bannerImage ?: "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/1091500/capsule_616x353.jpg",
                    contentDescription = featured?.title ?: "Cyberpunk 2077",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC000000), Color(0xEE0B0E14)),
                                startY = 50f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF6C5CE7))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "FEATURED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = featured?.title ?: "Cyberpunk 2077",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Text(
                        text = featured?.subtitle ?: "Compare prices across Steam and Epic Games Store.",
                        fontSize = 11.sp,
                        color = Color(0xFFD1D5DB)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (featured != null) {
                                onGameSelected(
                                    featured.gameId,
                                    featured.title,
                                    featured.bannerImage,
                                    featured.cheapestStore,
                                    featured.formattedLowestPrice,
                                    featured.stores
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "VIEW PRICE COMPARISON",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Section: Best Deals Today ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Best deals today",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(
                onClick = onNavigateToAllDeals,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "See all",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9D84FF)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val bestDeals = (dashboardData?.bestDeals ?: emptyList()).distinctBy { it.gameId }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(bestDeals) { deal ->
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .clickable {
                            onGameSelected(
                                deal.gameId,
                                deal.title,
                                deal.coverImage,
                                deal.store,
                                deal.formattedSalePrice,
                                emptyList()
                            )
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        AsyncImage(
                            model = deal.coverImage,
                            contentDescription = deal.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = deal.title,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Sale price",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF20C997)
                            )
                            Text(
                                text = deal.formattedSalePrice,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val isSteam = deal.store == StoreType.STEAM
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isSteam) Color(0x2200E676) else Color(0x220078F2)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isSteam) "Steam" else "Epic",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSteam) DealGreen else EpicAccent
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Section: Recently Added ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onNavigateToRecentlyAdded)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Recently added",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "See all",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9D84FF)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View all recently added",
                    tint = Color(0xFF9D84FF),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val recentlyAdded = (dashboardData?.recentlyAdded ?: emptyList()).distinctBy { it.gameId }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recentlyAdded) { item ->
                AsyncImage(
                    model = item.coverImage,
                    contentDescription = item.title,
                    modifier = Modifier
                        .width(75.dp)
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onGameSelected(
                                item.gameId,
                                item.title,
                                item.coverImage,
                                null,
                                null,
                                emptyList()
                            )
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun SearchContent(
    searchState: SearchState,
    selectedFilter: StoreFilter,
    onFilterSelected: (StoreFilter) -> Unit,
    wishlist: List<WishlistEntity>,
    onToggleWishlist: (GameSummary) -> Unit,
    onGameSelected: (String, String?, String?, StoreType?, String?, List<StorePriceInfo>) -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StoreFilter.entries.forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else DividerColor,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onFilterSelected(filter) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        when (searchState) {
            is SearchState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is SearchState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = searchState.message,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onRetry) {
                            Text("Retry Search")
                        }
                    }
                }
            }
            is SearchState.Success -> {
                val games = searchState.games
                if (games.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching games found across stores.", color = TextMuted, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        items(games) { game ->
                            GameResultCard(
                                game = game,
                                isWishlisted = wishlist.any { it.gameId == game.gameId },
                                onWishlistClick = { onToggleWishlist(game) },
                                onClick = {
                                    onGameSelected(
                                        game.gameId,
                                        game.title,
                                        game.coverImage,
                                        game.cheapestStore,
                                        game.formattedLowestPrice,
                                        game.stores
                                    )
                                }
                            )
                        }
                    }
                }
            }
            is SearchState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Type to search live games across Steam and Epic...", color = TextMuted, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun GameResultCard(
    game: GameSummary,
    isWishlisted: Boolean,
    onWishlistClick: () -> Unit,
    onClick: () -> Unit
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
                    .width(100.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray),
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
                    Text(
                        text = game.formattedLowestPrice ?: "N/A",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (game.lowestPrice == 0.0) DealGreen else MaterialTheme.colorScheme.onSurface
                    )

                    if (game.highestDiscountPercent > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DiscountRed)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "-${game.highestDiscountPercent}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    game.stores.forEach { store ->
                        val isCheapest = store.store == game.cheapestStore
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isCheapest) DealGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${store.store.name}: ${store.formattedPrice}",
                                fontSize = 9.sp,
                                fontWeight = if (isCheapest) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCheapest) DealGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onWishlistClick) {
                Icon(
                    imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Wishlist",
                    tint = if (isWishlisted) DiscountRed else TextMuted
                )
            }
        }
    }
}
