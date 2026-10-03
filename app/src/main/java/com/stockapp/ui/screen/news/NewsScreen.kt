package com.stockapp.ui.screen.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Chip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stockapp.R
import com.stockapp.data.model.ImpactLevel
import com.stockapp.data.model.NewsArticle
import com.stockapp.data.model.NewsSentiment
import com.stockapp.ui.components.CommonComponents
import com.stockapp.ui.theme.StockAppTheme
import com.stockapp.ui.theme.StockAppTheme.colorScheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@Composable
fun NewsScreen(
    viewModel: NewsViewModel = hiltViewModel(),
    onArticleClick: (NewsArticle) -> Unit
) {
    val articles by viewModel.articles.observeAsState(emptyList())
    val filteredArticles by viewModel.filteredArticles.observeAsState(emptyList())
    val dailyRecap by viewModel.dailyRecap.observeAsState(null)
    val selectedSentiment by viewModel.selectedSentiment.observeAsState<NewsSentiment?>(null)
    val selectedImpact by viewModel.selectedImpact.observeAsState<ImpactLevel?>(null)
    val isLoading by viewModel.isLoading.observeAsState(false)
    val isLoadingRecap by viewModel.isLoadingRecap.observeAsState(false)
    val error by viewModel.error.observeAsState<String?>(null)

    val showSentimentFilter = remember { mutableStateOf(false) }
    val showImpactFilter = remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(key1 = true) {
        viewModel.loadNews()
        viewModel.loadDailyRecap()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "News & Analysis", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                        Text(text = "Real-time updates for your portfolio", fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { viewModel.loadNews() }) {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Refresh, contentDescription = "Refresh", tint = colorScheme.primary)
                    }
                }
            }
        }

        // Daily Recap Banner
        dailyRecap?.let { recap ->
            RecapBanner(recap = recap, onDismiss = { viewModel.dismissDailyRecap() })
        }

        // Search & Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Search news...") },
                leadingIcon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            // Sentiment Filter
            FilterChip(
                text = selectedSentiment?.value ?: "Sentiment",
                selected = selectedSentiment != null,
                onClick = { showSentimentFilter.value = true }
            )

            // Impact Filter
            FilterChip(
                text = selectedImpact?.value ?: "Impact",
                selected = selectedImpact != null,
                onClick = { showImpactFilter.value = true }
            )
        }

        // Sentiment Filter Dropdown
        if (showSentimentFilter.value) {
            SentimentFilterDropdown(
                selected = selectedSentiment,
                onSelect = { sentiment ->
                    viewModel.setSentimentFilter(sentiment)
                    showSentimentFilter.value = false
                },
                onClear = {
                    viewModel.setSentimentFilter(null)
                    showSentimentFilter.value = false
                }
            )
        }

        // Impact Filter Dropdown
        if (showImpactFilter.value) {
            ImpactFilterDropdown(
                selected = selectedImpact,
                onSelect = { impact ->
                    viewModel.setImpactFilter(impact)
                    showImpactFilter.value = false
                },
                onClear = {
                    viewModel.setImpactFilter(null)
                    showImpactFilter.value = false
                }
            )
        }

        // News List
        if (isLoading && filteredArticles.isEmpty()) {
            CommonComponents.LoadingState(modifier = Modifier.fillMaxSize().padding(16.dp))
        } else if (filteredArticles.isEmpty()) {
            CommonComponents.EmptyState(
                icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Newspaper, contentDescription = "No news", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp)) },
                title = "No News Articles",
                message = "No news matches your current filters. Try adjusting your filters or pull to refresh.",
                actionText = "Refresh",
                onAction = { viewModel.loadNews() },
                modifier = Modifier.fillMaxSize().padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredArticles) { article ->
                    NewsArticleCard(
                        article = article,
                        onClick = { onArticleClick(article) },
                        onMarkRead = { viewModel.markAsRead(article) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecapBanner(
    recap: com.stockapp.data.model.DailyNewsRecap,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = StockAppTheme.colorScheme.primaryContainer,
            contentColor = StockAppTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Summarize,
                        contentDescription = "Recap",
                        tint = StockAppTheme.colorScheme.primary
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.width(8.dp))
                    Text(text = "Daily Market Recap", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StockAppTheme.colorScheme.onPrimaryContainer)
                }
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(4.dp))
                Text(text = recap.summary, fontSize = 14.sp, color = StockAppTheme.colorScheme.onPrimaryContainer, maxLines = 2, overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis)
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(4.dp))
                Text(text = "${recap.articlesCount} articles • ${recap.portfolioImpact} portfolio impact", fontSize = 12.sp, color = StockAppTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            }
            IconButton(onClick = onDismiss) {
                Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Dismiss", tint = StockAppTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Chip(
        onClick = onClick,
        modifier = modifier,
        colors = androidx.compose.material3.ChipDefaults.chipColors(
            containerColor = if (selected) StockAppTheme.colorScheme.primary else StockAppTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) StockAppTheme.colorScheme.onPrimary else StockAppTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SentimentFilterDropdown(
    selected: NewsSentiment?,
    onSelect: (NewsSentiment) -> Unit,
    onClear: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        androidx.compose.material3.DropdownMenu(
            expanded = true,
            onDismissRequest = { /* handled by parent */ }
        ) {
            DropdownMenuItem(
                text = { Text(text = "All Sentiments", fontWeight = if (selected == null) FontWeight.Bold else FontWeight.Normal) },
                onClick = onClear
            )
            androidx.compose.material3.Divider()
            NewsSentiment.values().forEach { sentiment ->
                DropdownMenuItem(
                    text = { Text(text = sentiment.value) },
                    onClick = { onSelect(sentiment) }
                )
            }
        }
    }
}

