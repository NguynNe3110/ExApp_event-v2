package com.uzuu.customer.core.constants

/**
 * PayOS Payment Configuration Constants
 * Điều chỉnh giá trị này nếu cần thay đổi deep link scheme/host/path
 */
object PaymentConstants {
    // Deep link configuration for payment return/callback
    const val PAYMENT_DEEP_LINK_SCHEME = "customer"
    const val PAYMENT_DEEP_LINK_HOST = "payment-status"
    const val PAYMENT_DEEP_LINK_PATH = "payment-status"

    // Query parameter names from PayOS redirect
    const val PARAM_ORDER_CODE = "orderCode"
    const val PARAM_STATUS = "status"
    const val PARAM_ORDER_ID = "orderId"

    // Payment status values
    const val STATUS_SUCCESS = "success"
    const val STATUS_PAID = "paid"
    const val STATUS_CANCEL = "cancel"
    const val STATUS_CANCELED = "canceled"

    // Full deep link URI for reference
    // Format: customer://payment-status?orderCode=<orderCode>&status=<status>
    // Example: customer://payment-status?orderCode=ORDER123&status=success
}
