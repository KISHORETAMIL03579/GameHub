package com.gamehub.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gamehub.app.data.remote.model.GameSummary
import com.gamehub.app.data.remote.model.StoreType
import com.gamehub.app.ui.theme.*
import com.gamehub.app.ui.viewmodel.GameHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailsSheet(
    viewModel: GameHubViewModel,
    onDismiss: () -> Unit
) {
    val details by viewModel.selectedGameDetails.collectAsState()
    val comparison by viewModel.priceComparison.collectAsState()
    val isLoading by viewModel.isLoadingDetails.collectAsState()
    val detailsError by viewModel.detailsError.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        if (details != null) {
            val game = details!!
            val isWishlisted = wishlist.any { it.gameId == game.gameId }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp)
            ) {
                // Non-blocking background loader bar
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Game Banner / Cover
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    AsyncImage(
                        model = game.bannerImage ?: game.coverImage,
                        contentDescription = game.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Wishlist floating button
                    IconButton(
                        onClick = {
                            viewModel.toggleWishlist(
                                GameSummary(
                                    gameId = game.gameId,
                                    title = game.title,
                                    coverImage = game.coverImage,
                                    lowestPrice = game.stores.minOfOrNull { it.price },
                                    formattedLowestPrice = game.stores.minOfOrNull { it.price }?.let { "₹${it.toInt()}" } ?: "Check store",
                                    cheapestStore = game.cheapestStore
                                )
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x88000000))
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) DiscountRed else Color.White
                        )
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = game.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Publisher: ${game.publisher ?: "Verified Store"} • Status: Live",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Smart Purchase Assistant Banner ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Smart Price Comparison", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = comparison?.recommendation ?: "Comparing real-time prices across Steam & Epic Games Store...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (comparison != null && comparison!!.potentialSavings > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DealGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Filled.Savings, contentDescription = null, tint = DealGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Potential Savings: ${comparison!!.potentialSavingsFormatted}",
                                        color = DealGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Store Pricing (INR ₹)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Store Price Cards
                    if (game.stores.isEmpty()) {
                        Text(
                            text = "Checking store pricing...",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        game.stores.forEach { storeInfo ->
                            val isCheapest = storeInfo.store == (comparison?.cheapestStore ?: game.cheapestStore)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCheapest) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isCheapest) BorderStroke(1.5.dp, DealGreen) else BorderStroke(1.dp, DividerColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val iconTint = if (storeInfo.store == StoreType.STEAM) SteamAccent else EpicAccent
                                        Icon(Icons.Filled.Storefront, contentDescription = null, tint = iconTint)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(storeInfo.store.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            if (isCheapest) {
                                                Text("LOWEST PRICE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = DealGreen)
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = storeInfo.formattedPrice,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isCheapest) DealGreen else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (storeInfo.discountPercent > 0) {
                                                Text(
                                                    text = "-${storeInfo.discountPercent}% (${storeInfo.formattedOriginalPrice})",
                                                    fontSize = 11.sp,
                                                    color = DiscountRed
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(storeInfo.storeUrl))
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isCheapest) DealGreen else MaterialTheme.colorScheme.primary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "Open",
                                                color = if (isCheapest) Color.Black else Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "About the Game",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = game.shortDescription ?: game.description ?: "Verified PC title across digital marketplaces.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        } else if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading live game details...", fontSize = 13.sp, color = TextSecondary)
                }
            }
        } else {
            // Error State with Retry
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = DiscountRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = detailsError ?: "Unable to load game details.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.retrySelectedGame("") }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
