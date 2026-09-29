package com.uzuu.customer.domain.repository

import com.uzuu.customer.core.result.ApiResult
import com.uzuu.customer.domain.model.Order
import com.uzuu.customer.domain.model.PagedResult

interface OrderRepository {
    suspend fun checkout(
        paymentMethod: String,
        voucherCode: String? = null,
        platform: String = "mobile"
    ): ApiResult<Order>
    suspend fun checkoutSelected(
        paymentMethod: String,
        itemIds: List<Long>,
        voucherCode: String? = null,
        platform: String = "mobile"
    ): ApiResult<Order>
    suspend fun getMyOrders(page: Int): ApiResult<PagedResult<Order>>
}
