package com.stockapp.data.remote

import com.stockapp.data.model.Brokerage
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Generic API interfaces for different brokerages
interface BrokerageApi {
    @GET("user/profile")
    suspend fun getProfile(@Header("Authorization") authToken: String): Response<BrokerageProfileResponse>

    @GET("portfolio/holdings")
    suspend fun getHoldings(@Header("Authorization") authToken: String): Response<HoldingsResponse>

    @GET("portfolio/positions")
    suspend fun getPositions(@Header("Authorization") authToken: String): Response<PositionsResponse>

    @GET("orders")
    suspend fun getOrders(@Header("Authorization") authToken: String): Response<OrdersResponse>

    @GET("funds")
    suspend fun getFunds(@Header("Authorization") authToken: String): Response<FundsResponse>
}

// Zerodha Kite Connect API
interface ZerodhaApi {
    @GET("user/profile")
    suspend fun getProfile(@Header("Authorization") authToken: String): Response<ZerodhaProfileResponse>

    @GET("portfolio/holdings")
    suspend fun getHoldings(@Header("Authorization") authToken: String): Response<ZerodhaHoldingsResponse>

    @GET("portfolio/positions")
    suspend fun getPositions(@Header("Authorization") authToken: String): Response<ZerodhaPositionsResponse>

    @GET("orders")
    suspend fun getOrders(@Header("Authorization") authToken: String): Response<ZerodhaOrdersResponse>

    @GET("mf/holdings")
    suspend fun getMFHoldings(@Header("Authorization") authToken: String): Response<ZerodhaMFHoldingsResponse>

    @POST("session/token")
    suspend fun generateSession(
        @Header("X-Kite-Version") version: String,
        @Query("api_key") apiKey: String,
        @Query("request_token") requestToken: String,
        @Query("checksum") checksum: String
    ): Response<ZerodhaSessionResponse>
}

// Groww API
interface GrowwApi {
    @GET("v1/user/profile")
    suspend fun getProfile(@Header("Authorization") authToken: String): Response<GrowwProfileResponse>

    @GET("v1/portfolio/holdings")
    suspend fun getHoldings(@Header("Authorization") authToken: String): Response<GrowwHoldingsResponse>

    @GET("v1/orders")
    suspend fun getOrders(@Header("Authorization") authToken: String): Response<GrowwOrdersResponse>
}

// Upstox API
interface UpstoxApi {
    @GET("v2/user/profile")
    suspend fun getProfile(@Header("Authorization") authToken: String): Response<UpstoxProfileResponse>

    @GET("v2/portfolio/holdings")
    suspend fun getHoldings(@Header("Authorization") authToken: String): Response<UpstoxHoldingsResponse>

    @GET("v2/orders")
    suspend fun getOrders(@Header("Authorization") authToken: String): Response<UpstoxOrdersResponse>
}

// Angel One API
interface AngelOneApi {
    @GET("rest/secure/angelbroking/user/v1/getProfile")
    suspend fun getProfile(@Header("Authorization") authToken: String): Response<AngelOneProfileResponse>

    @GET("rest/secure/angelbroking/portfolio/v1/holdings")
    suspend fun getHoldings(@Header("Authorization") authToken: String): Response<AngelOneHoldingsResponse>
}

// IPO Data APIs
interface IPOApi {
    @GET("ipos")
    suspend fun getIPOs(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<IPOListResponse>

    @GET("ipos/{symbol}")
    suspend fun getIPODetails(@Path("symbol") symbol: String): Response<IPODetailedResponse>

    @GET("ipos/{symbol}/gmp")
    suspend fun getGMP(@Path("symbol") symbol: String): Response<GMPResponse>

    @GET("ipos/{symbol}/subscription")
    suspend fun getSubscription(@Path("symbol") symbol: String): Response<SubscriptionResponse>

    @POST("ipos/apply")
    suspend fun applyIPO(@Body request: IPOApplicationRequest): Response<IPOApplicationResponse>

    @GET("ipos/allotment/{pan}")
    suspend fun checkAllotment(@Path("pan") pan: String): Response<AllotmentResponse>
}

// News APIs
interface NewsApi {
    @GET("news")
    suspend fun getNews(
        @Query("symbols") symbols: String? = null,
        @Query("categories") categories: String? = null,
        @Query("minImpact") minImpact: Double = 0.0,
        @Query("limit") limit: Int = 50,
        @Query("page") page: Int = 1
    ): Response<NewsListResponse>

