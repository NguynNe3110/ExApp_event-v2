package com.uzuu.customer.feature.middle.personal.changePassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzuu.customer.core.result.ApiResult
import com.uzuu.customer.data.remote.dto.request.UserRequestDto
import com.uzuu.customer.domain.model.Login
import com.uzuu.customer.domain.repository.AuthRepository
import com.uzuu.customer.domain.repository.UserRepository
import com.uzuu.customer.data.session.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            emitEvent("Vui lòng điền đầy đủ thông tin")
            return
        }
        if (newPassword != confirmPassword) {
            emitEvent("Mật khẩu mới không khớp")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val username = SessionManager.getUsername()
            if (username.isNullOrBlank()) {
                emitEvent("Không tìm thấy thông tin người dùng")
                _isLoading.value = false
                return@launch
            }

            when (val loginRes = authRepo.loginRequest(Login(username, oldPassword))) {
                is ApiResult.Success -> {
                    // authenticated, fetch current info
                    when (val infoRes = userRepo.getMyInfo()) {
                        is ApiResult.Success -> {
                            val info = infoRes.data
                            val request = UserRequestDto(
                                password = newPassword,
                                email = info.email ?: "",
                                fullName = info.fullName ?: "",
                                phone = info.phone ?: "",
                                address = info.address ?: ""
                            )
                            when (val updateRes = userRepo.updateInfo(username, request)) {
                                is ApiResult.Success -> {
                                    emitEvent("Đổi mật khẩu thành công")
                                }
                                is ApiResult.Error -> emitEvent(updateRes.message ?: "Cập nhật thất bại")
                            }
                        }
                        is ApiResult.Error -> emitEvent(infoRes.message ?: "Không lấy được thông tin người dùng")
                    }
                }
                is ApiResult.Error -> {
                    emitEvent(loginRes.message ?: "Xác thực thất bại, kiểm tra mật khẩu hiện tại")
                }
            }

            _isLoading.value = false
        }
    }

    private fun emitEvent(msg: String) {
        viewModelScope.launch { _events.emit(msg) }
    }
}
