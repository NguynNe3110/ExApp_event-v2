package com.uzuu.customer.feature.middle.checkout

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uzuu.customer.core.qrcode.QrCodeGenerator
import com.uzuu.customer.domain.model.Order
import com.uzuu.customer.ui.theme.BluePrimary700
import com.uzuu.customer.ui.theme.BluePrimary800

@Composable
fun CheckoutScreen(
    itemIds: LongArray,
    viewModel: CheckoutViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToVouchers: (Long?, String?, String?) -> Unit,
    onNavigateToTickets: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    var successOrder by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadCheckoutItems(itemIds)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is CheckoutUiEvent.Toast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is CheckoutUiEvent.CheckoutSuccess -> {
                    successOrder = event.order
                    // If order has paymentUrl (VNPAY / PAYOS web), open it
                    event.order.paymentUrl?.let { url ->
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 85.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                }
                Text(
                    text = "Xác Nhận Đơn Hàng",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary800
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Section: Danh sách vé
                Text(
                    text = "Danh Sách Vé (${uiState.ticketCount})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                uiState.items.forEach { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.eventName ?: "Sự kiện",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Loại vé: ${item.ticketTypeName ?: ""}",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Số lượng: x${item.quantity}",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = "${String.format("%,.0f", item.subtotal)} đ",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary800
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section: Phương thức thanh toán
                Text(
                    text = "Phương Thức Thanh Toán",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val paymentMethods = listOf(
                    "VIETQR" to "VietQR (Chuyển khoản nhanh)",
                    "MOMO" to "Ví điện tử MoMo",
                    "VNPAY" to "Cổng thanh toán VNPay"
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        paymentMethods.forEach { (code, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectPayment(code) }
                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.selectedPayment.equals(code, ignoreCase = true),
                                    onClick = { viewModel.selectPayment(code) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section: Voucher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mã Giảm Giá",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = {
                            onNavigateToVouchers(
                                uiState.selectedEventId,
                                uiState.selectedEventName,
                                uiState.selectedOrganizerName
                            )
                        }
                    ) {
                        Text(
                            text = if (uiState.selectedVoucher != null) "Đổi mã" else "Chọn voucher",
                            color = BluePrimary700
                        )
                    }
                }

                uiState.selectedVoucher?.let { voucher ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BluePrimary700.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Mã: ${voucher.code}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = BluePrimary800
                                )
                                Text(
                                    text = "Giảm ${String.format("%,.0f", uiState.discountAmount)} đ",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                            TextButton(onClick = { viewModel.selectVoucher(null) }) {
                                Text("Gỡ bỏ", color = Color.Red, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section: Chi tiết thanh toán
                Text(
                    text = "Chi Tiết Thanh Toán",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tạm tính", color = Color.Gray, fontSize = 14.sp)
                            Text("${String.format("%,.0f", uiState.subtotal)} đ", fontSize = 14.sp)
                        }
                        if (uiState.discountAmount > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Giảm giá", color = Color(0xFF10B981), fontSize = 14.sp)
                                Text("-${String.format("%,.0f", uiState.discountAmount)} đ", color = Color(0xFF10B981), fontSize = 14.sp)
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Thành tiền", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "${String.format("%,.0f", uiState.payableAmount)} đ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = BluePrimary800
                            )
                        }
                    }
                }
            }
        }

        // Bottom Confirm Button
        Surface(
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Tổng cần trả", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = "${String.format("%,.0f", uiState.payableAmount)} đ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary800
                    )
                }

                Button(
                    onClick = { viewModel.checkout() },
                    enabled = !uiState.isLoading && uiState.items.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary800),
                    modifier = Modifier.height(48.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Text(text = "Đặt Hàng Ngay", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // Order Success Dialog with QR code
        successOrder?.let { order ->
            val qrBitmap: Bitmap? = remember(order.paymentUrl) {
                order.paymentUrl?.let { url ->
                    if (!url.startsWith("http")) QrCodeGenerator.generateQrCode(url, 400) else null
                }
            }

            AlertDialog(
                onDismissRequest = {
                    successOrder = null
                    onNavigateToTickets()
                },
                title = {
                    Text("Đặt Vé Thành Công!", fontWeight = FontWeight.Bold, color = BluePrimary800)
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Mã đơn hàng: #${order.id}")
                        Text("Tổng thanh toán: ${String.format("%,.0f", order.totalAmount)} đ")

                        if (qrBitmap != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Quét mã VietQR để thanh toán:")
                            Spacer(modifier = Modifier.height(8.dp))
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "VietQR Code",
                                modifier = Modifier.size(200.dp)
                            )
                        } else if (!order.paymentUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Đang mở trang thanh toán ngân hàng...", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            successOrder = null
                            onNavigateToTickets()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary800)
                    ) {
                        Text("Xem Vé Của Tôi")
                    }
                }
            )
        }
    }
}
