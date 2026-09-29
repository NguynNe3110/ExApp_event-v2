package com.uzuu.customer.data.repository

import com.uzuu.customer.core.result.ApiResult
import com.uzuu.customer.core.result.safeApiCall
import com.uzuu.customer.data.remote.datasource.VoucherRemoteDataSource
import com.uzuu.customer.data.remote.dto.response.VoucherResponseDto
import com.uzuu.customer.domain.model.PagedResult
import com.uzuu.customer.domain.model.Voucher
import com.uzuu.customer.domain.repository.VoucherRepository
import javax.inject.Inject

class VoucherRepositoryImpl @Inject constructor(
    private val remote: VoucherRemoteDataSource
) : VoucherRepository {

    private fun isOk(code: Int) = code == 200 || code == 0 || code == 1000

    override suspend fun getVouchers(page: Int, size: Int): ApiResult<PagedResult<Voucher>> =
        safeApiCall {
            val r = remote.getVouchers(page, size)
            if (isOk(r.code)) {
                val p = r.result
                println("DEBUG [IN VOUCHERrEPOSITORYIPLM] data : $p")
                PagedResult(
                    data = p.content.map { it.toDomain() },
                    page = p.number,
                    totalPages = p.totalPages,
                    totalElements = p.totalElements,
                    isLast = p.last
                )
            } else {
                throw Exception(r.message ?: "Khong lay duoc danh sach voucher")
            }
        }

    override suspend fun getVouchersByEvent(eventId: Long): ApiResult<List<Voucher>> =
        safeApiCall {
            val r = remote.getVouchersByEvent(eventId)
            if (isOk(r.code)) {
                val list = r.result
                println("DEBUG [IN VOUCHERrEPOSITORYIPLM] data : $list")
                list.map { it.toDomain() }
            } else {
                throw Exception(r.message ?: "Khong lay duoc voucher cho event")
            }
        }

    override suspend fun validateVoucher(code: String, eventAmounts: Map<String, Double>): ApiResult<Double> =
        safeApiCall {
            val r = remote.validateVoucher(code, eventAmounts)
            if (isOk(r.code)) {
                r.result
            } else {
                throw Exception(r.message ?: "Khong tinh duoc gia tri giam gia")
            }
        }
}

private fun VoucherResponseDto.toDomain() = Voucher(
    id = id,
    code = code,
    discountType = discountType,
    amount = amount,
    maxDiscount = maxDiscount,
    minOrderAmount = minOrderAmount,
    quantity = quantity,
    startDate = startDate,
    endDate = endDate,
    eventId = eventId,
    eventName = eventName,
    creatorName = creatorName
)
