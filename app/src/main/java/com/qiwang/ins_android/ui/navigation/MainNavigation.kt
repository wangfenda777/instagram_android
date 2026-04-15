package com.qiwang.ins_android.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.qiwang.ins_android.R
import com.qiwang.ins_android.ui.screens.ExploreScreen
import com.qiwang.ins_android.ui.screens.HomeScreen
import com.qiwang.ins_android.ui.screens.LoginScreen
import com.qiwang.ins_android.ui.screens.MessagesScreen
import com.qiwang.ins_android.ui.screens.ProfileScreen
import com.qiwang.ins_android.ui.screens.PublishScreen
import com.qiwang.ins_android.ui.theme.TextPrimary
import com.qiwang.ins_android.ui.theme.TextSecondary
import com.qiwang.ins_android.ui.viewmodel.LoginViewModel

/**
 * App 主导航容器。
 *
 * 包含登录页和底部 Tab 导航栏，管理页面切换。
 * 启动时先显示登录页，登录成功后跳转首页。
 */
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // 登录页
        composable(Screen.Login.route) {
            val loginViewModel = LoginViewModel(context)
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 主页面（带底部 Tab）
        composable(Screen.Home.route) {
            MainScreenWithBottomNav()
        }
    }
}

/**
 * 带底部导航栏的主页面容器。
 */
@Composable
private fun MainScreenWithBottomNav() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = TextPrimary
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = if (selected) item.selectedIcon else item.unselectedIcon
                                ),
                                contentDescription = item.label
                            )
                        },
                        label = null,
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TextPrimary,
                            unselectedIconColor = TextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home_tab",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home_tab") { HomeScreen() }
            composable("explore_tab") { ExploreScreen() }
            composable("publish_tab") { PublishScreen() }
            composable("messages_tab") { MessagesScreen() }
            composable("profile_tab") { ProfileScreen() }
        }
    }
}

/**
 * 底部导航栏配置项。
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val unselectedIcon: Int,
    val selectedIcon: Int
)

private val bottomNavItems = listOf(
    BottomNavItem(
        route = "home_tab",
        label = "首页",
        unselectedIcon = R.drawable.home,
        selectedIcon = R.drawable.home_selected
    ),
    BottomNavItem(
        route = "explore_tab",
        label = "探索",
        unselectedIcon = R.drawable.direction,
        selectedIcon = R.drawable.direction_select
    ),
    BottomNavItem(
        route = "publish_tab",
        label = "发布",
        unselectedIcon = R.drawable.add,
        selectedIcon = R.drawable.add
    ),
    BottomNavItem(
        route = "messages_tab",
        label = "消息",
        unselectedIcon = R.drawable.video,
        selectedIcon = R.drawable.video_selected
    ),
    BottomNavItem(
        route = "profile_tab",
        label = "我的",
        unselectedIcon = R.drawable.my_selected,
        selectedIcon = R.drawable.my_selected
    )
)
