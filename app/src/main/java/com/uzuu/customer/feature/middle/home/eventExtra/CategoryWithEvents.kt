package com.uzuu.customer.feature.middle.home.eventExtra

import com.uzuu.customer.domain.model.Event

data class CategoryWithEvents(
    val categoryId: Long,
    val categoryName: String,
    val displayedEvents: List<Event>,  // 2-4 events to display
    val totalEventCount: Int,           // Total count in this category
    val hasMoreEvents: Boolean          // Whether there are more events than displayed
)