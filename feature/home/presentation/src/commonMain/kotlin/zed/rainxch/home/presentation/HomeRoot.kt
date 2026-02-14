package zed.rainxch.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import zed.rainxch.githubstore.core.presentation.res.*
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import zed.rainxch.core.domain.model.Component
import zed.rainxch.core.presentation.components.GithubStoreButton
import zed.rainxch.core.presentation.components.RepositoryCard
import zed.rainxch.core.presentation.locals.LocalBottomNavigationLiquid
import zed.rainxch.core.presentation.theme.GithubStoreTheme
import zed.rainxch.core.presentation.utils.ObserveAsEvents
import zed.rainxch.home.presentation.components.HomeFilterChip

@Composable
fun HomeRoot(
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToDetails: (Component) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyStaggeredGridState()
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

                is HomeAction.OnComponentClick -> {
                    onNavigateToDetails(action.component)
                }

                else -> {
                    viewModel.onAction(action)
                }
            }
        },
        listState = listState
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    listState: LazyStaggeredGridState,
) {
    val liquidState = LocalBottomNavigationLiquid.current

    Scaffold(
        topBar = {
            HomeTopAppBar()
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp)
                .liquefiable(liquidState)
        ) {
            FilterChips(state, onAction)

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

@Composable
private fun MainState(
    state: HomeState,
    listState: LazyStaggeredGridState,
    onAction: (HomeAction) -> Unit,
    liquidState: LiquidState
) {
    if (state.components.isNotEmpty()) {
        LazyVerticalStaggeredGrid(
            state = listState,
            columns = StaggeredGridCells.Adaptive(350.dp),
            verticalItemSpacing = 12.dp,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = state.components,
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
private fun FilterChips(
    state: HomeState,
    onAction: (HomeAction) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = state.categories,
            key = { it.name }
        ) { category ->
            HomeFilterChip(
                selectedCategory = state.currentCategory,
                category = category,
                onClick = {
                    onAction(HomeAction.SwitchCategory(category))
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeTopAppBar() {
    TopAppBar(
        navigationIcon = {
            Image(
                painter = painterResource(Res.drawable.app_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        },
        title = {
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 4.dp),
                maxLines = 2,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = Modifier.padding(12.dp)
    )
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
                listState = rememberLazyStaggeredGridState()
            )
        }
    }
}
