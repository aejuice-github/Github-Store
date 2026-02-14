package zed.rainxch.search.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.fletchmckee.liquid.liquefiable
import zed.rainxch.githubstore.core.presentation.res.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import zed.rainxch.core.domain.model.Component
import zed.rainxch.core.presentation.components.RepositoryCard
import zed.rainxch.core.presentation.locals.LocalBottomNavigationLiquid
import zed.rainxch.core.presentation.theme.GithubStoreTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRoot(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (Component) -> Unit,
    viewModel: SearchViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SearchScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is SearchAction.OnComponentClick -> {
                    onNavigateToDetails(action.component)
                }

                SearchAction.OnNavigateBackClick -> {
                    onNavigateBack()
                }

                else -> {
                    viewModel.onAction(action)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchScreen(
    state: SearchState,
    onAction: (SearchAction) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val liquidState = LocalBottomNavigationLiquid.current

    LaunchedEffect(Unit) {
        if (state.query.isEmpty()) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            SearchTopbar(
                onAction = onAction,
                state = state,
                focusRequester = focusRequester
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.liquefiable(liquidState)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp)
        ) {
            Box(Modifier.fillMaxSize()) {
                if (state.isLoading && state.results.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().imePadding(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularWavyProgressIndicator()
                    }
                }

                if (state.errorMessage != null && state.results.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.errorMessage
                            )

                            Spacer(Modifier.height(8.dp))

                            Button(onClick = { onAction(SearchAction.Retry) }) {
                                Text(
                                    text = stringResource(Res.string.retry)
                                )
                            }
                        }
                    }
                }

                if (state.results.isNotEmpty()) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(350.dp),
                        verticalItemSpacing = 12.dp,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .liquefiable(liquidState)
                    ) {
                        items(
                            items = state.results,
                            key = { it.component.id },
                        ) { discoveryRepository ->
                            RepositoryCard(
                                discoveryRepository = discoveryRepository,
                                onClick = {
                                    onAction(SearchAction.OnComponentClick(discoveryRepository.component))
                                },
                                modifier = Modifier
                                    .animateItem()
                                    .liquefiable(liquidState)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTopbar(
    onAction: (SearchAction) -> Unit,
    state: SearchState,
    focusRequester: FocusRequester
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = { onAction(SearchAction.OnNavigateBackClick) }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.navigate_back),
                modifier = Modifier.size(24.dp)
            )
        }

        TextField(
            value = state.query,
            onValueChange = { value ->
                onAction(SearchAction.OnSearchChange(value))
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            placeholder = {
                Text(
                    text = stringResource(Res.string.search_repositories_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onAction(SearchAction.OnSearchImeClick) }
            ),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            ),
            shape = CircleShape,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    GithubStoreTheme {
        SearchScreen(
            state = SearchState(),
            onAction = {}
        )
    }
}
