package com.chamaflow.ui.screens.marketplace

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.chamaflow.data.models.MarketplaceCategory
import com.chamaflow.data.models.MarketplaceListing
import com.chamaflow.ui.theme.ChamaBlue
import com.chamaflow.ui.theme.Primary
import com.chamaflow.ui.viewmodel.MarketplaceViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    chamaId: String,
    userId: String,
    userName: String,
    onBack: () -> Unit,
    onListingClick: (String) -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddListing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(chamaId) {
        viewModel.loadListings(chamaId)
    }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chama Marketplace", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Filter */ }) {
                        Icon(Icons.Default.FilterList, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddListing = true },
                containerColor = ChamaBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Add Listing")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            CategoryTabs(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.setCategory(it) }
            )

            if (uiState.isLoading && uiState.listings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val filteredListings = if (uiState.selectedCategory == null) {
                    uiState.listings
                } else {
                    uiState.listings.filter { it.category == uiState.selectedCategory }
                }

                if (filteredListings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No listings found in this category.", color = Color.Gray)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredListings) { listing ->
                            ListingCard(listing = listing, onClick = { onListingClick(listing.id) })
                        }
                    }
                }
            }
        }

        if (showAddListing) {
            AddListingDialog(
                isUploading = uiState.isUploading,
                onDismiss = { showAddListing = false },
                onSave = { title, desc, price, cat, uris ->
                    val newListing = MarketplaceListing(
                        chamaId = chamaId,
                        sellerId = userId,
                        sellerName = userName,
                        title = title,
                        description = desc,
                        price = price.toDoubleOrNull() ?: 0.0,
                        category = cat
                    )
                    viewModel.createListing(chamaId, newListing, uris)
                    showAddListing = false
                }
            )
        }
    }
}

@Composable
fun CategoryTabs(
    selectedCategory: MarketplaceCategory?,
    onCategorySelected: (MarketplaceCategory?) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = if (selectedCategory == null) 0 else MarketplaceCategory.entries.indexOf(selectedCategory) + 1,
        edgePadding = 16.dp,
        containerColor = Color.White,
        divider = {},
        indicator = {}
    ) {
        Tab(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            text = { Text("All") }
        )
        MarketplaceCategory.entries.forEach { category ->
            Tab(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                text = { 
                    Text(category.name.lowercase().replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                    }) 
                }
            )
        }
    }
}

@Composable
fun ListingCard(listing: MarketplaceListing, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = listing.imageUrls.firstOrNull() ?: "https://via.placeholder.com/150",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = listing.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "KES ${"%,.0f".format(listing.price)}",
                    color = ChamaBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "By ${listing.sellerName}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListingDialog(
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, MarketplaceCategory, List<Uri>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MarketplaceCategory.GOODS) }
    var expanded by remember { mutableStateOf(false) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> selectedImageUris = uris }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post New Listing") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Multi-Image Preview/Picker
                if (selectedImageUris.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                            Text("Add Photos", color = Color.Gray)
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    ) {
                        items(selectedImageUris) { uri ->
                            Box {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier.size(120.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { selectedImageUris = selectedImageUris - uri },
                                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray.copy(alpha = 0.3f))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color.Gray)
                            }
                        }
                    }
                }

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(100.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (KES)") }, modifier = Modifier.fillMaxWidth())
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        MarketplaceCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, desc, price, category, selectedImageUris) },
                enabled = title.isNotBlank() && price.isNotBlank() && !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Post Listing")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
