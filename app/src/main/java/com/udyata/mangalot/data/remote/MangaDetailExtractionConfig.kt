package com.udyata.mangalot.data.remote

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.udyata.mangalot.utils.extractAttribute
import com.udyata.mangalot.utils.extractText
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class MangaDetails(
    var thumbnail: String,
    var title: String,
    var alternativeTitle: String,
    var authors: List<String>,
    var status: String,
    var genres: List<String>,
    var update: String,
    var view: String,
    var description: String,
    var chapterList: List<ChapterDetails>,
    var ratings:Rating
)

data class Rating(
    val average: Double,
    val best: Int,
    val votes: Int
)

data class ChapterDetails(
    val name: String,
    val url: String,
    val views: String,
    val uploadedTime: String
)



data class MangaDetailsExtractionConfig(
    val chapterListSelector: String = "ul.row-content-chapter > li.a-h",
    val chapterNameSelector: String = "a.chapter-name",
    val chapterUrlSelector: String = "a.chapter-name",
    val chapterViewsSelector: String = "span.chapter-view",
    val chapterUploadedTimeSelector: String = "span.chapter-time"
)


fun parseMangaDetails(htmlContent: String, config: MangaDetailsExtractionConfig = MangaDetailsExtractionConfig()): MangaDetails {
    val document = Jsoup.parse(htmlContent)

    val thumbnailElement = document.select("div.story-info-left > span.info-image").firstOrNull()
    val thumbnailSrc = thumbnailElement?.select("img")?.attr("src") ?: ""

    val title = document.extractText("div.story-info-right > h1")
    val alternativeTitle = document.extractText("td.table-value > h2")
    val authors = document.select("tr:contains(Author(s)) .table-value a").eachText()
    val status = document.select("tr:contains(Status) .table-value").text()
    val genres = document.select("tr:contains(Genres) .table-value a").eachText()
    val update = document.select("p:contains(Updated) .stre-value").text()
    val view = document.select("p:contains(View) .stre-value").text()
    val ratings = extractRating(document)

    val description = document.select("#panel-story-info-description").text()

    val chapters = document.select(config.chapterListSelector).map {
        ChapterDetails(
            name = it.select(config.chapterNameSelector).text(),
            url = it.select(config.chapterUrlSelector).attr("href"),
            views = it.select(config.chapterViewsSelector).text(),
            uploadedTime = it.select(config.chapterUploadedTimeSelector).text()
        )
    }

    return MangaDetails(
        thumbnail = thumbnailSrc,
        title = title,
        alternativeTitle = alternativeTitle,
        authors= authors,
        status = status,
        genres = genres,
        update= update,
        view= view,
        ratings = ratings,
        description = description,
        chapterList = chapters
    )
}

fun extractRating(doc: Document): Rating {
    val average = doc.select("em[property='v:average']").text().toDouble()
    val best = doc.select("em[property='v:best']").text().toInt()
    val votes = doc.select("em[property='v:votes']").text().replace(" votes", "").toInt()

    return Rating(average, best, votes)
}