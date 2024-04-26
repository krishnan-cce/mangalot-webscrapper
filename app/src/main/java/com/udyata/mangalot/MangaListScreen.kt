package com.udyata.mangalot

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.udyata.mangalot.data.remote.Manga
import com.udyata.mangalot.data.remote.parseMangaList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

//@Composable
//fun MangaListScreen(navController: NavController) {
//
//    val mangaList = remember { mutableStateOf<List<Manga>>(emptyList()) }
//    val currentPage = remember { mutableStateOf(1) }
//    val isLoading = remember { mutableStateOf(false) }
//    val listState = rememberLazyListState()
//    val scope = rememberCoroutineScope()
//
//    Column (
//        modifier=Modifier.fillMaxSize()
//    ){
//        LazyColumn(state = listState) {
//            items(mangaList.value) { manga ->
//
//                MangaItem(manga,onCLick ={
//                    val encodedUrl = URLEncoder.encode(it.url, StandardCharsets.UTF_8.toString())
//                    navController.navigate("mangaDetails/$encodedUrl")
//                })
//            }
//
//            if (isLoading.value) {
//                item {
//                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
//                }
//            }
//        }
//
//        // Initial data load
//        LaunchedEffect(currentPage.value) {
//            isLoading.value = true
//            scope.launch(Dispatchers.IO) {
//                val newMangaList = fetchMangaPage(currentPage.value)
//                withContext(Dispatchers.Main) {
//                    mangaList.value = if (currentPage.value == 1) newMangaList else mangaList.value + newMangaList
//                    isLoading.value = false
//                }
//            }
//        }
//
//        // Pagination logic
//        LaunchedEffect(listState.layoutInfo.visibleItemsInfo, isLoading.value) {
//            val layoutInfo = listState.layoutInfo
//            if (!isLoading.value && layoutInfo.visibleItemsInfo.isNotEmpty() &&
//                layoutInfo.visibleItemsInfo.last().index >= mangaList.value.size - 1) {
//                currentPage.value += 1 // Increment page number to fetch
//            }
//        }
//    }
//}
//
//
//@Composable
//fun MangaItem(manga: Manga,onCLick:(Manga)->Unit) {
//
//
//    Column(
//        modifier = Modifier.clickable {
//        onCLick(manga)
//    }) {
//
//
//        AsyncImage(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(120.dp),
//            model = ImageRequest.Builder(LocalContext.current)
//                .data(manga.thumbnail)
//                .crossfade(true)
//                .diskCachePolicy(CachePolicy.DISABLED)
//                .build(),
//            contentScale = ContentScale.Fit,
//            contentDescription = "Gallery Image",
//            placeholder = painterResource(R.drawable.placeholder),
//            error = painterResource(R.drawable.placeholdererror)
//        )
//
//
//        Text(
//            text = manga.title,
//            fontSize = 20.sp,
//            textAlign = TextAlign.Center
//        )
//
//        Text(
//            text = manga.latestChapter,
//            fontSize = 14.sp,
//            textAlign = TextAlign.Start
//        )
//        Text(
//            text = manga.views,
//            fontSize = 14.sp,
//            textAlign = TextAlign.Start
//        )
//        Text(
//            text = manga.description,
//            fontSize = 14.sp,
//            textAlign = TextAlign.Start
//        )
//    }
//}


@Composable
fun MangaListScreen(onNavigate:(url:String)->Unit) {
    val mangaList = remember { mutableStateOf<List<Manga>>(emptyList()) }
    val currentPage = remember { mutableStateOf(1) }
    val isLoading = remember { mutableStateOf(false) }
    val listState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        LaunchedEffect(currentPage.value) {
            isLoading.value = true
            scope.launch(Dispatchers.IO) {
                val newMangaList = fetchMangaPage(currentPage.value)
                withContext(Dispatchers.Main) {
                    mangaList.value = if (currentPage.value == 1) newMangaList else mangaList.value + newMangaList
                    isLoading.value = false
                }
            }
        }

        // Pagination logic for LazyVerticalGrid
        LaunchedEffect(listState.layoutInfo.visibleItemsInfo, isLoading.value) {
            val layoutInfo = listState.layoutInfo
            if (!isLoading.value && layoutInfo.visibleItemsInfo.isNotEmpty()) {
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.last().index
                if (lastVisibleIndex >= mangaList.value.size - 2) {  // You may adjust this condition based on your grid
                    currentPage.value += 1 // Trigger to load next page
                }
            }
        }


        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(2),
            state = listState,
            content = {

                items(mangaList.value.size) { index ->

                    val manga = mangaList.value[index]
                    MangaItem(manga) {
                        val encodedUrl = URLEncoder.encode(manga.url, StandardCharsets.UTF_8.toString())
                        onNavigate(encodedUrl)
                    }
                }
            }
        )
    }
}
@Composable
fun MangaItem(manga: Manga,onCLick:(Manga)->Unit) {

    Surface(
        shadowElevation = 2.dp,
        modifier = Modifier
            .padding(4.dp),
        color = Color.White
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable {
                    onCLick(manga)
                }
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(manga.thumbnail)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .build(),
                contentScale = ContentScale.Fit,
                contentDescription = "Gallery Image"
            )

            Text(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                text = manga.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black,
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                text = "Last ${manga.latestChapter}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black,
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )

        }
    }
}

fun fetchMangaPage(pageNumber: Int): List<Manga> {
    val url = "https://mangakakalot.com/manga_list?type=topview&category=all&state=all&page=$pageNumber"
    val request = Request.Builder().url(url).build()
    val client = OkHttpClient()
    val response = client.newCall(request).execute()
    val htmlContent = response.body?.string()

    return parseMangaList(htmlContent ?: "")
}




