package app.dopedgoal.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.dopedgoal.R
import app.dopedgoal.ui.theme.DopedGoalTheme
import app.dopedgoal.ui.theme.Dimens

/**
 * The three top-level destinations. There is deliberately no "rewards" tab: the
 * accumulated work is the reward, so it lives on the walls themselves.
 */
private enum class Destination(
    val route: String,
    @param:StringRes val label: Int,
    val icon: ImageVector,
) {
    TODAY("today", R.string.nav_today, Icons.Outlined.DateRange),
    GOALS("goals", R.string.nav_goals, Icons.Outlined.Star),
    ARCHIVE("archive", R.string.nav_archive, Icons.Outlined.CheckCircle),
}

@Composable
fun DopedGoalApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                Destination.entries.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(destination.label)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.TODAY.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destination.TODAY.route) {
                Placeholder(R.string.today_empty_title)
            }
            composable(Destination.GOALS.route) {
                Placeholder(R.string.goals_empty)
            }
            composable(Destination.ARCHIVE.route) {
                Placeholder(R.string.archive_empty)
            }
        }
    }
}

/**
 * Stands in until the real screens land. Copy is already warm and additive —
 * empty states in this app never read as a blank checklist.
 */
@Composable
private fun Placeholder(@StringRes message: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.gutter),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(message),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DopedGoalAppPreview() {
    DopedGoalTheme {
        DopedGoalApp()
    }
}
