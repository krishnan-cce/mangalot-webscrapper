package com.udyata.mangalot

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.udyata.mangalot.data.remote.Manga
import com.udyata.mangalot.data.remote.MangaDetails
import com.udyata.mangalot.data.remote.parseMangaDetails
import com.udyata.mangalot.data.remote.parseMangaList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun MangaDetailScreenMain(mangaUrl: String,
                          onNavigateToChapter:(url:String,chapter:String)->Unit) {
    val isLoading = remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val mangaDetail = remember { mutableStateOf<MangaDetails?>(null) }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        LaunchedEffect(true) {
            isLoading.value = true
            scope.launch(Dispatchers.IO) {
                val newMangaDetail = fetchMangaDetailPage(mangaUrl)
                withContext(Dispatchers.Main) {
                    mangaDetail.value = newMangaDetail
                    isLoading.value = false
                }
            }
        }


        LazyColumn {
            item {
                MangaDetailItem(manga = mangaDetail.value ?: return@item)
            }
            mangaDetail.value?.chapterList?.let {
                items(
                    count = it.size,

                    ){chapters->
                    val chapterList = mangaDetail.value?.chapterList?.get(chapters)
                    Surface(
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable {
                                chapterList?.url?.let { it -> extractChapterNumber(it)?.let { chapter ->
                                    onNavigateToChapter(it, chapter)
                                } }
                                Log.d("URL",chapterList!!.url)
                            },
                        color = Color.White
                    ) {
                        Column {
                            if (chapterList != null) {
                                Text(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .fillMaxWidth(),
                                    text = chapterList.name.orEmpty(),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.Black,
                                )
                                Text(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .fillMaxWidth(),
                                    text = "Uploaded on : ${chapterList.uploadedTime}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                                    color = Color.Black,
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun MangaDetailItem(manga: MangaDetails) {

    Surface(
        shadowElevation = 2.dp,
        modifier = Modifier
            .padding(4.dp),
        color = Color.White
    ) {
        Column (modifier = Modifier
            .fillMaxSize()
        ){

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column {
                    AsyncImage(
                        modifier = Modifier
                            .width(160.dp)
                            .height(250.dp),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(manga.thumbnail)
                            .crossfade(true)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .build(),
                        contentScale = ContentScale.FillHeight,
                        contentDescription = "Gallery Image"
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                ) {
                    Text(
                        modifier = Modifier,
                        text = manga.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black,
                        maxLines = 1,
                        textAlign = TextAlign.Start,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        modifier = Modifier,
                        text = "Author(s) : ${manga.authors.joinToString(" , ")}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Gray,
                    )
                    Text(
                        modifier = Modifier,
                        text = "Status : ${manga.status} Mangakakalot(EN)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black,
                    )

                    Text(
                        modifier = Modifier,
                        text = "Updated : ${manga.update}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black,
                    )

                    Text(
                        modifier = Modifier,
                        text = "View : ${manga.view}, Rating : ${manga.ratings.average}/${manga.ratings.best}, Votes : ${manga.ratings.votes}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black,
                    )


                }
            }

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                    text = manga.description,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black,
                )
            }
        }
    }
}

fun fetchMangaDetailPage(url:String): MangaDetails {
    val request = Request.Builder().url(url).build()
    val client = OkHttpClient()
    val response = client.newCall(request).execute()
    val htmlContent = response.body?.string()

    return parseMangaDetails(htmlContent ?: "")
}


fun extractChapterNumber(url: String): String? {
    val regex = Regex("chapter-(\\d+)")
    return regex.find(url)?.groupValues?.get(1) // Returns the first group match
}