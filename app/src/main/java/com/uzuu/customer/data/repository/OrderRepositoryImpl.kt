package com.uzuu.customer.data.repository

import com.uzuu.customer.core.result.ApiResult
import com.uzuu.customer.core.result.safeApiCall
import com.uzuu.customer.data.local.datasource.OrderLocalDataSource
import com.uzuu.customer.data.local.entity.OrderEntity
import com.uzuu.customer.data.remote.datasource.OrderRemoteDataSource
import com.uzuu.customer.data.remote.dto.response.OrderResponseDto
import com.uzuu.customer.domain.model.Order
import com.uzuu.customer.domain.model.PagedResult
import com.uzuu.customer.domain.repository.OrderRepository
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val remote: OrderRemoteDataSource,
    private val local: OrderLocalDataSource
) : OrderRepository {

    private fun isOk(code: Int) = code == 200 || code == 0 || code == 1000

    private fun normalizePaymentMethod(pm: String): String {
        val s = pm.trim()
        return when {
            s.equals("vietqr", ignoreCase = true) -> "PAYOS"
            s.equals("vnpay", ignoreCase = true) -> "VNPAY"
            s.equals("momo", ignoreCase = true) -> "MOMO"
            s.equals("payos", ignoreCase = true) -> "PAYOS"
            else -> s.uppercase()
        }
    }

    override suspend fun checkout(
        paymentMethod: String,
        voucherCode: String?,
        platform: String
    ): ApiResult<Order> =
        safeApiCall {
            val normalized = normalizePaymentMethod(paymentMethod)
            val r = remote.checkout(
                paymentMethod = normalized,
                voucherCode = voucherCode?.takeIf { it.isNotBlank() },
                platform = platform
            )
            if (isOk(r.code)) {
                val order = r.result.toDomain()
                local.cacheOrder(order.toEntity())
                order
            } else throw Exception(r.message ?: "Thanh toán thất bại")
        }

    override suspend fun checkoutSelected(
        paymentMethod: String,
        itemIds: List<Long>,
        voucherCode: String?,
        platform: String
    ): ApiResult<Order> =
        safeApiCall {
            val r = remote.checkoutSelected(
                paymentMethod = normalizePaymentMethod(paymentMethod),
                itemIds = itemIds,
                voucherCode = voucherCode?.takeIf { it.isNotBlank() },
                platform = platform
            )
            if (isOk(r.code)) {
                val order = r.result.toDomain()
                local.cacheOrder(order.toEntity())
                order
            } else throw Exception(r.message ?: "Thanh toán thất bại")
        }

    override suspend fun getMyOrders(page: Int): ApiResult<PagedResult<Order>> =
        safeApiCall {
            try {
                val r = remote.getMyOrders(page)
                if (isOk(r.code)) {
                    val p = r.result
                    val cachedById = if (page <= 1) {
                        local.getAllOrders().associateBy { it.id }
                    } else {
                        emptyMap()
                    }
                    val orders = p.content.map { dto ->
                        val domain = dto.toDomain()
                        val cachedUrl = cachedById[domain.id]?.paymentUrl
                        if (domain.paymentUrl.isNullOrBlank() && !cachedUrl.isNullOrBlank()) {
                            domain.copy(paymentUrl = cachedUrl)
                        } else {
                            domain
                        }
                    }
                    if (page <= 1) local.clearAllOrders()
                    local.cacheOrders(orders.map { it.toEntity() })
                    PagedResult(
                        data          = orders,
                        page          = p.number,
                        totalPages    = p.totalPages,
                        totalElements = p.totalElements,
                        isLast        = p.last
                    )
                } else throw Exception(r.message ?: "Không lấy được lịch sử đơn hàng")
            } catch (e: Exception) {
                if (page <= 1) {
                    val cached = local.getAllOrders()
                    if (cached.isNotEmpty()) {
                        val orders = cached.map { it.toDomain() }
                        return@safeApiCall PagedResult(
                            data          = orders,
                            page          = 0,
                            totalPages    = 1,
                            totalElements = orders.size.toLong(),
                            isLast        = true
                        )
                    }
                }
                throw e
            }
        }
}

private fun OrderResponseDto.toDomain() = Order(
    id            = id,
    totalAmount   = totalAmount,
    paymentMethod = paymentMethod,
    paymentStatus = paymentStatus,
    orderStatus   = orderStatus,
    orderDate     = orderDate,
    paymentUrl    = paymentUrl
)

private fun Order.toEntity() = OrderEntity(
    id            = id,
    totalAmount   = totalAmount,
    paymentMethod = paymentMethod,
    paymentStatus = paymentStatus,
    orderStatus   = orderStatus,
    orderDate     = orderDate,
    paymentUrl    = paymentUrl
)

private fun OrderEntity.toDomain() = Order(
    id            = id,
    totalAmount   = totalAmount,
    paymentMethod = paymentMethod,
    paymentStatus = paymentStatus,
    orderStatus   = orderStatus,
    orderDate     = orderDate,
    paymentUrl    = paymentUrl
)
