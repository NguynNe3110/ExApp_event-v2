package com.uzuu.customer.data.remote.api

import com.uzuu.customer.data.remote.dto.BaseResponseDto
import com.uzuu.customer.data.remote.dto.response.OrderResponseDto
import com.uzuu.customer.data.remote.dto.response.PageResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OrderApi {

    @POST("bookings/checkout")
    suspend fun checkout(
        @Query("paymentMethod") paymentMethod: String,
        @Query("voucherCode") voucherCode: String? = null,
        @Query("platform") platform: String = "mobile"
    ): BaseResponseDto<OrderResponseDto>

    @GET("bookings")
    suspend fun getMyOrders(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): BaseResponseDto<PageResponse<OrderResponseDto>>

    @POST("bookings/checkout-selected")
    suspend fun checkoutSelected(
        @Query("paymentMethod") paymentMethod: String,
        @Query("voucherCode") voucherCode: String? = null,
        @Query("platform") platform: String = "mobile",
        @Body itemIds: List<Long>
    ): BaseResponseDto<OrderResponseDto>
}