    @GET("news/{id}")
    suspend fun getNewsDetail(@Path("id") id: String): Response<NewsDetailResponse>

    @GET("news/recap/daily")
    suspend fun getDailyRecap(@Query("date") date: String): Response<DailyRecapResponse>

    @GET("news/sentiment/{symbol}")
    suspend fun getSentiment(@Path("symbol") symbol: String): Response<SentimentResponse>
}

// Screener APIs
interface ScreenerApi {
    @GET("screeners")
    suspend fun getScreeners(@Query("category") category: String? = null): Response<ScreenerListResponse>

    @POST("screeners/run")
    suspend fun runScreener(@Body request: ScreenerRunRequest): Response<ScreenerRunResponse>

    @GET("screeners/{id}/results")
    suspend fun getResults(@Path("id") id: Long): Response<ScreenerResultsResponse>

    @GET("picks/weekly")
    suspend fun getWeeklyPicks(@Query("week") week: String): Response<WeeklyPicksResponse>
}

// AI APIs
interface AIApi {
    @POST("analyze/portfolio")
    suspend fun analyzePortfolio(@Body request: PortfolioAnalysisRequest): Response<AIAnalysisResponse>

    @POST("analyze/stock")
    suspend fun analyzeStock(@Body request: StockAnalysisRequest): Response<AIAnalysisResponse>

    @POST("analyze/news")
    suspend fun analyzeNews(@Body request: NewsAnalysisRequest): Response<AIAnalysisResponse>

    @POST("analyze/ipo")
    suspend fun analyzeIPO(@Body request: IPOAnalysisRequest): Response<AIAnalysisResponse>

    @POST("chat")
    suspend fun chat(@Body request: ChatRequest): Response<ChatResponse>
}

// Response data classes
data class BrokerageProfileResponse(
    val userId: String,
    val name: String,
    val email: String,
    val broker: String
)

data class HoldingsResponse(
    val holdings: List<HoldingData>
)

data class HoldingData(
    val symbol: String,
    val name: String,
    val isin: String,
    val quantity: Int,
    val averagePrice: Double,
    val currentPrice: Double,
    val dayChange: Double,
    val dayChangePercent: Double,
    val marketValue: Double,
    val pnl: Double,
    val pnlPercent: Double
)

data class PositionsResponse(
    val positions: List<PositionData>
)

data class PositionData(
    val symbol: String,
    val quantity: Int,
    val averagePrice: Double,
    val currentPrice: Double,
    val pnl: Double,
    val product: String
)

data class OrdersResponse(
    val orders: List<OrderData>
)

data class OrderData(
    val orderId: String,
    val symbol: String,
    val quantity: Int,
    val price: Double,
    val status: String,
    val orderType: String,
    val transactionType: String,
    val timestamp: String
)

data class FundsResponse(
    val availableCash: Double,
    val usedMargin: Double,
    val totalValue: Double
)

// Zerodha specific responses
data class ZerodhaProfileResponse(
    val userId: String,
    val userName: String,
    val email: String,
    val broker: String = "zerodha"
)

data class ZerodhaHoldingsResponse(
    val holdings: List<ZerodhaHolding>
)

data class ZerodhaHolding(
    val tradingsymbol: String,
    val name: String,
    val isin: String,
    val quantity: Int,
    val averagePrice: Double,
    val lastPrice: Double,
    val dayChange: Double,
    val dayChangePercent: Double,
    val marketValue: Double,
    val pnl: Double,
    val pnlPercent: Double
)

data class ZerodhaPositionsResponse(
    val net: List<ZerodhaPosition>,
    val day: List<ZerodhaPosition>
)

data class ZerodhaPosition(
    val tradingsymbol: String,
    val quantity: Int,
    val averagePrice: Double,
    val lastPrice: Double,
    val pnl: Double,
    val product: String
)

data class ZerodhaOrdersResponse(
    val orders: List<ZerodhaOrder>
)

data class ZerodhaOrder(
    val orderId: String,
    val tradingsymbol: String,
    val quantity: Int,
    val price: Double,
    val status: String,
    val orderType: String,
    val transactionType: String,
    val orderTimestamp: String
)

data class ZerodhaMFHoldingsResponse(
    val holdings: List<ZerodhaMFHolding>
)

data class ZerodhaMFHolding(
    val tradingsymbol: String,
    val quantity: Double,
    val averagePrice: Double,
    val lastPrice: Double,
    val marketValue: Double
)

data class ZerodhaSessionResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String
)

