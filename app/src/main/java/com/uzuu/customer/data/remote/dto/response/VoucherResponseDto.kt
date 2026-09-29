package com.uzuu.customer.data.remote.dto.response

data class VoucherResponseDto(
    val id: Long,
    val code: String,
    val discountType: String,
    val amount: Double,
    val maxDiscount: Double?,
    val minOrderAmount: Double?,
    val quantity: Int,
    val startDate: String?,
    val endDate: String?,
    val eventId: Long?,
    val eventName: String?,
    val creatorName: String?
)
