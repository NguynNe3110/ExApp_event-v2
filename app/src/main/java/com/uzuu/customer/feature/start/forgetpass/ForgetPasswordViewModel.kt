package com.uzuu.customer.feature.start.forgetpass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzuu.customer.core.result.ApiResult
import com.uzuu.customer.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ForgetPasswordViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgetPasswordUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ForgetPasswordUiEvent>(extraBufferCapacity = 3)
    val uiEvent = _uiEvent.asSharedFlow()

    fun sendOtp(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(400)

            if (email.isBlank()) {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.tryEmit(ForgetPasswordUiEvent.Toast("Vui lòng nhập email"))
                return@launch
            }

            when (val result = authRepo.forgotPassword(email)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.tryEmit(ForgetPasswordUiEvent.Toast("Mã xác thực đã được gửi tới email của bạn"))
                    _uiEvent.tryEmit(ForgetPasswordUiEvent.NavigateToOtp(email))
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.tryEmit(ForgetPasswordUiEvent.Toast(result.message))
                }
            }
        }
    }

    fun resetPassword(otp: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(400)

            if (otp.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.tryEmit(ForgetPasswordUiEvent.Toast("Vui lòng điền đầy đủ thông tin"))
                return@launch
            }

            if (newPassword != confirmPassword) {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.tryEmit(ForgetPasswordUiEvent.Toast("Mật khẩu xác nhận không khớp"))
                return@launch
            }

            when (val result = authRepo.resetPassword(otp, newPassword)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.tryEmit(ForgetPasswordUiEvent.Toast("Đặt lại mật khẩu thành công"))
                    _uiEvent.tryEmit(ForgetPasswordUiEvent.NavigateToLogin)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.tryEmit(ForgetPasswordUiEvent.Toast(result.message))
                }
            }
        }
    }
}
