package com.uzuu.customer.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.uzuu.customer.domain.model.Event
import com.uzuu.customer.feature.middle.blog.BlogScreen
import com.uzuu.customer.feature.middle.cart.CartScreen
import com.uzuu.customer.feature.middle.home.HomeScreen
import com.uzuu.customer.feature.middle.personal.PersonalScreen
import com.uzuu.customer.feature.middle.ticket.TicketScreen
import com.uzuu.customer.ui.theme.BluePrimary700
import com.uzuu.customer.ui.theme.BluePrimary800

sealed class BottomNavTab(val index: Int, val title: String, val icon: ImageVector) {
    object Blog : BottomNavTab(0, "Tin tức", Icons.Default.Article)
    object Events : BottomNavTab(1, "Sự kiện", Icons.Default.Event)
    object Cart : BottomNavTab(2, "Giỏ hàng", Icons.Default.ShoppingCart)
    object Ticket : BottomNavTab(3, "Vé của tôi", Icons.Default.ConfirmationNumber)
    object Personal : BottomNavTab(4, "Tài khoản", Icons.Default.Person)

    companion object {
        val allTabs = listOf(Blog, Events, Cart, Ticket, Personal)
    }
}

@Composable
fun MainScreen(
    onNavigateToEventDetail: (Event) -> Unit,
    onNavigateToCheckout: (LongArray) -> Unit,
    onNavigateToPersonalInfo: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(1) } // Default to Events tab

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                BottomNavTab.allTabs.forEach { tab ->
                    val isSelected = selectedTabIndex == tab.index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = tab.index },
                        icon = {
                            Icon(imageVector = tab.icon, contentDescription = tab.title)
                        },
                        label = {
                            Text(text = tab.title)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary800,
                            selectedTextColor = BluePrimary800,
                            indicatorColor = BluePrimary700.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                BottomNavTab.Blog.index -> BlogScreen()
                BottomNavTab.Events.index -> HomeScreen(onNavigateToEventDetail = onNavigateToEventDetail)
                BottomNavTab.Cart.index -> CartScreen(onNavigateToCheckout = onNavigateToCheckout)
                BottomNavTab.Ticket.index -> TicketScreen()
                BottomNavTab.Personal.index -> PersonalScreen(
                    onNavigateToPersonalInfo = onNavigateToPersonalInfo,
                    onNavigateToChangePassword = onNavigateToChangePassword,
                    onNavigateToHistory = onNavigateToHistory,
                    onLogout = onLogout
                )
            }
        }
    }
}
