package com.uzuu.customer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uzuu.customer.data.local.entity.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY cachedAt DESC")
    fun observeAllTickets(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE id = :id")
    suspend fun getTicketById(id: Long): TicketEntity?

    @Query("SELECT * FROM tickets ORDER BY cachedAt DESC")
    suspend fun getAllTickets(): List<TicketEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity)

    @Query("DELETE FROM tickets")
    suspend fun deleteAllTickets()

    @Query("DELETE FROM tickets WHERE id = :id")
    suspend fun deleteTicketById(id: Long)

    @Query("SELECT * FROM tickets WHERE status = :status")
    suspend fun getTicketsByStatus(status: String): List<TicketEntity>
}
