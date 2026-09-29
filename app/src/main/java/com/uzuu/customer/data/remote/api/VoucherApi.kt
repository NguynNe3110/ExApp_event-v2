package com.uzuu.customer.data.remote.api

import com.uzuu.customer.data.remote.dto.BaseResponseDto
import com.uzuu.customer.data.remote.dto.request.VoucherValidationRequestDto
import com.uzuu.customer.data.remote.dto.response.PageResponse
import com.uzuu.customer.data.remote.dto.response.VoucherResponseDto
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface VoucherApi {
    @GET("vouchers")
    suspend fun getVouchers(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 50
    ): BaseResponseDto<PageResponse<VoucherResponseDto>>

    @GET("vouchers/event/{eventId}")
    suspend fun getVouchersByEvent(
        @Path("eventId") eventId: Long
    ): BaseResponseDto<List<VoucherResponseDto>>

    @POST("vouchers/validate")
    suspend fun validateVoucher(
        @Body request: VoucherValidationRequestDto
    ): BaseResponseDto<Double>
}
