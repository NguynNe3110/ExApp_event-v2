package com.uzuu.customer.feature.middle.checkout

import com.uzuu.customer.domain.model.CartItem
import com.uzuu.customer.domain.model.Voucher
import java.util.Locale
import kotlin.math.min

data class CheckoutUiState(
    val isLoading: Boolean = false,
    val items: List<CartItem> = emptyList(),
    val selectedPayment: String = "MOMO",
    val selectedVoucher: Voucher? = null,
    val validatedDiscountAmount: Double? = null,
    val selectedEventId: Long? = null,
    val selectedEventName: String? = null,
    val selectedOrganizerName: String? = null,
    val resolvedAllocations: List<ResolvedCheckoutItem> = emptyList()
) {
    val ticketCount: Int
        get() = items.sumOf { it.quantity }

    val subtotal: Double
        get() = items.sumOf { it.subtotal }

    val discountAmount: Double
        get() {
            validatedDiscountAmount?.let { return it.coerceIn(0.0, subtotal) }
            val voucher = selectedVoucher ?: return 0.0
            val targetSubtotal = voucherTargetSubtotal(voucher)
            val minOrder = voucher.minOrderAmount ?: 0.0
            if (targetSubtotal < minOrder) return 0.0

            val rawDiscount = if (voucher.discountType.uppercase(Locale.US).contains("PERCENT")) {
                targetSubtotal * voucher.amount / 100.0
            } else {
                voucher.amount
            }
            val cap = voucher.maxDiscount?.takeIf { it > 0 } ?: rawDiscount
            return min(rawDiscount, cap).coerceAtMost(targetSubtotal)
        }

    val payableAmount: Double
        get() = (subtotal - discountAmount).coerceAtLeast(0.0)

    private fun voucherTargetSubtotal(voucher: Voucher): Double {
        val allocations = resolvedAllocations
        if (allocations.isEmpty()) return subtotal

        voucher.eventId?.let { eventId ->
            val matched = allocations.filter { it.eventId == eventId }
            if (matched.isNotEmpty()) return matched.sumOf { it.subtotal }
        }

        voucher.creatorName?.takeIf { it.isNotBlank() }?.let { creator ->
            val matched = allocations.filter { allocation ->
                matchesText(allocation.organizerName, creator)
            }
            if (matched.isNotEmpty()) return matched.sumOf { it.subtotal }
        }

        return subtotal
    }

    private fun matchesText(value: String?, expected: String): Boolean {
        val actual = value.orEmpty().trim()
        val target = expected.trim()
        if (actual.isEmpty() || target.isEmpty()) return false
        return actual.equals(target, ignoreCase = true) ||
            actual.contains(target, ignoreCase = true) ||
            target.contains(actual, ignoreCase = true)
    }
}

data class ResolvedCheckoutItem(
    val itemId: Long,
    val eventId: Long?,
    val eventName: String?,
    val organizerName: String?,
    val subtotal: Double
)
