package com.uzuu.customer.feature

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.uzuu.customer.core.constants.PaymentConstants
import com.uzuu.customer.data.session.SessionManager
import com.uzuu.customer.domain.repository.CartRepository
import com.uzuu.customer.feature.navigation.AppNavigation
import com.uzuu.customer.ui.theme.CustomerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var cartRepo: CartRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleDeepLink(intent)

        lifecycleScope.launch {
            SessionManager.sessionEvents().collect { ev ->
                when (ev) {
                    SessionManager.SessionEvent.LoggedOut -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        setContent {
            CustomerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(cartRepo = cartRepo)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val uri: Uri = intent.data ?: return

        if (uri.scheme != PaymentConstants.PAYMENT_DEEP_LINK_SCHEME ||
            uri.host != PaymentConstants.PAYMENT_DEEP_LINK_HOST) {
            return
        }

        val orderCode = uri.getQueryParameter(PaymentConstants.PARAM_ORDER_CODE).orEmpty()
        val status = uri.getQueryParameter(PaymentConstants.PARAM_STATUS).orEmpty()

        val message = when (status.lowercase()) {
            PaymentConstants.STATUS_SUCCESS,
            PaymentConstants.STATUS_PAID -> "Thanh toán thành công"
            PaymentConstants.STATUS_CANCEL,
            PaymentConstants.STATUS_CANCELED -> "Đã hủy thanh toán"
            else -> "Đã quay lại ứng dụng"
        }

        if (orderCode.isNotBlank()) {
            Toast.makeText(this, "$message: #$orderCode", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}