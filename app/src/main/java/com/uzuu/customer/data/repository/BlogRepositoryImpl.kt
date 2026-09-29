package com.uzuu.customer.data.repository

import com.uzuu.customer.core.result.ApiResult
import com.uzuu.customer.core.result.safeApiCall
import com.uzuu.customer.data.mapper.toDomain
import com.uzuu.customer.data.remote.datasource.BlogRemoteDataSource
import com.uzuu.customer.domain.model.BlogPost
import com.uzuu.customer.domain.repository.BlogRepository
import javax.inject.Inject

class BlogRepositoryImpl @Inject constructor(
    private val remote: BlogRemoteDataSource
) : BlogRepository {

    private fun isOk(code: Int) = code == 200 || code == 0 || code == 1000

    override suspend fun getNews(): ApiResult<List<BlogPost>> =
        safeApiCall {
            val response = remote.getNews(page = 0, size = 10)
            if (isOk(response.code)) {
                response.result.content.map { it.toDomain() }
            } else {
                throw Exception(response.message.ifBlank { "Không tải được blog" })
            }
        }
}
