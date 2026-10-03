package com.stockapp.ui.screen.portfolio

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
import com.stockapp.data.model.Holding
import com.stockapp.data.model.PortfolioSummary
import com.stockapp.data.model.Transaction
import com.stockapp.ui.components.CommonComponents
import com.stockapp.ui.components.StatCard
import com.stockapp.ui.theme.StockAppTheme
import com.stockapp.ui.theme.StockAppTheme.colorScheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@Composable
fun PortfolioScreen(
    viewModel: PortfolioViewModel = hiltViewModel(),
    onHoldingClick: (Holding) -> Unit
) {
    val portfolioSummary by viewModel.portfolioSummary.observeAsState(null)
    val holdings by viewModel.holdings.observeAsState(emptyList())
    val filteredHoldings by viewModel.filteredHoldings.observeAsState(emptyList())
    val selectedSort by viewModel.selectedSort.observeAsState(PortfolioSortBy.CURRENT_VALUE)
    val selectedFilter by viewModel.selectedFilter.observeAsState(PortfolioFilter.ALL)
    val isLoading by viewModel.isLoading.observeAsState(false)
    val isSyncing by viewModel.isSyncing.observeAsState(false)
    val error by viewModel.error.observeAsState<String?>(null)
    val lastSynced by viewModel.lastSynced.observeAsState<String?>(null)

    val showSortMenu = remember { mutableStateOf(false) }
    val showFilterMenu = remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(key1 = true) {
        viewModel.loadPortfolio()
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
                        Text(text = "Portfolio", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                        Text(text = "${holdings.size} holdings", fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        lastSynced?.let { time ->
                            Text(text = "Synced $time", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.width(8.dp))
                        IconButton(onClick = { viewModel.syncPortfolio() }, enabled = !isSyncing) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Icon(imageVector = androidx.compose.material.icons.Icons.Default.Sync, contentDescription = "Sync Portfolio", tint = colorScheme.primary)
                            }
                        }
                    }
                }
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

        // Toolbar with Search, Sort, Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search
            OutlinedTextField(
                value = "",
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Search holdings...") },
                leadingIcon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            // Sort Dropdown
            ExposedDropdownMenuBox(
                expanded = showSortMenu.value,
                onExpandedChange = { showSortMenu.value = it }
            ) {
                OutlinedTextField(
                    value = selectedSort.displayName,
                    onValueChange = {},
                    label = { Text("Sort") },
                    trailingIcon = {
                        Icon(
                            imageVector = if (showSortMenu.value) androidx.compose.material.icons.Icons.Default.ExpandLess else androidx.compose.material.icons.Icons.Default.ExpandMore,
                            contentDescription = "Sort"
                        )
                    },
                    readOnly = true,
                    modifier = Modifier.width(140.dp)
                )
                DropdownMenu(
                    expanded = showSortMenu.value,
                    onDismissRequest = { showSortMenu.value = false }
                ) {
                    PortfolioSortBy.values().forEach { sort ->
                        DropdownMenuItem(
                            text = { Text(text = sort.displayName) },
                            onClick = {
                                viewModel.setSortBy(sort)
                                showSortMenu.value = false
                            }
                        )
                    }
                }
            }

            // Filter Dropdown
            ExposedDropdownMenuBox(
                expanded = showFilterMenu.value,
                onExpandedChange = { showFilterMenu.value = it }
            ) {
                OutlinedTextField(
                    value = selectedFilter.displayName,
                    onValueChange = {},
                    label = { Text("Filter") },
                    trailingIcon = {
                        Icon(
                            imageVector = if (showFilterMenu.value) androidx.compose.material.icons.Icons.Default.ExpandLess else androidx.compose.material.icons.Icons.Default.ExpandMore,
                            contentDescription = "Filter"
                        )
                    },
                    readOnly = true,
                    modifier = Modifier.width(140.dp)
                )
                DropdownMenu(
                    expanded = showFilterMenu.value,
                    onDismissRequest = { showFilterMenu.value = false }
                ) {
                    PortfolioFilter.values().forEach { filter ->
                        DropdownMenuItem(
                            text = { Text(text = filter.displayName) },
                            onClick = {
                                viewModel.setFilter(filter)
                                showFilterMenu.value = false
                            }
                        )
                    }
                }
            }
        }

        // Holdings List
        if (isLoading && filteredHoldings.isEmpty()) {
            CommonComponents.LoadingState(modifier = Modifier.fillMaxSize().padding(16.dp))
        } else if (filteredHoldings.isEmpty()) {
            CommonComponents.EmptyState(
                icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.AccountBalanceWallet, contentDescription = "No holdings", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp)) },
                title = "No Holdings",
                            message = if (holdings.isEmpty())
                                "Your portfolio is empty. Sync with your broker to fetch holdings."
                            else
                                "No holdings match your current filter.",
                            actionText = if (holdings.isEmpty()) "Sync Portfolio" else null,
                            onAction = if (holdings.isEmpty()) { viewModel.syncPortfolio() } else null,
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredHoldings) { holding ->
                    HoldingCard(
                        holding = holding,
                        onClick = { onHoldingClick(holding) }
                    )
                }
            }
        }
    }
}

@Composable
fun HoldingCard(
    holding: Holding,
    onClick: () -> Unit
) {
    val isProfit = holding.isProfit
    val changeColor = if (isProfit) Color(0xFF2E7D32) else Color(0xFFC62828)

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = StockAppTheme.colorScheme.surface,
            contentColor = StockAppTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(text = holding.symbol, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StockAppTheme.colorScheme.onSurface)
                    Text(text = holding.name, fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "₹${String.format("%.2f", holding.currentPrice)}", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = StockAppTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${if (holding.dayChange >= 0) "+" else ""}${String.format("%.2f", holding.dayChange)} (${if (holding.dayChangePercent >= 0) "+" else ""}${String.format("%.2f", holding.dayChangePercent)}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = changeColor
                        )
                    }
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailColumn(label = "Quantity", value = holding.quantity.toString())
                DetailColumn(label = "Avg Price", value = "₹${String.format("%.2f", holding.averagePrice)}")
                DetailColumn(label = "Invested", value = "₹${String.format("%.2f", holding.totalInvested)}")
                DetailColumn(label = "Current", value = "₹${String.format("%.2f", holding.currentValue)}")
                DetailColumn(label = "P&L", value = holding.formattedGainLoss, valueColor = changeColor)
            }
        }
    }
}

@Composable
fun DetailColumn(label: String, value: String, valueColor: Color = StockAppTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}