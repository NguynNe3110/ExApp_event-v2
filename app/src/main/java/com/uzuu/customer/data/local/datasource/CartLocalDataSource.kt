package com.uzuu.customer.data.local.datasource

import com.uzuu.customer.data.local.dao.CartDao
import com.uzuu.customer.data.local.entity.CartEntity
import com.uzuu.customer.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CartLocalDataSource @Inject constructor(private val cartDao: CartDao) {
    
    fun observeCart(): Flow<CartEntity?> = cartDao.observeCart()
    
    fun observeCartItems(): Flow<List<CartItemEntity>> = cartDao.observeCartItems()
    
    suspend fun getCart(): CartEntity? = cartDao.getCart()
    
    suspend fun getAllCartItems(): List<CartItemEntity> = cartDao.getAllCartItems()
    
    suspend fun cacheCart(cart: CartEntity) {
        cartDao.insertCart(cart)
    }
    
    suspend fun cacheCartItems(items: List<CartItemEntity>) {
        cartDao.insertCartItems(items)
    }
    
    suspend fun clearCart() {
        cartDao.deleteCart()
        cartDao.deleteAllCartItems()
    }
    
    suspend fun clearCartItems() {
        cartDao.deleteAllCartItems()
    }
}
