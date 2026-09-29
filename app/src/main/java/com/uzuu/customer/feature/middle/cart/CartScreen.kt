package com.uzuu.customer.feature.middle.cart

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uzuu.customer.domain.model.CartItem
import com.uzuu.customer.ui.theme.BluePrimary700
import com.uzuu.customer.ui.theme.BluePrimary800

@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel(),
    onNavigateToCheckout: (LongArray) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.cartState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadCart()
    }

    LaunchedEffect(Unit) {
        viewModel.cartEvent.collect { event ->
            when (event) {
                is CartUiEvent.Toast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
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
                .padding(bottom = if (uiState.items.isNotEmpty()) 80.dp else 0.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Giỏ Hàng (${uiState.items.size})",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary800
                )

                if (uiState.items.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = uiState.isAllSelected,
                            onCheckedChange = { viewModel.toggleSelectAll() }
                        )
                        Text(text = "Tất cả", fontSize = 14.sp)
                    }
                }
            }

            if (uiState.isLoading && uiState.items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BluePrimary800)
                }
            } else if (uiState.items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Giỏ hàng trống",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        CartItemRow(
                            item = item,
                            isSelected = item.id in uiState.selectedItemIds,
                            onToggleSelect = { viewModel.toggleItemSelection(item.id) },
                            onQuantityChange = { viewModel.updateItemQuantity(item.id, it) },
                            onDelete = { viewModel.deleteItem(item.id) }
                        )
                    }
                }
            }
        }

        // Bottom Checkout Bar
        if (uiState.items.isNotEmpty()) {
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
                        Text(text = "Tổng thanh toán", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = "${String.format("%,.0f", uiState.selectedTotal)} đ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary800
                        )
                    }

                    Button(
                        onClick = {
                            val selectedIds = uiState.selectedItemIds.toLongArray()
                            if (selectedIds.isEmpty()) {
                                Toast.makeText(context, "Vui lòng chọn ít nhất 1 vé", Toast.LENGTH_SHORT).show()
                            } else {
                                onNavigateToCheckout(selectedIds)
                            }
                        },
                        enabled = uiState.hasSelection && !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary800),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(text = "Thanh Toán (${uiState.selectedItemIds.size})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.eventName ?: "Sự kiện",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Loại vé: ${item.ticketTypeName ?: ""}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${String.format("%,.0f", item.unitPrice)} đ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary800
                )
            }

            // Stepper and Delete
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xóa",
                        tint = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { if (item.quantity > 1) onQuantityChange(item.quantity - 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Giảm", modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "${item.quantity}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(
                        onClick = { onQuantityChange(item.quantity + 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tăng", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
