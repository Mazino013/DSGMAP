import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import app.cash.turbine.test
import com.example.dsgmap.data.model.StoreUiModel
import com.example.dsgmap.data.repository.StoreRepository
import com.example.dsgmap.ui.StoreSearchViewModel
import com.example.dsgmap.util.LocationProvider
import com.example.dsgmap.util.MainDispatcherRule
import com.example.dsgmap.util.MockLogRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class StoreSearchViewModelTest {

    private val instantExecutorRule = InstantTaskExecutorRule()
    private val mockLogRule = MockLogRule()
    private val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val testRule: TestRule = RuleChain
        .outerRule(mockLogRule)
        .around(mainDispatcherRule)
        .around(instantExecutorRule)

    private lateinit var viewModel: StoreSearchViewModel
    private val storeRepository = mockk<StoreRepository>()
    private val locationProvider = mockk<LocationProvider>()
    private val lifecycleOwner = mockk<LifecycleOwner>()
    private val lifecycle = mockk<Lifecycle>()

    @Before
    fun setup() {
        every { lifecycleOwner.lifecycle } returns lifecycle
        every { lifecycle.currentState } returns Lifecycle.State.RESUMED
        justRun { lifecycle.addObserver(any()) }
        justRun { lifecycle.removeObserver(any()) }

        viewModel = StoreSearchViewModel(storeRepository, locationProvider)
    }

    @Test
    fun `searchStoresByZipCode with valid zipCode properly transitions through loading states`() = runTest {
        // Given
        val zipCode = "15108"

        // Create UI models that would be returned from repository
        val expectedUiModels = listOf(
            StoreUiModel(
                id = "1",
                name = "DSG Pittsburgh",
                distance = 5.2,
                location = "Pittsburgh, PA",
                address = "123 Main St, Pittsburgh, PA 15108"
            ),
            StoreUiModel(
                id = "2",
                name = "DSG Philadelphia",
                distance = 15.7,
                location = "Philadelphia, PA",
                address = "456 Market St, Philadelphia, PA 19102"
            )
        )

        coEvery { storeRepository.searchStoresByZipCode(zipCode) } returns
            flowOf(Result.success(expectedUiModels))

        // Collect state changes from uiState flow to test loading state transitions
        viewModel.uiState.test(timeout = 5.seconds) {
            // Initial state check
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)
            assertTrue(initialState.stores.isEmpty())
            assertNull(initialState.error)

            // When
            viewModel.searchStoresByZipCode(zipCode)

            // First state update: loading = true
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertTrue(loadingState.stores.isEmpty())
            assertNull(loadingState.error)

            // Final state: loaded with stores
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertEquals(expectedUiModels, finalState.stores)
            assertFalse(finalState.isEmpty)
            assertNull(finalState.error)

            // No more emissions expected
            expectNoEvents()
        }
    }

    @Test
    fun `searchStoresByZipCode with empty results properly transitions through loading states`() = runTest {
        val zipCode = "00000"
        val emptyStores = emptyList<StoreUiModel>()

        coEvery { storeRepository.searchStoresByZipCode(zipCode) } returns
            flowOf(Result.success(emptyStores))

        viewModel.uiState.test(timeout = 5.seconds) {
            // Initial state check
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)
            assertTrue(initialState.stores.isEmpty())
            assertNull(initialState.error)

            viewModel.searchStoresByZipCode(zipCode)

            // First state update: loading = true
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertTrue(loadingState.stores.isEmpty())
            assertNull(loadingState.error)

            // Final state: loaded with empty stores
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertTrue(finalState.stores.isEmpty())
            assertTrue(finalState.isEmpty)
            assertNull(finalState.error)

            expectNoEvents()
        }
    }

    @Test
    fun `searchStoresByZipCode with error properly transitions through loading states`() = runTest {
        val zipCode = "15108"
        val errorMessage = "Failed to load stores"
        val exception = Exception(errorMessage)

        coEvery { storeRepository.searchStoresByZipCode(zipCode) } returns
            flowOf(Result.failure(exception))

        viewModel.uiState.test(timeout = 5.seconds) {
            // Initial state check
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)
            assertTrue(initialState.stores.isEmpty())
            assertNull(initialState.error)

            viewModel.searchStoresByZipCode(zipCode)

            // First state update: loading = true
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertTrue(loadingState.stores.isEmpty())
            assertNull(loadingState.error)

            // Final state: error state
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            val errorMsg = finalState.error
            assertNotNull(errorMsg)
            assertTrue(errorMsg!!.contains(errorMessage))

            expectNoEvents()
        }
    }

    // coVerify {Check}

    @Test
    fun `searchStoresByCurrentLocation with valid location properly transitions through loading states`() = runTest {
        val mockLocation = mockk<Location>()
        every { mockLocation.latitude } returns 40.4406
        every { mockLocation.longitude } returns -79.9959

        val expectedUiModels = listOf(
            StoreUiModel(
                id = "1",
                name = "DSG Pittsburgh",
                distance = 5.2,
                location = "Pittsburgh, PA",
                address = "123 Main St, Pittsburgh, PA 15108"
            )
        )

        coEvery { locationProvider.getCurrentLocation() } returns mockLocation
        coEvery { storeRepository.searchStoresByLocation(40.4406, -79.9959) } returns
            flowOf(Result.success(expectedUiModels))

        viewModel.uiState.test(timeout = 5.seconds) {
            // Initial state check
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)
            assertTrue(initialState.stores.isEmpty())
            assertNull(initialState.error)

            viewModel.searchStoresByCurrentLocation()

            // First state update: loading = true
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertTrue(loadingState.stores.isEmpty())
            assertNull(loadingState.error)

            // Final state: loaded with stores
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertEquals(expectedUiModels, finalState.stores)
            assertFalse(finalState.isEmpty)
            assertNull(finalState.error)

            expectNoEvents()
        }
    }

    @Test
    fun `searchStoresByCurrentLocation with null location properly transitions through loading states`() = runTest {
        coEvery { locationProvider.getCurrentLocation() } returns null

        viewModel.uiState.test(timeout = 5.seconds) {
            // Initial state check
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)
            assertTrue(initialState.stores.isEmpty())
            assertNull(initialState.error)

            viewModel.searchStoresByCurrentLocation()

            // First state update: loading = true
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertTrue(loadingState.stores.isEmpty())
            assertNull(loadingState.error)

            // Final state: error state
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            val errorMsg = finalState.error
            assertNotNull(errorMsg)
            assertTrue(errorMsg!!.contains("Could not determine your location"))

            expectNoEvents()
        }
    }

    @Test
    fun `onPause clears the store list and properly transitions states`() = runTest {
        // First populate with data
        val zipCode = "15108"
        val stores = listOf(
            StoreUiModel(
                id = "1",
                name = "DSG Pittsburgh",
                distance = 5.2,
                location = "Pittsburgh, PA",
                address = "123 Main St"
            )
        )

        coEvery { storeRepository.searchStoresByZipCode(zipCode) } returns
            flowOf(Result.success(stores))

        viewModel.uiState.test(timeout = 5.seconds) {
            // Initial state
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)
            assertTrue(initialState.stores.isEmpty())

            // Load stores
            viewModel.searchStoresByZipCode(zipCode)

            // Loading state
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            // Stores loaded state
            val loadedState = awaitItem()
            assertFalse(loadedState.isLoading)
            assertEquals(stores, loadedState.stores)

            // Simulate lifecycle pause event
            viewModel.onPause(lifecycleOwner)

            // State after pause: stores cleared
            val clearedState = awaitItem()
            assertFalse(clearedState.isLoading)
            assertTrue(clearedState.stores.isEmpty())
            assertNull(clearedState.error)

            expectNoEvents()
        }
    }
}