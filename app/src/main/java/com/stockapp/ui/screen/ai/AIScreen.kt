package com.stockapp.ui.screen.ai

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
import com.stockapp.data.model.AIAnalysis
import com.stockapp.data.model.AIAnalysisType
import com.stockapp.ui.components.CommonComponents
import com.stockapp.ui.theme.StockAppTheme
import com.stockapp.ui.theme.StockAppTheme.colorScheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@Composable
fun AIScreen(
    viewModel: AIViewModel = hiltViewModel(),
    onAnalysisClick: (AIAnalysis) -> Unit
) {
    val analyses by viewModel.analyses.observeAsState(emptyList())
    val portfolioInsights by viewModel.portfolioInsights.observeAsState("")
    val isLoading by viewModel.isLoading.observeAsState(false)
    val isGenerating by viewModel.isGenerating.observeAsState(false)
    val error by viewModel.error.observeAsState<String?>(null)
    val selectedType by viewModel.selectedType.observeAsState<AIAnalysisType?>(null)

    val queryText = remember { mutableStateOf("") }
    val showTypeSelector = remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(key1 = true) {
        viewModel.loadAnalyses()
        viewModel.generatePortfolioInsights()
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
                        Text(text = "AI Insights", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                        Text(text = "Smart analysis powered by AI", fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { viewModel.generatePortfolioInsights() }, enabled = !isGenerating) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Psychology, contentDescription = "Generate Insights", tint = colorScheme.tertiary)
                        }
                    }
                }
            }
        }

        // Portfolio Insights Banner
        if (portfolioInsights.isNotBlank()) {
            PortfolioInsightsBanner(insights = portfolioInsights, onDismiss = { viewModel.dismissPortfolioInsights() })
        }

        // Query Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = queryText.value,
                    onValueChange = { queryText.value = it },
                    label = { Text("Ask AI about stocks, market, portfolio...") },
                    leadingIcon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Psychology, contentDescription = "AI") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Send),
                    keyboardActions = androidx.compose.ui.text.input.KeyboardActions(onDone = { viewModel.askAI(queryText.value); queryText.value = "" })
                )
                Button(
                    onClick = { viewModel.askAI(queryText.value); queryText.value = "" },
                    enabled = queryText.value.isNotBlank() && !isGenerating,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = colorScheme.tertiary)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onTertiary)
                    } else {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Send, contentDescription = "Send", tint = colorScheme.onTertiary)
                    }
                }
            }
        }

        // Analysis Type Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Filter:", fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
            AnalysisTypeChips(
                selectedType = selectedType,
                onTypeClick = { viewModel.setAnalysisTypeFilter(it) }
            )
        }

        // Analyses List
        if (isLoading && analyses.isEmpty()) {
            CommonComponents.LoadingState(modifier = Modifier.fillMaxSize().padding(16.dp))
        } else if (analyses.isEmpty()) {
            CommonComponents.EmptyState(
                icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Psychology, contentDescription = "No analyses", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp)) },
                title = "No AI Analyses Yet",
                message = "Ask AI a question or generate portfolio insights to get started.",
                modifier = Modifier.fillMaxSize().padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(analyses) { analysis ->
                    AIAnalysisCard(
                        analysis = analysis,
                        onClick = { onAnalysisClick(analysis) }
                    )
                }
            }
        }
    }
}

@Composable
fun PortfolioInsightsBanner(
    insights: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = StockAppTheme.colorScheme.tertiaryContainer,
            contentColor = StockAppTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Lightbulb,
                        contentDescription = "Insights",
                        tint = StockAppTheme.colorScheme.tertiary
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.width(8.dp))
                    Text(text = "Portfolio Insights", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StockAppTheme.colorScheme.onTertiaryContainer)
                }
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(8.dp))
                Text(text = insights, fontSize = 14.sp, color = StockAppTheme.colorScheme.onTertiaryContainer, maxLines = 4, overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis)
            }
            IconButton(onClick = onDismiss) {
                Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Dismiss", tint = StockAppTheme.colorScheme.onTertiaryContainer)
            }
        }
    }
}

