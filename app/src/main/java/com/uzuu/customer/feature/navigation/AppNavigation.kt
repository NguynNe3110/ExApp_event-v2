package com.uzuu.customer.feature.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uzuu.customer.domain.model.Event
import com.uzuu.customer.domain.model.Voucher
import com.uzuu.customer.domain.repository.CartRepository
import com.uzuu.customer.feature.main.MainScreen
import com.uzuu.customer.feature.middle.checkout.CheckoutScreen
import com.uzuu.customer.feature.middle.home.eventDetail.EventDetailScreen
import com.uzuu.customer.feature.middle.personal.changePassword.ChangePasswordScreen
import com.uzuu.customer.feature.middle.personal.history.HistoryScreen
import com.uzuu.customer.feature.middle.personal.personalInfo.PersonalInfoScreen
import com.uzuu.customer.feature.middle.voucher.VoucherListScreen
import com.uzuu.customer.feature.start.forgetpass.ForgetPasswordScreen
import com.uzuu.customer.feature.start.login.LoginScreen
import com.uzuu.customer.feature.start.register.RegisterScreen
import com.uzuu.customer.feature.start.splash.SplashScreen

@Composable
fun AppNavigation(
    cartRepo: CartRepository,
    navController: NavHostController = rememberNavController()
) {
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var checkoutItemIds by remember { mutableStateOf(longArrayOf()) }
    var selectedVoucher by remember { mutableStateOf<Voucher?>(null) }

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.Register.route)
                },
                onNavigateToForget = {
                    navController.navigate(Routes.ForgotPassword.route)
                }
            )
        }

        composable(Routes.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ForgotPassword.route) {
            ForgetPasswordScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.Main.route) {
            MainScreen(
                onNavigateToEventDetail = { event ->
                    selectedEvent = event
                    navController.navigate(Routes.EventDetail.route)
                },
                onNavigateToCheckout = { ids ->
                    checkoutItemIds = ids
                    navController.navigate(Routes.Checkout.route)
                },
                onNavigateToPersonalInfo = {
                    navController.navigate(Routes.PersonalInfo.route)
                },
                onNavigateToChangePassword = {
                    navController.navigate(Routes.ChangePassword.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Routes.History.route)
                },
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Main.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.EventDetail.route) {
            selectedEvent?.let { event ->
                EventDetailScreen(
                    event = event,
                    cartRepo = cartRepo,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCheckout = { ids ->
                        checkoutItemIds = ids
                        navController.navigate(Routes.Checkout.route)
                    }
                )
            }
        }

        composable(Routes.Checkout.route) {
            CheckoutScreen(
                itemIds = checkoutItemIds,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVouchers = { _, _, _ ->
                    navController.navigate(Routes.VoucherList.route)
                },
                onNavigateToTickets = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Main.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.VoucherList.route) {
            VoucherListScreen(
                onNavigateBack = { navController.popBackStack() },
                onSelectVoucher = { voucher ->
                    selectedVoucher = voucher
                }
            )
        }

        composable(Routes.PersonalInfo.route) {
            PersonalInfoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ChangePassword.route) {
            ChangePasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
