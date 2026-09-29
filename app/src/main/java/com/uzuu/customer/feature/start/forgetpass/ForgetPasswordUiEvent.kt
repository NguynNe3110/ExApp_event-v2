package com.uzuu.customer.feature.start.forgetpass

sealed class ForgetPasswordUiEvent {
    data class Toast(val message: String) : ForgetPasswordUiEvent()
    data class NavigateToOtp(val email: String) : ForgetPasswordUiEvent()
    object NavigateToLogin : ForgetPasswordUiEvent()
}
