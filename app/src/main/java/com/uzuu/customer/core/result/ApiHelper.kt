package com.uzuu.customer.core.result

import retrofit2.HttpException
import com.uzuu.customer.data.session.SessionManager
import java.io.IOException
import org.json.JSONObject

suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(block())
    } catch (e: IOException) {
        ApiResult.Error("Không có kết nối mạng hoặc timeout", e)
    } catch (e: HttpException) {
        val errorMessage = extractErrorMessage(e)
        try {
            if (e.code() == 401) {
                // clear stored session and notify listeners
                try {
                    SessionManager.clear()
                } catch (_: Exception) {}
                try {
                    SessionManager.notifyLoggedOut()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
        ApiResult.Error(errorMessage, e)
    } catch (e: Exception) {
        ApiResult.Error("Lỗi không xác định: ${e.message}", e)
    }
}

private fun extractErrorMessage(e: HttpException): String {
    return try {
        val errorBody = e.response()?.errorBody()?.string()
        if (!errorBody.isNullOrEmpty()) {
            try {
                val jsonObject = JSONObject(errorBody)
                val message = jsonObject.optString("message")
                    ?: jsonObject.optString("msg")
                    ?: jsonObject.optString("error")
                    ?: jsonObject.optString("errorMessage")
                
                if (message.isNotEmpty()) {
                    return message
                }
                
                if (jsonObject.has("data")) {
                    val dataObj = jsonObject.optJSONObject("data")
                    dataObj?.let {
                        val dataMessage = it.optString("message")
                            ?: it.optString("msg")
                            ?: it.optString("error")
                        if (dataMessage.isNotEmpty()) {
                            return dataMessage
                        }
                    }
                }
            } catch (jsonException: Exception) {

            }
            
            if (errorBody.length > 200) {
                errorBody.substring(0, 200) + "..."
            } else {
                errorBody
            }
        } else {
            "HTTP ${e.code()} - ${getHttpErrorDescription(e.code())}"
        }
    } catch (ex: Exception) {
        "HTTP ${e.code()} - ${getHttpErrorDescription(e.code())}"
    }
}

private fun getHttpErrorDescription(code: Int): String {
    return when (code) {
        400 -> "Yêu cầu không hợp lệ"
        401 -> "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại"
        403 -> "Bạn không có quyền truy cập"
        404 -> "Không tìm thấy dữ liệu"
        409 -> "Dữ liệu đã tồn tại hoặc xung đột"
        422 -> "Dữ liệu không hợp lệ"
        500 -> "Lỗi máy chủ, vui lòng thử lại sau"
        502 -> "Máy chủ đang gặp sự cố"
        503 -> "Dịch vụ tạm thời không khả dụng"
        else -> "Lỗi máy chủ ($code)"
    }
}