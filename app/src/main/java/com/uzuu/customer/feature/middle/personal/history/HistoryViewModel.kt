package com.uzuu.customer.feature.middle.personal.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzuu.customer.core.result.ApiResult
import com.uzuu.customer.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val orderRepo: OrderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<String>(extraBufferCapacity = 3)
    val event = _event.asSharedFlow()

    private var currentPage = 1

    fun refreshOrders() {
        currentPage = 1
        _state.update { it.copy(isLoading = false, orders = emptyList(), isLastPage = false) }
        loadOrders()
    }

    fun loadOrders() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = orderRepo.getMyOrders(currentPage)) {
                is ApiResult.Success -> {
                    val paged = result.data
                    _state.update {
                        it.copy(
                            isLoading = false,
                            orders    = it.orders + paged.data,
                            isLastPage = paged.isLast
                        )
                    }
                    if (!paged.isLast) currentPage++
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _event.emit(result.message)
                }
            }
        }
    }
}