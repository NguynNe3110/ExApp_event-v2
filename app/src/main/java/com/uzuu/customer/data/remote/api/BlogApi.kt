package com.uzuu.customer.data.remote.api

import com.uzuu.customer.data.remote.dto.BaseResponseDto
import com.uzuu.customer.data.remote.dto.response.BlogPostResponseDto
import com.uzuu.customer.data.remote.dto.response.PageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BlogApi {
    @GET("blog/posts")
    suspend fun getNews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): BaseResponseDto<PageResponse<BlogPostResponseDto>>
}
