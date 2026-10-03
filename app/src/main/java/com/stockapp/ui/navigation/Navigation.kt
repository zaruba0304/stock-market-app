package com.stockapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stockapp.ui.screen.ai.AIScreen
import com.stockapp.ui.screen.ai.AIViewModel
import com.stockapp.ui.screen.home.HomeScreen
import com.stockapp.ui.screen.home.HomeViewModel
import com.stockapp.ui.screen.ipo.IPOViewModel
import com.stockapp.ui.screen.ipo.IPOScreen
import com.stockapp.ui.screen.news.NewsScreen
import com.stockapp.ui.screen.news.NewsViewModel
import com.stockapp.ui.screen.portfolio.PortfolioScreen
import com.stockapp.ui.screen.portfolio.PortfolioViewModel
import com.stockapp.ui.screen.screeners.ScreenersScreen
import com.stockapp.ui.screen.screeners.ScreenersViewModel
import com.stockapp.ui.screen.settings.SettingsScreen
import com.stockapp.ui.screen.settings.SettingsViewModel

@Composable
fun AppNavHost(navController: NavHostController, startDestination: String = "home") {
    NavHost(navController, startDestination) {
        composable("home") {
            HomeScreen(viewModel = hiltViewModel<HomeViewModel>())
        }
        composable("ipo") {
            IPOScreen(viewModel = hiltViewModel<IPOViewModel>())
        }
        composable("portfolio") {
            PortfolioScreen(viewModel = hiltViewModel<PortfolioViewModel>())
        }
        composable("news") {
            NewsScreen(viewModel = hiltViewModel<NewsViewModel>())
        }
        composable("screeners") {
            ScreenersScreen(viewModel = hiltViewModel<ScreenersViewModel>())
        }
        composable("ai") {
            AIScreen(viewModel = hiltViewModel<AIViewModel>())
        }
        composable("settings") {
            SettingsScreen(viewModel = hiltViewModel<SettingsViewModel>())
        }
    }
}

@Composable
fun RememberNavController(): NavHostController = rememberNavController()