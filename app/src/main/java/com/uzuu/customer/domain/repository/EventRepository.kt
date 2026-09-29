package com.uzuu.customer.domain.repository

import com.uzuu.customer.domain.model.Event
import com.uzuu.customer.domain.model.PagedResult

interface EventRepository {
    suspend fun getEvent(page: Int): PagedResult<Event>
    suspend fun searchEvents(
        page: Int,
        search: String?,
        province: String?,
        minPrice: Double?,
        maxPrice: Double?,
        categoryId: Long? = null
    ): PagedResult<Event>
    suspend fun getCachedEvents(): List<Event>
}


