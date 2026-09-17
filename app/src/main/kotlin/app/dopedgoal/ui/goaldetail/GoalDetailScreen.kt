package app.dopedgoal.ui.goaldetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.dopedgoal.R
import app.dopedgoal.core.Goal
import app.dopedgoal.core.Task
import app.dopedgoal.data.InMemoryGoalRepository
import app.dopedgoal.ui.theme.Dimens
import app.dopedgoal.ui.theme.DopedGoalTheme
import app.dopedgoal.ui.wall.BannerCanvas
import app.dopedgoal.ui.wall.BrickCanvas
import app.dopedgoal.ui.wall.BrickLod
import app.dopedgoal.ui.wall.WallCanvas
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: String,
    viewModel: GoalDetailViewModel = viewModel(factory = GoalDetailViewModelFactory(goalId)),
    onNavigateUp: () -> Unit,
) {
    val goal by viewModel.goal.collectAsStateWithLifecycle(initialValue = null)
    val undoState by viewModel.undoSnackbar.collectAsStateWithLifecycle(initialValue = null)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var placeSheetTask by remember { mutableStateOf<Task?>(null) }
    var detailTask by remember { mutableStateOf<Task?>(null) }

    goal?.let { goal ->
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = goal.name,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.content_desc_back))
                        }
                    },
                    actions = {
                        if (!goal.isAchieved) {
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.content_desc_more))
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_delete_goal)) },
                                        onClick = {
                                            showMenu = false
                                            showDeleteConfirm = true
                                        },
                                    )
                                }
                            }
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
                // Banner + Wall
                GoalWallSection(goal = goal)

                // Next brick section
                val nextIncomplete = goal.tasks.firstOrNull { !it.isComplete }
                nextIncomplete?.let { task ->
                    val brickIndex = goal.tasks.indexOf(task)
                    NextBrickSection(
                        goal = goal,
                        task = task,
                        brickIndex = brickIndex,
                        onPlace = {
                            scope.launch {
                                viewModel.completeTask(task)
                            }
                        },
                    )
                }

                // All bricks list
                AllBricksSection(
                    goal = goal,
                    onTaskClick = { task ->
                        if (!task.isComplete) {
                            placeSheetTask = task
                        } else {
                            detailTask = task
                        }
                    },
                )

                // Bottom padding for nav bar
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Handle undo snackbar
        val placedMessage = stringResource(R.string.snackbar_brick_placed, goal.placedCount, goal.total)
        val undoLabel = stringResource(R.string.snackbar_undo)
        undoState?.let { state ->
            LaunchedEffect(state) {
                val result = snackbarHostState.showSnackbar(
                    message = placedMessage,
                    actionLabel = undoLabel,
                    duration = androidx.compose.material3.SnackbarDuration.Short,
                )
                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                    viewModel.undoLastCompletion()
                } else {
                    viewModel.dismissUndo()
                }
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text(stringResource(R.string.delete_goal_dialog_title)) },
                text = { Text(stringResource(R.string.delete_goal_dialog_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteGoal()
                        onNavigateUp()
                    }) {
                        Text(stringResource(R.string.action_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                },
            )
        }

        placeSheetTask?.let { task ->
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { placeSheetTask = null },
                sheetState = sheetState,
            ) {
                PlaceBrickSheetContent(
                    task = task,
                    onCancel = {
                        scope.launch {
                            sheetState.hide()
                            placeSheetTask = null
                        }
                    },
                    onConfirm = {
                        scope.launch {
                            viewModel.completeTask(task)
                            sheetState.hide()
                            placeSheetTask = null
                        }
                    },
                )
            }
        }

        detailTask?.let { task ->
            BrickDetailDialog(task = task, onDismiss = { detailTask = null })
        }
    }
}

@Composable
private fun PlaceBrickSheetContent(
    task: Task,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp, 42.dp)
                .clipToBounds(),
        ) {
            BrickCanvas(
                brick = task.brick,
                modifier = Modifier.size(96.dp, 42.dp),
                lod = BrickLod.Full,
            )
        }
        Text(
            text = task.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.action_cancel))
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.button_place_brick))
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(Dimens.spacingMedium))
    }
}

