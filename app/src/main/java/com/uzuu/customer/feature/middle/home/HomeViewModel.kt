package com.uzuu.customer.feature.middle.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzuu.customer.domain.model.CategoryItem
import com.uzuu.customer.domain.model.Event
import com.uzuu.customer.domain.repository.CategoryRepository
import com.uzuu.customer.domain.repository.EventRepository
import com.uzuu.customer.feature.middle.home.eventExtra.CategoryWithEvents
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val eventRepo: EventRepository,
    private val categoryRepo: CategoryRepository
) : ViewModel() {

    private var currentPage = 1
    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState = _homeState.asStateFlow()

    private val _homeEvent = MutableSharedFlow<HomeUiEvent>(extraBufferCapacity = 3)
    val homeEvent = _homeEvent.asSharedFlow()

    private var pollingJob: Job? = null
    private var searchJob: Job? = null

    fun init() {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true) }

            val categoriesDeferred = async {
                try {
                    val result = categoryRepo.getAllCategories()
                    listOf(CategoryItem(id = -1, name = "Tat ca", isSelected = false)) + result
                } catch (e: Exception) {
                    val msg = e.message ?: ""
                    if (msg.contains("401")) {
                        _homeEvent.emit(HomeUiEvent.Toast("Phiên đăng nhập hết hạn"))
                        _homeEvent.emit(HomeUiEvent.navigateBack)
                    }
                    emptyList<CategoryItem>()
                }
            }

            val eventsDeferred = async {
                try {
                    eventRepo.getEvent(1)
                } catch (e: Exception) {
                    val msg = e.message ?: ""
                    if (msg.contains("401")) {
                        _homeEvent.emit(HomeUiEvent.Toast("Phiên đăng nhập hết hạn"))
                        _homeEvent.emit(HomeUiEvent.navigateBack)
                    }
                    null
                }
            }

            val categories = categoriesDeferred.await()
            val eventsResult = eventsDeferred.await()
            val events = eventsResult?.data ?: emptyList()

            _homeState.update { state ->
                state.copy(
                    isLoading = false,
                    categories = categories,
                    allEvents = events,
                    events = events,
                    groupedEvents = getGroupedEventsForHome(events, categories).first,
                    suggestionEvents = getGroupedEventsForHome(events, categories).second,
                    isLastPage = eventsResult?.isLast ?: true
                )
            }

            currentPage = 2
        }
    }

    fun loadMoreEvents() {
        val state = _homeState.value
        if (state.isLoading || state.isLastPage) return
        loadEventsPage(page = currentPage, append = true)
    }

    fun onCategorySelected(category: CategoryItem) {
        _homeState.update { state ->
            val updatedCategories = state.categories.map {
                it.copy(isSelected = it.id == category.id)
            }
            val filtered = filterForVisibleList(
                events = state.allEvents,
                categoryId = category.id,
                categories = updatedCategories,
                state = state
            )

            state.copy(
                categories = updatedCategories,
                selectedCategoryId = category.id,
                events = filtered,
                groupedEvents = getGroupedEventsForHome(filtered, updatedCategories).first,
                suggestionEvents = getGroupedEventsForHome(filtered, updatedCategories).second
            )
        }
    }

    fun onSearch(query: String) {
        _homeState.update { state ->
            val nextState = state.copy(searchQuery = query)
            val filtered = filterForVisibleList(
                events = nextState.allEvents,
                categoryId = nextState.selectedCategoryId,
                categories = nextState.categories,
                state = nextState,
                forceLocalFilters = true
            )
            nextState.copy(
                events = filtered,
                groupedEvents = getGroupedEventsForHome(filtered, nextState.categories).first,
                suggestionEvents = getGroupedEventsForHome(filtered, nextState.categories).second
            )
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400L)
            loadEventsPage(page = 1, append = false)
        }
    }

    fun onFiltersChanged(city: String, minPrice: Double?, maxPrice: Double?) {
        _homeState.update {
            it.copy(
                cityFilter = city.trim(),
                minPriceFilter = minPrice,
                maxPriceFilter = maxPrice
            )
        }
        loadEventsPage(page = 1, append = false)
    }

    fun clearFilters() {
        _homeState.update {
            it.copy(
                cityFilter = "",
                minPriceFilter = null,
                maxPriceFilter = null
            )
        }
        loadEventsPage(page = 1, append = false)
    }

    fun startPolling(intervalMs: Long = 30_000L) {
        stopPolling()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(intervalMs)
                refresh()
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun refresh() {
        loadEventsPage(page = 1, append = false, showLoading = false)
    }

    private fun loadEventsPage(
        page: Int,
        append: Boolean,
        showLoading: Boolean = true
    ) {
        viewModelScope.launch {
            if (showLoading) _homeState.update { it.copy(isLoading = true) }
            try {
                val stateBeforeCall = _homeState.value
                val result = if (hasServerFilters(stateBeforeCall)) {
                    eventRepo.searchEvents(
                        page = page,
                        search = stateBeforeCall.searchQuery.takeIf { it.isNotBlank() },
                        province = stateBeforeCall.cityFilter.takeIf { it.isNotBlank() },
                        minPrice = stateBeforeCall.minPriceFilter,
                        maxPrice = stateBeforeCall.maxPriceFilter
                    )
                } else {
                    eventRepo.getEvent(page)
                }

                currentPage = page + 1
                _homeState.update { state ->
                    val allEvents = if (append) state.allEvents + result.data else result.data
                    val filtered = filterForVisibleList(
                        events = allEvents,
                        categoryId = state.selectedCategoryId,
                        categories = state.categories,
                        state = state
                    )

                    state.copy(
                        isLoading = false,
                        allEvents = allEvents,
                        events = filtered,
                        groupedEvents = getGroupedEventsForHome(filtered, state.categories).first,
                        suggestionEvents = getGroupedEventsForHome(filtered, state.categories).second,
                        isLastPage = result.isLast
                    )
                }
            } catch (e: Exception) {
                val msg = e.message ?: ""
                if (msg.contains("401")) {
                    _homeEvent.emit(HomeUiEvent.Toast("Phiên đăng nhập hết hạn, vui lòng đăng nhập lại"))
                    _homeEvent.emit(HomeUiEvent.navigateBack)
                } else {
                    _homeEvent.emit(HomeUiEvent.Toast(msg.ifBlank { "Không tải được sự kiện" }))
                }
                _homeState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun filterForVisibleList(
        events: List<Event>,
        categoryId: Int,
        categories: List<CategoryItem>,
        state: HomeUiState,
        forceLocalFilters: Boolean = false
    ): List<Event> {
        return filterByCategoryAndQuery(
            events = events,
            categoryId = categoryId,
            categories = categories,
            query = state.searchQuery,
            city = state.cityFilter,
            minPrice = state.minPriceFilter,
            maxPrice = state.maxPriceFilter,
            applyNonCategoryFilters = forceLocalFilters || !hasServerFilters(state)
        )
    }

    private fun filterByCategoryAndQuery(
        events: List<Event>,
        categoryId: Int,
        categories: List<CategoryItem>,
        query: String,
        city: String = "",
        minPrice: Double? = null,
        maxPrice: Double? = null,
        applyNonCategoryFilters: Boolean = true
    ): List<Event> {
        var result = events

        if (categoryId != -1) {
            val selectedName = categories.find { it.id == categoryId }?.name
            if (selectedName != null) {
                result = result.filter { it.categoryName == selectedName }
            }
        }

        if (!applyNonCategoryFilters) return result

        if (query.isNotBlank()) {
            val normalizedQuery = query.trim()
            result = result.filter {
                it.name.contains(normalizedQuery, ignoreCase = true) ||
                    it.location.contains(normalizedQuery, ignoreCase = true)
            }
        }

        if (city.isNotBlank()) {
            val normalizedCity = city.trim()
            result = result.filter {
                it.location.contains(normalizedCity, ignoreCase = true)
            }
        }

        if (minPrice != null || maxPrice != null) {
            result = result.filter { event ->
                val price = event.ticketTypes.minOfOrNull { it.price }
                val aboveMin = minPrice == null || (price != null && price >= minPrice)
                val belowMax = maxPrice == null || (price != null && price <= maxPrice)
                aboveMin && belowMax
            }
        }

        return result
    }

    private fun hasServerFilters(state: HomeUiState): Boolean {
        return state.searchQuery.isNotBlank() ||
            state.cityFilter.isNotBlank() ||
            state.minPriceFilter != null ||
            state.maxPriceFilter != null
    }
    fun getGroupedEventsForHome(events: List<Event>, categories: List<CategoryItem>): Pair<List<CategoryWithEvents>, List<Event>> {
        // Group events by category name
        val eventsByCategory = events.groupBy { it.categoryName }

        // Create CategoryWithEvents for each category with events
        val groupedByCategory = eventsByCategory.entries
            .filter { it.value.isNotEmpty() }  // Only show categories with events
            .map { (categoryName, categoryEvents) ->
                // Determine how many to display based on count
                val displayCount = when {
                    categoryEvents.size == 1 -> 0  // Single events go to suggestions
                    categoryEvents.size <= 3 -> 2
                    else -> 4
                }

                CategoryWithEvents(
                    categoryId = categories.find { it.name == categoryName }?.id?.toLong() ?: -1L,
                    categoryName = categoryName,
                    displayedEvents = categoryEvents.take(displayCount),
                    totalEventCount = categoryEvents.size,
                    hasMoreEvents = categoryEvents.size > displayCount
                )
            }
            .filter { it.displayedEvents.isNotEmpty() }
            .sortedByDescending { it.displayedEvents.size }  // Show categories with most events first

        // Collect single events for "you might like" section
        val suggestionEvents = eventsByCategory.entries
            .filter { it.value.size == 1 }
            .flatMap { it.value }
            .take(4)  // Limit to 4 suggestion events

        return Pair(groupedByCategory, suggestionEvents)
    }

}
