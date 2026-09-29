package com.uzuu.customer.feature.middle.voucher

import com.uzuu.customer.domain.model.Voucher

data class VoucherListUiState(
    val isLoading: Boolean = false,
    val vouchers: List<Voucher> = emptyList()
)
