package com.uzuu.customer.domain.repository

import com.uzuu.customer.core.result.ApiResult
import com.uzuu.customer.domain.model.BlogPost

interface BlogRepository {
    suspend fun getNews(): ApiResult<List<BlogPost>>
}