// Groww responses
data class GrowwProfileResponse(
    val userId: String,
    val name: String,
    val email: String
)

data class GrowwHoldingsResponse(
    val holdings: List<GrowwHolding>
)

data class GrowwHolding(
    val symbol: String,
    val name: String,
    val isin: String,
    val quantity: Int,
    val avgPrice: Double,
    val currentPrice: Double,
    val dayChange: Double,
    val dayChangePercent: Double,
    val value: Double,
    val pnl: Double,
    val pnlPercent: Double
)

data class GrowwOrdersResponse(
    val orders: List<GrowwOrder>
)

data class GrowwOrder(
    val orderId: String,
    val symbol: String,
    val quantity: Int,
    val price: Double,
    val status: String,
    val orderType: String,
    val side: String,
    val timestamp: String
)

// Upstox responses
data class UpstoxProfileResponse(
    val userId: String,
    val name: String,
    val email: String
)

data class UpstoxHoldingsResponse(
    val holdings: List<UpstoxHolding>
)

data class UpstoxHolding(
    val symbol: String,
    val name: String,
    val isin: String,
    val quantity: Int,
    val averagePrice: Double,
    val currentPrice: Double,
    val dayChange: Double,
    val dayChangePercent: Double,
    val marketValue: Double,
    val pnl: Double,
    val pnlPercent: Double
)

data class UpstoxOrdersResponse(
    val orders: List<UpstoxOrder>
)

data class UpstoxOrder(
    val orderId: String,
    val symbol: String,
    val quantity: Int,
    val price: Double,
    val status: String,
    val orderType: String,
    val transactionType: String,
    val timestamp: String
)

// Angel One responses
data class AngelOneProfileResponse(
    val data: AngelOneProfileData
)

data class AngelOneProfileData(
    val clientId: String,
    val name: String,
    val email: String
)

data class AngelOneHoldingsResponse(
    val data: List<AngelOneHolding>
)

data class AngelOneHolding(
    val symbol: String,
    val name: String,
    val isin: String,
    val quantity: Int,
    val averagePrice: Double,
    val currentPrice: Double,
    val dayChange: Double,
    val dayChangePercent: Double,
    val marketValue: Double,
    val pnl: Double,
    val pnlPercent: Double
)

