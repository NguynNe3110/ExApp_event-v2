package com.uzuu.customer.data.remote.datasource

import com.uzuu.customer.data.remote.api.OrderApi
import com.uzuu.customer.data.remote.dto.BaseResponseDto
import com.uzuu.customer.data.remote.dto.response.OrderResponseDto
import com.uzuu.customer.data.remote.dto.response.PageResponse
import javax.inject.Inject

class OrderRemoteDataSource @Inject constructor(private val orderApi: OrderApi) {

    suspend fun checkout(
        paymentMethod: String,
        voucherCode: String?,
        platform: String = "mobile"
    ): BaseResponseDto<OrderResponseDto> =
        orderApi.checkout(paymentMethod, voucherCode, platform)

    suspend fun checkoutSelected(
        paymentMethod: String,
        itemIds: List<Long>,
        voucherCode: String?,
        platform: String = "mobile"
    ): BaseResponseDto<OrderResponseDto> =
        orderApi.checkoutSelected(
            paymentMethod = paymentMethod,
            voucherCode = voucherCode,
            platform = platform,
            itemIds = itemIds
        )

    suspend fun getMyOrders(page: Int): BaseResponseDto<PageResponse<OrderResponseDto>> =
        orderApi.getMyOrders(page, 20)
}
