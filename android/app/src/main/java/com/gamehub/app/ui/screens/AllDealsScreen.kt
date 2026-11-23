package com.gamehub.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gamehub.app.data.remote.model.DashboardDeal
import com.gamehub.app.data.remote.model.StorePriceInfo
import com.gamehub.app.data.remote.model.StoreType
import com.gamehub.app.ui.theme.DealGreen
import com.gamehub.app.ui.theme.EpicAccent
import com.gamehub.app.ui.theme.TextMuted
import com.gamehub.app.ui.viewmodel.GameHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllDealsScreen(
    viewModel: GameHubViewModel,
    onBack: () -> Unit,
    onGameSelected: (String, String?, String?, StoreType?, String?, List<StorePriceInfo>) -> Unit
) {
    val dashboardData by viewModel.dashboardData.collectAsState()
    val isLoading by viewModel.isLoadingDashboard.collectAsState()
    var selectedStoreFilter by remember { mutableStateOf<StoreType?>(null) }

    val allDeals = remember(dashboardData, selectedStoreFilter) {
        val list = (dashboardData?.bestDeals ?: emptyList()).distinctBy { it.gameId }
        if (selectedStoreFilter == null) list else list.filter { it.store == selectedStoreFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "All Current Deals & Sales",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Store Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedStoreFilter == null,
                    onClick = { selectedStoreFilter = null },
                    label = { Text("All Stores", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6C5CE7),
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedStoreFilter == StoreType.STEAM,
                    onClick = { selectedStoreFilter = StoreType.STEAM },
                    label = { Text("Steam Deals", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6C5CE7),
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedStoreFilter == StoreType.EPIC,
                    onClick = { selectedStoreFilter = StoreType.EPIC },
                    label = { Text("Epic Deals", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6C5CE7),
                        selectedLabelColor = Color.White
                    )
                )
            }

            if (isLoading && allDeals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (allDeals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No deals available for the selected filter.", color = TextMuted, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(allDeals, key = { index, deal -> "${deal.gameId}_$index" }) { _, deal ->
                        DealCardItem(
                            deal = deal,
                            onClick = {
                                onGameSelected(
                                    deal.gameId,
                                    deal.title,
                                    deal.coverImage,
                                    deal.store,
                                    deal.formattedSalePrice,
                                    emptyList()
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DealCardItem(
    deal: DashboardDeal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = deal.coverImage,
                contentDescription = deal.title,
                modifier = Modifier
                    .size(width = 110.dp, height = 75.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deal.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isSteam = deal.store == StoreType.STEAM
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSteam) Color(0x2200E676) else Color(0x220078F2))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isSteam) "Steam" else "Epic",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSteam) DealGreen else EpicAccent
                        )
                    }

                    if (deal.discountPercent > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF6C5CE7))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "-${deal.discountPercent}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sale price",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF20C997)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = deal.formattedSalePrice,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (deal.originalPrice > deal.salePrice && deal.originalPrice > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "₹${deal.originalPrice.toInt()}",
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.LineThrough,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}
