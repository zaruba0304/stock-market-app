package com.stockapp.ui.screen.home

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Chip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stockapp.R
import com.stockapp.data.model.Holding
import com.stockapp.data.model.PortfolioSummary
import com.stockapp.data.model.User
import com.stockapp.ui.components.CommonComponents
import com.stockapp.ui.components.StatCard
import com.stockapp.ui.theme.StockAppTheme
import com.stockapp.ui.theme.StockAppTheme.colorScheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToPortfolio: () -> Unit,
    onNavigateToIPO: () -> Unit,
    onNavigateToNews: () -> Unit,
    onNavigateToScreeners: () -> Unit,
    onNavigateToAI: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val portfolioSummary by viewModel.portfolioSummary.observeAsState(null)
    val topMovers by viewModel.topMovers.observeAsState(emptyList())
    val topLosers by viewModel.topLosers.observeAsState(emptyList())
    val upcomingIPOs by viewModel.upcomingIPOs.observeAsState(emptyList())
    val latestNews by viewModel.latestNews.observeAsState(emptyList())
    val primaryUser by viewModel.primaryUser.observeAsState(null)
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState<String?>(null)

    androidx.compose.runtime.LaunchedEffect(key1 = true) {
        viewModel.loadHomeData()
    }

    if (isLoading && portfolioSummary == null) {
        CommonComponents.LoadingState()
        return
    }

    error?.let { errorMsg ->
        CommonComponents.ErrorState(message = errorMsg, onRetry = { viewModel.loadHomeData() })
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Header with user greeting
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = "Good ${getGreeting()}, ${primaryUser?.name?.split(" ").first() ?: "Investor"}!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "Here's your portfolio snapshot",
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        // Portfolio Summary Cards
        portfolioSummary?.let { summary ->
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Value",
                        value = summary.formattedTotalValue,
                        change = "${if (summary.totalGainLoss >= 0) "+" else ""}₹${String.format("%.2f", summary.totalGainLoss)} (${if (summary.totalGainLossPercent >= 0) "+" else ""}${String.format("%.2f", summary.totalGainLossPercent)}%)",
                        isPositive = summary.totalGainLoss >= 0,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Day Change",
                        value = "${if (summary.dayChange >= 0) "+" else ""}₹${String.format("%.2f", summary.dayChange)}",
                        change = "${if (summary.dayChangePercent >= 0) "+" else ""}${String.format("%.2f", summary.dayChangePercent)}%",
                        isPositive = summary.dayChange >= 0,
                        modifier = Modifier.weight(1f)
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Invested",
                        value = summary.formattedTotalInvested,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Holdings",
                        value = "${summary.holdingsCount}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(16.dp))

        // Quick Actions
        CommonComponents.StockTopAppBar(
            title = "Quick Actions",
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.AccountBalance, contentDescription = "Portfolio", tint = colorScheme.primary) },
                title = "Portfolio",
                subtitle = "${portfolioSummary?.holdingsCount ?: 0} holdings",
                onClick = onNavigateToPortfolio
            )
            QuickActionCard(
                icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.HowToReg, contentDescription = "IPO", tint = colorScheme.secondary) },
                title = "IPO",
                subtitle = "${upcomingIPOs.count { it.status.value == "Open" }} open",
                onClick = onNavigateToIPO
            )
            QuickActionCard(
                icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Newspaper, contentDescription = "News", tint = colorScheme.tertiary) },
                title = "News",
                subtitle = "${latestNews.size} articles",
                onClick = onNavigateToNews
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.FilterList, contentDescription = "Screeners", tint = Color(0xFF7B1FA2)) },
                title = "Screeners",
                subtitle = "Find opportunities",
                onClick = onNavigateToScreeners
            )
            QuickActionCard(
                icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Psychology, contentDescription = "AI Analysis", tint = Color(0xFF00695C)) },
                title = "AI Insights",
                subtitle = "Smart analysis",
                onClick = onNavigateToAI
            )
            QuickActionCard(
                icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Settings, contentDescription = "Settings", tint = colorScheme.onSurfaceVariant) },
                title = "Settings",
                subtitle = "Configure app",
                onClick = onNavigateToSettings
            )
        }

        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(16.dp))

        // Top Movers
        if (topMovers.isNotEmpty()) {
            CommonComponents.StockTopAppBar(
                title = "Top Gainers",
                modifier = Modifier.padding(top = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(topMovers) { holding ->
                    CommonComponents.HoldingRow(holding = holding)
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(16.dp))
        }

        // Top Losers
        if (topLosers.isNotEmpty()) {
            CommonComponents.StockTopAppBar(
                title = "Top Losers",
                modifier = Modifier.padding(top = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(topLosers) { holding ->
                    CommonComponents.HoldingRow(holding = holding)
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(16.dp))
        }

        // Upcoming IPOs
        if (upcomingIPOs.isNotEmpty()) {
            CommonComponents.StockTopAppBar(
                title = "Upcoming IPOs",
                modifier = Modifier.padding(top = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(upcomingIPOs.take(3)) { ipo ->
                    CommonComponents.IPORow(ipo = ipo)
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(16.dp))
        }

        // Latest News
        if (latestNews.isNotEmpty()) {
            CommonComponents.StockTopAppBar(
                title = "Latest News",
                modifier = Modifier.padding(top = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(latestNews.take(3)) { article ->
                    CommonComponents.NewsCard(article = article)
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(16.dp))
        }
    }
}

@Composable
fun QuickActionCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.weight(1f)
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = StockAppTheme.colorScheme.surface,
            contentColor = StockAppTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(8.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StockAppTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun getGreeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour in 5..11 -> "Morning"
        hour in 12..16 -> "Afternoon"
        hour in 17..21 -> "Evening"
        else -> "Night"
    }
}