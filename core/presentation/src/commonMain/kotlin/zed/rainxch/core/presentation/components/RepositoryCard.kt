package zed.rainxch.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.coil3.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.crossfade.CrossfadePlugin
import zed.rainxch.githubstore.core.presentation.res.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import zed.rainxch.core.domain.model.Component
import zed.rainxch.core.domain.model.ComponentType
import zed.rainxch.core.presentation.model.DiscoveryRepository
import zed.rainxch.core.presentation.theme.GithubStoreTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RepositoryCard(
    discoveryRepository: DiscoveryRepository,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box {
            if (discoveryRepository.isFavourite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.BottomStart)
                        .offset(x = (-32).dp, y = 32.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (discoveryRepository.component.icon.isNotEmpty()) {
                        CoilImage(
                            imageModel = { discoveryRepository.component.icon },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularWavyProgressIndicator()
                                }
                            },
                            component = rememberImageComponent {
                                CrossfadePlugin()
                            }
                        )
                    }

                    Text(
                        text = discoveryRepository.component.author,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "• ${discoveryRepository.component.type.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = discoveryRepository.component.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = discoveryRepository.component.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    softWrap = true
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "v${discoveryRepository.component.version}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "• ${discoveryRepository.component.category}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (discoveryRepository.isInstalled) {
                    Spacer(Modifier.height(12.dp))

                    InstallStatusBadge(
                        isUpdateAvailable = discoveryRepository.isUpdateAvailable
                    )
                }

                Spacer(Modifier.height(24.dp))

                GithubStoreButton(
                    text = stringResource(Res.string.home_view_details),
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun InstallStatusBadge(
    isUpdateAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isUpdateAvailable) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val textColor = if (isUpdateAvailable) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    val icon = if (isUpdateAvailable) {
        Icons.Default.Update
    } else {
        Icons.Default.CheckCircle
    }

    val text = if (isUpdateAvailable) {
        stringResource(Res.string.update_available)
    } else {
        stringResource(Res.string.installed)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = textColor
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview
@Composable
fun RepositoryCardPreview() {
    GithubStoreTheme {
        RepositoryCard(
            discoveryRepository = DiscoveryRepository(
                component = Component(
                    id = "auto-crop",
                    name = "Auto Crop",
                    type = ComponentType.PLUGIN,
                    description = "Automatically crop layers to content bounds",
                    version = "2.1.0",
                    author = "AEJuice",
                    category = "Plugins",
                    icon = ""
                ),
                isUpdateAvailable = true,
                isFavourite = true,
                isInstalled = true,
            ),
            onClick = { },
        )
    }
}
