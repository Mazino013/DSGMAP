# DSG Store Finder

A mobile application that helps users locate DICK'S Sporting Goods stores using ZIP codes or their current location.

## Architecture Components

This app follows Clean Architecture principles with MVVM pattern.

## Screenshots

<div style="text-align: center;">
  <h1 style="font-size: 36px; margin-bottom: 20px;">Pokedex</h1>
  
  <div style="display: flex; justify-content: center; gap: 20px;">
    <img src="https://github.com/user-attachments/assets/72707731-b309-46e2-a55f-909926fa6cdb" style="width: 30%; max-width: 100;">
    <img src="https://github.com/user-attachments/assets/9329eb92-5972-4c53-881f-7ff222c5fa3" style="width: 30%; max-width: 100px;">
    <img src="https://github.com/user-attachments/assets/1b066175-63cb-4200-9fe9-c0f734445dff" style="width: 30%; max-width: 100px;">
    <img src="https://github.com/user-attachments/assets/ff1b8be7-d79e-4662-bae0-0dfbaf861c34" style="width: 30%; max-width: 100px;">
    <img src="https://github.com/user-attachments/assets/88f18f98-e248-4322-bf17-1364f449fa72" style="width: 30%; max-width: 100px;">
  </div>
</div>

### 1. Data Layer
- **StoreApiService**: Interface for making API calls to the DSG store search endpoint
- **StoreRepository**: Handles data operations and transforms API responses to UI models
- **Data Models**: StoreResponse, Store, StoreUiModel
- **API Security**: Keys stored in gradle.properties and accessed via BuildConfig

### 2. Domain Layer
- **LocationProvider**: Utility for accessing the device's location

### 3. UI Layer
- **StoreSearchViewModel**: Connects UI with the repository and manages UI state
- **StoreSearchScreen**: Main composable screen for searching stores
- **UI Components**: SearchBar, StoreList, StoreItem, ErrorMessage

### 4. Dependency Injection
- **AppModule**: Provides application context
- **NetworkModule**: Provides Retrofit, OkHttp, and API service instances

### 5. Tests
- **StoreSearchViewModelTest**: Unit tests for the ViewModel
- **StoreRepositoryTest**: Unit tests for the Repository

## Features

### 1. Search stores by ZIP code
- User can enter a ZIP code and search for nearby stores
- Displays a list of stores within 100 miles of the ZIP code

### 2. Search stores by current location
- User can use their device's GPS to find nearby stores
- Handles location permission requests appropriately

### 3. Store List Display
- Shows store name, distance, and city/state
- Handles empty states when no stores are found

### 4. Error Handling
- Displays appropriate error messages when API calls fail
- Handles location permission denials and errors
- 404 responses mapped to success with empty list for better UX

### 5. Security
- API keys stored securely in gradle.properties (excluded from version control)
- Keys accessed at build-time via BuildConfig fields
- Consistent error handling prevents sensitive information exposure

## Setup Instructions

1. Clone the repository
2. Create a `gradle.properties` file in the project root
3. Add the following line to your gradle.properties file: `API_KEY=code-challenge`
4. Sync the project with Gradle files
5. Build and run the application

## Requirements
- Android Studio Hedgehog or later
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 35
- Kotlin 1.9.0 or later

## Libraries Used
- Jetpack Compose for UI
- Retrofit for network calls
- Hilt for dependency injection
- Kotlin Coroutines and Flow
- Google Play Services Location API
