package com.uzuu.customer.data.remote.datasource

import com.uzuu.customer.data.remote.api.AuthApi
import com.uzuu.customer.data.remote.dto.BaseResponseDto
import com.uzuu.customer.data.remote.dto.request.ForgotPasswordRequestDto
import com.uzuu.customer.data.remote.dto.request.LoginRequestDto
import com.uzuu.customer.data.remote.dto.request.RegisterRequestDto
import com.uzuu.customer.data.remote.dto.request.ResetPasswordRequestDto
import com.uzuu.customer.data.remote.dto.response.TokenResponseDto
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val authApi: AuthApi
) {

    suspend fun register(request: RegisterRequestDto): BaseResponseDto<String> {
        return authApi.register(request)
    }

    suspend fun login(request: LoginRequestDto): BaseResponseDto<TokenResponseDto> {
        return authApi.login(request)
    }

    suspend fun forgotPassword(email: String): BaseResponseDto<String> {
        return authApi.forgotPassword(ForgotPasswordRequestDto(email))
    }

    suspend fun resetPassword(otp: String, newPassword: String): BaseResponseDto<String> {
        return authApi.resetPassword(ResetPasswordRequestDto(otp, newPassword))
    }
}