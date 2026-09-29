package com.uzuu.customer.data.remote.datasource

import com.uzuu.customer.data.remote.api.BlogApi
import com.uzuu.customer.data.remote.dto.BaseResponseDto
import com.uzuu.customer.data.remote.dto.response.BlogPostResponseDto
import com.uzuu.customer.data.remote.dto.response.PageResponse
import javax.inject.Inject

class BlogRemoteDataSource @Inject constructor(
    private val blogApi: BlogApi
) {
    suspend fun getNews(page: Int = 0, size: Int = 10): BaseResponseDto<PageResponse<BlogPostResponseDto>> {
        return blogApi.getNews(page = page, size = size)
    }
}
