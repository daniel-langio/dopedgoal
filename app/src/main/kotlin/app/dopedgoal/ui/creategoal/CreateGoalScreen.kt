package app.dopedgoal.ui.creategoal

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import kotlin.math.max
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.rememberCoroutineScope
import app.dopedgoal.R
import app.dopedgoal.core.Category
import app.dopedgoal.core.Cohesion
import app.dopedgoal.core.Goal
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
fun CreateGoalScreen(
    viewModel: CreateGoalViewModel = viewModel(factory = CreateGoalViewModelFactory()),
    onNavigateUp: () -> Unit,
    onGoalCreated: (Goal) -> Unit,
) {
    val canProceedToStep2 by viewModel.canProceedToStep2.collectAsStateWithLifecycle(initialValue = false)
    val canCreateGoal by viewModel.canCreateGoal.collectAsStateWithLifecycle(initialValue = false)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (viewModel.currentStep == 1) stringResource(R.string.create_goal_step1_title) else stringResource(R.string.create_goal_step2_title),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.currentStep == 2) {
                            viewModel.goBackToStep1()
                        } else {
                            onNavigateUp()
                        }
                    }) {
                        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.content_desc_back))
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
            if (viewModel.currentStep == 1) {
                CreateGoalStep1(
                    viewModel = viewModel,
                    onProceed = { viewModel.proceedToStep2() },
                    canProceed = canProceedToStep2,
                )
            } else {
                CreateGoalStep2(
                    viewModel = viewModel,
                    onBack = { viewModel.goBackToStep1() },
                    onCreate = {
                        val goal = viewModel.buildGoal()
                        scope.launch {
                            InMemoryGoalRepository.getInstance().createGoal(goal)
                            onGoalCreated(goal)
                        }
                    },
                    canCreate = canCreateGoal,
                    showLargeWallWarning = viewModel.isLargeWallWarning(),
                )
            }

            // Bottom padding for nav bar
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun CreateGoalStep1(
    viewModel: CreateGoalViewModel,
    onProceed: () -> Unit,
    canProceed: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.gutter),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge),
    ) {
        // Sticky wall preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            // Ghost wall preview - show ghost bricks for all tasks
            val taskCount = max(viewModel.tasks.size, 1)
            WallCanvas(
                goalId = "preview",
                placedBricks = emptyList(),
                ghostCount = taskCount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = Dimens.gutter),
                columns = 5,
                lod = BrickLod.Thumbnail,
            )
        }

        // Goal name
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        ) {
            Text(
                text = stringResource(R.string.create_goal_name_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
            TextField(
                value = viewModel.name,
                onValueChange = { viewModel.updateName(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.create_goal_name_hint)) },
                singleLine = true,
            )
        }

        // Category chips
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        ) {
            Text(
                text = stringResource(R.string.create_goal_category_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
            ) {
                Category.entries.forEach { category ->
                    val isSelected = viewModel.category == category
                    OutlinedButton(
                        onClick = { viewModel.updateCategory(category) },
                        modifier = Modifier
                            .height(36.dp)
                            .padding(horizontal = Dimens.spacingSmall),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text(category.name.lowercase())
                    }
                }
            }
        }

        // Emoji picker
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        ) {
            Text(
                text = stringResource(R.string.create_goal_emoji_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
            ) {
                listOf("🎯", "🔥", "💪", "📚", "🏃", "🎸", "🌱", "🧠").forEach { emoji ->
                    val isSelected = viewModel.emoji == emoji
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            )
                            .clickable { viewModel.updateEmoji(emoji) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                    }
                }
            }
        }

        // Live banner preview
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        ) {
            Text(
                text = stringResource(R.string.create_goal_banner_preview),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = Dimens.gutter),
                contentAlignment = Alignment.Center,
            ) {
                BannerCanvas(
                    banner = viewModel.banner,
                    modifier = Modifier.size(92.dp, 150.dp),
                )
            }
        }

        // Proceed button
        Button(
            onClick = onProceed,
            modifier = Modifier.fillMaxWidth(),
            enabled = canProceed,
        ) {
            Text(stringResource(R.string.create_goal_choose_bricks))
        }
    }
}

@Composable
private fun CreateGoalStep2(
    viewModel: CreateGoalViewModel,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    canCreate: Boolean,
    showLargeWallWarning: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.gutter),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
    ) {
        // Sticky wall preview at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            WallCanvas(
                goalId = "preview",
                placedBricks = viewModel.tasks.map { it.brick }.filter { it.taskId != "ghost" },
                ghostCount = viewModel.tasks.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = Dimens.gutter),
                columns = 5,
                lod = BrickLod.Full,
            )
        }

        // Supportive label
        Text(
            text = stringResource(R.string.create_goal_small_steps),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
        )

        // Task list
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        ) {
            viewModel.tasks.forEachIndexed { index, task ->
                TaskCard(
                    task = task,
                    ordinal = index + 1,
                    onNameChange = { viewModel.updateTaskName(index, it) },
                    onDelete = { viewModel.removeTask(index) },
                )
            }
        }

        // Add task button
        OutlinedButton(
            onClick = { viewModel.addTask() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Text(stringResource(R.string.create_goal_add_task))
        }

        // Cohesion slider
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        ) {
            Text(
                text = stringResource(R.string.create_goal_wall_character),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
            ) {
                Text(
                    text = stringResource(R.string.create_goal_wild),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
                Slider(
                    modifier = Modifier.weight(1f),
                    value = viewModel.cohesion.value,
                    onValueChange = { viewModel.updateCohesion(Cohesion(it)) },
                    valueRange = 0f..1f,
                    steps = 10,
                )
                Text(
                    text = stringResource(R.string.create_goal_tame),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Text(
                text = "${(viewModel.cohesion.value * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
        }

        // Large wall warning
        if (showLargeWallWarning) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
                ) {
                    Text(
                        text = stringResource(R.string.create_goal_large_wall_warning),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Button(
                        onClick = onCreate,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(stringResource(R.string.create_goal_create_anyway))
                    }
                }
            }
        } else {
            // Create goal button
            Button(
                onClick = onCreate,
                modifier = Modifier.fillMaxWidth(),
                enabled = canCreate,
            ) {
                Text(pluralStringResource(R.plurals.create_goal_create, viewModel.tasks.size))
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: app.dopedgoal.core.Task,
    ordinal: Int,
    onNameChange: (String) -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
        ) {
            // Ordinal
            Text(
                text = "#$ordinal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )

            // Brick preview
            Box(
                modifier = Modifier
                    .size(48.dp, 21.dp)
                    .clipToBounds(),
            ) {
                BrickCanvas(
                    brick = task.brick,
                    modifier = Modifier.size(48.dp, 21.dp),
                    lod = BrickLod.Thumbnail,
                )
            }

            // Task name input
            TextField(
                value = task.name,
                onValueChange = onNameChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.create_goal_task_hint)) },
                singleLine = true,
            )

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.content_desc_delete_task))
            }
        }
    }
}

class CreateGoalViewModelFactory : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: kotlin.reflect.KClass<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
        return CreateGoalViewModel() as T
    }
}