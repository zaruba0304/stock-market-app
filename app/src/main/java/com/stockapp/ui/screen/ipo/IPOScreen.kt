package com.stockapp.ui.screen.ipo

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
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
import com.stockapp.data.model.IPO
import com.stockapp.data.model.IPOStatus
import com.stockapp.ui.components.CommonComponents
import com.stockapp.ui.theme.StockAppTheme
import com.stockapp.ui.theme.StockAppTheme.colorScheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@Composable
fun IPOScreen(
    viewModel: IPOViewModel = hiltViewModel(),
    onIPOClick: (IPO) -> Unit
) {
    val ipos by viewModel.ipos.observeAsState(emptyList())
    val filteredIPOs by viewModel.filteredIPOs.observeAsState(emptyList())
    val watchlist by viewModel.watchlist.observeAsState(emptyList())
    val selectedStatus by viewModel.selectedStatus.observeAsState(IPOStatus.ALL)
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState<String?>(null)

    val expandedIPOs = remember { mutableStateOf<Set<Long>>(emptySet()) }
    val dropdownExpanded = remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(key1 = true) {
        viewModel.loadIPOs()
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
                Text(text = "IPO Center", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                Text(text = "Track and apply for IPOs", fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
            }
        }

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IPOStatus.values().forEach { status ->
                FilterChip(
                    text = status.value,
                    selected = selectedStatus == status,
                    onClick = { viewModel.setFilterStatus(status) }
                )
            }
        }

        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Search IPOs...") },
                leadingIcon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // IPO List
        if (isLoading && filteredIPOs.isEmpty()) {
            CommonComponents.LoadingState(modifier = Modifier.fillMaxSize().padding(16.dp))
        } else if (filteredIPOs.isEmpty()) {
            CommonComponents.EmptyState(
                icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.HowToReg, contentDescription = "No IPOs", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp)) },
                title = "No IPOs Found",
                message = "No IPOs match your current filter. Try changing the filter or search term.",
                modifier = Modifier.fillMaxSize().padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredIPOs) { ipo ->
                    IPOCard(
                        ipo = ipo,
                        isInWatchlist = watchlist.contains(ipo.id!!),
                        isExpanded = expandedIPOs.value.contains(ipo.id!!),
                        onExpandClick = {
                            if (expandedIPOs.value.contains(ipo.id!!)) {
                                expandedIPOs.value = expandedIPOs.value - ipo.id!!
                            } else {
                                expandedIPOs.value = expandedIPOs.value + ipo.id!!
                            }
                        },
                        onWatchlistClick = { viewModel.toggleWatchlist(ipo) },
                        onApplyClick = { onIPOClick(ipo) },
                        onDetailClick = { onIPOClick(ipo) }
                    )
                }
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
fun IPOCard(
    ipo: IPO,
    isInWatchlist: Boolean,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onApplyClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    val statusColor = when (ipo.status) {
        IPOStatus.OPEN -> Color(0xFF2E7D32)
        IPOStatus.UPCOMING -> Color(0xFF1976D2)
        IPOStatus.ALLOTMENT -> Color(0xFFF57C00)
        IPOStatus.LISTED -> Color(0xFF7B1FA2)
        else -> StockAppTheme.colorScheme.onSurfaceVariant
    }

    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = StockAppTheme.colorScheme.surface,
            contentColor = StockAppTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = ipo.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StockAppTheme.colorScheme.onSurface)
                    Text(text = ipo.symbol, fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.Top) {
                    Chip(
                        onClick = {},
                        colors = androidx.compose.material3.ChipDefaults.chipColors(
                            containerColor = statusColor.copy(alpha = 0.1f),
                            contentColor = statusColor
                        )
                    ) {
                        Text(text = ipo.status.value, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.width(8.dp))
                    IconButton(onClick = onWatchlistClick) {
                        Icon(
                            imageVector = if (isInWatchlist) androidx.compose.material.icons.Icons.Default.Bookmark else androidx.compose.material.icons.Icons.Default.BookmarkOutline,
                            contentDescription = if (isInWatchlist) "Remove from watchlist" else "Add to watchlist",
                            tint = if (isInWatchlist) StockAppTheme.colorScheme.primary else StockAppTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.width(4.dp))
                    IconButton(onClick = onExpandClick) {
                        Icon(
                            imageVector = if (isExpanded) androidx.compose.material.icons.Icons.Default.ExpandLess else androidx.compose.material.icons.Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = StockAppTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Basic Info
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = ipo.formattedPriceBand, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = StockAppTheme.colorScheme.onSurface)
                Text(text = "Lot: ${ipo.lotSize} | ₹${String.format("%.0f", ipo.issueSize / 10000000)} Cr", fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
            }

            ipo.gmp?.let { gmp ->
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.TrendingUp, contentDescription = "GMP", tint = Color(0xFFF57C00), modifier = Modifier.size(16.dp))
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.width(4.dp))
                    Text(text = "GMP: ${ipo.formattedGMP}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFFF57C00))
                }
            }

            Row(modifier = Modifier.padding(top = 4.dp)) {
                Text(text = "Open: ${ipo.openDate} | Close: ${ipo.closeDate}", fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
                if (ipo.daysToClose > 0) {
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.width(16.dp))
                    Text(
                        text = "${ipo.daysToClose} days left",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (ipo.daysToClose <= 2) Color(0xFFC62828) else StockAppTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded Details
            if (isExpanded) {
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(12.dp))
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(StockAppTheme.colorScheme.outlineVariant)
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow(label = "Registrar", value = ipo.registrar ?: "N/A")
                    DetailRow(label = "Listing Date", value = ipo.listingDate?.toString() ?: "TBA")
                    DetailRow(label = "Face Value", value = "₹${ipo.faceValue}")
                    ipo.peRatio?.let { DetailRow(label = "P/E Ratio", value = String.format("%.1f", it)) }
                    ipo.subscriptionData?.let { sub ->
                        DetailRow(label = "Subscription", value = "${String.format("%.2f", sub.overall)}x overall")
                        DetailRow(label = "QIB", value = "${String.format("%.2f", sub.qib)}x")
                        DetailRow(label = "HNI", value = "${String.format("%.2f", sub.hni)}x")
                        DetailRow(label = "Retail", value = "${String.format("%.2f", sub.retail)}x")
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        androidx.compose.material3.Button(
                            onClick = onApplyClick,
                            modifier = Modifier.weight(1f),
                            enabled = ipo.status == IPOStatus.OPEN,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (ipo.status == IPOStatus.OPEN) StockAppTheme.colorScheme.primary else StockAppTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(text = "Apply", fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.material3.OutlinedButton(
                            onClick = onDetailClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Details", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = StockAppTheme.colorScheme.onSurface)
    }
}