@Composable
fun ImpactFilterDropdown(
    selected: ImpactLevel?,
    onSelect: (ImpactLevel) -> Unit,
    onClear: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        androidx.compose.material3.DropdownMenu(
            expanded = true,
            onDismissRequest = { /* handled by parent */ }
        ) {
            DropdownMenuItem(
                text = { Text(text = "All Impact Levels", fontWeight = if (selected == null) FontWeight.Bold else FontWeight.Normal) },
                onClick = onClear
            )
            androidx.compose.material3.Divider()
            ImpactLevel.values().forEach { impact ->
                DropdownMenuItem(
                    text = { Text(text = impact.value) },
                    onClick = { onSelect(impact) }
                )
            }
        }
    }
}

@Composable
fun NewsArticleCard(
    article: NewsArticle,
    onClick: () -> Unit,
    onMarkRead: () -> Unit
) {
    val sentimentColor = when (article.sentiment) {
        NewsSentiment.POSITIVE -> Color(0xFF2E7D32)
        NewsSentiment.NEGATIVE -> Color(0xFFC62828)
        else -> StockAppTheme.colorScheme.onSurfaceVariant
    }

    val impactColor = when (article.impactLevel) {
        ImpactLevel.CRITICAL -> Color(0xFFC62828)
        ImpactLevel.HIGH -> Color(0xFFF57C00)
        ImpactLevel.MEDIUM -> Color(0xFF1976D2)
        else -> StockAppTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (article.isRead) StockAppTheme.colorScheme.surfaceVariant else StockAppTheme.colorScheme.surface,
            contentColor = StockAppTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = article.source, fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
                        Chip(
                            onClick = {},
                            colors = androidx.compose.material3.ChipDefaults.chipColors(
                                containerColor = impactColor.copy(alpha = 0.1f),
                                contentColor = impactColor
                            )
                        ) {
                            Text(text = article.impactLevel.value, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(4.dp))
                    Text(text = article.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StockAppTheme.colorScheme.onSurface, maxLines = 2, overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis)
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(4.dp))
                    Text(text = article.summary, fontSize = 14.sp, color = StockAppTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis)
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            article.symbols.take(3).forEach { symbol ->
                                Chip(
                                    onClick = {},
                                    modifier = Modifier.padding(end = 4.dp),
                                    colors = androidx.compose.material3.ChipDefaults.chipColors(
                                        containerColor = StockAppTheme.colorScheme.primaryContainer,
                                        contentColor = StockAppTheme.colorScheme.onPrimaryContainer
                                    )
                                ) {
                                    Text(text = symbol, fontSize = 10.sp)
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.SentimentSatisfied,
                                contentDescription = "Sentiment",
                                tint = sentimentColor
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.width(4.dp))
                            Text(text = article.sentiment.value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = sentimentColor)
                            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.width(16.dp))
                            Text(text = article.publishedAt.toString(), fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                IconButton(onClick = onMarkRead) {
                    Icon(
                        imageVector = if (article.isRead) androidx.compose.material.icons.Icons.Default.MarkEmailRead else androidx.compose.material.icons.Icons.Default.MarkEmailUnread,
                        contentDescription = if (article.isRead) "Mark unread" else "Mark read",
                        tint = StockAppTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}