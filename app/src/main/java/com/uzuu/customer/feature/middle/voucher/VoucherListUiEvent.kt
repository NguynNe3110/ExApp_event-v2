package com.uzuu.customer.feature.middle.voucher

sealed class VoucherListUiEvent {
    data class Toast(val message: String) : VoucherListUiEvent()
}
