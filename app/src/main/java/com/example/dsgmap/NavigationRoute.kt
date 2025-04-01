package com.example.dsgmap

sealed class NavigationRoute(val route: String) {
    data object StoreSearch : NavigationRoute("store_search")
}