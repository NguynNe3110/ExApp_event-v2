package com.uzuu.customer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.uzuu.customer.data.local.dao.CartDao
import com.uzuu.customer.data.local.dao.CategoryDao
import com.uzuu.customer.data.local.dao.EventDao
import com.uzuu.customer.data.local.dao.OrderDao
import com.uzuu.customer.data.local.dao.TicketDao
import com.uzuu.customer.data.local.dao.UsersDao
import com.uzuu.customer.data.local.entity.CartEntity
import com.uzuu.customer.data.local.entity.CartItemEntity
import com.uzuu.customer.data.local.entity.CategoryEntity
import com.uzuu.customer.data.local.entity.EventEntity
import com.uzuu.customer.data.local.entity.OrderEntity
import com.uzuu.customer.data.local.entity.TicketEntity
import com.uzuu.customer.data.local.entity.UsersEntity


@Database(
    entities = [
        UsersEntity::class,
        EventEntity::class,
        TicketEntity::class,
        CartEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        CategoryEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun userDao(): UsersDao
    abstract fun eventDao(): EventDao
    abstract fun ticketDao(): TicketDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "customer_event.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = db
                db
            }
        }
    }
}