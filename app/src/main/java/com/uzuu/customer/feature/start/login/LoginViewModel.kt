package com.uzuu.customer.feature.start.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzuu.customer.core.result.ApiResult
import com.uzuu.customer.data.mapper.userDtoToDomain
import com.uzuu.customer.data.session.SessionManager
import com.uzuu.customer.domain.model.Login
import com.uzuu.customer.domain.model.Users
import com.uzuu.customer.domain.repository.AuthRepository
import com.uzuu.customer.domain.repository.UserRepository
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
class LoginViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState = _loginState.asStateFlow()

    private val _loginEvent = MutableSharedFlow<LoginUiEvent>(extraBufferCapacity = 3)
    val loginEvent = _loginEvent.asSharedFlow()

    fun onClickForgetPass(user: String) {
        _loginEvent.tryEmit(LoginUiEvent.navigateForget(user))
    }

    fun onClickRegister() {
        _loginEvent.tryEmit(LoginUiEvent.navigateRegister)
    }

    fun observe(username: String) {
        _loginState.update { it.copy(username = username) }
    }

    fun onClickLogin(user: String, pass: String) {
        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true) }
            _loginEvent.tryEmit(LoginUiEvent.Loading)
            delay(400)

            if (user.isBlank() || pass.isBlank()) {
                _loginState.update { it.copy(isLoading = false) }
                _loginEvent.tryEmit(LoginUiEvent.Toast("Thông tin không được để trống"))
                return@launch
            }

            val login = Login(user, pass)

            when (val r = authRepo.loginRequest(login)) {
                is ApiResult.Success -> {
                    val token = r.data.result.token
                    SessionManager.saveToken(token)
                    SessionManager.saveUsername(user)   // ← lưu username
                    println("DEBUG [LoginVM] saved token + username=$user")

                    val us = userRepo.getMyInfo()
                    if (us is ApiResult.Success) {
                        val data1 = userRepo.createUser(us.data.userDtoToDomain())
                        println("DEBUG in [LoginViewmodel] insert room db success: $data1")
                    }

                    _loginState.update { it.copy(isLoading = false) }
                    _loginEvent.tryEmit(LoginUiEvent.Success)
                    _loginEvent.tryEmit(LoginUiEvent.Toast("Đăng nhập thành công"))
                    _loginEvent.tryEmit(LoginUiEvent.navigateHome)
                }
                is ApiResult.Error -> {
                    println("DEBUG [LoginVM] login FAILED: ${r.message}")
                    _loginState.update { it.copy(isLoading = false) }
                    _loginEvent.tryEmit(LoginUiEvent.Error)
                    _loginEvent.tryEmit(LoginUiEvent.Toast(r.message))
                }
            }
        }
    }
}