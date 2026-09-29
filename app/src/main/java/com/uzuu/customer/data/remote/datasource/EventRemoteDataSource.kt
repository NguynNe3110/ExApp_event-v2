package com.uzuu.customer.data.remote.datasource

import com.uzuu.customer.data.remote.api.EventApi
import com.uzuu.customer.data.remote.dto.BaseResponseDto
import com.uzuu.customer.data.remote.dto.response.EventResponseDto
import com.uzuu.customer.data.remote.dto.response.PageResponse
import javax.inject.Inject

class EventRemoteDataSource @Inject constructor(
    private val eventApi: EventApi
) {
    suspend fun getEvent(page: Int):  BaseResponseDto<PageResponse<EventResponseDto>> {
        return eventApi.searchEvents(page = page, size = 10)
    }

    suspend fun searchEvents(
        page: Int,
        search: String?,
        province: String?,
        minPrice: Double?,
        maxPrice: Double?,
        categoryId: Long? = null
    ): BaseResponseDto<PageResponse<EventResponseDto>> {
        return eventApi.searchEvents(
            page = page,
            size = 10,
            search = search,
            province = province,
            minPrice = minPrice,
            maxPrice = maxPrice,
            categoryId = categoryId
        )
    }
}
