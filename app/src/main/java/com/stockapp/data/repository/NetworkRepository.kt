package com.stockapp.data.repository

import com.stockapp.data.local.AppDatabase
import com.stockapp.data.local.dao.BrokerageCredentialsDao
import com.stockapp.data.model.Brokerage
import com.stockapp.data.model.BrokerageCredentials
import com.stockapp.data.model.Holding
import com.stockapp.data.model.User
import com.stockapp.data.remote.ApiInterfaces.*
import com.stockapp.data.remote.ApiModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkRepository @Inject constructor(
    private val database: AppDatabase,
    private val apiModule: ApiModule,
    private val credentialsDao: BrokerageCredentialsDao
) {

    private fun getZerodhaApi(credentials: BrokerageCredentials): ZerodhaApi {
        val retrofit = apiModule.createRetrofit(Brokerage.ZERODHA.apiBaseUrl)
        return retrofit.create(ZerodhaApi::class.java)
    }

    private fun getGrowwApi(credentials: BrokerageCredentials): GrowwApi {
        val retrofit = apiModule.createRetrofit(Brokerage.GROWW.apiBaseUrl)
        return retrofit.create(GrowwApi::class.java)
    }

    private fun getUpstoxApi(credentials: BrokerageCredentials): UpstoxApi {
        val retrofit = apiModule.createRetrofit(Brokerage.UPSTOX.apiBaseUrl)
        return retrofit.create(UpstoxApi::class.java)
    }

    private fun getAngelOneApi(credentials: BrokerageCredentials): AngelOneApi {
        val retrofit = apiModule.createRetrofit(Brokerage.ANGELONE.apiBaseUrl)
        return retrofit.create(AngelOneApi::class.java)
    }

    private fun getIPOApi(): IPOApi {
        val retrofit = apiModule.createRetrofit("https://api.ipodata.com/")
        return retrofit.create(IPOApi::class.java)
    }

    private fun getNewsApi(): NewsApi {
        val retrofit = apiModule.createRetrofit("https://api.financialnews.com/")
        return retrofit.create(NewsApi::class.java)
    }

    private fun getScreenerApi(): ScreenerApi {
        val retrofit = apiModule.createRetrofit("https://api.screener.com/")
        return retrofit.create(ScreenerApi::class.java)
    }

    private fun getAIApi(): AIApi {
        val retrofit = apiModule.createRetrofit("https://api.ai.com/")
        return retrofit.create(AIApi::class.java)
    }

    // Sync holdings from brokerage
    suspend fun syncHoldings(user: User): Result<List<Holding>> = withContext(Dispatchers.IO) {
        try {
            val credentials = credentialsDao.getByUserAndBrokerage(user.id, user.brokerageId)
                ?: return@withContext Result.failure(Exception("No credentials found for ${user.brokerageId}"))

            val authToken = "Bearer ${credentials.accessToken}"
            val holdings = when (user.brokerageId) {
                Brokerage.ZERODHA.id -> {
                    val api = getZerodhaApi(credentials)
                    val response = api.getHoldings(authToken)
                    if (response.isSuccessful) {
                        response.body()?.holdings?.map { mapZerodhaHolding(it, user.id) } ?: emptyList()
                    } else {
                        throw Exception("Failed to fetch holdings: ${response.message()}")
                    }
                }
                Brokerage.GROWW.id -> {
                    val api = getGrowwApi(credentials)
                    val response = api.getHoldings(authToken)
                    if (response.isSuccessful) {
                        response.body()?.holdings?.map { mapGrowwHolding(it, user.id) } ?: emptyList()
                    } else {
                        throw Exception("Failed to fetch holdings: ${response.message()}")
                    }
                }
                Brokerage.UPSTOX.id -> {
                    val api = getUpstoxApi(credentials)
                    val response = api.getHoldings(authToken)
                    if (response.isSuccessful) {
                        response.body()?.holdings?.map { mapUpstoxHolding(it, user.id) } ?: emptyList()
                    } else {
                        throw Exception("Failed to fetch holdings: ${response.message()}")
                    }
                }
                Brokerage.ANGELONE.id -> {
                    val api = getAngelOneApi(credentials)
                    val response = api.getHoldings(authToken)
                    if (response.isSuccessful) {
                        response.body()?.data?.map { mapAngelOneHolding(it, user.id) } ?: emptyList()
                    } else {
                        throw Exception("Failed to fetch holdings: ${response.message()}")
                    }
                }
                else -> emptyList()
            }
            Result.success(holdings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sync IPO data
    suspend fun syncIPOs(): Result<List<IPOData>> = withContext(Dispatchers.IO) {
        try {
            val api = getIPOApi()
            val response = api.getIPOs()
            if (response.isSuccessful) {
                Result.success(response.body()?.ipos ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch IPOs: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fetch news
    suspend fun fetchNews(symbols: List<String> = emptyList(), minImpact: Double = 0.0): Result<List<NewsArticleData>> = withContext(Dispatchers.IO) {
        try {
            val api = getNewsApi()
            val response = api.getNews(
                symbols = symbols.joinToString(","),
                minImpact = minImpact,
                limit = 50
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.articles ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch news: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fetch daily recap
    suspend fun fetchDailyRecap(date: String): Result<DailyRecapResponse> = withContext(Dispatchers.IO) {
        try {
            val api = getNewsApi()
            val response = api.getDailyRecap(date)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch daily recap: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Run screener
    suspend fun runScreener(screenerId: Long): Result<List<ScreenerResultData>> = withContext(Dispatchers.IO) {
        try {
            val api = getScreenerApi()
            val response = api.runScreener(ScreenerRunRequest(screenerId))
            if (response.isSuccessful) {
                // Poll for results
                val jobId = response.body()?.jobId ?: return@withContext Result.failure(Exception("No job ID"))
                Result.success(pollScreenerResults(api, jobId))
            } else {
                Result.failure(Exception("Failed to run screener: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun pollScreenerResults(api: ScreenerApi, jobId: String): List<ScreenerResultData> {
        // In a real implementation, you'd poll the API until results are ready
        // For now, return empty list
        return emptyList()
    }

    // Get weekly picks
    suspend fun getWeeklyPicks(week: String): Result<List<WeeklyPickData>> = withContext(Dispatchers.IO) {
        try {
            val api = getScreenerApi()
            val response = api.getWeeklyPicks(week)
            if (response.isSuccessful) {
                Result.success(response.body()?.picks ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch weekly picks: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // AI Analysis
    suspend fun analyzePortfolio(holdings: List<Holding>, language: String = "en"): Result<AIAnalysisResponse> = withContext(Dispatchers.IO) {
        try {
            val api = getAIApi()
            val request = PortfolioAnalysisRequest(
                holdings = holdings.map { mapToHoldingData(it) },
                totalValue = holdings.sumOf { it.totalValue },
                totalPnL = holdings.sumOf { it.totalGainLoss },
                language = language
            )
            val response = api.analyzePortfolio(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to analyze portfolio: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeStock(symbol: String, name: String, currentPrice: Double, language: String = "en"): Result<AIAnalysisResponse> = withContext(Dispatchers.IO) {
        try {
            val api = getAIApi()
            val request = StockAnalysisRequest(
                symbol = symbol,
                name = name,
                currentPrice = currentPrice,
                language = language
            )
            val response = api.analyzeStock(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to analyze stock: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeNews(articles: List<NewsArticleData>, symbols: List<String>, language: String = "en"): Result<AIAnalysisResponse> = withContext(Dispatchers.IO) {
        try {
            val api = getAIApi()
            val request = NewsAnalysisRequest(
                articles = articles,
                symbols = symbols,
                language = language
            )
            val response = api.analyzeNews(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to analyze news: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun chat(message: String, context: String? = null, language: String = "en"): Result<ChatResponse> = withContext(Dispatchers.IO) {
        try {
            val api = getAIApi()
            val request = ChatRequest(message = message, context = context, language = language)
            val response = api.chat(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to chat: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Apply to IPO
    suspend fun applyIPO(request: IPOApplicationRequest): Result<IPOApplicationResponse> = withContext(Dispatchers.IO) {
        try {
            val api = getIPOApi()
            val response = api.applyIPO(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to apply IPO: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Check allotment
    suspend fun checkAllotment(pan: String): Result<AllotmentResponse> = withContext(Dispatchers.IO) {
        try {
            val api = getIPOApi()
            val response = api.checkAllotment(pan)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to check allotment: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mapping functions
    private fun mapZerodhaHolding(h: ZerodhaHolding, userId: Long): Holding {
        return Holding(
            userId = userId,
            symbol = h.tradingsymbol,
            name = h.name,
            isin = h.isin,
            quantity = h.quantity,
            averagePrice = h.averagePrice,
            currentPrice = h.lastPrice,
            dayChange = h.dayChange,
            dayChangePercent = h.dayChangePercent,
            totalValue = h.marketValue,
            totalGainLoss = h.pnl,
            totalGainLossPercent = h.pnlPercent
        )
    }

    private fun mapGrowwHolding(h: GrowwHolding, userId: Long): Holding {
        return Holding(
            userId = userId,
            symbol = h.symbol,
            name = h.name,
            isin = h.isin,
            quantity = h.quantity,
            averagePrice = h.avgPrice,
            currentPrice = h.currentPrice,
            dayChange = h.dayChange,
            dayChangePercent = h.dayChangePercent,
            totalValue = h.value,
            totalGainLoss = h.pnl,
            totalGainLossPercent = h.pnlPercent
        )
    }

    private fun mapUpstoxHolding(h: UpstoxHolding, userId: Long): Holding {
        return Holding(
            userId = userId,
            symbol = h.symbol,
            name = h.name,
            isin = h.isin,
            quantity = h.quantity,
            averagePrice = h.averagePrice,
            currentPrice = h.currentPrice,
            dayChange = h.dayChange,
            dayChangePercent = h.dayChangePercent,
            totalValue = h.marketValue,
            totalGainLoss = h.pnl,
            totalGainLossPercent = h.pnlPercent
        )
    }

    private fun mapAngelOneHolding(h: AngelOneHolding, userId: Long): Holding {
        return Holding(
            userId = userId,
            symbol = h.symbol,
            name = h.name,
            isin = h.isin,
            quantity = h.quantity,
            averagePrice = h.averagePrice,
            currentPrice = h.currentPrice,
            dayChange = h.dayChange,
            dayChangePercent = h.dayChangePercent,
            totalValue = h.marketValue,
            totalGainLoss = h.pnl,
            totalGainLossPercent = h.pnlPercent
        )
    }

    private fun mapToHoldingData(h: Holding): HoldingData {
        return HoldingData(
            symbol = h.symbol,
            name = h.name,
            isin = h.isin,
            quantity = h.quantity,
            averagePrice = h.averagePrice,
            currentPrice = h.currentPrice,
            dayChange = h.dayChange,
            dayChangePercent = h.dayChangePercent,
            marketValue = h.totalValue,
            pnl = h.totalGainLoss,
            pnlPercent = h.totalGainLossPercent
        )
    }
}