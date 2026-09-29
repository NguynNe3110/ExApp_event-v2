package com.uzuu.customer.feature.start.login

import com.uzuu.customer.feature.start.register.RegisterUiEvent

sealed class LoginUiEvent {
    data class Toast(val message: String) : LoginUiEvent()

    data class navigateForget(val username: String) : LoginUiEvent()

    object navigateRegister : LoginUiEvent()

    object navigateHome : LoginUiEvent()

    object Loading : LoginUiEvent()
    object Success  : LoginUiEvent()
    object Error : LoginUiEvent()
}