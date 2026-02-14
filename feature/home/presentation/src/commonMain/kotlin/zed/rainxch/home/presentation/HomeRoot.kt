package zed.rainxch.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import zed.rainxch.githubstore.core.presentation.res.*
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import zed.rainxch.core.domain.model.Component
import zed.rainxch.core.presentation.components.GithubStoreButton
import zed.rainxch.core.presentation.components.RepositoryCard
import zed.rainxch.core.presentation.locals.LocalBottomNavigationLiquid
import zed.rainxch.core.presentation.theme.GithubStoreTheme
import zed.rainxch.core.presentation.utils.ObserveAsEvents
import zed.rainxch.home.domain.model.HomeCategory

@Composable
fun HomeRoot(
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToFavourites: () -> Unit,
    onNavigateToDetails: (Component) -> Unit,
    onInstallModeClick: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            HomeEvent.OnScrollToListTop -> {
                scope.launch {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }

    HomeScreen(
        state = state,
        onAction = { action ->
            when (action) {
                HomeAction.OnSearchClick -> {
                    onNavigateToSearch()
                }

                HomeAction.OnSettingsClick -> {
                    onNavigateToSettings()
                }

                HomeAction.OnAppsClick -> {
                    onNavigateToApps()
                }

                HomeAction.OnFavouritesClick -> {
                    onNavigateToFavourites()
                }

                is HomeAction.OnComponentClick -> {
                    onNavigateToDetails(action.component)
                }

                HomeAction.OnInstallModeClick -> {
                    onInstallModeClick()
                }

                else -> {
                    viewModel.onAction(action)
                }
            }
        },
        listState = listState
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    listState: LazyGridState,
) {
    val liquidState = LocalBottomNavigationLiquid.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .liquefiable(liquidState)
        ) {
            HomeSidebar(state, onAction)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                HomeTopBar(onAction)

                Box(Modifier.fillMaxSize()) {
                    LoadingState(state)

                    ErrorState(state, onAction)

                    MainState(
                        state = state,
                        listState = listState,
                        onAction = onAction,
                        liquidState = liquidState
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSidebar(
    state: HomeState,
    onAction: (HomeAction) -> Unit
) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(12.dp)
    ) {
        AppSelector(
            selectedApp = state.selectedApp,
            availableApps = state.availableApps,
            onAppSelected = { onAction(HomeAction.SwitchApp(it)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(12.dp))

        PriceFilter(
            selectedFilter = state.priceFilter,
            onFilterSelected = { onAction(HomeAction.SwitchPriceFilter(it)) }
        )

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(12.dp))

        Text(
            text = "Categories",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Spacer(Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(
                items = state.categories,
                key = { it.name }
            ) { category ->
                SidebarCategoryItem(
                    category = category,
                    isSelected = state.currentCategory == category,
                    onClick = { onAction(HomeAction.SwitchCategory(category)) }
                )
            }
        }
    }
}

@Composable
private fun SidebarCategoryItem(
    category: HomeCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val label = if (category == HomeCategory.ALL) "All" else category.name
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
private fun PriceFilter(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("All", "Free", "Paid")

    Text(
        text = "Price",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )

    Spacer(Modifier.height(4.dp))

    filters.forEach { filter ->
        val isSelected = selectedFilter == filter
        val backgroundColor = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }
        val textColor = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .clickable { onFilterSelected(filter) }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = filter,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    onAction: (HomeAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onAction(HomeAction.OnSearchClick) }
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        IconButton(
            onClick = { onAction(HomeAction.OnFavouritesClick) }
        ) {
            Icon(
                imageVector = Icons.Filled.FavoriteBorder,
                contentDescription = "Favourites",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        IconButton(
            onClick = { onAction(HomeAction.OnAppsClick) }
        ) {
            Icon(
                imageVector = Icons.Filled.Apps,
                contentDescription = "Installed",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        IconButton(
            onClick = { onAction(HomeAction.OnInstallModeClick) }
        ) {
            Icon(
                imageVector = Icons.Filled.CloudDownload,
                contentDescription = "Custom Install",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun MainState(
    state: HomeState,
    listState: LazyGridState,
    onAction: (HomeAction) -> Unit,
    liquidState: LiquidState
) {
    val appFiltered = if (state.selectedApp == "All") {
        state.components
    } else {
        state.components.filter { item ->
            item.component.compatibleApps.contains(state.selectedApp) ||
                item.component.compatibleApps.contains("Standalone")
        }
    }

    val filteredComponents = when (state.priceFilter) {
        "Free" -> appFiltered.filter { it.component.price == 0 }
        "Paid" -> appFiltered.filter { it.component.price > 0 }
        else -> appFiltered
    }

    if (filteredComponents.isNotEmpty()) {
        LazyVerticalGrid(
            state = listState,
            columns = GridCells.Adaptive(350.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = filteredComponents,
                key = { it.component.id },
                contentType = { "component" }
            ) { item ->
                RepositoryCard(
                    discoveryRepository = item,
                    onClick = {
                        onAction(HomeAction.OnComponentClick(item.component))
                    },
                    modifier = Modifier
                        .animateItem()
                        .liquefiable(liquidState)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingState(state: HomeState) {
    if (state.isLoading && state.components.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularWavyProgressIndicator()

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.home_finding_repositories),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ErrorState(
    state: HomeState,
    onAction: (HomeAction) -> Unit
) {
    if (state.errorMessage != null && state.components.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                GithubStoreButton(
                    text = stringResource(Res.string.home_retry),
                    onClick = {
                        onAction(HomeAction.Retry)
                    }
                )
            }
        }
    }
}

@Composable
private fun AppSelector(
    selectedApp: String,
    availableApps: List<String>,
    onAppSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (selectedApp == "All") "All Apps" else selectedApp,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableApps.forEach { app ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (app == "All") "All Apps" else app,
                            fontWeight = if (app == selectedApp) {
                                FontWeight.Bold
                            } else null
                        )
                    },
                    onClick = {
                        onAppSelected(app)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Preview
@Composable
private fun Preview() {
    GithubStoreTheme {
        val liquidState = rememberLiquidState()

        CompositionLocalProvider(
            value = LocalBottomNavigationLiquid provides liquidState
        ) {
            HomeScreen(
                state = HomeState(),
                onAction = {},
                listState = rememberLazyGridState()
            )
        }
    }
}
