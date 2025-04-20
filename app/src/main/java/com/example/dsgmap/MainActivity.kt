package com.example.dsgmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dsgmap.ui.StoreSearchScreen
import com.example.dsgmap.ui.theme.DSGMAPTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DSGMAPTheme {
                val navController = rememberNavController()

                Scaffold { paddingValues ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)) {
                        NavHost(
                            navController = navController,
                            startDestination = NavigationRoute.StoreSearch.route
                        ) {
                            composable(route = NavigationRoute.StoreSearch.route) {
                                val context = LocalContext.current
                                StoreSearchScreen(
                                    viewModel = hiltViewModel(),
                                    onNavigateToMapDetail = { storeName, lat, lng ->
                                        // Open in maps using our utility function
                                        MapUtils.openInMaps(context, storeName, lat, lng)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}