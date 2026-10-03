package com.stockapp.data.local

import com.stockapp.data.model.Screener
import com.stockapp.data.model.ScreenerType
import java.time.LocalDateTime

object DefaultScreeners {

    fun getDefaultScreeners(): List<Screener> {
        return listOf(
            // Fundamental Screener - Quality Companies
            Screener(
                name = "Quality Compounders",
                description = "High ROE, low debt, consistent growth companies",
                type = ScreenerType.FUNDAMENTAL,
                criteria = mapOf(
                    "minROE" to 15.0,
                    "maxDebtToEquity" to 0.5,
                    "minRevenueGrowth" to 10.0,
                    "minProfitGrowth" to 10.0,
                    "maxPE" to 30.0,
                    "maxPB" to 5.0,
                    "minScore" to 70.0
                ),
                isActive = true,
                isDefault = true,
                createdAt = LocalDateTime.now()
            ),

            // Value Screener - Undervalued Stocks
            Screener(
                name = "Value Gems",
                description = "Undervalued stocks with strong fundamentals",
                type = ScreenerType.VALUE,
                criteria = mapOf(
                    "maxPE" to 15.0,
                    "maxPB" to 1.5,
                    "minROE" to 12.0,
                    "maxDebtToEquity" to 0.7,
                    "minScore" to 65.0
                ),
                isActive = true,
                isDefault = true,
                createdAt = LocalDateTime.now()
            ),

            // Growth Screener - High Growth Companies
            Screener(
                name = "Growth Stars",
                description = "High revenue and profit growth companies",
                type = ScreenerType.GROWTH,
                criteria = mapOf(
                    "minRevenueGrowth" to 20.0,
                    "minProfitGrowth" to 20.0,
                    "minROE" to 15.0,
                    "maxPE" to 40.0,
                    "minScore" to 65.0
                ),
                isActive = true,
                isDefault = true,
                createdAt = LocalDateTime.now()
            ),

            // Momentum Screener - Trending Stocks
            Screener(
                name = "Momentum Leaders",
                description = "Stocks with strong price momentum and volume",
                type = ScreenerType.MOMENTUM,
                criteria = mapOf(
                    "minPriceChange1M" to 10.0,
                    "minVolumeRatio" to 1.5,
                    "minRSI" to 60.0,
                    "maxRSI" to 80.0,
                    "minScore" to 60.0
                ),
                isActive = true,
                isDefault = true,
                createdAt = LocalDateTime.now()
            ),

            // Technical Screener - Breakout Stocks
            Screener(
                name = "Breakout Candidates",
                description = "Stocks breaking out from consolidation",
                type = ScreenerType.TECHNICAL,
                criteria = mapOf(
                    "priceAboveSMA20" to true,
                    "priceAboveSMA50" to true,
                    "volumeBreakout" to true,
                    "minScore" to 60.0
                ),
                isActive = true,
                isDefault = true,
                createdAt = LocalDateTime.now()
            ),

            // Dividend Screener
            Screener(
                name = "Dividend Aristocrats",
                description = "Consistent dividend payers with growing payouts",
                type = ScreenerType.FUNDAMENTAL,
                criteria = mapOf(
                    "minDividendYield" to 1.5,
                    "maxPayoutRatio" to 60.0,
                    "minDividendGrowth5Y" to 5.0,
                    "minROE" to 12.0,
                    "maxDebtToEquity" to 0.5,
                    "minScore" to 65.0
                ),
                isActive = true,
                isDefault = true,
                createdAt = LocalDateTime.now()
            ),

            // Smallcap Quality
            Screener(
                name = "Smallcap Quality",
                description = "Quality smallcap companies with growth potential",
                type = ScreenerType.FUNDAMENTAL,
                criteria = mapOf(
                    "maxMarketCap" to 5000.0, // 5000 Cr
                    "minROE" to 15.0,
                    "maxDebtToEquity" to 0.5,
                    "minRevenueGrowth" to 15.0,
                    "minProfitGrowth" to 15.0,
                    "minScore" to 70.0
                ),
                isActive = true,
                isDefault = true,
                createdAt = LocalDateTime.now()
            ),

            // Largecap Stability
            Screener(
                name = "Largecap Stability",
                description = "Stable largecap companies for core portfolio",
                type = ScreenerType.FUNDAMENTAL,
                criteria = mapOf(
                    "minMarketCap" to 50000.0, // 50000 Cr
                    "minROE" to 12.0,
                    "maxDebtToEquity" to 0.5,
                    "minRevenueGrowth" to 5.0,
                    "maxPE" to 25.0,
                    "minScore" to 65.0
                ),
                isActive = true,
                isDefault = true,
                createdAt = LocalDateTime.now()
            ),

            // Turnaround Candidates
            Screener(
                name = "Turnaround Candidates",
                description = "Companies showing signs of recovery",
                type = ScreenerType.FUNDAMENTAL,
                criteria = mapOf(
                    "profitGrowthImproving" to true,
                    "revenueGrowthPositive" to true,
                    "debtReducing" to true,
                    "minScore" to 55.0
                ),
                isActive = true,
                isDefault = true,
                createdAt = LocalDateTime.now()
            ),

            // IPO Candidates (for pre-IPO screening)
            Screener(
                name = "Pre-IPO Quality",
                description = "Quality metrics for upcoming IPOs",
                type = ScreenerType.FUNDAMENTAL,
                criteria = mapOf(
                    "minROE" to 15.0,
                    "maxDebtToEquity" to 0.5,
                    "minRevenueGrowth" to 15.0,
                    "minProfitGrowth" to 15.0,
                    "minScore" to 70.0
                ),
                isActive = true,
                isDefault = true,
                createdAt = LocalDateTime.now()
            )
        )
    }
}