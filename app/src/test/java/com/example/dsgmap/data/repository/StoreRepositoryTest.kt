package com.example.dsgmap.data.repository

import com.example.dsgmap.data.model.Store
import com.example.dsgmap.data.model.StoreResponse
import com.example.dsgmap.data.model.StoreResult
import com.example.dsgmap.data.remote.StoreApiService
import com.example.dsgmap.util.MockLogRule
import com.example.dsgmap.util.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import io.mockk.coEvery
import io.mockk.mockk

class StoreRepositoryTest {
    
    // Define rules separately 
    private val mockLogRule = MockLogRule()
    private val mainDispatcherRule = MainDispatcherRule()
    
    // Chain rules to ensure proper execution order
    @get:Rule
    val testRule: TestRule = RuleChain
        .outerRule(mockLogRule)
        .around(mainDispatcherRule)
    
    private lateinit var repository: StoreRepository
    private val mockApiService = mockk<StoreApiService>()
    
    @Before
    fun setup() {
        repository = StoreRepository(mockApiService)
    }
    
    @Test
    fun `searchStoresByZipCode returns mapped store ui models on success`() = runTest {
        // Given
        val zipCode = "15108"
        val mockStore = Store(
            location = "123",
            name = "DSG Store",
            street1 = "123 Main St",
            city = "Pittsburgh",
            state = "PA",
            zip = "15108"
        )
        val storeResult = StoreResult(
            store = mockStore,
            distance = 10.5,
            units = "mi"
        )
        val response = StoreResponse(results = listOf(storeResult))
        
        coEvery { mockApiService.searchStoresByZipCode(zipCode = zipCode) } returns response
        
        // When
        val result = repository.searchStoresByZipCode(zipCode).first()
        
        // Then
        assertTrue(result.isSuccess)
        val stores = result.getOrNull()!!
        assertEquals(1, stores.size)
        assertEquals("123", stores[0].id)
        assertEquals("DSG Store", stores[0].name)
        assertEquals(10.5, stores[0].distance, 0.001)
        assertEquals("Pittsburgh, PA", stores[0].location)
    }
    
    @Test
    fun `searchStoresByLocation returns mapped store ui models on success`() = runTest {
        // Given
        val latitude = 40.4406
        val longitude = -79.9959
        val latLng = "$latitude,$longitude"
        
        val mockStore = Store(
            location = "456",
            name = "DSG Store 2",
            street1 = "456 Oak St",
            city = "Philadelphia",
            state = "PA",
            zip = "19019"
        )
        val storeResult = StoreResult(
            store = mockStore,
            distance = 25.3,
            units = "mi"
        )
        val response = StoreResponse(results = listOf(storeResult))
        
        coEvery { mockApiService.searchStoresByLocation(latLong = latLng) } returns response
        
        // When
        val result = repository.searchStoresByLocation(latitude, longitude).first()
        
        // Then
        assertTrue(result.isSuccess)
        val stores = result.getOrNull()!!
        assertEquals(1, stores.size)
        assertEquals("456", stores[0].id)
        assertEquals("DSG Store 2", stores[0].name)
        assertEquals(25.3, stores[0].distance, 0.001)
        assertEquals("Philadelphia, PA", stores[0].location)
    }
    
    @Test
    fun `searchStoresByZipCode returns error when api call fails`() = runTest {
        // Given
        val zipCode = "15108"
        val exception = RuntimeException("API Error")
        
        coEvery { mockApiService.searchStoresByZipCode(zipCode = zipCode) } throws exception
        
        // When
        val result = repository.searchStoresByZipCode(zipCode).first()
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
    
    @Test
    fun `searchStoresByLocation returns error when api call fails`() = runTest {
        // Given
        val latitude = 40.4406
        val longitude = -79.9959
        val latLng = "$latitude,$longitude"
        val exception = RuntimeException("API Error")
        
        coEvery { mockApiService.searchStoresByLocation(latLong = latLng) } throws exception
        
        // When
        val result = repository.searchStoresByLocation(latitude, longitude).first()
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}