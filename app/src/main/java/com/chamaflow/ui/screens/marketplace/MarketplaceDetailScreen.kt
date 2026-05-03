package com.chamaflow.ui.screens.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.chamaflow.data.models.MarketplaceListing
import com.chamaflow.ui.theme.ChamaBlue
import com.chamaflow.ui.theme.ChamaBlueLight
import com.chamaflow.ui.theme.Primary
import com.chamaflow.ui.viewmodel.MarketplaceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceDetailScreen(
    listingId: String,
    onBack: () -> Unit,
    onContactSeller: (String, String) -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listing = uiState.listings.find { it.id == listingId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listing Details", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        },
        bottomBar = {
            listing?.let {
                Surface(
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onContactSeller(it.sellerId, it.sellerName) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChamaBlue)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Message Seller")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (listing == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Listing not found.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = listing.imageUrl ?: "https://via.placeholder.com/600",
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = listing.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "KES ${listing.price}",
                        style = MaterialTheme.typography.titleLarge,
                        color = ChamaBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    Surface(
                        color = ChamaBlueLight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = listing.category.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = ChamaBlue
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("Description", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = listing.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray
                    )

                    Spacer(Modifier.height(32.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color.Gray)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(text = listing.sellerName, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Posted on ${formatDate(listing.timestamp)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { /* View Profile */ }) {
                            Text("View Profile")
                        }
                    }
                    
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
