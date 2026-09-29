package com.uzuu.customer.data.local.datasource

import com.uzuu.customer.data.local.dao.OrderDao
import com.uzuu.customer.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OrderLocalDataSource @Inject constructor(private val orderDao: OrderDao) {
    
    fun observeAllOrders(): Flow<List<OrderEntity>> = orderDao.observeAllOrders()
    
    suspend fun getAllOrders(): List<OrderEntity> = orderDao.getAllOrders()
    
    suspend fun getOrderById(id: Long): OrderEntity? = orderDao.getOrderById(id)
    
    suspend fun cacheOrders(orders: List<OrderEntity>) {
        orderDao.insertOrders(orders)
    }
    
    suspend fun cacheOrder(order: OrderEntity) {
        orderDao.insertOrder(order)
    }
    
    suspend fun clearAllOrders() {
        orderDao.deleteAllOrders()
    }
    
    suspend fun getOrdersByStatus(status: String): List<OrderEntity> {
        return orderDao.getOrdersByStatus(status)
    }
}
