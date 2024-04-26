package com.udyata.mangalot


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.udyata.mangalot.ui.theme.MangaLotTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            MangaLotTheme(
                darkTheme = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "mangaList") {
                        composable("mangaList") {
                            MangaListScreen(onNavigate = {
                                navController.navigate("mangaMainDetailsMain/$it")
                            })
                        }

                        composable("mangaMainDetailsMain/{mangaUrl}") { backStackEntry ->
                            val url = backStackEntry.arguments?.getString("mangaUrl") ?: ""
                            MangaDetailScreenMain(url,
                                onNavigateToChapter = { url,chapter->
                                    val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                                    navController.navigate("mangaDetails/$encodedUrl/$chapter")
                                }
                            )
                        }

                        composable("mangaDetails/{mangaUrl}/{chapter}") { backStackEntry ->
                            val chapter  = backStackEntry.arguments?.getString("chapter") ?: "1"
                            val mangaUrl  = backStackEntry.arguments?.getString("mangaUrl") ?: "1"
                            MangaDetailsScreen(mangaUrl=mangaUrl,chapter = chapter.toInt())
                        }
                    }
                }
            }

        }
    }
}