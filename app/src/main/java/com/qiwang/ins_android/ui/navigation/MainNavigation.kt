package com.qiwang.ins_android.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.qiwang.ins_android.ui.screens.MessagesScreen
import com.qiwang.ins_android.ui.screens.ProfileScreen
import com.qiwang.ins_android.ui.screens.PublishScreen
import com.qiwang.ins_android.ui.theme.InstagramBlue
import com.qiwang.ins_android.ui.theme.TextPrimary
import com.qiwang.ins_android.ui.theme.TextSecondary

/**
 * App 主导航容器。
 *
 * 包含底部 Tab 导航栏和 NavHost，管理 5 个主页面的切换。
 * 底部 Tab 使用 SVG 图标（从 drawable 加载）。
 */
@Composable
fun MainNavigation() {
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
                        label = null, // Instagram 风格不显示文字标签
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
                            indicatorColor = Color.Transparent // 去掉选中指示器背景
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Explore.route) { ExploreScreen() }
            composable(Screen.Publish.route) { PublishScreen() }
            composable(Screen.Messages.route) { MessagesScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
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
        route = Screen.Home.route,
        label = "首页",
        unselectedIcon = R.drawable.home,
        selectedIcon = R.drawable.home_selected
    ),
    BottomNavItem(
        route = Screen.Explore.route,
        label = "探索",
        unselectedIcon = R.drawable.direction,
        selectedIcon = R.drawable.direction_select
    ),
    BottomNavItem(
        route = Screen.Publish.route,
        label = "发布",
        unselectedIcon = R.drawable.add,
        selectedIcon = R.drawable.add
    ),
    BottomNavItem(
        route = Screen.Messages.route,
        label = "消息",
        unselectedIcon = R.drawable.video,
        selectedIcon = R.drawable.video_selected
    ),
    BottomNavItem(
        route = Screen.Profile.route,
        label = "我的",
        unselectedIcon = R.drawable.my_selected, // 注意：原项目只有 selected 版本
        selectedIcon = R.drawable.my_selected
    )
)
