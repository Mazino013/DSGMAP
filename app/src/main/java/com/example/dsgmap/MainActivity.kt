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
import androidx.hilt.navigation.compose.hiltViewModel
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
                Scaffold { paddingValues ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)) {
                        StoreSearchScreen(
                            viewModel = hiltViewModel()
                        )
                    }
                }
            }
        }
    }
}