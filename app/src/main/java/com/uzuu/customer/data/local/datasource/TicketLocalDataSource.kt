package com.uzuu.customer.data.local.datasource

import com.uzuu.customer.data.local.dao.TicketDao
import com.uzuu.customer.data.local.entity.TicketEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TicketLocalDataSource @Inject constructor(private val ticketDao: TicketDao) {
    
    fun observeAllTickets(): Flow<List<TicketEntity>> = ticketDao.observeAllTickets()
    
    suspend fun getAllTickets(): List<TicketEntity> = ticketDao.getAllTickets()
    
    suspend fun getTicketById(id: Long): TicketEntity? = ticketDao.getTicketById(id)
    
    suspend fun cacheTickets(tickets: List<TicketEntity>) {
        ticketDao.insertTickets(tickets)
    }
    
    suspend fun clearAllTickets() {
        ticketDao.deleteAllTickets()
    }
    
    suspend fun getTicketsByStatus(status: String): List<TicketEntity> {
        return ticketDao.getTicketsByStatus(status)
    }
}
