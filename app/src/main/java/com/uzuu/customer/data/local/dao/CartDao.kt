package com.uzuu.customer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uzuu.customer.data.local.entity.CartEntity
import com.uzuu.customer.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM carts LIMIT 1")
    fun observeCart(): Flow<CartEntity?>

    @Query("SELECT * FROM carts LIMIT 1")
    suspend fun getCart(): CartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCart(cart: CartEntity)

    @Query("DELETE FROM carts")
    suspend fun deleteCart()

    @Query("SELECT * FROM cart_items ORDER BY id ASC")
    fun observeCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items ORDER BY id ASC")
    suspend fun getAllCartItems(): List<CartItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItems(items: List<CartItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items")
    suspend fun deleteAllCartItems()

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItemById(id: Long)

    @Query("SELECT * FROM cart_items WHERE id = :id")
    suspend fun getCartItemById(id: Long): CartItemEntity?
}