// IPO responses
data class IPOListResponse(
    val ipos: List<IPOData>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class IPOData(
    val symbol: String,
    val name: String,
    val isin: String,
    val priceBandMin: Double,
    val priceBandMax: Double,
    val lotSize: Int,
    val issueSize: Double,
    val openDate: String,
    val closeDate: String,
    val listingDate: String?,
    val status: String,
    val registrar: String?,
    val gmp: Double?,
    val gmpPercent: Double?
)

data class IPODetailedResponse(
    val ipo: IPOData,
    val documents: List<IPODocument>,
    val financials: IPOFinancials?,
    val peerComparison: List<PeerData>?
)

data class IPODocument(
    val name: String,
    val url: String,
    val type: String
)

data class IPOFinancials(
    val revenue: Double?,
    val profit: Double?,
    val eps: Double?,
    val peRatio: Double?,
    val roe: Double?
)

data class PeerData(
    val symbol: String,
    val name: String,
    val peRatio: Double,
    val marketCap: Double
)

data class GMPResponse(
    val symbol: String,
    val gmp: Double,
    val gmpPercent: Double,
    val lastUpdated: String
)

data class SubscriptionResponse(
    val symbol: String,
    val qib: Double,
    val nii: Double,
    val rii: Double,
    val employee: Double?,
    val total: Double,
    val lastUpdated: String
)

data class IPOApplicationRequest(
    val ipoId: Long,
    val pan: String,
    val dematAccount: String,
    val lots: Int,
    val price: Double,
    val upiId: String?
)

data class IPOApplicationResponse(
    val applicationId: String,
    val status: String,
    val message: String
)

data class AllotmentResponse(
    val pan: String,
    val applications: List<AllotmentData>
)

data class AllotmentData(
    val ipoSymbol: String,
    val applicationNumber: String,
    val lotsApplied: Int,
    val lotsAllotted: Int,
    val status: String,
    val refundAmount: Double
)

// News responses
data class NewsListResponse(
    val articles: List<NewsArticleData>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class NewsArticleData(
    val id: String,
    val title: String,
    val summary: String,
    val content: String?,
    val url: String,
    val imageUrl: String?,
    val source: String,
    val author: String?,
    val publishedAt: String,
    val symbols: List<String>,
    val categories: List<String>,
    val sentiment: String,
    val impactScore: Double
)

data class NewsDetailResponse(
    val article: NewsArticleData
)

data class DailyRecapResponse(
    val date: String,
    val summary: String,
    val topNews: List<NewsArticleData>,
    val marketSummary: String,
    val portfolioImpact: String
)

data class SentimentResponse(
    val symbol: String,
    val sentiment: String,
    val score: Double,
    val keyPoints: List<String>
)

// Screener responses
data class ScreenerListResponse(
    val screeners: List<ScreenerData>
)

data class ScreenerData(
    val id: Long,
    val name: String,
    val description: String,
    val category: String,
    val criteria: String,
    val isDefault: Boolean
)

data class ScreenerRunRequest(
    val screenerId: Long,
    val parameters: Map<String, Any>? = null
)

data class ScreenerRunResponse(
    val jobId: String,
    val status: String
)

data class ScreenerResultsResponse(
    val results: List<ScreenerResultData>
)

data class ScreenerResultData(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val changePercent: Double,
    val volume: Long,
    val marketCap: Double,
    val peRatio: Double?,
    val pbRatio: Double?,
    val roe: Double?,
    val debtToEquity: Double?,
    val score: Double,
    val matchedCriteria: List<String>
)

data class WeeklyPicksResponse(
    val week: String,
    val picks: List<WeeklyPickData>
)

data class WeeklyPickData(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val targetPrice: Double,
    val stopLoss: Double,
    val reason: String,
    val riskLevel: String,
    val timeHorizon: String,
    val confidence: Double
)

// AI responses
data class PortfolioAnalysisRequest(
    val holdings: List<HoldingData>,
    val totalValue: Double,
    val totalPnL: Double,
    val language: String = "en"
)

data class StockAnalysisRequest(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val peRatio: Double?,
    val pbRatio: Double?,
    val roe: Double?,
    val debtToEquity: Double?,
    val language: String = "en"
)

data class NewsAnalysisRequest(
    val articles: List<NewsArticleData>,
    val symbols: List<String>,
    val language: String = "en"
)

data class IPOAnalysisRequest(
    val ipo: IPOData,
    val gmp: Double?,
    val subscription: SubscriptionResponse?,
    val language: String = "en"
)

data class ChatRequest(
    val message: String,
    val context: String? = null,
    val language: String = "en"
)

data class AIAnalysisResponse(
    val analysis: String,
    val confidence: Double,
    val tokensUsed: Int,
    val model: String
)

data class ChatResponse(
    val response: String,
    val tokensUsed: Int
)