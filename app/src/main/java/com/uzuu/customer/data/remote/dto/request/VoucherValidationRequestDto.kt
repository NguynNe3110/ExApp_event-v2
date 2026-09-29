package com.uzuu.customer.data.remote.dto.request

data class VoucherValidationRequestDto(
    val code: String,
    val eventAmounts: Map<String, Double>
)