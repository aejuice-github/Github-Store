package zed.rainxch.githubstore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import zed.rainxch.core.presentation.theme.GithubStoreTheme
import zed.rainxch.core.presentation.utils.ApplyAndroidSystemBars
import zed.rainxch.githubstore.app.navigation.AppNavigation

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App(
    mainViewModel: MainViewModel = koinViewModel(),
    onBrowseFiles: () -> Unit = {},
    dragDropModifier: Modifier = Modifier
) {
    val state by mainViewModel.state.collectAsStateWithLifecycle()

    val navBackStack = rememberNavController()

    GithubStoreTheme(
        fontTheme = state.currentFontTheme,
        appTheme = state.currentColorTheme,
        isAmoledTheme = state.isAmoledTheme,
        isDarkTheme = state.isDarkTheme ?: isSystemInDarkTheme()
    ) {
        ApplyAndroidSystemBars(state.isDarkTheme)

        Box(modifier = Modifier.fillMaxSize().then(dragDropModifier)) {
            when (state.appMode) {
                AppMode.STORE -> {
                    AppNavigation(
                        navController = navBackStack,
                        onInstallModeClick = {
                            mainViewModel.onAction(MainAction.SwitchMode(AppMode.INSTALL))
                        }
                    )
                }

                AppMode.INSTALL -> {
                    InstallDropZone(
                        onSwitchToStore = {
                            mainViewModel.onAction(MainAction.SwitchMode(AppMode.STORE))
                        },
                        onBrowseFiles = onBrowseFiles
                    )
                }
            }

            DragOverlay(isDraggingOver = state.isDraggingOver)

            DragDropSnackbar(
                message = state.dragDropMessage,
                showBrowseMore = state.showBrowseMore,
                onDismiss = { mainViewModel.onAction(MainAction.DismissDragDropMessage) },
                onBrowseMore = {
                    mainViewModel.onAction(MainAction.DismissDragDropMessage)
                    navBackStack.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun DragOverlay(isDraggingOver: Boolean) {
    AnimatedVisibility(
        visible = isDraggingOver,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Drop to install",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Supported: .jsx, .jsxbin, .aex, .ofx, .zxp",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun InstallDropZone(
    onSwitchToStore: () -> Unit,
    onBrowseFiles: () -> Unit = {}
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 480.dp, height = 320.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onBrowseFiles() }
                    .drawBehind {
                        val stroke = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(12.dp.toPx(), 8.dp.toPx()),
                                0f
                            )
                        )
                        drawRoundRect(
                            color = borderColor,
                            cornerRadius = CornerRadius(16.dp.toPx()),
                            style = stroke
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Click to browse files",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = ".jsx  .jsxbin  .aex  .ofx  .zxp",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            FilledTonalButton(onClick = onSwitchToStore) {
                Icon(
                    imageVector = Icons.Filled.Store,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Browse Store")
            }
        }
    }
}

@Composable
private fun DragDropSnackbar(
    message: String?,
    showBrowseMore: Boolean,
    onDismiss: () -> Unit,
    onBrowseMore: () -> Unit
) {
    if (message != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = if (showBrowseMore) {
                    {
                        TextButton(onClick = onBrowseMore) {
                            Text("Browse more components")
                        }
                    }
                } else null,
                dismissAction = {
                    TextButton(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(message)
            }
        }
    }
}
