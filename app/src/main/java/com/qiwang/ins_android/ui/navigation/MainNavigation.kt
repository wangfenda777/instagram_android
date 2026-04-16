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
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.qiwang.ins_android.R
import com.qiwang.ins_android.ui.screens.EditFieldScreen
import com.qiwang.ins_android.ui.screens.EditProfileScreen
import com.qiwang.ins_android.ui.screens.ExploreScreen
import com.qiwang.ins_android.ui.screens.FollowListScreen
import com.qiwang.ins_android.ui.screens.HomeScreen
import com.qiwang.ins_android.ui.screens.LoginScreen
import com.qiwang.ins_android.ui.screens.MessagesScreen
import com.qiwang.ins_android.ui.screens.PostDetailScreen
import com.qiwang.ins_android.ui.screens.ProfileScreen
import com.qiwang.ins_android.ui.screens.PublishScreen
import com.qiwang.ins_android.ui.screens.SearchOverviewScreen
import com.qiwang.ins_android.ui.screens.UserDetailScreen
import com.qiwang.ins_android.ui.screens.UserPostsDetailScreen
import com.qiwang.ins_android.ui.theme.TextPrimary
import com.qiwang.ins_android.ui.theme.TextSecondary
import com.qiwang.ins_android.ui.viewmodel.LoginViewModel

/**
 * App 主导航容器。
 *
 * 外层 NavHost 包含登录页、主页面（带底部 Tab）以及所有子页面路由。
 * 子页面（用户详情、粉丝列表、编辑资料等）在外层导航，不显示底部 Tab。
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
            MainScreenWithBottomNav(rootNavController = navController)
        }

        // 用户详情页
        composable(
            route = Screen.UserDetail.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserDetailScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
                onNavigateToFollowList = { uid ->
                    navController.navigate(Screen.FollowList.createRoute(uid))
                },
                onNavigateToUserDetail = { uid ->
                    navController.navigate(Screen.UserDetail.createRoute(uid))
                },
                onNavigateToUserPostsDetail = { uid, postId ->
                    navController.navigate(Screen.UserPostsDetail.createRoute(uid, postId))
                }
            )
        }

        // 帖子详情页（探索页点击）
        composable(
            route = Screen.PostDetail.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostDetailScreen(
                postId = postId,
                onBack = { navController.popBackStack() },
                onNavigateToUserDetail = { uid ->
                    navController.navigate(Screen.UserDetail.createRoute(uid))
                }
            )
        }

        // 用户帖子滚动列表（个人主页/用户详情页点击）
        composable(
            route = Screen.UserPostsDetail.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("postId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            UserPostsDetailScreen(
                userId = userId,
                postId = postId,
                onBack = { navController.popBackStack() },
                onNavigateToUserDetail = { uid ->
                    navController.navigate(Screen.UserDetail.createRoute(uid))
                }
            )
        }

        // 粉丝/关注列表页
        composable(
            route = Screen.FollowList.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            FollowListScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
                onNavigateToUserDetail = { uid ->
                    navController.navigate(Screen.UserDetail.createRoute(uid))
                }
            )
        }

        // 搜索结果页
        composable(
            route = Screen.SearchOverview.route,
            arguments = listOf(navArgument("keyword") { type = NavType.StringType })
        ) { backStackEntry ->
            val keyword = backStackEntry.arguments?.getString("keyword") ?: ""
            SearchOverviewScreen(
                keyword = keyword,
                onBack = { navController.popBackStack() },
                onNavigateToUserDetail = { uid ->
                    navController.navigate(Screen.UserDetail.createRoute(uid))
                }
            )
        }

        // 编辑资料页
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigateToEditField = { fieldName, currentValue ->
                    navController.navigate(Screen.EditField.createRoute(fieldName, currentValue))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 编辑单个字段页
        composable(
            route = Screen.EditField.route,
            arguments = listOf(
                navArgument("fieldName") { type = NavType.StringType },
                navArgument("currentValue") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val fieldName = backStackEntry.arguments?.getString("fieldName") ?: ""
            val currentValue = backStackEntry.arguments?.getString("currentValue") ?: ""
            EditFieldScreen(
                fieldName = fieldName,
                currentValue = currentValue,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * 带底部导航栏的主页面容器。
 *
 * 接收外层 navController 以便子页面跳转到外层路由（不带底部 Tab）。
 */
@Composable
private fun MainScreenWithBottomNav(rootNavController: NavController) {
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
            composable("home_tab") {
                HomeScreen(
                    onNavigateToUserDetail = { userId ->
                        rootNavController.navigate(Screen.UserDetail.createRoute(userId))
                    }
                )
            }
            composable("explore_tab") {
                ExploreScreen(
                    onNavigateToUserDetail = { userId ->
                        rootNavController.navigate(Screen.UserDetail.createRoute(userId))
                    },
                    onNavigateToSearchOverview = { keyword ->
                        rootNavController.navigate(Screen.SearchOverview.createRoute(keyword))
                    },
                    onNavigateToPostDetail = { postId ->
                        rootNavController.navigate(Screen.PostDetail.createRoute(postId))
                    }
                )
            }
            composable("publish_tab") {
                PublishScreen(
                    onBack = {
                        navController.navigate("home_tab") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable("messages_tab") { MessagesScreen() }
            composable("profile_tab") {
                ProfileScreen(
                    onNavigateToFollowList = { userId ->
                        rootNavController.navigate(Screen.FollowList.createRoute(userId))
                    },
                    onNavigateToEditProfile = {
                        rootNavController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateToUserPostsDetail = { userId, postId ->
                        rootNavController.navigate(Screen.UserPostsDetail.createRoute(userId, postId))
                    }
                )
            }
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