@Composable
private fun BrickDetailDialog(task: Task, onDismiss: () -> Unit) {
    val brick = task.brick
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.brick_detail_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clipToBounds(),
                ) {
                    BrickCanvas(
                        brick = brick,
                        modifier = Modifier.size(120.dp, 52.dp),
                        lod = BrickLod.Full,
                    )
                }
                Text(task.name, style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.brick_detail_material, brick.material.name.lowercase()))
                Text(stringResource(R.string.brick_detail_pattern, brick.paint.pattern.name.lowercase()))
                val kintsugiSuffix = if (brick.wear.kintsugi) stringResource(R.string.brick_detail_kintsugi) else ""
                val cracksText = pluralStringResource(R.plurals.brick_detail_cracks, brick.wear.cracks, brick.wear.cracks)
                val chipsText = pluralStringResource(R.plurals.brick_detail_chips, brick.wear.chips, brick.wear.chips)
                Text(stringResource(R.string.brick_detail_wear, cracksText, chipsText, kintsugiSuffix))
                val artifactKind = brick.artifact.kind
                if (artifactKind != null) {
                    Text(stringResource(R.string.brick_detail_artifact, artifactKind.name.lowercase(), brick.artifact.count))
                } else {
                    Text(stringResource(R.string.brick_detail_no_artifact))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        },
    )
}

@Composable
private fun GoalWallSection(goal: Goal) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
    ) {
        // Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = Dimens.gutter),
        ) {
            BannerCanvas(banner = goal.banner, modifier = Modifier.size(92.dp, 150.dp))
        }

        // Wall
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = Dimens.gutter),
        ) {
            WallCanvas(
                goalId = goal.id,
                placedBricks = goal.placedBricks,
                ghostCount = goal.total - goal.placedCount,
                modifier = Modifier.fillMaxSize(),
                columns = 5,
                lod = BrickLod.Full,
            )
        }

        // Progress text
        Row(
            modifier = Modifier.padding(horizontal = Dimens.gutter),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${goal.category.name.lowercase()} · ${goal.placedCount} of ${goal.total} bricks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
            if (goal.isAchieved) {
                Text(
                    text = stringResource(R.string.goal_achieved),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun NextBrickSection(
    goal: Goal,
    task: Task,
    brickIndex: Int,
    onPlace: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.gutter),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        ) {
            Text(
                text = stringResource(R.string.next_brick),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
            ) {
                // Brick preview
                Box(
                    modifier = Modifier
                        .size(64.dp, 28.dp)
                        .clipToBounds(),
                ) {
                    BrickCanvas(
                        brick = task.brick,
                        modifier = Modifier.size(64.dp, 28.dp),
                        lod = BrickLod.Full,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "Brick ${brickIndex + 1} of ${goal.total}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace,
                    )
                }

                Button(onClick = onPlace) {
                    Text(stringResource(R.string.button_place_brick))
                }
            }
        }
    }
}

@Composable
private fun AllBricksSection(
    goal: Goal,
    onTaskClick: (Task) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.gutter),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
    ) {
        Text(
            text = stringResource(R.string.all_bricks),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        ) {
            goal.tasks.forEachIndexed { index, task ->
                TaskRow(
                    task = task,
                    ordinal = index + 1,
                    isPlaced = task.isComplete,
                    onClick = { onTaskClick(task) },
                )
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: Task,
    ordinal: Int,
    isPlaced: Boolean,
    onClick: () -> Unit,
) {
    val icon = if (isPlaced) Icons.Outlined.Check else Icons.Outlined.Close
    val iconColor = if (isPlaced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = Dimens.gutter)
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
        ) {
            // Ordinal
            Text(
                text = "$ordinal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )

            // Icon
            Icon(
                imageVector = icon,
                contentDescription = if (isPlaced) stringResource(R.string.content_desc_complete) else stringResource(R.string.content_desc_incomplete),
                tint = iconColor,
            )

            // Task name
            Text(
                text = task.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

class GoalDetailViewModelFactory(private val goalId: String) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: kotlin.reflect.KClass<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
        return GoalDetailViewModel(
            InMemoryGoalRepository.getInstance(),
            goalId,
        ) as T
    }
}