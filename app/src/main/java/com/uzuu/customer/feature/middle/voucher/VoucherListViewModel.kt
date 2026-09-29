package com.uzuu.customer.feature.middle.voucher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzuu.customer.core.result.ApiResult
import com.uzuu.customer.domain.model.Voucher
import com.uzuu.customer.domain.repository.VoucherRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VoucherListViewModel @Inject constructor(
    private val voucherRepo: VoucherRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val eventId: Long? = savedStateHandle.get<Long>("eventId") ?: savedStateHandle.get<String>("eventId")?.toLongOrNull()
    private val eventName: String? = savedStateHandle.get<String>("eventName")
    private val organizerName: String? = savedStateHandle.get<String>("organizerName")

    private val _state = MutableStateFlow(VoucherListUiState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<VoucherListUiEvent>(extraBufferCapacity = 1)
    val event = _event.asSharedFlow()

    fun loadVouchers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = if (eventId != null) {
                voucherRepo.getVouchersByEvent(eventId)
            } else {
                // fall back to paged list
                when (val r = voucherRepo.getVouchers(page = 1)) {
                    is ApiResult.Success -> ApiResult.Success(r.data.data)
                    is ApiResult.Error -> ApiResult.Error(r.message, r.throwable)
                }
            }

            when (result) {
                is ApiResult.Success -> {
                    val dataList = result.data
                    val filtered = dataList
                        .filter { voucher -> voucher.quantity > 0 }
                        .filter { voucher -> matchesContext(voucher) }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            vouchers = filtered
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _event.emit(VoucherListUiEvent.Toast(result.message))
                }
            }
        }
    }

    private fun matchesContext(voucher: Voucher): Boolean {
        val matchesEventId = eventId == null || voucher.eventId == null || voucher.eventId == eventId
        val matchesEventName = eventName.isNullOrBlank() || voucher.eventName.isNullOrBlank() || matchesText(voucher.eventName, eventName)
        val matchesOrganizer = organizerName.isNullOrBlank() || voucher.creatorName.isNullOrBlank() || matchesText(voucher.creatorName, organizerName)
        return matchesEventId && matchesEventName && matchesOrganizer
    }

    private fun matchesText(value: String?, expected: String?): Boolean {
        val actual = value.orEmpty().trim()
        val target = expected.orEmpty().trim()
        if (actual.isEmpty() || target.isEmpty()) return false
        return actual.equals(target, ignoreCase = true) ||
            actual.contains(target, ignoreCase = true) ||
            target.contains(actual, ignoreCase = true)
    }
}
