package com.stockapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Chip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stockapp.ui.theme.StockAppTheme

@Composable
fun StockTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    colors: TopAppBarDefaults.TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = StockAppTheme.colorScheme.surface,
        titleContentColor = StockAppTheme.colorScheme.onSurface
    )
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors
    )
}

@Composable
fun StatCard(
    title: String,
    value: String,
    change: String? = null,
    isPositive: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = StockAppTheme.colorScheme.surface,
            contentColor = StockAppTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                icon()
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = title,
                fontSize = 14.sp,
                color = StockAppTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = StockAppTheme.colorScheme.onSurface
            )
            change?.let {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }
    }
}

@Composable
fun HoldingRow(
    holding: com.stockapp.data.model.Holding,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val isProfit = holding.isProfit
    val changeColor = if (isProfit) Color(0xFF2E7D32) else Color(0xFFC62828)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = StockAppTheme.colorScheme.surface,
            contentColor = StockAppTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = holding.symbol,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StockAppTheme.colorScheme.onSurface
                )
                Text(
                    text = holding.name,
                    fontSize = 12.sp,
                    color = StockAppTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "₹${String.format("%.2f", holding.currentPrice)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = StockAppTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${if (holding.dayChange >= 0) "+" else ""}${String.format("%.2f", holding.dayChange)} (${if (holding.dayChangePercent >= 0) "+" else ""}${String.format("%.2f", holding.dayChangePercent)}%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = changeColor
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Qty: ${holding.quantity}",
                    fontSize = 12.sp,
                    color = StockAppTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = holding.formattedGainLoss,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = changeColor
                )
            }
        }
    }
}

