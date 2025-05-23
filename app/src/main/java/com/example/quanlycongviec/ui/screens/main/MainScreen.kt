package com.example.quanlycongviec.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.ui.navigation.AppNavHost
import com.example.quanlycongviec.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val uiState by viewModel.uiState.collectAsState()

    // Refresh user data when the screen becomes active
    LaunchedEffect(Unit) {
        viewModel.refreshUserData()
    }

    // Listen for navigation changes to refresh user data when needed
    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Screen.Home.route) {
            viewModel.refreshUserData()
        }
    }

    val isAuthenticated = remember(uiState.isLoggedIn) { uiState.isLoggedIn }
    val isBottomBarVisible = remember(currentDestination) {
        currentDestination?.hierarchy?.any {
            it.route == Screen.Home.route ||
                    it.route == Screen.Notifications.route ||
                    it.route == Screen.Profile.route
        } ?: false
    }

    val isDrawerEnabled = remember(currentDestination, isAuthenticated) {
        isAuthenticated && (currentDestination?.hierarchy?.any {
            it.route == Screen.Home.route ||
                    it.route == Screen.PersonalTasks.route ||
                    it.route == Screen.GroupTasks.route ||
                    it.route == Screen.Groups.route ||
                    it.route == Screen.Statistics.route ||
                    it.route == Screen.Settings.route ||
                    it.route == Screen.Notifications.route ||
                    it.route == Screen.Profile.route ||
                    it.route == Screen.Labels.route ||
                    it.route == Screen.Calendar.route
        } ?: false)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (isDrawerEnabled) {
                ModalDrawerSheet {
                    DrawerHeader(
                        userName = uiState.userName,
                        userEmail = uiState.userEmail
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    NavigationDrawerItems(
                        navController = navController,
                        closeDrawer = { scope.launch { drawerState.close() } }
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    NavigationDrawerItem(
                        icon = Icons.Default.Logout,
                        label = "Sign Out",
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                viewModel.signOut {
                                    navController.navigate(Screen.SignIn.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        },
        gesturesEnabled = isDrawerEnabled
    ) {
        Scaffold(
            topBar = {
                if (isDrawerEnabled) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = getTitleForRoute(currentDestination?.route ?: ""),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            },
            bottomBar = {
                if (isBottomBarVisible) {
                    BottomNavigationBar(navController = navController)
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                AppNavHost(navController = navController)
            }
        }
    }
}

@Composable
private fun DrawerHeader(
    userName: String,
    userEmail: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(vertical = 24.dp, horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = userName.ifEmpty { "Guest User" },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = userEmail.ifEmpty { "Not signed in" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun NavigationDrawerItems(
    navController: NavHostController,
    closeDrawer: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        NavigationItem("Home", Icons.Default.Home, Screen.Home.route),
        NavigationItem("Personal Tasks", Icons.Default.Person, Screen.PersonalTasks.route),
        NavigationItem("Group Tasks", Icons.Default.Group, Screen.GroupTasks.route),
        NavigationItem("Groups", Icons.Default.People, Screen.Groups.route),
        NavigationItem("Calendar", Icons.Default.DateRange, Screen.Calendar.route),
        NavigationItem("Labels", Icons.Default.Label, Screen.Labels.route),
        NavigationItem("Statistics", Icons.Default.BarChart, Screen.Statistics.route),
        NavigationItem("Settings", Icons.Default.Settings, Screen.Settings.route)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationDrawerItem(
                icon = item.icon,
                label = item.label,
                selected = isSelected,
                onClick = {
                    closeDrawer()
                    if (currentDestination?.route != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationDrawerItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    else
        Color.Transparent

    val contentColor = if (selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurface

    androidx.compose.material3.NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
        },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = backgroundColor,
            unselectedContainerColor = Color.Transparent,
            selectedIconColor = contentColor,
            unselectedIconColor = contentColor,
            selectedTextColor = contentColor,
            unselectedTextColor = contentColor
        )
    )
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomNavigationItem(
            icon = Icons.Default.Home,
            label = "Home",
            route = Screen.Home.route
        ),
        BottomNavigationItem(
            icon = Icons.Default.Notifications,
            label = "Notifications",
            route = Screen.Notifications.route
        ),
        BottomNavigationItem(
            icon = Icons.Default.Person,
            label = "Profile",
            route = Screen.Profile.route
        )
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        tonalElevation = 4.dp
    ) {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentDestination?.route != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            )
        }
    }
}

fun getTitleForRoute(route: String): String {
    return when {
        route.startsWith(Screen.Home.route) -> "Task Manager"
        route.startsWith(Screen.PersonalTasks.route) -> "Personal Tasks"
        route.startsWith(Screen.GroupTasks.route) -> "Group Tasks"
        route.startsWith(Screen.Groups.route) -> "Groups"
        route.startsWith(Screen.Notifications.route) -> "Notifications"
        route.startsWith(Screen.Profile.route) -> "Profile"
        route.startsWith(Screen.Statistics.route) -> "Statistics"
        route.startsWith(Screen.Settings.route) -> "Settings"
        route.startsWith(Screen.Labels.route) -> "Manage Labels"
        route.startsWith(Screen.Calendar.route) -> "Calendar"
        route.startsWith(Screen.PersonalTaskDetail.route) -> "Task Details"
        route.startsWith(Screen.GroupTaskDetail.route) -> "Group Task"
        route.startsWith(Screen.CreateGroup.route) -> "Create Group"
        route.startsWith(Screen.GroupDetail.route) -> "Group Details"
        route.startsWith(Screen.EditGroupTask.route) -> "Edit Task"
        else -> "Task Manager"
    }
}

data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

data class BottomNavigationItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)
