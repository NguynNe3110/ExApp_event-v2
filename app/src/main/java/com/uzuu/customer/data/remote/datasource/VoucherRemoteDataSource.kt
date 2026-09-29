package com.uzuu.customer.data.remote.datasource

import com.uzuu.customer.data.remote.api.VoucherApi
import com.uzuu.customer.data.remote.dto.request.VoucherValidationRequestDto
import javax.inject.Inject

class VoucherRemoteDataSource @Inject constructor(private val voucherApi: VoucherApi) {
    suspend fun getVouchers(page: Int = 1, size: Int = 50) =
        voucherApi.getVouchers(page.coerceAtLeast(1), size)

    suspend fun getVouchersByEvent(eventId: Long) =
        voucherApi.getVouchersByEvent(eventId)

    suspend fun validateVoucher(code: String, eventAmounts: Map<String, Double>) =
        voucherApi.validateVoucher(VoucherValidationRequestDto(code = code, eventAmounts = eventAmounts))
}
