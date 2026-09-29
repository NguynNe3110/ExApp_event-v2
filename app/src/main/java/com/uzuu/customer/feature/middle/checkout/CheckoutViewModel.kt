package com.uzuu.customer.feature.middle.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzuu.customer.core.result.ApiResult
import com.uzuu.customer.domain.model.CartItem
import com.uzuu.customer.domain.model.Event
import com.uzuu.customer.domain.model.Voucher
import com.uzuu.customer.feature.middle.checkout.ResolvedCheckoutItem
import com.uzuu.customer.domain.repository.CartRepository
import com.uzuu.customer.domain.repository.EventRepository
import com.uzuu.customer.domain.repository.OrderRepository
import com.uzuu.customer.domain.repository.VoucherRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepo: CartRepository,
    private val orderRepo: OrderRepository,
    private val eventRepo: EventRepository,
    private val voucherRepo: VoucherRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutUiState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<CheckoutUiEvent>(extraBufferCapacity = 2)
    val event = _event.asSharedFlow()

    private var selectedIds: List<Long> = emptyList()

    fun loadCheckoutItems(ids: LongArray) {
        selectedIds = ids.toList()
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val r = cartRepo.getCart()) {
                is ApiResult.Success -> {
                    val items = if (selectedIds.isEmpty()) {
                        r.data.items
                    } else {
                        r.data.items.filter { it.id in selectedIds }
                    }
                    val allocations = resolveAllocations(items)
                    val voucherContext = allocations.firstOrNull { it.eventId != null }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            items = items,
                            selectedEventId = voucherContext?.eventId,
                            selectedEventName = voucherContext?.eventName,
                            selectedOrganizerName = voucherContext?.organizerName,
                            resolvedAllocations = allocations
                        )
                    }
                    validateSelectedVoucher()
                    if (items.isEmpty()) {
                        _event.emit(CheckoutUiEvent.Toast("Không có vé để thanh toán"))
                    }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _event.emit(CheckoutUiEvent.Toast(r.message))
                }
            }
        }
    }

    fun selectPayment(method: String) {
        _state.update { it.copy(selectedPayment = method) }
    }

    fun selectVoucher(voucher: Voucher?) {
        _state.update { it.copy(selectedVoucher = voucher, validatedDiscountAmount = null) }
        validateSelectedVoucher()
    }

    fun checkout() {
        val current = _state.value
        if (current.items.isEmpty()) {
            viewModelScope.launch { _event.emit(CheckoutUiEvent.Toast("Không có vé để thanh toán")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val voucherCode = current.selectedVoucher?.code
            val result = if (selectedIds.isEmpty()) {
                orderRepo.checkout(current.selectedPayment, voucherCode)
            } else {
                orderRepo.checkoutSelected(current.selectedPayment, selectedIds, voucherCode)
            }
            when (result) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _event.emit(CheckoutUiEvent.Toast("Đặt hàng thành công"))
                    _event.emit(CheckoutUiEvent.CheckoutSuccess(result.data))
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _event.emit(CheckoutUiEvent.Toast(result.message))
                }
            }
        }
    }

    private suspend fun resolveAllocations(items: List<CartItem>): List<ResolvedCheckoutItem> {
        if (items.isEmpty()) return emptyList()

        val cachedEvents = runCatching { eventRepo.getCachedEvents() }.getOrDefault(emptyList())
        return items.map { item ->
            val event = findMatchingEvent(cachedEvents, item) ?: findMatchingEventByPaging(item)
            ResolvedCheckoutItem(
                itemId = item.id,
                eventId = event?.id,
                eventName = event?.name ?: item.eventName,
                organizerName = event?.organizerName,
                subtotal = item.subtotal
            )
        }
    }

    private fun findMatchingEvent(events: List<Event>, item: CartItem): Event? {
        if (events.isEmpty()) return null

        return events.firstOrNull { event ->
            event.ticketTypes.any { ticket -> ticket.id == item.ticketTypeId }
        }
    }

    private suspend fun findMatchingEventByPaging(item: CartItem): Event? {
        var page = 1
        var totalPages = 1

        while (page <= totalPages) {
            val result = runCatching { eventRepo.getEvent(page) }.getOrNull() ?: break
            val match = findMatchingEvent(result.data, item)
            if (match != null) return match
            totalPages = result.totalPages.takeIf { it > 0 } ?: page
            if (result.isLast) break
            page++
        }

        return null
    }

    private fun validateSelectedVoucher() {
        val current = _state.value
        val voucher = current.selectedVoucher ?: return
        val eventAmounts = current.resolvedAllocations
            .groupBy { it.eventId ?: -1L }
            .mapNotNull { (eventId, allocations) ->
                if (eventId < 0) null else eventId.toString() to allocations.sumOf { it.subtotal }
            }
            .toMap()

        if (eventAmounts.isEmpty()) return

        viewModelScope.launch {
            when (val result = voucherRepo.validateVoucher(voucher.code, eventAmounts)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(validatedDiscountAmount = result.data) }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(validatedDiscountAmount = null) }
                    _event.emit(CheckoutUiEvent.Toast(result.message ?: "Voucher không hợp lệ"))
                }
            }
        }
    }
}
