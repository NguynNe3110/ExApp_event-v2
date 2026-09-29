package com.uzuu.customer.data.remote.dto.request

data class ResetPasswordRequestDto(
    val otp: String,
    val newPassword: String
)