@Composable
fun AnalysisTypeChips(
    selectedType: AIAnalysisType?,
    onTypeClick: (AIAnalysisType?) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Chip(
            onClick = { onTypeClick(null) },
            colors = androidx.compose.material3.ChipDefaults.chipColors(
                containerColor = if (selectedType == null) StockAppTheme.colorScheme.tertiary else StockAppTheme.colorScheme.surfaceVariant,
                contentColor = if (selectedType == null) StockAppTheme.colorScheme.onTertiary else StockAppTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text(text = "All", fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        AIAnalysisType.values().forEach { type ->
            Chip(
                onClick = { onTypeClick(type) },
                colors = androidx.compose.material3.ChipDefaults.chipColors(
                    containerColor = if (selectedType == type) StockAppTheme.colorScheme.tertiary else StockAppTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedType == type) StockAppTheme.colorScheme.onTertiary else StockAppTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(text = type.value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun AIAnalysisCard(
    analysis: AIAnalysis,
    onClick: () -> Unit
) {
    val typeColor = when (analysis.type) {
        AIAnalysisType.PORTFOLIO_REVIEW -> Color(0xFF7B1FA2)
        AIAnalysisType.STOCK_ANALYSIS -> Color(0xFF1976D2)
        AIAnalysisType.MARKET_OUTLOOK -> Color(0xFF00695C)
        AIAnalysisType.RISK_ASSESSMENT -> Color(0xFFC62828)
        AIAnalysisType.IPO_ANALYSIS -> Color(0xFFF57C00)
        AIAnalysisType.NEWS_SUMMARY -> Color(0xFF2E7D32)
        AIAnalysisType.CUSTOM_QUERY -> StockAppTheme.colorScheme.tertiary
    }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (analysis.type) {
                                AIAnalysisType.PORTFOLIO_REVIEW -> androidx.compose.material.icons.Icons.Default.AccountBalance
                                AIAnalysisType.STOCK_ANALYSIS -> androidx.compose.material.icons.Icons.Default.Analytics
                                AIAnalysisType.MARKET_OUTLOOK -> androidx.compose.material.icons.Icons.Default.Public
                                AIAnalysisType.RISK_ASSESSMENT -> androidx.compose.material.icons.Icons.Default.Shield
                                AIAnalysisType.IPO_ANALYSIS -> androidx.compose.material.icons.Icons.Default.HowToReg
                                AIAnalysisType.NEWS_SUMMARY -> androidx.compose.material.icons.Icons.Default.Summarize
                                AIAnalysisType.CUSTOM_QUERY -> androidx.compose.material.icons.Icons.Default.Chat
                            },
                            contentDescription = analysis.type.value,
                            tint = typeColor
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.width(8.dp))
                        Text(text = analysis.type.value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = typeColor)
                    }
                    analysis.symbol?.let { symbol ->
                        Text(text = symbol, fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(text = analysis.createdAt.toString(), fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
            }

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(12.dp))

            Text(
                text = analysis.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = StockAppTheme.colorScheme.onSurface
            )

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(8.dp))

            Text(
                text = analysis.summary,
                fontSize = 14.sp,
                color = StockAppTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = androidx.compose.ui.text.overflow.TextOverflow.Ellipsis
            )

            analysis.recommendation?.let { rec ->
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Chip(
                        onClick = {},
                        colors = androidx.compose.material3.ChipDefaults.chipColors(
                            containerColor = when (rec) {
                                "BUY" -> Color(0xFF2E7D32).copy(alpha = 0.1f)
                                "SELL" -> Color(0xFFC62828).copy(alpha = 0.1f)
                                "HOLD" -> Color(0xFFF57C00).copy(alpha = 0.1f)
                                else -> StockAppTheme.colorScheme.primaryContainer
                            },
                            contentColor = when (rec) {
                                "BUY" -> Color(0xFF2E7D32)
                                "SELL" -> Color(0xFFC62828)
                                "HOLD" -> Color(0xFFF57C00)
                                else -> StockAppTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    ) {
                        Text(text = rec, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    analysis.confidence?.let { conf ->
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.width(8.dp))
                        Text(text = "Confidence: ${String.format("%.0f", conf * 100)}%", fontSize = 12.sp, color = StockAppTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            analysis.targetPrice?.let { target ->
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.height(8.dp))
                Text(text = "Target: ₹${String.format("%.2f", target)}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E7D32))
            }
        }
    }
}