package com.qiwang.ins_android.ui.navigation

/**
 * 应用路由定义。
 *
 * 包含底部 Tab 的 5 个主路由和所有子页面路由。
 */
sealed class Screen(val route: String) {
    // 底部 Tab 主页面
    object Home : Screen("home")
    object Explore : Screen("explore")
    object Publish : Screen("publish")
    object Messages : Screen("messages")
    object Profile : Screen("profile")

    // 子页面
    object Login : Screen("login")
    object UserDetail : Screen("user_detail/{userId}") {
        fun createRoute(userId: String) = "user_detail/$userId"
    }
    object SearchOverview : Screen("search_overview/{keyword}") {
        fun createRoute(keyword: String) = "search_overview/$keyword"
    }
    object FollowList : Screen("follow_list/{userId}") {
        fun createRoute(userId: String) = "follow_list/$userId"
    }
    object EditProfile : Screen("edit_profile")
    object EditField : Screen("edit_field/{fieldName}?currentValue={currentValue}") {
        fun createRoute(fieldName: String, currentValue: String) =
            "edit_field/$fieldName?currentValue=${java.net.URLEncoder.encode(currentValue, "UTF-8")}"
    }
    object PostDetail : Screen("post_detail/{postId}") {
        fun createRoute(postId: String) = "post_detail/$postId"
    }
    object UserPostsDetail : Screen("user_posts_detail/{userId}/{postId}") {
        fun createRoute(userId: String, postId: String) = "user_posts_detail/$userId/$postId"
    }
}
