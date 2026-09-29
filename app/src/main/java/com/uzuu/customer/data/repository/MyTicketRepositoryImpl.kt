package com.uzuu.customer.data.repository

import android.util.Log
import com.uzuu.customer.core.result.ApiResult
import com.uzuu.customer.core.result.safeApiCall
import com.uzuu.customer.data.local.datasource.TicketLocalDataSource
import com.uzuu.customer.data.mapper.toDomain
import com.uzuu.customer.data.mapper.toEntity
import com.uzuu.customer.data.mapper.toMyTicketDomain
import com.uzuu.customer.data.remote.datasource.MyTicketRemoteDataSource
import com.uzuu.customer.domain.model.MyTicket
import com.uzuu.customer.domain.repository.MyTicketRepository
import javax.inject.Inject

class MyTicketRepositoryImpl @Inject constructor(
    private val remote: MyTicketRemoteDataSource,
    private val local: TicketLocalDataSource
) : MyTicketRepository {

    companion object {
        private const val TAG = "MyTicketRepo"
    }

    override suspend fun getMyTickets(): ApiResult<List<MyTicket>> =
        safeApiCall {
            try {
                Log.d(TAG, "Bắt đầu gọi API lấy danh sách vé của tôi...")
                val response = remote.getMyTickets()
                Log.d(TAG, "API response code: ${response.code}, message: ${response.message}")
                
                if (response.code == 200 || response.code == 0 || response.code == 1000) {
                    val tickets = response.result.map { it.toDomain() }
                    Log.d(TAG, "API trả về ${tickets.size} vé. Bắt đầu lưu cache...")
                    
                    local.cacheTickets(response.result.map { it.toEntity() })
                    
                    Log.d(TAG, "Đã lưu cache thành công. Trả về ${tickets.size} vé.")
                    tickets
                } else {
                    Log.e(TAG, "API trả về lỗi: code=${response.code}, message=${response.message}")
                    throw Exception(response.message ?: "Không lấy được danh sách vé")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi gọi API: ${e.message}. Đang fallback về cache...", e)
                val cached = local.getAllTickets()
                Log.d(TAG, "Cache hiện có ${cached.size} vé.")
                
                if (cached.isNotEmpty()) {
                    Log.d(TAG, "Sử dụng cache: ${cached.size} vé.")
                    cached.map { it.toMyTicketDomain() }
                } else {
                    Log.e(TAG, "Không có cache. Ném lại exception.")
                    throw e
                }
            }
        }
}