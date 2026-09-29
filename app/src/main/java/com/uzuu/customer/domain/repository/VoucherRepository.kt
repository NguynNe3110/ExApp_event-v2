package com.uzuu.customer.domain.repository

import com.uzuu.customer.core.result.ApiResult
import com.uzuu.customer.domain.model.PagedResult
import com.uzuu.customer.domain.model.Voucher

interface VoucherRepository {
    suspend fun getVouchers(page: Int = 1, size: Int = 50): ApiResult<PagedResult<Voucher>>
    suspend fun getVouchersByEvent(eventId: Long): ApiResult<List<Voucher>>
    suspend fun validateVoucher(code: String, eventAmounts: Map<String, Double>): ApiResult<Double>
}
