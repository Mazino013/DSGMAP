package com.example.dsgmap.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dsgmap.R
import com.example.dsgmap.data.model.StoreUiModel
import java.text.DecimalFormat
import androidx.compose.material3.HorizontalDivider

@Composable
fun StoreSearchScreen(
    viewModel: StoreSearchViewModel,
    onNavigateToMapDetail: (String, Double, Double) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val isValidZipCode = { zipCode: String ->
        zipCode.length == 5 && zipCode.all {it.isDigit()}

    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.values.all { it }
        if (allPermissionsGranted) {
            viewModel.searchStoresByCurrentLocation()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Find a Store",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = {
                        focusManager.clearFocus()
                        if (isValidZipCode(searchQuery)) {
                            viewModel.searchStoresByZipCode(searchQuery)
                        }
                    },
                    onLocationClick = {
                        searchQuery = ""
                        if (viewModel.hasLocationPermission()) {
                            viewModel.searchStoresByCurrentLocation()
                        } else {
                            requestPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when (uiState) {
                is StoreSearchViewModel.StoreSearchUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is StoreSearchViewModel.StoreSearchUiState.Error -> {
                    ErrorMessage(message = (uiState as StoreSearchViewModel.StoreSearchUiState.Error).message)
                }
                is StoreSearchViewModel.StoreSearchUiState.Empty -> {
                    NoStoresFoundScreen()
                }
                is StoreSearchViewModel.StoreSearchUiState.Success -> {
                    StoreList(
                        stores = (uiState as StoreSearchViewModel.StoreSearchUiState.Success).stores,
                        modifier = Modifier.fillMaxWidth(),
                        onNavigateToMapDetail = onNavigateToMapDetail
                    )
                }
                is StoreSearchViewModel.StoreSearchUiState.Initial -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Enter a ZIP code to search for stores")
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onLocationClick: () -> Unit
) {
    val isValidZipCode = query.length == 5 && query.all { it.isDigit() }
    val handleSearch = {
        if (isValidZipCode) {
            onSearch()
        }
    }

    OutlinedTextField(
        value = query,
        onValueChange = { 
            // Only allow numbers for ZIP codes
            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                onQueryChange(it)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        placeholder = { Text("Search by ZIP Code") },
        isError = query.isNotEmpty() && !isValidZipCode,
        supportingText = {
            if (query.isNotEmpty() && !isValidZipCode) {
                Text("Please enter a valid 5-digit ZIP code")
            }
        },
        shape = RoundedCornerShape(28.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                handleSearch()
            }
        ),
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
            ) {
                IconButton(
                    onClick = onLocationClick,
                    modifier = Modifier
                        .size(40.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_loc_service),
                        contentDescription = "Use current location",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun StoreList(
    stores: List<StoreUiModel>,
    modifier: Modifier = Modifier,
    onNavigateToMapDetail: (String, Double, Double) -> Unit = { _, _, _ -> }
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Stores Near You",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            LazyColumn {
                items(stores) { store ->
                    StoreItem(
                        store = store,
                        onItemClick = {
                            onNavigateToMapDetail(
                                store.name,
                                store.latitude,
                                store.longitude
                            )
                        }
                    )
                    if (stores.last() != store) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 1.dp,
                            color = Color.LightGray.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoreItem(
    store: StoreUiModel,
    onItemClick: () -> Unit = {}
) {
    val distanceFormat = remember { DecimalFormat("0.0") }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onItemClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = store.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${distanceFormat.format(store.distance)} miles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Text(
                    text = " | ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Text(
                    text = store.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        
        Icon(
            painter = painterResource(id = R.drawable.ic_chev_right),
            contentDescription = "View details",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun NoStoresFoundScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_no_stores),
                contentDescription = "No stores found",
                tint = Color.Unspecified,
                modifier = Modifier.size(100.dp)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "We are unable to find stores\nwithin 100 miles of search",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 32.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_empty_search),
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(120.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Something Went Wrong",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}
