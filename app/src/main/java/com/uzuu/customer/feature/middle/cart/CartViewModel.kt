package com.uzuu.customer.feature.middle.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzuu.customer.core.result.ApiResult
import com.uzuu.customer.domain.model.CartItem
import com.uzuu.customer.domain.model.Event
import com.uzuu.customer.domain.repository.CartRepository
import com.uzuu.customer.domain.repository.EventRepository
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepo: CartRepository,
    private val eventRepo: EventRepository
) : ViewModel() {

    private val _cartState = MutableStateFlow(CartUiState())
    val cartState = _cartState.asStateFlow()

    private val _cartEvent = MutableSharedFlow<CartUiEvent>(extraBufferCapacity = 3)
    val cartEvent = _cartEvent.asSharedFlow()

    fun loadCart() {
        viewModelScope.launch {
            _cartState.update { it.copy(isLoading = true) }
            when (val r = cartRepo.getCart()) {
                is ApiResult.Success -> {
                    val cart = r.data
                    val unavailableIds = findUnavailableCartItemIds(cart.items)
                    _cartState.update {
                        it.copy(
                            isLoading = false,
                            items = cart.items,
                            totalAmount = cart.totalAmount,
                            unavailableItemIds = unavailableIds,
                            selectedItemIds = it.selectedItemIds
                                .filter { id -> cart.items.any { item -> item.id == id } }
                                .toSet()
                        )
                    }
                }
                is ApiResult.Error -> {
                    _cartState.update { it.copy(isLoading = false) }
                    _cartEvent.emit(CartUiEvent.Toast(r.message))
                }
            }
        }
    }

    fun toggleItemSelection(itemId: Long) {
        _cartState.update { state ->
            val newSet = state.selectedItemIds.toMutableSet()
            if (itemId in newSet) newSet.remove(itemId) else newSet.add(itemId)
            state.copy(selectedItemIds = newSet)
        }
    }

    fun toggleSelectAll() {
        _cartState.update { state ->
            val newSet = if (state.isAllSelected) emptySet()
            else state.items.map { it.id }.toSet()
            state.copy(selectedItemIds = newSet)
        }
    }

    fun updateItemQuantity(itemId: Long, quantity: Int) {
        if (quantity <= 0) return

        viewModelScope.launch {
            when (val r = cartRepo.updateCartItem(itemId, quantity)) {
                is ApiResult.Success -> {
                    val cart = r.data
                    val unavailableIds = findUnavailableCartItemIds(cart.items)
                    _cartState.update {
                        it.copy(
                            items = cart.items,
                            totalAmount = cart.totalAmount,
                            unavailableItemIds = unavailableIds
                        )
                    }
                }
                is ApiResult.Error -> _cartEvent.emit(CartUiEvent.Toast(r.message))
            }
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            when (val r = cartRepo.deleteCartItem(itemId)) {
                is ApiResult.Success -> {
                    val cart = r.data
                    val unavailableIds = findUnavailableCartItemIds(cart.items)
                    _cartState.update {
                        it.copy(
                            items = cart.items,
                            totalAmount = cart.totalAmount,
                            unavailableItemIds = unavailableIds,
                            selectedItemIds = it.selectedItemIds - itemId
                        )
                    }
                    _cartEvent.emit(CartUiEvent.Toast("Đã xóa vé khỏi giỏ"))
                }
                is ApiResult.Error -> _cartEvent.emit(CartUiEvent.Toast(r.message))
            }
        }
    }

    fun deleteSelectedItems() {
        val ids = _cartState.value.selectedItemIds
        if (ids.isEmpty()) {
            viewModelScope.launch { _cartEvent.emit(CartUiEvent.Toast("Chua chon muc nao")) }
            return
        }

        viewModelScope.launch {
            _cartState.update { it.copy(isLoading = true) }
            var lastCart = _cartState.value.items
            var lastTotal = _cartState.value.totalAmount
            var hasError = false

            ids.forEach { itemId ->
                when (val r = cartRepo.deleteCartItem(itemId)) {
                    is ApiResult.Success -> {
                        lastCart = r.data.items
                        lastTotal = r.data.totalAmount
                    }
                    is ApiResult.Error -> hasError = true
                }
            }

            val unavailableIds = findUnavailableCartItemIds(lastCart)
            _cartState.update {
                it.copy(
                    isLoading = false,
                    items = lastCart,
                    totalAmount = lastTotal,
                    unavailableItemIds = unavailableIds,
                    selectedItemIds = emptySet()
                )
            }

            if (hasError) {
                _cartEvent.emit(CartUiEvent.Toast("Có lỗi khi xóa, vui lòng thử lại"))
            } else {
                _cartEvent.emit(CartUiEvent.Toast("Đã xóa ${ids.size} mục khỏi giỏ"))
            }
        }
    }

    fun onClearCart() {
        viewModelScope.launch {
            _cartState.update { it.copy(isLoading = true) }
            when (val r = cartRepo.clearCart()) {
                is ApiResult.Success -> {
                    _cartState.update {
                        it.copy(
                            isLoading = false,
                            items = emptyList(),
                            totalAmount = 0.0,
                            unavailableItemIds = emptySet(),
                            selectedItemIds = emptySet()
                        )
                    }
                    _cartEvent.emit(CartUiEvent.CartCleared)
                    _cartEvent.emit(CartUiEvent.Toast("Đã xóa toàn bộ giỏ hàng"))
                }
                is ApiResult.Error -> {
                    _cartState.update { it.copy(isLoading = false) }
                    _cartEvent.emit(CartUiEvent.Toast(r.message))
                }
            }
        }
    }

    private suspend fun findUnavailableCartItemIds(items: List<CartItem>): Set<Long> {
        if (items.isEmpty()) return emptySet()

        val cachedEvents = runCatching { eventRepo.getCachedEvents() }.getOrDefault(emptyList())
        val eventsByName = mutableMapOf<String, List<Event>>()

        return items.filter { item ->
            val events = eventsByName.getOrPut(item.eventName) {
                runCatching {
                    eventRepo.searchEvents(
                        page = 1,
                        search = item.eventName,
                        province = null,
                        minPrice = null,
                        maxPrice = null
                    ).data
                }.getOrElse {
                    cachedEvents.filter { event ->
                        event.name.equals(item.eventName, ignoreCase = true) ||
                            event.name.contains(item.eventName, ignoreCase = true)
                    }
                }
            }

            val event = events.firstOrNull { event ->
                event.ticketTypes.any { it.id == item.ticketTypeId }
            } ?: return@filter false

            val ticket = event.ticketTypes.firstOrNull { it.id == item.ticketTypeId }
            ticket == null ||
                ticket.remainingQuantity < item.quantity ||
                !isEventOpenForSale(event)
        }.map { it.id }.toSet()
    }

    private fun isEventOpenForSale(event: Event): Boolean {
        val status = event.status.uppercase(Locale.US)
        if (status in setOf(
                "CANCELLED",
                "CANCELED",
                "COMPLETED",
                "REJECTED",
                "PENDING",
                "DRAFT",
                "INACTIVE",
                "ENDED",
                "CLOSED"
            )
        ) {
            return false
        }

        val now = System.currentTimeMillis()
        val saleStart = parseApiMillis(event.saleStartDate)
        val saleEnd = parseApiMillis(event.saleEndDate)

        if (saleStart != null && now < saleStart) return false
        if (saleEnd != null && now > saleEnd) return false
        return true
    }

    private fun parseApiMillis(value: String?): Long? {
        if (value.isNullOrBlank()) return null

        val normalized = value.replace(Regex("\\.(\\d{3})\\d+"), ".$1")
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )

        return patterns.firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.US).apply {
                    isLenient = false
                }.parse(normalized)?.time
            }.getOrNull()
        }
    }
}
