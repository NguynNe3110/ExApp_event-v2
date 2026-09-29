package com.uzuu.customer.feature.middle.checkout

import com.uzuu.customer.domain.model.Order

sealed class CheckoutUiEvent {
    data class Toast(val message: String) : CheckoutUiEvent()
    data class CheckoutSuccess(val order: Order) : CheckoutUiEvent()
}
