package com.uzuu.customer.data.remote.api

import com.uzuu.customer.data.remote.dto.BaseResponseDto
import com.uzuu.customer.data.remote.dto.response.EventResponseDto
import com.uzuu.customer.data.remote.dto.response.PageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface EventApi {
    @GET("events/search")
    suspend fun searchEvents(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("search") search: String? = null,
        @Query("province") province: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("categoryId") categoryId: Long? = null
    ): BaseResponseDto<PageResponse<EventResponseDto>>
}