@Composable
fun IPORow(
    ipo: com.stockapp.data.model.IPO,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val statusColor = when (ipo.status) {
        com.stockapp.data.model.IPOStatus.OPEN -> Color(0xFF2E7D32)
        com.stockapp.data.model.IPOStatus.UPCOMING -> Color(0xFF1976D2)
        com.stockapp.data.model.IPOStatus.ALLOTMENT -> Color(0xFFF57C00)
        com.stockapp.data.model.IPOStatus.LISTED -> Color(0xFF7B1FA2)
        else -> StockAppTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
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
                Column {
                    Text(
                        text = ipo.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StockAppTheme.colorScheme.onSurface
                    )
                    Text(
                        text = ipo.symbol,
                        fontSize = 12.sp,
                        color = StockAppTheme.colorScheme.onSurfaceVariant
                    )
                }
                Chip(
                    onClick = {},
                    colors = ChipDefaults.chipColors(
                        containerColor = statusColor.copy(alpha = 0.1f),
                        contentColor = statusColor
                    )
                ) {
                    Text(text = ipo.status.value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ipo.formattedPriceBand,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = StockAppTheme.colorScheme.onSurface
                )
                Text(
                    text = "Lot: ${ipo.lotSize} | ₹${String.format("%.0f", ipo.issueSize / 10000000)} Cr",
                    fontSize = 12.sp,
                    color = StockAppTheme.colorScheme.onSurfaceVariant
                )
            }
            ipo.gmp?.let { gmp ->
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.TrendingUp,
                        contentDescription = "GMP",
                        tint = Color(0xFFF57C00)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GMP: ${ipo.formattedGMP}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF57C00)
                    )
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = "Open: ${ipo.openDate} | Close: ${ipo.closeDate}",
                    fontSize = 12.sp,
                    color = StockAppTheme.colorScheme.onSurfaceVariant
                )
                if (ipo.daysToClose > 0) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${ipo.daysToClose} days left",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (ipo.daysToClose <= 2) Color(0xFFC62828) else StockAppTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun NewsCard(
    article: com.stockapp.data.model.NewsArticle,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val sentimentColor = when (article.sentiment) {
        com.stockapp.data.model.NewsSentiment.POSITIVE -> Color(0xFF2E7D32)
        com.stockapp.data.model.NewsSentiment.NEGATIVE -> Color(0xFFC62828)
        else -> StockAppTheme.colorScheme.onSurfaceVariant
    }

    val impactColor = when (article.impactLevel) {
        com.stockapp.data.model.ImpactLevel.CRITICAL -> Color(0xFFC62828)
        com.stockapp.data.model.ImpactLevel.HIGH -> Color(0xFFF57C00)
        com.stockapp.data.model.ImpactLevel.MEDIUM -> Color(0xFF1976D2)
        else -> StockAppTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
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
                        Text(
                            text = article.source,
                            fontSize = 12.sp,
                            color = StockAppTheme.colorScheme.onSurfaceVariant
                        )
                        Chip(
                            onClick = {},
                            colors = ChipDefaults.chipColors(
                                containerColor = impactColor.copy(alpha = 0.1f),
                                contentColor = impactColor
                            )
                        ) {
                            Text(text = article.impactLevel.value, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StockAppTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.summary,
                        fontSize = 14.sp,
                        color = StockAppTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            article.symbols.take(3).forEach { symbol ->
                                Chip(
                                    onClick = {},
                                    modifier = Modifier.padding(end = 4.dp),
                                    colors = ChipDefaults.chipColors(
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
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = article.sentiment.value,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = sentimentColor
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = article.publishedAt.toString(),
                                fontSize = 12.sp,
                                color = StockAppTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenerResultRow(
    result: com.stockapp.data.model.ScreenerResult,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val changeColor = if (result.changePercent >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = StockAppTheme.colorScheme.surface,
            contentColor = StockAppTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = result.symbol,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StockAppTheme.colorScheme.onSurface
                )
                Text(
                    text = result.name,
                    fontSize = 12.sp,
                    color = StockAppTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "₹${String.format("%.2f", result.currentPrice)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = StockAppTheme.colorScheme.onSurface
                )
                Text(
                    text = "${if (result.changePercent >= 0) "+" else ""}${String.format("%.2f", result.changePercent)}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = changeColor
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Score: ${String.format("%.1f", result.score)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = StockAppTheme.colorScheme.primary
                )
                Text(
                    text = "P/E: ${result.peRatio?.let { String.format("%.1f", it) } ?: "N/A"}",
                    fontSize = 12.sp,
                    color = StockAppTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WeeklyPickCard(
    pick: com.stockapp.data.model.WeeklyStockPick,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val riskColor = when (pick.riskLevel) {
        com.stockapp.data.model.RiskLevel.LOW -> Color(0xFF2E7D32)
        com.stockapp.data.model.RiskLevel.MEDIUM -> Color(0xFFF57C00)
        com.stockapp.data.model.RiskLevel.HIGH -> Color(0xFFC62828)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    Text(
                        text = pick.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StockAppTheme.colorScheme.onSurface
                    )
                    Text(
                        text = pick.symbol,
                        fontSize = 14.sp,
                        color = StockAppTheme.colorScheme.onSurfaceVariant
                    )
                }
                Chip(
                    onClick = {},
                    colors = ChipDefaults.chipColors(
                        containerColor = riskColor.copy(alpha = 0.1f),
                        contentColor = riskColor
                    )
                ) {
                    Text(text = pick.riskLevel.value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Current", fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
                    Text(text = "₹${String.format("%.2f", pick.currentPrice)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StockAppTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Target", fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
                    Text(text = "₹${String.format("%.2f", pick.targetPrice)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Stop Loss", fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
                    Text(text = "₹${String.format("%.2f", pick.stopLoss)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pick.reason,
                    fontSize = 14.sp,
                    color = StockAppTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Confidence: ${String.format("%.0f", pick.confidence * 100)}%", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = StockAppTheme.colorScheme.primary)
                    Text(text = pick.timeHorizon.value.replace("_", " "), fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (pick.aiGenerated) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Psychology,
                        contentDescription = "AI Generated",
                        tint = StockAppTheme.colorScheme.tertiary
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "AI Generated", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = StockAppTheme.colorScheme.tertiary)
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: @Composable (() -> Unit),
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StockAppTheme.colorScheme.onSurface)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, fontSize = 16.sp, color = StockAppTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            actionText?.let { text ->
                onAction?.let { action ->
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = action) {
                        Text(text = text)
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier.fillMaxSize()) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = Color(0xFFC62828),
                modifier = Modifier.size(64.dp)
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Something went wrong", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StockAppTheme.colorScheme.onSurface)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, fontSize = 16.sp, color = StockAppTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            onRetry?.let { retry ->
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = retry) {
                    Text(text = "Retry")
                }
            }
        }
    }
}