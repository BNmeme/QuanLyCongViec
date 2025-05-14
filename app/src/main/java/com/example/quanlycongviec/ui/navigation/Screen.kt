package com.example.quanlycongviec.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object SignIn : Screen("sign_in")
    object SignUp : Screen("sign_up")
    object Home : Screen("home")
    object PersonalTasks : Screen("personal_tasks")
    object PersonalTaskDetail : Screen("personal_task_detail")
    object EditPersonalTask : Screen("edit_personal_task")
    object Groups : Screen("groups")
    object GroupDetail : Screen("group_detail")
    object GroupTasks : Screen("group_tasks")
    object GroupTaskDetail : Screen("group_task_detail")
    object EditGroupTask : Screen("edit_group_task")
    object CreateGroup : Screen("create_group")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
    object Notifications : Screen("notifications")
    object Statistics : Screen("statistics")
    object Labels : Screen("labels")

    // Password reset screens
    object ForgotPassword : Screen("forgot_password")
    object OtpVerification : Screen("otp_verification")
    object ResetPassword : Screen("reset_password")
}
