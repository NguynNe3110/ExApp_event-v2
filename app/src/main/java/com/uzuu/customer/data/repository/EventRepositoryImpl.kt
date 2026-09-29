package com.uzuu.customer.data.repository

import com.uzuu.customer.data.local.datasource.EventLocalDataSource
import com.uzuu.customer.data.mapper.toEntity
import com.uzuu.customer.data.mapper.eventDtoToDomain
import com.uzuu.customer.data.mapper.toDomain
import com.uzuu.customer.data.remote.datasource.EventRemoteDataSource
import com.uzuu.customer.domain.model.Event
import com.uzuu.customer.domain.model.PagedResult
import com.uzuu.customer.domain.repository.EventRepository
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventRemote: EventRemoteDataSource,
    private val eventLocal: EventLocalDataSource
) : EventRepository {

    private val visibleEventStatuses = setOf("PENDING", "ON_SALE", "UPCOMING", "OPENING")

    override suspend fun getEvent(page: Int): PagedResult<Event> {
        println("DEBUG [EventRepositoryImpl] getEvent(page=$page) called")

        // Luôn ưu tiên gọi API trước
        return try {
            val response = eventRemote.getEvent(page)
            println("DEBUG [EventRepositoryImpl] raw response — code=${response.code}, message='${response.message}'")

            val pageData = response.result
            println("DEBUG [EventRepositoryImpl] result — content=${pageData.content.size}, totalPages=${pageData.totalPages}, totalElements=${pageData.totalElements}, isLast=${pageData.last}, currentPage=${pageData.number}")

            if (pageData.content.isNotEmpty()) {
                val events = pageData.content
                    .map { it.eventDtoToDomain() }
                    .filter { it.isVisibleEvent() }
                val entities = events.map { it.toEntity() }
                eventLocal.cacheEvents(entities)
                println("DEBUG [EventRepositoryImpl] Cached ${events.size} events")
                
                PagedResult(
                    data = events,
                    page = pageData.number,
                    totalPages = pageData.totalPages,
                    totalElements = pageData.totalElements,
                    isLast = pageData.last
                )
            } else {
                println("DEBUG [EventRepositoryImpl] WARNING: content is empty!")
                // Nếu API trả về rỗng nhưng có cache thì dùng cache
                val cachedEvents = getCachedEvents()
                if (cachedEvents.isNotEmpty() && page == 1) {
                    println("DEBUG [EventRepositoryImpl] Using ${cachedEvents.size} cached events as fallback")
                    return PagedResult(
                        data = cachedEvents,
                        page = 0,
                        totalPages = 1,
                        totalElements = cachedEvents.size.toLong(),
                        isLast = true
                    )
                }
                PagedResult(
                    data = emptyList(),
                    page = 0,
                    totalPages = 1,
                    totalElements = 0,
                    isLast = true
                )
            }
        } catch (e: Exception) {
            println("DEBUG [EventRepositoryImpl] Error fetching events: ${e.message}")
            // Chỉ dùng cache khi gọi API thất bại
            val cachedEvents = getCachedEvents()
            if (cachedEvents.isNotEmpty()) {
                println("DEBUG [EventRepositoryImpl] Returning ${cachedEvents.size} cached events as fallback")
                return PagedResult(
                    data = cachedEvents,
                    page = 0,
                    totalPages = 1,
                    totalElements = cachedEvents.size.toLong(),
                    isLast = true
                )
            }
            throw e
        }
    }

    override suspend fun searchEvents(
        page: Int,
        search: String?,
        province: String?,
        minPrice: Double?,
        maxPrice: Double?,
        categoryId: Long?
    ): PagedResult<Event> {
        return try {
            val response = eventRemote.searchEvents(
                page = page,
                search = search?.takeIf { it.isNotBlank() },
                province = province?.takeIf { it.isNotBlank() },
                minPrice = minPrice,
                maxPrice = maxPrice,
                categoryId = categoryId
            )
            val pageData = response.result
            val events = pageData.content
                .map { it.eventDtoToDomain() }
                .filter { it.isVisibleEvent() }
            if (page == 1 && events.isNotEmpty()) {
                eventLocal.cacheEvents(events.map { it.toEntity() })
            }
            PagedResult(
                data = events,
                page = pageData.number,
                totalPages = pageData.totalPages,
                totalElements = pageData.totalElements,
                isLast = pageData.last
            )
        } catch (e: Exception) {
            println("DEBUG [EventRepositoryImpl] Error searching events: ${e.message}")
            if (page == 1) {
                val cached = getCachedEvents()
                if (cached.isNotEmpty()) {
                    var filtered = cached.filterBySearchParams(search, province, minPrice, maxPrice)
                    if (categoryId != null) {
                        filtered = filtered.filter { it.categoryId == categoryId }
                    }
                    return PagedResult(
                        data = filtered,
                        page = 0,
                        totalPages = 1,
                        totalElements = filtered.size.toLong(),
                        isLast = true
                    )
                }
            }
            throw e
        }
    }

    override suspend fun getCachedEvents(): List<Event> {
        return try {
            eventLocal.getAllEvents().map { it.toDomain() }.filter { it.isVisibleEvent() }
        } catch (e: Exception) {
            println("DEBUG [EventRepositoryImpl] Error getting cached events: ${e.message}")
            emptyList()
        }
    }

    private fun Event.isVisibleEvent(): Boolean {
        return status.uppercase() in visibleEventStatuses
    }

    private fun List<Event>.filterBySearchParams(
        search: String?,
        province: String?,
        minPrice: Double?,
        maxPrice: Double?
    ): List<Event> {
        val query = search?.trim().orEmpty()
        val city = province?.trim().orEmpty()
        return filter { event ->
            val minTicketPrice = event.ticketTypes.minOfOrNull { it.price }
            val matchesSearch = query.isBlank() ||
                event.name.contains(query, ignoreCase = true) ||
                event.location.contains(query, ignoreCase = true)
            val matchesCity = city.isBlank() || event.location.contains(city, ignoreCase = true)
            val matchesMin = minPrice == null || (minTicketPrice != null && minTicketPrice >= minPrice)
            val matchesMax = maxPrice == null || (minTicketPrice != null && minTicketPrice <= maxPrice)
            matchesSearch && matchesCity && matchesMin && matchesMax
        }
    }
}
