package app.dopedgoal.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import app.dopedgoal.R
import app.dopedgoal.core.Goal
import app.dopedgoal.core.Task
import app.dopedgoal.data.InMemoryGoalRepository
import app.dopedgoal.ui.theme.DopedGoalTheme
import app.dopedgoal.ui.theme.Dimens
import app.dopedgoal.ui.wall.BannerCanvas
import app.dopedgoal.ui.wall.BrickCanvas
import app.dopedgoal.ui.wall.BrickLod
import app.dopedgoal.ui.wall.WallCanvas
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel = viewModel(factory = TodayViewModelFactory()),
    onNavigateToCreateGoal: () -> Unit,
    onNavigateToGoal: (String) -> Unit,
) {
    val activeGoals = viewModel.activeGoals.collectAsStateWithLifecycle(initialValue = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToCreateGoal) {
                        Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.content_desc_add_goal))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )
        },
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
        ) {
            // Greeting section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
            ) {
                Text(
                    text = stringResource(R.string.today_greeting),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.today_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Next brick card
            val nextBrick = viewModel.nextBrick.collectAsStateWithLifecycle(initialValue = null)
            nextBrick.value?.let { (goal, brickIndex) ->
                val task = goal.tasks[brickIndex]
                val placedMessage = stringResource(R.string.snackbar_brick_placed, goal.placedCount + 1, goal.total)
                val undoLabel = stringResource(R.string.snackbar_undo)
                NextBrickCard(
                    goal = goal,
                    task = task,
                    brickIndex = brickIndex,
                    onPlace = {
                        scope.launch {
                            viewModel.completeTask(task.id)
                            val snackbar = snackbarHostState.showSnackbar(
                                message = placedMessage,
                                actionLabel = undoLabel,
                                duration = androidx.compose.material3.SnackbarDuration.Short,
                            )
                            if (snackbar == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                viewModel.uncompleteTask(task.id)
                            }
                        }
                    },
                    onNavigate = { onNavigateToGoal(goal.id) },
                )
            }

            // In progress goals
            val goalsList = activeGoals.value
            if (goalsList.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.today_in_progress, goalsList.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
                ) {
                    goalsList.forEach { goal ->
                        InProgressGoalCard(
                            goal = goal,
                            onClick = { onNavigateToGoal(goal.id) },
                        )
                    }
                }
            } else {
                // Empty state
                EmptyStateCard(onCreateGoal = onNavigateToCreateGoal)
            }

            // Bottom padding for nav bar
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun NextBrickCard(
    goal: Goal,
    task: Task,
    brickIndex: Int,
    onPlace: () -> Unit,
    onNavigate: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
                ) {
                    // Brick preview
                    Box(
                        modifier = Modifier
                            .size(56.dp, 24.dp)
                            .clipToBounds(),
                    ) {
                        BrickCanvas(
                            brick = task.brick,
                            modifier = Modifier.size(56.dp, 24.dp),
                            lod = BrickLod.Thumbnail,
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = task.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        )
                    }
                }

                Button(onClick = onPlace) {
                    Text(stringResource(R.string.button_place_brick))
                }
            }
        }
    }
}

@Composable
private fun InProgressGoalCard(
    goal: Goal,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.gutter)
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Column(
            modifier = Modifier.padding(Dimens.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
                ) {
                    // Mini banner
                    Box(modifier = Modifier.size(Dimens.miniBannerWidth, Dimens.miniBannerHeight)) {
                        BannerCanvas(
                            banner = goal.banner,
                            modifier = Modifier.size(Dimens.miniBannerWidth, Dimens.miniBannerHeight),
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${goal.category.name.lowercase()} · ${goal.placedCount} / ${goal.total} bricks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }

                // Mini wall
                Box(
                    modifier = Modifier
                        .width(Dimens.miniWallWidth)
                        .height(Dimens.miniWallHeight)
                        .clipToBounds(),
                ) {
                    WallCanvas(
                        goalId = goal.id,
                        placedBricks = goal.placedBricks,
                        ghostCount = goal.total - goal.placedCount,
                        modifier = Modifier.size(Dimens.miniWallWidth, Dimens.miniWallHeight),
                        columns = 5,
                        lod = BrickLod.Thumbnail,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(onCreateGoal: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingLarge)
                .padding(vertical = Dimens.spacingXLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
        ) {
            // Ghost brick illustration
            Box(
                modifier = Modifier
                    .size(80.dp, 35.dp)
                    .clipToBounds(),
            ) {
                BrickCanvas(
                    brick = createGhostBrick(),
                    modifier = Modifier.size(80.dp, 35.dp),
                    lod = BrickLod.Thumbnail,
                )
            }

            Text(
                text = stringResource(R.string.empty_state_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.empty_state_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Button(onClick = onCreateGoal) {
                Text(stringResource(R.string.button_build_goal))
            }
        }
    }
}

// Create a simple ghost brick for the empty state
private fun createGhostBrick(): app.dopedgoal.core.Brick {
    val ghostMaterial = app.dopedgoal.core.Material.CLAY
    val ghostPaint = app.dopedgoal.core.Paint(
        hue = 0f, saturation = 0f, lightness = 50f,
        coverage = 0f, pattern = app.dopedgoal.core.PaintPattern.BANDED,
    )
    val ghostWear = app.dopedgoal.core.Wear(0, 0, false, 0f)
    return app.dopedgoal.core.Brick(
        taskId = "ghost",
        material = ghostMaterial,
        paint = ghostPaint,
        wear = ghostWear,
        artifact = app.dopedgoal.core.Artifact.NONE,
        aspect = 1f,
        seed = 0L,
    )
}

class TodayViewModelFactory : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: kotlin.reflect.KClass<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
        return TodayViewModel(InMemoryGoalRepository.getInstance()) as T
    }
}