package com.udyata.mangalot

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@Composable
fun MangaDetailsScreen(mangaUrl: String, chapter: Int) {
    val currentPage = remember { mutableIntStateOf(chapter) }
    val coroutineScope = rememberCoroutineScope()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { if (currentPage.value > 1) currentPage.value-- }) {
                Text("Previous")
            }
            Text("Chapter ${currentPage.value}", modifier = Modifier.align(Alignment.CenterVertically))
            Button(onClick = { currentPage.value++ }) {
                Text("Next")
            }
        }
        val newUrl = replaceChapterNumber(mangaUrl, currentPage.value)
        WebViewScreen(newUrl)

        // If no more pages are available, show a message
        val hasMorePages = remember { mutableStateOf(true) }
        LaunchedEffect(currentPage.value) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val client = OkHttpClient()
                    val request = Request.Builder().url("$mangaUrl/chapter-${currentPage.value}").build()
                    val response = client.newCall(request).execute()

                    withContext(Dispatchers.Main) {
                        hasMorePages.value = response.isSuccessful // Assuming a successful response means a page exists
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        hasMorePages.value = false
                    }
                }
            }
        }

        if (!hasMorePages.value) {
            Text("No more pages available", modifier = Modifier.padding(16.dp))
        }
    }
}
@Composable
fun WebViewScreen(chapterUrl: String) {
    val htmlContent = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(true) }  // State to track loading

    LaunchedEffect(chapterUrl) {
        fetchChapterPagesForWebView(chapterUrl) { html ->
            htmlContent.value = html
            isLoading.value = false  // Set loading to false after content is loaded
        }
    }

    if (isLoading.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()  // Show progress bar while loading
        }
    } else {
        AndroidView(factory = { context ->
            WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isLoading.value = false  // Ensure loading is set to false when the page is fully loaded
                    }
                }
                settings.javaScriptEnabled = true  // Enable JavaScript if needed
                if (htmlContent.value.isNotBlank()) {
                    loadDataWithBaseURL(chapterUrl, htmlContent.value, "text/html", "UTF-8", null)
                }
            }
        }, update = { webView ->
            if (htmlContent.value.isNotBlank()) {
                webView.loadDataWithBaseURL(chapterUrl, htmlContent.value, "text/html", "UTF-8", null)
            }
        })
    }
}


fun fetchChapterPagesForWebView(chapterUrl: String, callback: (String) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val client = OkHttpClient()
        val request = Request.Builder().url(chapterUrl).build()
        val response = client.newCall(request).execute()
        val htmlContent = response.body?.string() ?: ""

        val document: Document = Jsoup.parse(htmlContent)
        val imageElements = document.select("div.container-chapter-reader img")

        val imagesHtml = imageElements.joinToString(separator = "") { element ->
            val imgSrc = element.attr("abs:src").trim()
            val title = element.attr("alt").trim()
            // Create an HTML string for each image
            "<div><img src=\"$imgSrc\" alt=\"$title\"/></div>"
        }

        val customHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Manga Chapter</title>
                <style>
                    img { width: 100%; height: auto; }
                    div { text-align: center; }
                </style>
            </head>
            <body>
                $imagesHtml
            </body>
            </html>
        """.trimIndent()

        withContext(Dispatchers.Main) {
            callback(customHtml)
        }
    }
}

fun replaceChapterNumber(url: String, currentPage: Int): String {
    val regex = Regex("chapter-\\d+")
    return regex.replace(url, "chapter-$currentPage")
}
