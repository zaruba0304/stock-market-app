package com.stockapp.ui.screen.screeners

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
import com.stockapp.data.model.RiskLevel
import com.stockapp.data.model.Screener
import com.stockapp.data.model.ScreenerResult
import com.stockapp.data.model.WeeklyStockPick
import com.stockapp.ui.components.CommonComponents
import com.stockapp.ui.theme.StockAppTheme
import com.stockapp.ui.theme.StockAppTheme.colorScheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@Composable
fun ScreenersScreen(
    viewModel: ScreenersViewModel = hiltViewModel(),
    onResultClick: (ScreenerResult) -> Unit,
    onPickClick: (WeeklyStockPick) -> Unit
) {
    val screeners by viewModel.screeners.observeAsState(emptyList())
    val selectedScreener by viewModel.selectedScreener.observeAsState<Screener?>(null)
    val results by viewModel.results.observeAsState(emptyList())
    val weeklyPicks by viewModel.weeklyPicks.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val isRunning by viewModel.isRunning.observeAsState(false)
    val error by viewModel.error.observeAsState<String?>(null)

    val showScreenerSelector = remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(key1 = true) {
        viewModel.loadScreeners()
        viewModel.loadWeeklyPicks()
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
                        Text(text = "Screeners", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                        Text(text = "Find investment opportunities", fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { viewModel.runSelectedScreener() }, enabled = selectedScreener != null && !isRunning) {
                            if (isRunning) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Icon(imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow, contentDescription = "Run Screener", tint = colorScheme.primary)
                            }
                        }
                        IconButton(onClick = { viewModel.loadWeeklyPicks() }) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Refresh, contentDescription = "Refresh Picks", tint = colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Screener Selector
        if (screeners.isNotEmpty()) {
            ScreenerSelector(
                screeners = screeners,
                selected = selectedScreener,
                onSelect = { viewModel.selectScreener(it) },
                isRunning = isRunning
            )
        }

        // Weekly Picks Section
        if (weeklyPicks.isNotEmpty()) {
            CommonComponents.StockTopAppBar(
                title = "This Week's Picks",
                modifier = Modifier.padding(top = 16.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(weeklyPicks) { pick ->
                    CommonComponents.WeeklyPickCard(pick = pick, onClick = { onPickClick(pick) })
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(16.dp))
        }

        // Results Section
        if (selectedScreener != null) {
            CommonComponents.StockTopAppBar(
                title = "Results: ${selectedScreener!!.name}",
                modifier = Modifier.padding(top = 16.dp)
            )
            if (isLoading && results.isEmpty()) {
                CommonComponents.LoadingState(modifier = Modifier.fillMaxSize().padding(16.dp))
            } else if (results.isEmpty()) {
                CommonComponents.EmptyState(
                    icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.FilterList, contentDescription = "No results", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp)) },
                    title = "No Results",
                    message = "Run the screener to see matching stocks.",
                    actionText = "Run Screener",
                    onAction = { viewModel.runSelectedScreener() },
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results) { result ->
                        CommonComponents.ScreenerResultRow(result = result, onClick = { onResultClick(result) })
                    }
                }
            }
        } else if (screeners.isEmpty()) {
            CommonComponents.EmptyState(
                icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.FilterListOff, contentDescription = "No screeners", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp)) },
                title = "No Screeners Available",
                message = "Default screeners will be loaded on first run.",
                actionText = "Refresh",
                onAction = { viewModel.loadScreeners() },
                modifier = Modifier.fillMaxSize().padding(16.dp)
            )
        }
    }
}

@Composable
fun ScreenerSelector(
    screeners: List<Screener>,
    selected: Screener?,
    onSelect: (Screener) -> Unit,
    isRunning: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = StockAppTheme.colorScheme.surface,
            contentColor = StockAppTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Select Screener", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StockAppTheme.colorScheme.onSurface)
                if (selected != null) {
                    Chip(
                        onClick = {},
                        colors = androidx.compose.material3.ChipDefaults.chipColors(
                            containerColor = StockAppTheme.colorScheme.primaryContainer,
                            contentColor = StockAppTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(text = selected.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(12.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(screeners) { screener ->
                    ScreenerChip(
                        screener = screener,
                        selected = selected?.id == screener.id,
                        onClick = { onSelect(screener) },
                        isRunning = isRunning && selected?.id == screener.id
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenerChip(
    screener: Screener,
    selected: Boolean,
    onClick: () -> Unit,
    isRunning: Boolean
) {
    val typeColor = when (screener.type) {
        com.stockapp.data.model.ScreenerType.FUNDAMENTAL -> Color(0xFF1976D2)
        com.stockapp.data.model.ScreenerType.TECHNICAL -> Color(0xFF7B1FA2)
        com.stockapp.data.model.ScreenerType.MOMENTUM -> Color(0xFFF57C00)
        com.stockapp.data.model.ScreenerType.VALUE -> Color(0xFF2E7D32)
        com.stockapp.data.model.ScreenerType.GROWTH -> Color(0xFF00695C)
        com.stockapp.data.model.ScreenerType.CUSTOM -> StockAppTheme.colorScheme.primary
    }

    Chip(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(100.dp),
        colors = androidx.compose.material3.ChipDefaults.chipColors(
            containerColor = if (selected) typeColor.copy(alpha = 0.15f) else StockAppTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) typeColor else StockAppTheme.colorScheme.onSurfaceVariant
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = typeColor
                )
            } else {
                Icon(
                    imageVector = when (screener.type) {
                        com.stockapp.data.model.ScreenerType.FUNDAMENTAL -> androidx.compose.material.icons.Icons.Default.Analytics
                        com.stockapp.data.model.ScreenerType.TECHNICAL -> androidx.compose.material.icons.Icons.Default.ShowChart
                        com.stockapp.data.model.ScreenerType.MOMENTUM -> androidx.compose.material.icons.Icons.Default.TrendingUp
                        com.stockapp.data.model.ScreenerType.VALUE -> androidx.compose.material.icons.Icons.Default.AttachMoney
                        com.stockapp.data.model.ScreenerType.GROWTH -> androidx.compose.material.icons.Icons.Default.TrendingUp
                        com.stockapp.data.model.ScreenerType.CUSTOM -> androidx.compose.material.icons.Icons.Default.Tune
                    },
                    contentDescription = screener.type.value,
                    tint = typeColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(8.dp))
            Text(
                text = screener.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis
            )
        }
    }
}