package com.uzuu.customer.feature.middle.home

import com.uzuu.customer.domain.model.CategoryItem
import com.uzuu.customer.domain.model.Event
import com.uzuu.customer.feature.middle.home.eventExtra.CategoryWithEvents

data class HomeUiState(
    val isLoading: Boolean = false,
    val isLastPage: Boolean = false,
    val allEvents: List<Event> = emptyList(),
    val events: List<Event> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val selectedCategoryId: Int = -1,
    val searchQuery: String = "",
    val cityFilter: String = "",
    val minPriceFilter: Double? = null,
    val maxPriceFilter: Double? = null,
    val groupedEvents: List<CategoryWithEvents> = emptyList(),
    val suggestionEvents: List<Event> = emptyList()
)

