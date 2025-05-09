package com.example.quanlycongviec.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.quanlycongviec.ui.screens.auth.SignInScreen
import com.example.quanlycongviec.ui.screens.auth.SignUpScreen
import com.example.quanlycongviec.ui.screens.home.HomeScreen
import com.example.quanlycongviec.ui.screens.labels.LabelsScreen
import com.example.quanlycongviec.ui.screens.notifications.NotificationsScreen
import com.example.quanlycongviec.ui.screens.profile.ProfileScreen
import com.example.quanlycongviec.ui.screens.settings.SettingsScreen
import com.example.quanlycongviec.ui.screens.splash.SplashScreen
import com.example.quanlycongviec.ui.screens.statistics.StatisticsScreen
import com.example.quanlycongviec.ui.screens.tasks.group.CreateGroupScreen
import com.example.quanlycongviec.ui.screens.tasks.group.EditGroupTaskScreen
import com.example.quanlycongviec.ui.screens.tasks.group.GroupDetailScreen
import com.example.quanlycongviec.ui.screens.tasks.group.GroupTaskDetailScreen
import com.example.quanlycongviec.ui.screens.tasks.group.GroupTasksScreen
import com.example.quanlycongviec.ui.screens.tasks.group.GroupsScreen
import com.example.quanlycongviec.ui.screens.tasks.personal.EditPersonalTaskScreen
import com.example.quanlycongviec.ui.screens.tasks.personal.PersonalTaskDetailScreen
import com.example.quanlycongviec.ui.screens.tasks.personal.PersonalTasksScreen

@Composable
fun AppNavHost(navController: NavHostController, startDestination: String = Screen.Splash.route) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.SignIn.route) {
            SignInScreen(navController = navController)
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.PersonalTasks.route) {
            PersonalTasksScreen(navController = navController)
        }

        composable(
            route = "${Screen.PersonalTaskDetail.route}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            PersonalTaskDetailScreen(navController = navController, taskId = taskId)
        }

        composable(
            route = "${Screen.EditPersonalTask.route}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            EditPersonalTaskScreen(navController = navController, taskId = taskId)
        }

        composable(Screen.Groups.route) {
            GroupsScreen(navController = navController)
        }

        composable(Screen.CreateGroup.route) {
            CreateGroupScreen(navController = navController)
        }

        composable(
            route = "${Screen.GroupDetail.route}/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupDetailScreen(navController = navController, groupId = groupId)
        }

        // Fixed: Removed groupId parameter as it's not expected by GroupTasksScreen
        composable(Screen.GroupTasks.route) {
            GroupTasksScreen(navController = navController)
        }

        composable(
            route = "${Screen.GroupTaskDetail.route}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            // Fixed: Added required parameters
            GroupTaskDetailScreen(
                taskId = taskId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToEditTask = { taskId ->
                    navController.navigate("${Screen.EditGroupTask.route}/$taskId")
                }
            )
        }

        composable(
            route = "${Screen.EditGroupTask.route}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            // Fixed: Added required parameter
            EditGroupTaskScreen(
                taskId = taskId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(navController = navController)
        }

        composable(Screen.Labels.route) {
            LabelsScreen(navController = navController)
        }
    }
}
