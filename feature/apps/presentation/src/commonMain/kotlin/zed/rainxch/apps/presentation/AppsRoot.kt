@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package zed.rainxch.apps.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skydoves.landscapist.coil3.CoilImage
import io.github.fletchmckee.liquid.liquefiable
import zed.rainxch.githubstore.core.presentation.res.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import zed.rainxch.apps.presentation.model.AppItem
import zed.rainxch.apps.presentation.model.UpdateAllProgress
import zed.rainxch.apps.presentation.model.UpdateState
import zed.rainxch.core.presentation.locals.LocalBottomNavigationLiquid
import zed.rainxch.core.presentation.theme.GithubStoreTheme
import zed.rainxch.core.presentation.utils.ObserveAsEvents

@Composable
fun AppsRoot(
    onNavigateBack: () -> Unit,
    onNavigateToComponent: (String) -> Unit,
    viewModel: AppsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is AppsEvent.NavigateToComponent -> {
                onNavigateToComponent(event.componentId)
            }

            is AppsEvent.ShowError -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
            }

            is AppsEvent.ShowSuccess -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    AppsScreen(
        state = state,
        onAction = { action ->
            when (action) {
                AppsAction.OnNavigateBackClick -> {
                    onNavigateBack()
                }

                else -> {
                    viewModel.onAction(action)
                }
            }
        },
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun AppsScreen(
    state: AppsState,
    onAction: (AppsAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val liquidState = LocalBottomNavigationLiquid.current
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(AppsAction.OnNavigateBackClick) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.navigate_back)
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(Res.string.installed_apps),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        modifier = Modifier.liquefiable(liquidState)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TextField(
                value = state.searchQuery,
                onValueChange = { onAction(AppsAction.OnSearchChange(it)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                placeholder = { Text(stringResource(Res.string.search_your_apps)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            val hasUpdates = state.apps.any { it.installedApp.isUpdateAvailable }
            if (hasUpdates && !state.isUpdatingAll) {
                Button(
                    onClick = { onAction(AppsAction.OnUpdateAll) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    enabled = state.updateAllButtonEnabled
                ) {
                    Icon(
                        imageVector = Icons.Default.Update,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = stringResource(Res.string.update_all)
                    )
                }
            }

            if (state.isUpdatingAll && state.updateAllProgress != null) {
                UpdateAllProgressCard(
                    progress = state.updateAllProgress,
                    onCancel = { onAction(AppsAction.OnCancelUpdateAll) }
                )
            }

            val filteredApps = remember(state.apps, state.searchQuery) {
                if (state.searchQuery.isBlank()) {
                    state.apps
                } else {
                    state.apps.filter { appItem ->
                        appItem.installedApp.name.contains(
                            state.searchQuery,
                            ignoreCase = true
                        ) ||
                                appItem.installedApp.author.contains(
                                    state.searchQuery,
                                    ignoreCase = true
                                ) ||
                                appItem.installedApp.category.contains(
                                    state.searchQuery,
                                    ignoreCase = true
                                )
                    }
                }
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                filteredApps.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(Res.string.no_apps_found))
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredApps,
                            key = { it.installedApp.componentId }
                        ) { appItem ->
                            AppItemCard(
                                appItem = appItem,
                                onOpenClick = { onAction(AppsAction.OnOpenApp(appItem.installedApp)) },
                                onUpdateClick = { onAction(AppsAction.OnUpdateApp(appItem.installedApp)) },
                                onUninstallClick = { onAction(AppsAction.OnUninstallApp(appItem.installedApp)) },
                                onDetailsClick = { onAction(AppsAction.OnNavigateToComponent(appItem.installedApp.componentId)) },
                                modifier = Modifier.liquefiable(liquidState)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateAllProgressCard(
    progress: UpdateAllProgress,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        Res.string.updating_x_of_y,
                        progress.current,
                        progress.total
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onCancel) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(Res.string.cancel)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(
                    Res.string.currently_updating,
                    progress.currentAppName
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            LinearWavyProgressIndicator(
                progress = { progress.current.toFloat() / progress.total },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AppItemCard(
    appItem: AppItem,
    onOpenClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onUninstallClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = appItem.installedApp

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onDetailsClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (app.icon.isNotBlank()) {
                    CoilImage(
                        imageModel = { app.icon },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularWavyProgressIndicator()
                            }
                        }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (app.author.isNotBlank()) {
                        Text(
                            text = app.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (app.category.isNotBlank()) {
                        Text(
                            text = app.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (app.isUpdateAvailable && app.latestVersion != null) {
                        Text(
                            text = "${app.installedVersion} → ${app.latestVersion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = app.installedVersion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val description = app.description
            if (!description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(12.dp))

            when (val state = appItem.updateState) {
                is UpdateState.Downloading -> {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(Res.string.downloading),
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (appItem.downloadProgress != null) {
                                Text(
                                    text = "${appItem.downloadProgress}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearWavyProgressIndicator(
                            progress = { (appItem.downloadProgress ?: 0) / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                is UpdateState.Installing -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = stringResource(Res.string.installing),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                is UpdateState.CheckingUpdate -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = stringResource(Res.string.checking),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                is UpdateState.Success -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(Res.string.updated_successfully),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                is UpdateState.Error -> {
                    Text(
                        text = stringResource(Res.string.error_with_message, state.message),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                UpdateState.Idle -> {
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (app.runnable) {
                    Button(
                        onClick = onOpenClick,
                        modifier = Modifier.weight(1f),
                        enabled = appItem.updateState !is UpdateState.Downloading &&
                                appItem.updateState !is UpdateState.Installing
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(Res.string.open)
                        )
                    }
                }

                if (app.isUpdateAvailable &&
                    appItem.updateState !is UpdateState.Downloading &&
                    appItem.updateState !is UpdateState.Installing &&
                    appItem.updateState !is UpdateState.CheckingUpdate
                ) {
                    Button(
                        onClick = onUpdateClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(Res.string.update)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onUninstallClick,
                    enabled = appItem.updateState !is UpdateState.Downloading &&
                            appItem.updateState !is UpdateState.Installing
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                OutlinedButton(
                    onClick = onDetailsClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    GithubStoreTheme {
        AppsScreen(
            state = AppsState(),
            onAction = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}
